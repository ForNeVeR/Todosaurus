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

let private unwrapOk(result: Result<'T, string>): 'T =
    match result with
    | Ok v -> v
    | Error e -> failwith $"Unexpected scan error: %s{e}"

[<Fact>]
let ``Bare TODO in a file is detected``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "line one\n// TODO fix this\nline three"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let matches = unwrapOk result
        Assert.Equal(1, matches.Count)
        Assert.Equal(2, matches[0].Line)
        Assert.Contains("TODO", matches[0].Text)
    })

[<Fact>]
let ``TODO with issue number is not detected``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#123]: this is tracked"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let matches = unwrapOk result
        Assert.Empty matches
    })

[<Fact>]
let ``Case-insensitive TODO variants are detected``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// todo fix\n// Todo fix\n// ToDo fix\n// TODO fix"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let matches = unwrapOk result
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
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let matches = unwrapOk result
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

[<Fact>]
let ``TODOs inside ignore region are skipped``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// IgnoreTODO-Start\n// TODO fix this\n// IgnoreTODO-End"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let matches = unwrapOk result
        Assert.Empty matches
    })

[<Fact>]
let ``TODOs outside ignore region are still detected``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO first\n// IgnoreTODO-Start\n// TODO ignored\n// IgnoreTODO-End\n// TODO second"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let matches = unwrapOk result
        Assert.Equal(2, matches.Count)
        Assert.Equal(1, matches[0].Line)
        Assert.Equal(5, matches[1].Line)
    })

[<Fact>]
let ``Unclosed IgnoreTODO-Start is an error``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// IgnoreTODO-Start\n// TODO fix this"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        match result with
        | Error msg -> Assert.Contains("Unclosed IgnoreTODO-Start", msg)
        | Ok _ -> failwith "Expected an error for unclosed IgnoreTODO-Start"
    })

[<Fact>]
let ``Nested IgnoreTODO-Start is an error``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// IgnoreTODO-Start\n// IgnoreTODO-Start\n// IgnoreTODO-End"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        match result with
        | Error msg -> Assert.Contains("Nested IgnoreTODO-Start", msg)
        | Ok _ -> failwith "Expected an error for nested IgnoreTODO-Start"
    })

[<Fact>]
let ``IgnoreTODO-End without matching Start is an error``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// IgnoreTODO-End"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        match result with
        | Error msg -> Assert.Contains("IgnoreTODO-End without matching IgnoreTODO-Start", msg)
        | Ok _ -> failwith "Expected an error for IgnoreTODO-End without Start"
    })

[<Fact>]
let ``Scan returns exit code 2 on marker error``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// IgnoreTODO-Start\n// TODO fix this"
        let! exitCode = ScanCommand.Scan tempDir
        Assert.Equal(2, exitCode)
    })

[<Fact>]
let ``Scan returns zero exit code when all TODOs are inside ignore regions``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "clean line\n// IgnoreTODO-Start\n// TODO fix this\n// IgnoreTODO-End\nclean line"
        let! exitCode = ScanCommand.Scan tempDir
        Assert.Equal(0, exitCode)
    })

[<Fact>]
let ``Multiple markers on the same line is an error``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// IgnoreTODO-Start IgnoreTODO-End"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        match result with
        | Error msg -> Assert.Contains("Multiple IgnoreTODO markers", msg)
        | Ok _ -> failwith "Expected an error for multiple markers on same line"
    })

[<Fact>]
let ``Marker and TODO on the same line is an error``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// IgnoreTODO-Start TODO fix"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        match result with
        | Error msg -> Assert.Contains("IgnoreTODO marker and TODO on the same line", msg)
        | Ok _ -> failwith "Expected an error for marker and TODO on same line"
    })

[<Fact>]
let ``Multiple unresolved TODOs on the same line are detected as one match``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO first TODO second"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let matches = unwrapOk result
        Assert.Equal(1, matches.Count)
    })

[<Fact>]
let ``Resolved and unresolved TODO on the same line: unresolved is detected``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#123]: tracked TODO fix"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let matches = unwrapOk result
        Assert.Equal(1, matches.Count)
    })

[<Fact>]
let ``Multiple resolved TODOs on the same line: no match``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#123]: a TODO[#456]: b"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let matches = unwrapOk result
        Assert.Empty matches
    })

[<Fact>]
let ``Scan returns exit code 2 when marker and TODO on same line``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// IgnoreTODO-Start TODO fix"
        let! exitCode = ScanCommand.Scan tempDir
        Assert.Equal(2, exitCode)
    })
