// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module Todosaurus.Tests.RunScanTests

open System.Threading.Tasks
open Todosaurus.Cli
open Todosaurus.Tests.TestFramework
open TruePath.SystemIo
open Xunit

// NOTE: this is needed to make compilation significantly quicker (90 sec -> 4 sec), see
// https://github.com/dotnet/fsharp/issues/18807 for more details.
let private assertIntEqual (x: int) (y: int) =
    Assert.Equal<int>(x, y)

let private allOpenChecker(_ctx: LoggerContext): GitHubClient.IIssueChecker =
    { new GitHubClient.IIssueChecker with
        member _.CheckIssue(_, _, _) = task { return GitHubClient.Open }
    }

// IgnoreTODO-Start

[<Fact>]
let ``RunScan with no config uses default``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "clean.txt").WriteAllTextAsync "no issues"
        let! exitCode = ScanCommand.RunScan(tempDir, None, false, allOpenChecker)
        assertIntEqual 0 exitCode
    })

[<Fact>]
let ``RunScan with missing explicit config returns exit code 7``(): Task =
    WithTempDir(fun tempDir -> task {
        let! exitCode = ScanCommand.RunScan(tempDir, Some <| tempDir / "nonexistent.toml", false, allOpenChecker)
        assertIntEqual 7 exitCode
    })

[<Fact>]
let ``RunScan without strict mode does not override exit codes``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#123]: tracked"
        // No todosaurus.toml, no git remote -> tracker unresolvable -> warning + exit 2
        let! exitCode = ScanCommand.RunScan(tempDir, None, false, allOpenChecker)
        assertIntEqual 2 exitCode
    })

[<Fact>]
let ``RunScan with strict mode doesn't override higher exit code``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#123]: tracked"
        // No todosaurus.toml, no git remote -> tracker unresolvable -> warning + exit 2
        // Exit code 2 is non-zero, so strict mode should not override it to 1
        // (strict only triggers when exitCode = 0)
        let! exitCode = ScanCommand.RunScan(tempDir, None, true, allOpenChecker)
        assertIntEqual 2 exitCode
    })

[<Fact>]
let ``RunScan strict mode triggers on warnings with exit code 0``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "clean file"
        // Create a checker that emits a warning via its LoggerContext
        let warningChecker(ctx: LoggerContext): GitHubClient.IIssueChecker =
            Logger.Warning(ctx, "Test warning for strict mode.")
            { new GitHubClient.IIssueChecker with
                member _.CheckIssue(_, _, _) = task { return GitHubClient.Open }
            }
        do! (tempDir / "todosaurus.toml").WriteAllTextAsync """
[tracker]
url = "https://github.com/owner/repo"
"""
        do! (tempDir / "tracked.txt").WriteAllTextAsync "// TODO[#1]: tracked"
        let! exitCode = ScanCommand.RunScan(tempDir, None, true, warningChecker)
        assertIntEqual 1 exitCode
    })

// IgnoreTODO-End
