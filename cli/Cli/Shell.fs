// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module internal Todosaurus.Cli.Shell

open System.Threading.Tasks
open Medallion.Shell
open TruePath

let RunProcess(workingDirectory: AbsolutePath, executable: LocalPath, args: string seq): Task<CommandResult> =
    task {
        let boxedArgs = args |> Seq.map (fun s -> s :> obj) |> Seq.toArray
        let command = Command.Run(
            executable.Value,
            boxedArgs,
            fun (options: Shell.Options) ->
                options.WorkingDirectory(workingDirectory.Value) |> ignore
        )

        let! result = command.Task

        if not result.Success then
            failwith
                $"Process \"%s{executable.Value}\" exited with code %d{result.ExitCode}. Stderr: %s{result.StandardError}"

        return result
    }
