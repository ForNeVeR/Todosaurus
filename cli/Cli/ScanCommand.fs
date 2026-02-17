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

let ScanFile(workingDirectory: AbsolutePath, filePath: LocalPath): Task<IReadOnlyList<TodoMatch>> =
    task {
        try
            let absolutePath = workingDirectory / filePath.Value
            let! lines = File.ReadAllLinesAsync(absolutePath.Value)
            let matches = ResizeArray<TodoMatch>()
            for i in 0 .. lines.Length - 1 do
                if todoPattern.IsMatch(lines[i]) then
                    matches.Add({ File = filePath; Line = i + 1; Text = lines[i] })
            return matches :> IReadOnlyList<_>
        with
        | :? IOException as ex ->
            Logger.Warning $"Cannot read file %s{filePath.Value}: %s{ex.Message}"
            return ResizeArray<TodoMatch>() :> IReadOnlyList<_>
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
        for file in files do
            let! matches = ScanFile(workingDirectory, file)
            allMatches.AddRange(matches)
        for m in allMatches do
            Logger.Info(FormatMatch(m, isCi))
        if allMatches.Count > 0 then
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
