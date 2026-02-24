// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module internal Todosaurus.Cli.ScanCommand

open System
open System.Collections.Generic
open System.CommandLine
open System.IO
open System.Text.RegularExpressions
open System.Threading.Tasks
open TruePath

type TodoMatch = {
    File: LocalPath
    Line: int
    Text: string
}

type ConnectedTodoMatch = {
    File: LocalPath
    Line: int
    IssueNumber: int
    Text: string
}

type ScanFileResult =
    {
        UnresolvedMatches: IReadOnlyList<TodoMatch>
        ConnectedMatches: IReadOnlyList<ConnectedTodoMatch>
    }
    with

    static member Empty = {
        UnresolvedMatches = Array.empty
        ConnectedMatches = Array.empty
    }

// IgnoreTODO-Start

/// Matches TODO items without issue numbers. Direct port of IntelliJ plugin's ToDoItem.newItemPattern.
/// See: https://regex101.com/r/lDDqm7/2
let private todoPattern = Regex(@"\b(?i)TODO(?-i)\b:?(?!\[.*?\])", RegexOptions.Compiled)

/// Matches TODO items WITH issue numbers (connected TODOs). Captures the issue number in group 1.
let private connectedTodoPattern = Regex(@"\b(?i)TODO(?-i)\b:?\[#(\d+)\]", RegexOptions.Compiled)

// IgnoreTODO-End


let private CountOccurrences (text: string) (substring: string): int =
    let mutable count = 0
    let mutable index = text.IndexOf(substring, StringComparison.Ordinal)
    while index >= 0 do
        count <- count + 1
        index <- text.IndexOf(substring, index + substring.Length, StringComparison.Ordinal)
    count

let private ProcessLines
    (cwd: AbsolutePath)
    (filePath: LocalPath)
    (lines: string array)
    : Result<ScanFileResult, unit> =
    let unresolvedMatches = ResizeArray()
    let connectedMatches = ResizeArray()
    let incorrectStructure line message =
        Logger.Error("Incorrect file structure", message, SourceInfo(cwd, filePath, line))
        Error()
    let rec loop i ignoring ignoreStartLine =
        if i >= lines.Length then
            if ignoring then
                incorrectStructure ignoreStartLine $"Unclosed {Markers.IgnoreToDoStart} marker."
            else
                Ok {
                    UnresolvedMatches = unresolvedMatches :> IReadOnlyList<_>
                    ConnectedMatches = connectedMatches :> IReadOnlyList<_>
                }
        else
            let line = lines[i]
            let startCount = CountOccurrences line Markers.IgnoreToDoStart
            let endCount = CountOccurrences line Markers.IgnoreToDoEnd
            let markerCount = startCount + endCount
            if markerCount > 1 then
                incorrectStructure (i + 1) "Multiple IgnoreTODO markers on the same line."
            elif markerCount = 1 && todoPattern.IsMatch(line) then
                incorrectStructure (i + 1) $"IgnoreTODO marker and {Markers.ToDoItem} on the same line."
            elif startCount = 1 then
                if ignoring then
                    incorrectStructure (i + 1) $"Nested {Markers.IgnoreToDoStart} marker (previous at line %d{ignoreStartLine})."
                else
                    loop (i + 1) true (i + 1)
            elif endCount = 1 then
                if not ignoring then
                    incorrectStructure (i + 1) $"{Markers.IgnoreToDoEnd} without a matching {Markers.IgnoreToDoStart}."
                else
                    loop (i + 1) false 0
            else
                if not ignoring then
                    if todoPattern.IsMatch(line) then
                        unresolvedMatches.Add({ File = filePath; Line = i + 1; Text = line })
                    let connected = connectedTodoPattern.Matches(line)
                    for m in connected do
                        connectedMatches.Add({
                            File = filePath
                            Line = i + 1
                            IssueNumber = int m.Groups[1].Value
                            Text = line
                        })
                loop (i + 1) ignoring ignoreStartLine
    loop 0 false 0

