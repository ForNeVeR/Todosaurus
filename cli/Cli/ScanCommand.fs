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

/// Matches TODO items without issue numbers. Direct port of IntelliJ plugin's ToDoItem.newItemPattern.
/// See: https://regex101.com/r/lDDqm7/2
let private todoPattern = Regex(@"\b(?i)TODO(?-i)\b:?(?!\[.*?\])", RegexOptions.Compiled)

let private IsCi(): bool =
    Environment.GetEnvironmentVariable("CI") |> isNull |> not

let private ProcessLines (filePath: LocalPath) (lines: string array) (i: int) (ignoring: bool) (ignoreStartLine: int) (matches: ResizeArray<TodoMatch>): Result<IReadOnlyList<TodoMatch>, string> =
    let rec loop i ignoring ignoreStartLine =
        if i >= lines.Length then
            if ignoring then
                Error $"%s{filePath.Value}(%d{ignoreStartLine}): Unclosed IgnoreTODO-Start marker"
            else
                Ok(matches :> IReadOnlyList<_>)
        else
            let line = lines[i]
            if line.Contains("IgnoreTODO-Start") then
                if ignoring then
                    Error $"%s{filePath.Value}(%d{i + 1}): Nested IgnoreTODO-Start marker (previous at line %d{ignoreStartLine})"
                else
                    loop (i + 1) true (i + 1)
            elif line.Contains("IgnoreTODO-End") then
                if not ignoring then
                    Error $"%s{filePath.Value}(%d{i + 1}): IgnoreTODO-End without matching IgnoreTODO-Start"
                else
                    loop (i + 1) false 0
            else
                if not ignoring && todoPattern.IsMatch(line) then
                    matches.Add({ File = filePath; Line = i + 1; Text = line })
                loop (i + 1) ignoring ignoreStartLine
    loop i ignoring ignoreStartLine

let ScanFile(workingDirectory: AbsolutePath, filePath: LocalPath): Task<Result<IReadOnlyList<TodoMatch>, string>> =
    task {
        try
            let absolutePath = workingDirectory / filePath.Value
            let! lines = File.ReadAllLinesAsync(absolutePath.Value)
            return ProcessLines filePath lines 0 false 0 (ResizeArray<TodoMatch>())
        with
        | :? IOException as ex ->
            Logger.Warning $"Cannot read file %s{filePath.Value}: %s{ex.Message}"
            return Ok(ResizeArray<TodoMatch>() :> IReadOnlyList<_>)
    }

let FormatMatch(m: TodoMatch, isCi: bool): string =
    if isCi then
        let filePath = m.File.Value.Replace(Path.DirectorySeparatorChar, '/')
        $"::warning file=%s{filePath},line=%d{m.Line},title=Unresolved TODO::%s{m.Text.TrimEnd()}"
    else
        $"%s{m.File.Value}(%d{m.Line}): %s{m.Text.TrimEnd()}"

let Scan(workingDirectory: AbsolutePath): Task<int> =
    task {
        let! files = FilesCommand.ListEligibleFiles workingDirectory
        let isCi = IsCi()
        let allMatches = ResizeArray<TodoMatch>()
        let mutable hasErrors = false
        for file in files do
            let! result = ScanFile(workingDirectory, file)
            match result with
            | Ok matches -> allMatches.AddRange(matches)
            | Error message ->
                Logger.Error message
                hasErrors <- true
        for m in allMatches do
            Logger.Info(FormatMatch(m, isCi))
        if hasErrors then
            return 2
        elif allMatches.Count > 0 then
            return 1
        else
            return 0
    }

let CreateCommand(): Command =
    let command = Command("scan", "Scan for unresolved TODO items and report them as GitHub Actions annotations")
    command.SetAction(fun (_parseResult: ParseResult) ->
        task {
            let workingDirectory = AbsolutePath.CurrentWorkingDirectory
            return! Scan workingDirectory
        } : Task<int>)
    command
