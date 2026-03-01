// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module internal Todosaurus.Cli.TextFileFilter

open System
open System.IO
open System.Threading.Tasks
open TruePath
open TruePath.SystemIo

let private bufferSize = 8000

let IsTextFile(ctx: LoggerContext, path: AbsolutePath): Task<bool> =
    task {
        try
            use stream = path.OpenRead()
            let buffer = Array.zeroCreate bufferSize
            let! bytesRead = stream.ReadAsync(buffer, 0, bufferSize)
            if bytesRead = 0 then
                return true // empty file is text
            else
                return not (Array.exists (fun b -> b = 0uy) buffer[..bytesRead - 1])
        with
        | :? IOException as ex ->
            Logger.Warning(ctx, $"Cannot read file %s{path.Value}: %s{ex.Message}")
            return false
        | :? UnauthorizedAccessException as ex ->
            Logger.Warning(ctx, $"Cannot read file %s{path.Value}: %s{ex.Message}")
            return false
    }
