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

let ListFiles(directory: AbsolutePath, includeUntracked: bool): Task<IReadOnlyList<AbsolutePath>> =
    task {
        let args =
            if includeUntracked then
                [ "ls-files"; "-z"; "--cached"; "--others"; "--exclude-standard" ]
            else
                [ "ls-files"; "-z"; "--cached" ]

        let! result = Shell.RunProcess(directory, LocalPath "git", args)

        return
            result.StandardOutput.Split('\000', StringSplitOptions.RemoveEmptyEntries)
            |> Array.map(fun relativePath -> directory / relativePath)
            :> IReadOnlyList<_>
    }
