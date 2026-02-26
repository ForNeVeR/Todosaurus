// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module Todosaurus.Cli.Markers

[<Literal>]
let IgnoreToDoStart: string = "IgnoreTODO-Start"

// NOTE: This strategic placement of the markers allow us to ignore this TODO item easily.
[<Literal>]
let ToDoItem: string = "TODO"

[<Literal>]
let IgnoreToDoEnd: string = "IgnoreTODO-End"
