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

/// Matches TODO items without issue numbers. Direct port of IntelliJ plugin's ToDoItem.newItemPattern.
/// See: https://regex101.com/r/lDDqm7/2
let private todoPattern = Regex(@"\b(?i)TODO(?-i)\b:?(?!\[.*?\])", RegexOptions.Compiled)

/// Matches TODO items WITH issue numbers (connected TODOs). Captures the issue number in group 1.
let private connectedTodoPattern = Regex(@"\b(?i)TODO(?-i)\b:?\[#(\d+)\]", RegexOptions.Compiled)



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
    : ScanFileResult =
    let unresolvedMatches = ResizeArray()
    let connectedMatches = ResizeArray()
    let incorrectStructure line message =
        Logger.Error("Incorrect file structure", message, SourceInfo(cwd, filePath, line))
        ScanFileResult.Empty
    let rec loop i ignoring ignoreStartLine =
        if i >= lines.Length then
            if ignoring then
                incorrectStructure ignoreStartLine "Unclosed IgnoreTODO-Start marker."
            else
                {
                    UnresolvedMatches = unresolvedMatches :> IReadOnlyList<_>
                    ConnectedMatches = connectedMatches :> IReadOnlyList<_>
                }
        else
            let line = lines[i]
            let startCount = CountOccurrences line "IgnoreTODO-Start"
            let endCount = CountOccurrences line "IgnoreTODO-End"
            let markerCount = startCount + endCount
            if markerCount > 1 then
                incorrectStructure (i + 1) "Multiple IgnoreTODO markers on the same line."
            elif markerCount = 1 && todoPattern.IsMatch(line) then
                incorrectStructure (i + 1) "IgnoreTODO marker and TODO on the same line."
            elif startCount = 1 then
                if ignoring then
                    incorrectStructure (i + 1) $"Nested IgnoreTODO-Start marker (previous at line %d{ignoreStartLine})."
                else
                    loop (i + 1) true (i + 1)
            elif endCount = 1 then
                if not ignoring then
                    incorrectStructure (i + 1) "IgnoreTODO-End without a matching IgnoreTODO-Start."
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

let ScanFile(workingDirectory: AbsolutePath, filePath: LocalPath): Task<Result<ScanFileResult, string>> =
    task {
        try
            let absolutePath = workingDirectory / filePath.Value
            let! lines = File.ReadAllLinesAsync(absolutePath.Value)
            let result = ProcessLines workingDirectory filePath lines
            return Ok result
        with
        | :? IOException as ex ->
            return Error $"Cannot read file: %s{ex.Message}"
    }

let TrackerOption =
    let opt = Option<string>("--tracker")
    opt.Description <- "GitHub repository (owner/repo or URL) for issue checking"
    opt

let Scan(workingDirectory: AbsolutePath, tracker: string option, createIssueChecker: unit -> GitHubClient.IIssueChecker): Task<int> =
    task {
        let! files = FilesCommand.ListEligibleFiles workingDirectory
        let allUnresolved = ResizeArray<TodoMatch>()
        let allConnected = ResizeArray<ConnectedTodoMatch>()
        let mutable hasErrors = false
        for file in files do
            let! result = ScanFile(workingDirectory, file)
            match result with
            | Ok scanResult ->
                allUnresolved.AddRange(scanResult.UnresolvedMatches)
                allConnected.AddRange(scanResult.ConnectedMatches)
            | Error message ->
                Logger.Error("Incorrect file structure", message, SourceInfo(workingDirectory, file, 1))
                hasErrors <- true

        for m in allUnresolved do
            Logger.Warning(
                "Unresolved TODO",
                "TODO item has no issue number assigned.",
                SourceInfo(workingDirectory, m.File, m.Line)
            )

        let mutable hasNonExistent = false
        let mutable hasClosed = false
        let mutable trackerUnresolvable = false

        if allConnected.Count > 0 then
            let! repo =
                task {
                    match tracker with
                    | Some t ->
                        match GitRemote.ParseTrackerArgument t with
                        | Some r -> return Some r
                        | None ->
                            Logger.Warning $"Cannot parse tracker argument: %s{t}"
                            return None
                    | None ->
                        return! GitRemote.DiscoverFromOrigin workingDirectory
                }

            match repo with
            | None ->
                Logger.Warning "Could not determine GitHub repository. Skipping connected TODO issue checks."
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
                    Logger.Warning $"GitHub API error: %s{ex.Message}. Skipping connected TODO issue checks."

        if hasErrors then return 2
        elif allUnresolved.Count > 0 then return 1
        elif hasNonExistent then return 3
        elif hasClosed then return 4
        elif trackerUnresolvable then return 5
        else return 0
    }

let CreateCommand(): Command =
    let command = Command("scan", "Scan for unresolved TODO items and report them as GitHub Actions annotations")
    command.Add(TrackerOption)
    command.SetAction(fun (parseResult: ParseResult) ->
        task {
            let workingDirectory = AbsolutePath.CurrentWorkingDirectory
            let tracker =
                match parseResult.GetValue(TrackerOption) with
                | null -> None
                | v -> Some v
            return! Scan(workingDirectory, tracker, GitHubClient.CreateClient)
        } : Task<int>)
    command
