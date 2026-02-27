// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module Todosaurus.Cli.Program

open System.CommandLine
open System.Threading.Tasks

let internal ConfigOption = Option<string>("--config")
let internal StrictOption = Option<bool>("--strict")

let internal CreateRootCommand(): RootCommand =
    // IgnoreTODO-Start
    let rootCommand = RootCommand("Todosaurus — a tool to process TODO issues in a repository.")
    // IgnoreTODO-End
    ConfigOption.Description <- "Path to configuration file (default: todosaurus.toml in working directory)"
    StrictOption.Description <- "Treat warnings as errors (non-zero exit code)"
    rootCommand.Add(ConfigOption)
    rootCommand.Add(StrictOption)
    rootCommand.Add(FilesCommand.CreateCommand())
    rootCommand.Add(ScanCommand.CreateCommand(ConfigOption, StrictOption))
    rootCommand.SetAction(fun (parseResult: ParseResult) ->
        ScanCommand.RunScan(
            TruePath.AbsolutePath.CurrentWorkingDirectory,
            parseResult.GetValue(ConfigOption),
            #nowarn 3265 // F# nullable value type limitation with System.CommandLine GetValue<bool>
            parseResult.GetValue(StrictOption),
            #warnon 3265
            GitHubClient.CreateClient
        ) : Task<int>)
    rootCommand

[<EntryPoint>]
let main(args: string[]): int =
    CreateRootCommand().Parse(args).Invoke()
