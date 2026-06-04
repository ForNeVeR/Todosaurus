// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module internal Todosaurus.Cli.GitFileProvider

open System
open System.Collections.Generic
open System.Threading.Tasks
open TruePath

let private gitlinkMode = "160000"

let private splitNullSeparated(value: string): string array =
    value.Split('\000', StringSplitOptions.RemoveEmptyEntries)

let private tryGetPathFromCachedLsFilesEntry(entry: string): string option =
    match entry.IndexOf('\t') with
    | -1 -> Some entry
    | metadataSeparator ->
        let metadata = entry.Substring(0, metadataSeparator)
        let relativePath = entry.Substring(metadataSeparator + 1)
        let metadataParts = metadata.Split(' ', StringSplitOptions.RemoveEmptyEntries)

        match metadataParts with
        | [| mode; _objectId; _stage |] when mode = gitlinkMode -> None
        | [| _mode; _objectId; _stage |] -> Some relativePath
        | _ -> Some entry

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
        let! cachedResult =
            Shell.RunProcess(directory, LocalPath "git", [ "ls-files"; "-z"; "--stage"; "--cached" ])

        let cachedFiles =
            cachedResult.StandardOutput
            |> splitNullSeparated
            |> Array.choose tryGetPathFromCachedLsFilesEntry

        let! untrackedFiles =
            if includeUntracked then
                task {
                    let! result =
                        Shell.RunProcess(directory, LocalPath "git", [ "ls-files"; "-z"; "--others"; "--exclude-standard" ])

                    return splitNullSeparated result.StandardOutput
                }
            else
                task {
                    return [||]
                }

        return
            Array.append cachedFiles untrackedFiles
            |> Array.map(fun relativePath -> directory / relativePath)
            :> IReadOnlyList<_>
    }
