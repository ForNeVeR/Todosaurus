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
    Context: LoggerContext
    Warnings: ResizeArray<string>
    Errors: ResizeArray<string>
}

let RunWithLoggerCollector(action: LoggerContext -> Task): Task<LogResult> =
    task {
        let warnings = ResizeArray()
        let errors = ResizeArray()
        let ctx = {
            WarningCount = 0
            ErrorCount = 0
            OnWarning = Some warnings.Add
            OnError = Some errors.Add
        }
        Env.SetIsCiOverride(Some false)
        try
            do! action ctx
            return { Context = ctx; Warnings = warnings; Errors = errors }
        finally
            Env.SetIsCiOverride(None)
    }
