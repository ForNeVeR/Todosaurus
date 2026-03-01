// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module internal Todosaurus.Cli.FilesCommand

open System.Collections.Generic
open System.CommandLine
open System.Threading.Tasks
open TruePath

let ListEligibleFiles(ctx: LoggerContext, workingDirectory: AbsolutePath): Task<IReadOnlyList<LocalPath>> =
    task {
        let! files =
            match GitDiscovery.FindGitRepoRoot workingDirectory with
            | Some _ ->
                task {
                    let! gitAvailable = GitFileProvider.IsGitAvailable()

                    if gitAvailable then
                        let includeUntracked = not <| Env.IsCi()
                        return! GitFileProvider.ListFiles(workingDirectory, includeUntracked)
                    else
                        Logger.Warning(ctx,
                            "git not found in PATH, falling back to file system enumeration.")

                        return! FileSystemFileProvider.ListAllFiles workingDirectory
                }
            | None -> FileSystemFileProvider.ListAllFiles workingDirectory

        let! textFiles =
            task {
                let results = ResizeArray<AbsolutePath>()

                for file in files do
                    let! isText = TextFileFilter.IsTextFile(ctx, file)

                    if isText then
                        results.Add(file)

                return results
            }

        return
            textFiles
            |> Seq.map _.RelativeTo(workingDirectory)
            |> Seq.sortBy _.Value
            |> Seq.toArray
            :> IReadOnlyList<_>
    }

let CreateCommand(): Command =
    // IgnoreTODO-Start
    let command = Command("files", "List text files eligible for TODO checking")
    // IgnoreTODO-End

    command.SetAction(fun (_parseResult: ParseResult) ->
        task {
            let ctx = LoggerContext.Create()
            let workingDirectory = AbsolutePath.CurrentWorkingDirectory
            let! files = ListEligibleFiles(ctx, workingDirectory)

            for file in files do
                Logger.Info(file.Value)
        }
        :> Task)

    command
