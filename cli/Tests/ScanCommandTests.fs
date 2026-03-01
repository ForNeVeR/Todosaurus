// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module Todosaurus.Tests.ScanCommandTests

open System.Threading.Tasks
open Todosaurus.Cli
open Todosaurus.Tests.TestFramework
open TruePath
open TruePath.SystemIo
open Xunit

let private unwrapOk(result: Result<'T, unit>): 'T =
    match result with
    | Ok v -> v
    | Error() -> failwith "Unexpected scan error"

let private mockChecker(responses: Map<int, GitHubClient.IssueStatus>): GitHubClient.IIssueChecker =
    { new GitHubClient.IIssueChecker with
        member _.CheckIssue(_owner, _repo, issueNumber) =
            task {
                return
                    match responses |> Map.tryFind issueNumber with
                    | Some status -> status
                    | None -> GitHubClient.NotFound
            }
    }

let private allOpenChecker(): GitHubClient.IIssueChecker =
    { new GitHubClient.IIssueChecker with
        member _.CheckIssue(_owner, _repo, _issueNumber) = task { return GitHubClient.Open }
    }

let private configWithTracker(dir: AbsolutePath): Configuration.TodosaurusConfig =
    { Configuration.Empty dir with TrackerUrl = Some "https://github.com/owner/repo" }

let private scanSimple(ctx: LoggerContext, dir: AbsolutePath): Task<int> =
    ScanCommand.Scan(ctx, dir, configWithTracker dir, allOpenChecker)

// NOTE: this is needed to make compilation significantly quicker (90 sec -> 4 sec), see
// https://github.com/dotnet/fsharp/issues/18807 for more details.
let private assertIntEqual (x: int) (y: int) =
    Assert.Equal<int>(x, y)

// IgnoreTODO-Start

// --- ScanFile: unresolved TODO detection ---

[<Fact>]
let ``Bare TODO in a file is detected``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "line one\n// TODO fix this\nline three"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            let scanResult = unwrapOk result
            assertIntEqual 1 scanResult.UnresolvedMatches.Count
            assertIntEqual 2 scanResult.UnresolvedMatches[0].Line
            Assert.Contains("TODO", scanResult.UnresolvedMatches[0].Text)
        })
        Assert.Empty log.Warnings
        Assert.Empty log.Errors
    })

[<Fact>]
let ``TODO with issue number is not detected as unresolved``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#123]: this is tracked"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            let scanResult = unwrapOk result
            Assert.Empty scanResult.UnresolvedMatches
        })
        Assert.Empty log.Warnings
        Assert.Empty log.Errors
    })

[<Fact>]
let ``Case-insensitive TODO variants are detected``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "// todo fix\n// Todo fix\n// ToDo fix\n// TODO fix"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            let scanResult = unwrapOk result
            assertIntEqual 4 scanResult.UnresolvedMatches.Count
        })
        Assert.Empty log.Warnings
        Assert.Empty log.Errors
    })

[<Fact>]
let ``TODO with space before bracket is detected``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "// TODO [#124]: this has a space"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            let scanResult = unwrapOk result
            assertIntEqual 1 scanResult.UnresolvedMatches.Count
        })
        Assert.Empty log.Warnings
        Assert.Empty log.Errors
    })

[<Fact>]
let ``Scan returns non-zero exit code when TODOs found``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "// TODO fix this"
            let! exitCode = scanSimple(ctx, tempDir)
            assertIntEqual 5 exitCode
        })
        assertIntEqual 1 log.Warnings.Count
        Assert.Contains("Unresolved TODO", log.Warnings[0])
        Assert.Empty log.Errors
    })

[<Fact>]
let ``Scan returns zero exit code when no TODOs found``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "clean line\nmore clean"
            let! exitCode = scanSimple(ctx, tempDir)
            assertIntEqual 0 exitCode
        })
        Assert.Empty log.Warnings
        Assert.Empty log.Errors
    })

