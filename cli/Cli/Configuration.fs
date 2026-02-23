// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module internal Todosaurus.Cli.Configuration

open System.Collections.Immutable
open System.IO
open System.Threading.Tasks
open Microsoft.Extensions.FileSystemGlobbing
open Tomlyn
open Tomlyn.Model
open TruePath

type TodosaurusConfig = {
    TrackerUrl: string option
    Exclusions: ImmutableArray<LocalPathPattern>
    ConfigDirectory: AbsolutePath
}

let Empty(directory: AbsolutePath): TodosaurusConfig = {
    TrackerUrl = None
    Exclusions = ImmutableArray.Empty
    ConfigDirectory = directory
}

let private tryGetValue<'a> (table: TomlTable) (key: string): 'a option =
    let value = ref Unchecked.defaultof<_>
    if table.TryGetValue(key, value)
    then Some(value.Value :?> 'a)
    else None

let private getValue<'a> (table: TomlTable) (key: string): 'a =
    tryGetValue table key
    |> Option.defaultWith(fun () -> failwithf $"Key not found in table: \"%s{key}\".")

let ReadConfig(configPath: AbsolutePath option, workingDirectory: AbsolutePath): Task<Result<TodosaurusConfig, string>> =
    task {
        let resolvedPath, isExplicit =
            match configPath with
            | Some p -> p, true
            | None -> workingDirectory / "todosaurus.toml", false

        if not(File.Exists resolvedPath.Value) then
            if isExplicit then
                return Error $"Configuration file not found: %s{resolvedPath.Value}"
            else
                return Ok(Empty workingDirectory)
        else
            try
                let! text = File.ReadAllTextAsync(resolvedPath.Value)
                let table = Toml.ToModel(text, sourcePath = resolvedPath.Value)

                let trackerUrl =
                    tryGetValue<TomlTable> table "tracker"
                    |> Option.bind (fun t -> tryGetValue<string> t "url")

                let trackerValid =
                    match trackerUrl with
                    | Some url ->
                        match GitRemote.ParseGitHubUrl url with
                        | None -> Some $"Invalid tracker URL (must be a GitHub URL): %s{url}"
                        | Some _ -> None
                    | None -> None

                match trackerValid with
                | Some errorMsg ->
                    return Error errorMsg
                | None ->

                let exclusions =
                    tryGetValue<TomlArray> table "exclusions"
                    |> Option.map (fun a ->
                        a |> Seq.cast<string> |> Seq.map LocalPathPattern |> ImmutableArray.CreateRange
                    )
                    |> Option.defaultValue ImmutableArray.Empty

                let configDir = resolvedPath.Parent
                return Ok {
                    TrackerUrl = trackerUrl
                    Exclusions = exclusions
                    ConfigDirectory = configDir.Value
                }
            with
            | ex ->
                return Error $"Error reading configuration file: %s{ex.Message}"
    }

let ApplyExclusions(files: LocalPath seq, config: TodosaurusConfig): LocalPath seq =
    if config.Exclusions.IsEmpty then
        files
    else
        let matcher = Matcher()
        matcher.AddIncludePatterns(config.Exclusions |> Seq.map _.Value)

        files
        |> Seq.filter (fun f ->
            let result = matcher.Match(f.Value)
            not result.HasMatches
        )

