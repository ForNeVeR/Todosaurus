// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module internal Todosaurus.Cli.GitDiscovery

open TruePath
open TruePath.SystemIo

let FindGitRepoRoot(startDirectory: AbsolutePath): AbsolutePath option =
    let rec walk(directory: AbsolutePath) =
        let gitPath = (directory / ".git")

        if gitPath.Exists() then
            Some directory
        else
            let parent = directory.Parent

            if parent.HasValue then
                walk parent.Value
            else
                None

    walk startDirectory
