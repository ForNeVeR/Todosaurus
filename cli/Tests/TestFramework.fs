// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module internal Todosaurus.Tests.TestFramework

open System.IO
open System.Threading.Tasks
open Todosaurus.Cli
open TruePath
open TruePath.SystemIo

let private forceDeleteDirectory(path: AbsolutePath) =
    for file in path.GetFiles("*", SearchOption.AllDirectories) do
        File.SetAttributes(file, FileAttributes.Normal)

    path.DeleteDirectoryRecursively()

let WithTempDir(action: AbsolutePath -> Task): Task =
    task {
        let tempDir = Temporary.CreateTempFolder("todosaurus-test-")

        try
            do! action tempDir
        finally
            forceDeleteDirectory tempDir
    }

type LogResult = {
    Warnings: ResizeArray<string>
    Errors: ResizeArray<string>
}

let RunWithLoggerCollector(action: unit -> Task): Task<LogResult> =
    task {
        let result = { Warnings = ResizeArray(); Errors = ResizeArray() }
        Logger.SetCollectors(Some result.Warnings.Add, Some result.Errors.Add)
        Env.SetIsCiOverride(Some false)
        try
            do! action()
            return result
        finally
            Logger.SetCollectors(None, None)
            Env.SetIsCiOverride(None)
    }