[<Fact>]
let ``TODOs inside ignore region are skipped``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync $"// {Markers.IgnoreToDoStart}\n// TODO fix this\n// {Markers.IgnoreToDoEnd}"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            let scanResult = unwrapOk result
            Assert.Empty scanResult.UnresolvedMatches
        })
        Assert.Empty log.Warnings
        Assert.Empty log.Errors
    })

[<Fact>]
let ``TODOs outside ignore region are still detected``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync $"// TODO first\n// {Markers.IgnoreToDoStart}\n// TODO ignored\n// {Markers.IgnoreToDoEnd}\n// TODO second"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            let scanResult = unwrapOk result
            assertIntEqual 2 scanResult.UnresolvedMatches.Count
            assertIntEqual 1 scanResult.UnresolvedMatches[0].Line
            assertIntEqual 5 scanResult.UnresolvedMatches[1].Line
        })
        Assert.Empty log.Warnings
        Assert.Empty log.Errors
    })

[<Fact>]
let ``Unclosed IgnoreTODO start is an error``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync $"// {Markers.IgnoreToDoStart}\n// TODO fix this"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            match result with
            | Error() -> ()
            | Ok _ -> failwith $"Expected an error for unclosed {Markers.IgnoreToDoStart}"
        })
        Assert.Empty log.Warnings
        let error = Assert.Single log.Errors
        Assert.Contains($"Unclosed {Markers.IgnoreToDoStart}", error)
    })

[<Fact>]
let ``Nested IgnoreTODO start is an error``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync $"// {Markers.IgnoreToDoStart}\n// {Markers.IgnoreToDoStart}\n// {Markers.IgnoreToDoEnd}"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            match result with
            | Error() -> ()
            | Ok _ -> failwith $"Expected an error for nested {Markers.IgnoreToDoStart}"
        })
        Assert.Empty log.Warnings
        let error = Assert.Single log.Errors
        Assert.Contains($"Nested {Markers.IgnoreToDoStart}", error)
    })

[<Fact>]
let ``IgnoreTODO end without a matching Start is an error``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync $"// {Markers.IgnoreToDoEnd}"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            match result with
            | Error() -> ()
            | Ok _ -> failwith $"Expected an error for {Markers.IgnoreToDoEnd} without Start"
        })
        Assert.Empty log.Warnings
        let error = Assert.Single log.Errors
        Assert.Contains($"{Markers.IgnoreToDoEnd} without", error)
    })

[<Fact>]
let ``Scan returns exit code 6 on marker error``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync $"// {Markers.IgnoreToDoStart}\n// TODO fix this"
            let! exitCode = scanSimple(ctx, tempDir)
            assertIntEqual 6 exitCode
        })
        Assert.Empty log.Warnings
        assertIntEqual 1 log.Errors.Count
    })

[<Fact>]
let ``Scan returns zero exit code when all TODOs are inside ignore regions``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync $"clean line\n// {Markers.IgnoreToDoStart}\n// TODO fix this\n// {Markers.IgnoreToDoEnd}\nclean line"
            let! exitCode = scanSimple(ctx, tempDir)
            assertIntEqual 0 exitCode
        })
        Assert.Empty log.Warnings
        Assert.Empty log.Errors
    })

[<Fact>]
let ``Multiple markers on the same line is an error``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync $"// {Markers.IgnoreToDoStart} {Markers.IgnoreToDoEnd}"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            match result with
            | Error() -> ()
            | Ok _ -> failwith "Expected an error for multiple markers on same line"
        })
        Assert.Empty log.Warnings
        let error = Assert.Single log.Errors
        Assert.Contains("Multiple IgnoreTODO markers", error)
    })

[<Fact>]
let ``Marker and TODO on the same line is an error``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync $"// {Markers.IgnoreToDoStart} TODO fix"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            match result with
            | Error() -> ()
            | Ok _ -> failwith "Expected an error for marker and TODO on same line"
        })
        Assert.Empty log.Warnings
        let error = Assert.Single log.Errors
        Assert.Contains("IgnoreTODO marker and TODO", error)
    })

