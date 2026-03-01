// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

#nowarn "3265" // F# nullable value type limitation with System.CommandLine GetValue<bool>

module Todosaurus.Tests.ProgramTests

open Todosaurus.Cli
open Xunit

[<Fact>]
let ``--config option is parsed correctly``() =
    let cmd = Program.CreateRootCommand()
    let parseResult = cmd.Parse("--config foo.toml")
    Assert.Equal("foo.toml", parseResult.GetValue(Program.ConfigOption))

[<Fact>]
let ``--strict option is parsed correctly``() =
    let cmd = Program.CreateRootCommand()
    let parseResult = cmd.Parse("--strict")
    Assert.True(parseResult.GetValue(Program.StrictOption))

[<Fact>]
let ``--strict defaults to false``() =
    let cmd = Program.CreateRootCommand()
    let parseResult = cmd.Parse("")
    Assert.False(parseResult.GetValue(Program.StrictOption))

[<Fact>]
let ``--config defaults to null``() =
    let cmd = Program.CreateRootCommand()
    let parseResult = cmd.Parse("")
    Assert.Null(parseResult.GetValue(Program.ConfigOption))

[<Fact>]
let ``scan subcommand accepts --strict and --config``() =
    let cmd = Program.CreateRootCommand()
    let parseResult = cmd.Parse("scan --strict --config bar.toml")
    Assert.True(parseResult.GetValue(Program.StrictOption))
    Assert.Equal("bar.toml", parseResult.GetValue(Program.ConfigOption))

[<Fact>]
let ``Unknown option produces parse errors``() =
    let cmd = Program.CreateRootCommand()
    let parseResult = cmd.Parse("--unknown")
    Assert.NotEmpty(parseResult.Errors)
