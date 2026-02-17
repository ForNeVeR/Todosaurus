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
    rootCommand.Add(FilesCommand.CreateCommand())
    rootCommand.Add(ScanCommand.CreateCommand())
    rootCommand.SetAction(fun (_parseResult: ParseResult) ->
        task {
            let workingDirectory = AbsolutePath.CurrentWorkingDirectory
            return! ScanCommand.Scan workingDirectory
        } : Task<int>)
    rootCommand.Parse(args).Invoke()
