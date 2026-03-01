// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module internal Todosaurus.Cli.Env

open System
open System.Threading
open TruePath

let private isCiOverride = AsyncLocal<bool option>()

let IsCi(): bool =
    match isCiOverride.Value with
    | Some v -> v
    | None ->
        let var = Environment.GetEnvironmentVariable"CI"
        not(isNull var) && var <> "0" && not (String.Equals(var, "false", StringComparison.InvariantCultureIgnoreCase))

let CiWorkspace = lazy AbsolutePath(nonNull <| Environment.GetEnvironmentVariable "GITHUB_WORKSPACE")

let SetIsCiOverride(value: bool option): unit =
    isCiOverride.Value <- value
