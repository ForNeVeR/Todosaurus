// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module internal Todosaurus.Cli.Logger

let Info(message: string): unit =
    printfn $"%s{message}"

let Warning(message: string): unit =
    eprintfn $"WARNING: %s{message}"

let Error(message: string): unit =
    eprintfn $"ERROR: %s{message}"
