// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module Todosaurus.Cli.Program

open System.CommandLine
open Todosaurus.Cli

[<EntryPoint>]
let main(args: string[]): int =
    let rootCommand = RootCommand("Todosaurus — a tool to process TODO issues in a repository.")
    rootCommand.Add(FilesCommand.CreateCommand())
    rootCommand.Parse(args).Invoke()
