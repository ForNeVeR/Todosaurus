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

type ScanFileResult = {
    UnresolvedMatches: IReadOnlyList<TodoMatch>
    ConnectedMatches: IReadOnlyList<ConnectedTodoMatch>
}

/// Matches TODO items without issue numbers. Direct port of IntelliJ plugin's ToDoItem.newItemPattern.
/// See: https://regex101.com/r/lDDqm7/2
let private todoPattern = Regex(@"\b(?i)TODO(?-i)\b:?(?!\[.*?\])", RegexOptions.Compiled)

/// Matches TODO items WITH issue numbers (connected TODOs). Captures the issue number in group 1.
let private connectedTodoPattern = Regex(@"\b(?i)TODO(?-i)\b:?\[#(\d+)\]", RegexOptions.Compiled)

let private IsCi(): bool =
    Environment.GetEnvironmentVariable("CI") |> isNull |> not

let private CountOccurrences (text: string) (substring: string): int =
    let mutable count = 0
    let mutable index = text.IndexOf(substring, StringComparison.Ordinal)
    while index >= 0 do
        count <- count + 1
        index <- text.IndexOf(substring, index + substring.Length, StringComparison.Ordinal)
    count

let private ProcessLines
    (filePath: LocalPath)
    (lines: string array)
    (i: int)
    (ignoring: bool)
    (ignoreStartLine: int)
    (unresolvedMatches: ResizeArray<TodoMatch>)
    (connectedMatches: ResizeArray<ConnectedTodoMatch>)
    : Result<ScanFileResult, string> =
    let rec loop i ignoring ignoreStartLine =
        if i >= lines.Length then
            if ignoring then
                Error $"%s{filePath.Value}(%d{ignoreStartLine}): Unclosed IgnoreTODO-Start marker"
            else
                Ok {
                    UnresolvedMatches = unresolvedMatches :> IReadOnlyList<_>
                    ConnectedMatches = connectedMatches :> IReadOnlyList<_>
                }
        else
            let line = lines[i]
            let startCount = CountOccurrences line "IgnoreTODO-Start"
            let endCount = CountOccurrences line "IgnoreTODO-End"
            let markerCount = startCount + endCount
            if markerCount > 1 then
                Error $"%s{filePath.Value}(%d{i + 1}): Multiple IgnoreTODO markers on the same line"
            elif markerCount = 1 && todoPattern.IsMatch(line) then
                Error $"%s{filePath.Value}(%d{i + 1}): IgnoreTODO marker and TODO on the same line"
            elif startCount = 1 then
                if ignoring then
                    Error $"%s{filePath.Value}(%d{i + 1}): Nested IgnoreTODO-Start marker (previous at line %d{ignoreStartLine})"
                else
                    loop (i + 1) true (i + 1)
            elif endCount = 1 then
                if not ignoring then
                    Error $"%s{filePath.Value}(%d{i + 1}): IgnoreTODO-End without matching IgnoreTODO-Start"
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
    loop i ignoring ignoreStartLine

let ScanFile(workingDirectory: AbsolutePath, filePath: LocalPath): Task<Result<ScanFileResult, string>> =
    task {
        try
            let absolutePath = workingDirectory / filePath.Value
            let! lines = File.ReadAllLinesAsync(absolutePath.Value)
            return ProcessLines filePath lines 0 false 0 (ResizeArray<TodoMatch>()) (ResizeArray<ConnectedTodoMatch>())
        with
        | :? IOException as ex ->
            Logger.Warning $"Cannot read file %s{filePath.Value}: %s{ex.Message}"
            return Ok {
                UnresolvedMatches = ResizeArray<TodoMatch>() :> IReadOnlyList<_>
                ConnectedMatches = ResizeArray<ConnectedTodoMatch>() :> IReadOnlyList<_>
            }
    }

let FormatMatch(m: TodoMatch, isCi: bool): string =
    if isCi then
        let filePath = m.File.Value.Replace(Path.DirectorySeparatorChar, '/')
        $"::warning file=%s{filePath},line=%d{m.Line},title=Unresolved TODO::%s{m.Text.TrimEnd()}"
    else
        $"%s{m.File.Value}(%d{m.Line}): %s{m.Text.TrimEnd()}"

let FormatConnectedMatch(m: ConnectedTodoMatch, isCi: bool, reason: string): string =
    if isCi then
        let filePath = m.File.Value.Replace(Path.DirectorySeparatorChar, '/')
        $"::warning file=%s{filePath},line=%d{m.Line},title=TODO[#%d{m.IssueNumber}] %s{reason}::%s{m.Text.TrimEnd()}"
    else
        $"%s{m.File.Value}(%d{m.Line}): TODO[#%d{m.IssueNumber}] %s{reason}: %s{m.Text.TrimEnd()}"

let TrackerOption =
    let opt = Option<string>("--tracker")
    opt.Description <- "GitHub repository (owner/repo or URL) for issue checking"
    opt

let Scan(workingDirectory: AbsolutePath, tracker: string option, createIssueChecker: unit -> GitHubClient.IIssueChecker): Task<int> =
    task {
        let! files = FilesCommand.ListEligibleFiles workingDirectory
        let isCi = IsCi()
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
                Logger.Error message
                hasErrors <- true

        for m in allUnresolved do
            Logger.Info(FormatMatch(m, isCi))

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
                            Logger.Warning(FormatConnectedMatch(connected, isCi, "references non-existent issue"))
                        | Some GitHubClient.Closed ->
                            hasClosed <- true
                            Logger.Warning(FormatConnectedMatch(connected, isCi, "references closed issue"))
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
