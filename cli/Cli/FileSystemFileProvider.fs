// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module internal Todosaurus.Cli.FileSystemFileProvider

open System.Collections.Generic
open System.IO
open System.Threading.Tasks
open TruePath
open TruePath.SystemIo

let ListAllFiles(directory: AbsolutePath): Task<IReadOnlyList<AbsolutePath>> =
    task {
        return upcast (
            directory.GetFiles("*", SearchOption.AllDirectories)
            |> Seq.map AbsolutePath
            |> Seq.toArray
        )
    }