[<Fact>]
let ``Multiple unresolved TODOs on the same line are detected as one match``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "// TODO first TODO second"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            let scanResult = unwrapOk result
            assertIntEqual 1 scanResult.UnresolvedMatches.Count
        })
        Assert.Empty log.Warnings
        Assert.Empty log.Errors
    })

[<Fact>]
let ``Resolved and unresolved TODO on the same line: unresolved is detected``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#123]: tracked TODO fix"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            let scanResult = unwrapOk result
            assertIntEqual 1 scanResult.UnresolvedMatches.Count
        })
        Assert.Empty log.Warnings
        Assert.Empty log.Errors
    })

[<Fact>]
let ``Multiple resolved TODOs on the same line: no match``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#123]: a TODO[#456]: b"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            let scanResult = unwrapOk result
            Assert.Empty scanResult.UnresolvedMatches
        })
        Assert.Empty log.Warnings
        Assert.Empty log.Errors
    })

[<Fact>]
let ``Scan returns exit code 6 when marker and TODO on same line``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync $"// {Markers.IgnoreToDoStart} TODO fix"
            let! exitCode = scanSimple(ctx, tempDir)
            assertIntEqual 6 exitCode
        })
        Assert.Empty log.Warnings
        assertIntEqual 1 log.Errors.Count
    })

// ## ScanFile: connected TODO detection

[<Fact>]
let ``Connected TODO extracts issue number``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#123]: fix this"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            let scanResult = unwrapOk result
            assertIntEqual 1 scanResult.ConnectedMatches.Count
            assertIntEqual 123 scanResult.ConnectedMatches[0].IssueNumber
            assertIntEqual 1 scanResult.ConnectedMatches[0].Line
        })
        Assert.Empty log.Warnings
        Assert.Empty log.Errors
    })

[<Fact>]
let ``Connected TODO with colon extracts issue number``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "// TODO:[#456] fix"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            let scanResult = unwrapOk result
            assertIntEqual 1 scanResult.ConnectedMatches.Count
            assertIntEqual 456 scanResult.ConnectedMatches[0].IssueNumber
        })
        Assert.Empty log.Warnings
        Assert.Empty log.Errors
    })

[<Fact>]
let ``Multiple connected TODOs on same line``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#1]: a TODO[#2]: b"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            let scanResult = unwrapOk result
            assertIntEqual 2 scanResult.ConnectedMatches.Count
            assertIntEqual 1 scanResult.ConnectedMatches[0].IssueNumber
            assertIntEqual 2 scanResult.ConnectedMatches[1].IssueNumber
        })
        Assert.Empty log.Warnings
        Assert.Empty log.Errors
    })

[<Fact>]
let ``Bare TODO is not matched as connected``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "// TODO fix this"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            let scanResult = unwrapOk result
            Assert.Empty scanResult.ConnectedMatches
        })
        Assert.Empty log.Warnings
        Assert.Empty log.Errors
    })

[<Fact>]
let ``TODO with space before bracket is not matched as connected``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "// TODO [#123]: has space"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            let scanResult = unwrapOk result
            Assert.Empty scanResult.ConnectedMatches
        })
        Assert.Empty log.Warnings
        Assert.Empty log.Errors
    })

[<Fact>]
let ``Connected TODO inside IgnoreTODO region is skipped``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync $"// {Markers.IgnoreToDoStart}\n// TODO[#99]: ignored\n// {Markers.IgnoreToDoEnd}"
            let! result = ScanCommand.ScanFile(ctx, tempDir, LocalPath "test.txt")
            let scanResult = unwrapOk result
            Assert.Empty scanResult.ConnectedMatches
        })
        Assert.Empty log.Warnings
        Assert.Empty log.Errors
    })

