// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module Todosaurus.Tests.ScanCommandTests

open System.IO
open System.Threading.Tasks
open Todosaurus.Cli
open Todosaurus.Tests.TestFramework
open TruePath
open TruePath.SystemIo
open Xunit

[<Fact>]
let ``Bare TODO in a file is detected``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "line one\n// TODO fix this\nline three"
        let! matches = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        Assert.Equal(1, matches.Count)
        Assert.Equal(2, matches[0].Line)
        Assert.Contains("TODO", matches[0].Text)
    })

[<Fact>]
let ``TODO with issue number is not detected``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#123]: this is tracked"
        let! matches = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        Assert.Empty matches
    })

[<Fact>]
let ``Case-insensitive TODO variants are detected``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// todo fix\n// Todo fix\n// ToDo fix\n// TODO fix"
        let! matches = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        Assert.Equal(4, matches.Count)
    })

[<Fact>]
let ``FormatMatch produces GitHub Actions warning format in CI mode``(): unit =
    let m: ScanCommand.TodoMatch = { File = LocalPath "src/Main.fs"; Line = 42; Text = "// TODO fix this" }
    let result = ScanCommand.FormatMatch(m, true)
    Assert.Equal("::warning file=src/Main.fs,line=42,title=Unresolved TODO::// TODO fix this", result)

[<Fact>]
let ``FormatMatch produces human-readable format in local mode``(): unit =
    let m: ScanCommand.TodoMatch = { File = LocalPath "src/Main.fs"; Line = 42; Text = "// TODO fix this" }
    let result = ScanCommand.FormatMatch(m, false)
    let expectedPath = "src" + string Path.DirectorySeparatorChar + "Main.fs"
    Assert.Equal($"%s{expectedPath}(42): // TODO fix this", result)

[<Fact>]
let ``TODO with space before bracket is detected``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO [#124]: this has a space"
        let! matches = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        Assert.Equal(1, matches.Count)
    })

[<Fact>]
let ``Scan returns non-zero exit code when TODOs found``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO fix this"
        let! exitCode = ScanCommand.Scan tempDir
        Assert.Equal(1, exitCode)
    })

[<Fact>]
let ``Scan returns zero exit code when no TODOs found``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#123]: tracked\nclean line"
        let! exitCode = ScanCommand.Scan tempDir
        Assert.Equal(0, exitCode)
    })
