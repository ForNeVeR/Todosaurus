// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module Todosaurus.Cli.Program

open System.CommandLine
open System.Threading.Tasks
open TruePath

[<EntryPoint>]
let main(args: string[]): int =
    let rootCommand = RootCommand("Todosaurus — a tool to process TODO issues in a repository.")
    rootCommand.Add(ScanCommand.TrackerOption)
    rootCommand.Add(FilesCommand.CreateCommand())
    rootCommand.Add(ScanCommand.CreateCommand())
    rootCommand.SetAction(fun (parseResult: ParseResult) ->
        task {
            let workingDirectory = AbsolutePath.CurrentWorkingDirectory
            let tracker =
                match parseResult.GetValue(ScanCommand.TrackerOption) with
                | null -> None
                | v -> Some v
            return! ScanCommand.Scan(workingDirectory, tracker, GitHubClient.CreateClient)
        } : Task<int>)
    rootCommand.Parse(args).Invoke()
