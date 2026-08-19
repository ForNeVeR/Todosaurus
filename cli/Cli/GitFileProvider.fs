// SPDX-FileCopyrightText: 2026 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

module internal Todosaurus.Cli.GitFileProvider

open System
open System.Collections.Generic
open System.Threading.Tasks
open TruePath

/// https://git-scm.com/book/en/v2/Git-Tools-Submodules
let private submoduleMode = "160000"

let private splitNullSeparated(value: string): string array =
    value.Split('\000', StringSplitOptions.RemoveEmptyEntries)

/// Format: "<mode> <hash> <stage>\t<relative-path>"
let private getPathFromCachedLsFilesEntry(entry: string): string option =
    match entry.IndexOf('\t') with
    | -1 -> failwithf $"Cannot parse ls-files output line: \"%s{entry}\"."
    | metadataSeparator ->
        let metadata = entry.Substring(0, metadataSeparator)
        let relativePath = entry.Substring(metadataSeparator + 1)
        let metadataParts = metadata.Split(' ', StringSplitOptions.RemoveEmptyEntries)

        match metadataParts with
        | [| mode; _objectId; _stage |] when mode = submoduleMode -> None // ignore submodule entries
        | [| _mode; _objectId; _stage |] -> Some relativePath
        | _ -> failwithf $"Cannot parse ls-files output line: \"%s{entry}\"."

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
        // Ideally, we would collect all the info with one call to git ls-files, but unfortunately, it's not possible:
        // the output formats for most common call (--stage --others) differs between different kinds of entries, and
        // it's impossible to know in advance if we observe a valid staged file entry, or an unstaged file with its name
        // including tabs and spaces, so that it looks like a valid staged entry.
        let! cachedResult =
            Shell.RunProcess(directory, LocalPath "git", [ "ls-files"; "-z"; "--stage"; "--cached" ])

        let cachedFiles =
            cachedResult.StandardOutput
            |> splitNullSeparated
            |> Array.choose getPathFromCachedLsFilesEntry

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