// ## Scan: connected TODO exit codes

[<Fact>]
let ``Scan returns zero when connected TODOs reference open issues``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#123]: tracked"
            let checker = mockChecker (Map.ofList [ (123, GitHubClient.Open) ])
            let! exitCode = ScanCommand.Scan(ctx, tempDir, configWithTracker tempDir, fun () -> checker)
            assertIntEqual 0 exitCode
        })
        Assert.Empty log.Warnings
        Assert.Empty log.Errors
    })

[<Fact>]
let ``Scan returns exit code 4 for non-existent issues``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#999]: tracked"
            let checker = mockChecker (Map.ofList [ (999, GitHubClient.NotFound) ])
            let! exitCode = ScanCommand.Scan(ctx, tempDir, configWithTracker tempDir, fun () -> checker)
            assertIntEqual 4 exitCode
        })
        assertIntEqual 1 log.Warnings.Count
        Assert.Contains("Non-existent issue", log.Warnings[0])
        Assert.Empty log.Errors
    })

[<Fact>]
let ``Scan returns exit code 3 for closed issues``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#10]: tracked"
            let checker = mockChecker (Map.ofList [ (10, GitHubClient.Closed) ])
            let! exitCode = ScanCommand.Scan(ctx, tempDir, configWithTracker tempDir, fun () -> checker)
            assertIntEqual 3 exitCode
        })
        assertIntEqual 1 log.Warnings.Count
        Assert.Contains("Closed issue", log.Warnings[0])
        Assert.Empty log.Errors
    })

[<Fact>]
let ``Scan returns exit code 2 when tracker cannot be resolved``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#123]: tracked"
            let! exitCode = ScanCommand.Scan(ctx, tempDir, Configuration.Empty tempDir, allOpenChecker)
            assertIntEqual 2 exitCode
        })
        assertIntEqual 1 log.Warnings.Count
        Assert.Contains("Could not determine", log.Warnings[0])
        Assert.Empty log.Errors
    })

[<Fact>]
let ``Exit code 5 takes priority over exit code 4``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "// TODO fix this\n// TODO[#999]: tracked"
            let checker = mockChecker (Map.ofList [ (999, GitHubClient.NotFound) ])
            let! exitCode = ScanCommand.Scan(ctx, tempDir, configWithTracker tempDir, fun () -> checker)
            assertIntEqual 5 exitCode
        })
        assertIntEqual 2 log.Warnings.Count
        Assert.Empty log.Errors
    })

[<Fact>]
let ``Exit code 5 takes priority over exit code 3``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "// TODO fix this\n// TODO[#10]: tracked"
            let checker = mockChecker (Map.ofList [ (10, GitHubClient.Closed) ])
            let! exitCode = ScanCommand.Scan(ctx, tempDir, configWithTracker tempDir, fun () -> checker)
            assertIntEqual 5 exitCode
        })
        assertIntEqual 2 log.Warnings.Count
        Assert.Empty log.Errors
    })

[<Fact>]
let ``Exit code 4 takes priority over exit code 3``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#999]: not found\n// TODO[#10]: closed"
            let checker = mockChecker (Map.ofList [ (999, GitHubClient.NotFound); (10, GitHubClient.Closed) ])
            let! exitCode = ScanCommand.Scan(ctx, tempDir, configWithTracker tempDir, fun () -> checker)
            assertIntEqual 4 exitCode
        })
        assertIntEqual 2 log.Warnings.Count
        Assert.Empty log.Errors
    })

[<Fact>]
let ``Exit code 6 takes priority over exit code 5``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun ctx -> task {
            do! (tempDir / "test.txt").WriteAllTextAsync $"// {Markers.IgnoreToDoStart}\n// TODO fix this"
            let! exitCode = scanSimple(ctx, tempDir)
            assertIntEqual 6 exitCode
        })
        Assert.Empty log.Warnings
    })

// IgnoreTODO-End