let ScanFile(workingDirectory: AbsolutePath, filePath: LocalPath): Task<Result<ScanFileResult, unit>> =
    task {
        try
            let absolutePath = workingDirectory / filePath.Value
            let! lines = File.ReadAllLinesAsync(absolutePath.Value)
            return ProcessLines workingDirectory filePath lines
        with
        | :? IOException as ex ->
            Logger.Error("Cannot read file", ex.Message, SourceInfo(workingDirectory, filePath, 1))
            return Error()
    }

let Scan(workingDirectory: AbsolutePath, config: Configuration.TodosaurusConfig, createIssueChecker: unit -> GitHubClient.IIssueChecker): Task<int> =
    task {
        let! allFiles = FilesCommand.ListEligibleFiles workingDirectory
        let files = Configuration.ApplyExclusions(allFiles, config)
        let allUnresolved = ResizeArray<TodoMatch>()
        let allConnected = ResizeArray<ConnectedTodoMatch>()
        let mutable hasErrors = false
        for file in files do
            let! result = ScanFile(workingDirectory, file)
            match result with
            | Ok scanResult ->
                allUnresolved.AddRange(scanResult.UnresolvedMatches)
                allConnected.AddRange(scanResult.ConnectedMatches)
            | Error() ->
                hasErrors <- true

        for m in allUnresolved do
            Logger.Warning(
                $"Unresolved {Markers.ToDoItem}",
                $"{Markers.ToDoItem} item has no issue number assigned.",
                SourceInfo(workingDirectory, m.File, m.Line)
            )

        let mutable hasNonExistent = false
        let mutable hasClosed = false
        let mutable trackerUnresolvable = false

        if allConnected.Count > 0 then
            let! repo =
                task {
                    match config.TrackerUrl with
                    | Some url ->
                        return GitRemote.ParseGitHubUrl url
                    | None ->
                        return! GitRemote.DiscoverFromOrigin workingDirectory
                }

            match repo with
            | None ->
                Logger.Warning $"Could not determine GitHub repository. Skipping connected {Markers.ToDoItem} issue checks."
                trackerUnresolvable <- true
            | Some repo ->
                try
                    let issueChecker = createIssueChecker()
                    let issueNumbers = allConnected |> Seq.map _.IssueNumber
                    let! statuses = GitHubClient.CheckIssues(issueChecker, repo.Owner, repo.Repo, issueNumbers)

                    for connected in allConnected do
                        match statuses |> Map.tryFind connected.IssueNumber with
                        | Some GitHubClient.NotFound ->
                            hasNonExistent <- true
                            Logger.Warning(
                                "Non-existent issue reference",
                                $"TODO[#%d{connected.IssueNumber}] references a non-existent issue.",
                                SourceInfo(workingDirectory, connected.File, connected.Line)
                            )
                        | Some GitHubClient.Closed ->
                            hasClosed <- true
                            Logger.Warning(
                                "Closed issue reference",
                                $"TODO[#%d{connected.IssueNumber}] references a closed issue.",
                                SourceInfo(workingDirectory, connected.File, connected.Line)
                            )
                        | _ -> ()
                with
                | ex ->
                    Logger.Warning $"GitHub API error: %s{ex.Message}. Skipping connected {Markers.ToDoItem} issue checks."

        if hasErrors then return 2
        elif allUnresolved.Count > 0 then return 1
        elif hasNonExistent then return 3
        elif hasClosed then return 4
        elif trackerUnresolvable then return 5
        else return 0
    }

let CreateCommand(configOption: Option<string>): Command =
    let command = Command("scan", $"Scan for unresolved {Markers.ToDoItem} items and report them as GitHub Actions annotations")
    command.Add(configOption)
    command.SetAction(fun (parseResult: ParseResult) ->
        task {
            let workingDirectory = AbsolutePath.CurrentWorkingDirectory
            let configPath =
                match parseResult.GetValue(configOption) with
                | null -> None
                | v -> Some(AbsolutePath(Path.GetFullPath(v, workingDirectory.Value)))
            let! configResult = Configuration.ReadConfig(configPath, workingDirectory)
            match configResult with
            | Error msg ->
                Logger.Error msg
                return 2
            | Ok config ->
                return! Scan(workingDirectory, config, GitHubClient.CreateClient)
        } : Task<int>)
    command
