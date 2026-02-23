// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module Todosaurus.Cli.Program

open System.CommandLine
open System.IO
open System.Threading.Tasks
open TruePath

[<EntryPoint>]
let main(args: string[]): int =
    let rootCommand = RootCommand("Todosaurus — a tool to process TODO issues in a repository.")
    let configOption = Option<string>("--config")
    configOption.Description <- "Path to configuration file (default: todosaurus.toml in working directory)"
    rootCommand.Add(configOption)
    rootCommand.Add(FilesCommand.CreateCommand())
    rootCommand.Add(ScanCommand.CreateCommand(configOption))
    rootCommand.SetAction(fun (parseResult: ParseResult) ->
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
                return! ScanCommand.Scan(workingDirectory, config, GitHubClient.CreateClient)
        } : Task<int>)
    rootCommand.Parse(args).Invoke()
