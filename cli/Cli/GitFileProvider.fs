// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module internal Todosaurus.Cli.GitFileProvider

open System
open System.Collections.Generic
open System.Threading.Tasks
open TruePath

let IsGitAvailable(): Task<bool> =
    task {
        try
            let! _ = Shell.RunProcess(Temporary.SystemTempDirectory(), LocalPath "git", [ "--version" ])
            return true
        with
        | _ ->
            return false
    }

let ListFiles(directory: AbsolutePath): Task<IReadOnlyList<AbsolutePath>> =
    task {
        let! result =
            Shell.RunProcess(
                directory,
                LocalPath "git",
                [ "ls-files"; "-z"; "--cached"; "--others"; "--exclude-standard" ]
            )

        return
            result.StandardOutput.Split('\000', StringSplitOptions.RemoveEmptyEntries)
            |> Array.map(fun relativePath -> directory / relativePath)
            :> IReadOnlyList<_>
    }
