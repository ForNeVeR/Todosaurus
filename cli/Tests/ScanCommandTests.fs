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

let private unwrapOk(result: Result<'T, string>): 'T =
    match result with
    | Ok v -> v
    | Error e -> failwith $"Unexpected scan error: %s{e}"

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

let private scanSimple(dir: AbsolutePath): Task<int> =
    ScanCommand.Scan(dir, Some "owner/repo", allOpenChecker)

// --- ScanFile: unresolved TODO detection ---

[<Fact>]
let ``Bare TODO in a file is detected``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "line one\n// TODO fix this\nline three"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let scanResult = unwrapOk result
        Assert.Equal(1, scanResult.UnresolvedMatches.Count)
        Assert.Equal(2, scanResult.UnresolvedMatches[0].Line)
        Assert.Contains("TODO", scanResult.UnresolvedMatches[0].Text)
    })

[<Fact>]
let ``TODO with issue number is not detected as unresolved``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#123]: this is tracked"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let scanResult = unwrapOk result
        Assert.Empty scanResult.UnresolvedMatches
    })

[<Fact>]
let ``Case-insensitive TODO variants are detected``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// todo fix\n// Todo fix\n// ToDo fix\n// TODO fix"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let scanResult = unwrapOk result
        Assert.Equal(4, scanResult.UnresolvedMatches.Count)
    })

[<Fact>]
let ``TODO with space before bracket is detected``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO [#124]: this has a space"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let scanResult = unwrapOk result
        Assert.Equal(1, scanResult.UnresolvedMatches.Count)
    })

[<Fact>]
let ``Scan returns non-zero exit code when TODOs found``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO fix this"
        let! exitCode = scanSimple tempDir
        Assert.Equal(1, exitCode)
    })

[<Fact>]
let ``Scan returns zero exit code when no TODOs found``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "clean line\nmore clean"
        let! exitCode = scanSimple tempDir
        Assert.Equal(0, exitCode)
    })

[<Fact>]
let ``TODOs inside ignore region are skipped``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// IgnoreTODO-Start\n// TODO fix this\n// IgnoreTODO-End"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let scanResult = unwrapOk result
        Assert.Empty scanResult.UnresolvedMatches
    })

[<Fact>]
let ``TODOs outside ignore region are still detected``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO first\n// IgnoreTODO-Start\n// TODO ignored\n// IgnoreTODO-End\n// TODO second"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let scanResult = unwrapOk result
        Assert.Equal(2, scanResult.UnresolvedMatches.Count)
        Assert.Equal(1, scanResult.UnresolvedMatches[0].Line)
        Assert.Equal(5, scanResult.UnresolvedMatches[1].Line)
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
        let! exitCode = scanSimple tempDir
        Assert.Equal(2, exitCode)
    })

[<Fact>]
let ``Scan returns zero exit code when all TODOs are inside ignore regions``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "clean line\n// IgnoreTODO-Start\n// TODO fix this\n// IgnoreTODO-End\nclean line"
        let! exitCode = scanSimple tempDir
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
        let scanResult = unwrapOk result
        Assert.Equal(1, scanResult.UnresolvedMatches.Count)
    })

[<Fact>]
let ``Resolved and unresolved TODO on the same line: unresolved is detected``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#123]: tracked TODO fix"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let scanResult = unwrapOk result
        Assert.Equal(1, scanResult.UnresolvedMatches.Count)
    })

[<Fact>]
let ``Multiple resolved TODOs on the same line: no match``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#123]: a TODO[#456]: b"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let scanResult = unwrapOk result
        Assert.Empty scanResult.UnresolvedMatches
    })

[<Fact>]
let ``Scan returns exit code 2 when marker and TODO on same line``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// IgnoreTODO-Start TODO fix"
        let! exitCode = scanSimple tempDir
        Assert.Equal(2, exitCode)
    })

// ## ScanFile: connected TODO detection

[<Fact>]
let ``Connected TODO extracts issue number``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#123]: fix this"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let scanResult = unwrapOk result
        Assert.Equal(1, scanResult.ConnectedMatches.Count)
        Assert.Equal(123, scanResult.ConnectedMatches[0].IssueNumber)
        Assert.Equal(1, scanResult.ConnectedMatches[0].Line)
    })

[<Fact>]
let ``Connected TODO with colon extracts issue number``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO:[#456] fix"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let scanResult = unwrapOk result
        Assert.Equal(1, scanResult.ConnectedMatches.Count)
        Assert.Equal(456, scanResult.ConnectedMatches[0].IssueNumber)
    })

[<Fact>]
let ``Multiple connected TODOs on same line``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#1]: a TODO[#2]: b"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let scanResult = unwrapOk result
        Assert.Equal(2, scanResult.ConnectedMatches.Count)
        Assert.Equal(1, scanResult.ConnectedMatches[0].IssueNumber)
        Assert.Equal(2, scanResult.ConnectedMatches[1].IssueNumber)
    })

[<Fact>]
let ``Bare TODO is not matched as connected``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO fix this"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let scanResult = unwrapOk result
        Assert.Empty scanResult.ConnectedMatches
    })

[<Fact>]
let ``TODO with space before bracket is not matched as connected``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO [#123]: has space"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let scanResult = unwrapOk result
        Assert.Empty scanResult.ConnectedMatches
    })

[<Fact>]
let ``Connected TODO inside IgnoreTODO region is skipped``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// IgnoreTODO-Start\n// TODO[#99]: ignored\n// IgnoreTODO-End"
        let! result = ScanCommand.ScanFile(tempDir, LocalPath "test.txt")
        let scanResult = unwrapOk result
        Assert.Empty scanResult.ConnectedMatches
    })

// ## Scan: connected TODO exit codes

[<Fact>]
let ``Scan returns zero when connected TODOs reference open issues``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#123]: tracked"
        let checker = mockChecker (Map.ofList [ (123, GitHubClient.Open) ])
        let! exitCode = ScanCommand.Scan(tempDir, Some "owner/repo", fun () -> checker)
        Assert.Equal(0, exitCode)
    })

[<Fact>]
let ``Scan returns exit code 3 for non-existent issues``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#999]: tracked"
        let checker = mockChecker (Map.ofList [ (999, GitHubClient.NotFound) ])
        let! exitCode = ScanCommand.Scan(tempDir, Some "owner/repo", fun () -> checker)
        Assert.Equal(3, exitCode)
    })

[<Fact>]
let ``Scan returns exit code 4 for closed issues``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#10]: tracked"
        let checker = mockChecker (Map.ofList [ (10, GitHubClient.Closed) ])
        let! exitCode = ScanCommand.Scan(tempDir, Some "owner/repo", fun () -> checker)
        Assert.Equal(4, exitCode)
    })

[<Fact>]
let ``Scan returns exit code 5 when tracker cannot be resolved``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#123]: tracked"
        let! exitCode = ScanCommand.Scan(tempDir, None, allOpenChecker)
        Assert.Equal(5, exitCode)
    })

[<Fact>]
let ``Exit code 1 takes priority over exit code 3``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO fix this\n// TODO[#999]: tracked"
        let checker = mockChecker (Map.ofList [ (999, GitHubClient.NotFound) ])
        let! exitCode = ScanCommand.Scan(tempDir, Some "owner/repo", fun () -> checker)
        Assert.Equal(1, exitCode)
    })

[<Fact>]
let ``Exit code 1 takes priority over exit code 4``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO fix this\n// TODO[#10]: tracked"
        let checker = mockChecker (Map.ofList [ (10, GitHubClient.Closed) ])
        let! exitCode = ScanCommand.Scan(tempDir, Some "owner/repo", fun () -> checker)
        Assert.Equal(1, exitCode)
    })

[<Fact>]
let ``Exit code 3 takes priority over exit code 4``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// TODO[#999]: not found\n// TODO[#10]: closed"
        let checker = mockChecker (Map.ofList [ (999, GitHubClient.NotFound); (10, GitHubClient.Closed) ])
        let! exitCode = ScanCommand.Scan(tempDir, Some "owner/repo", fun () -> checker)
        Assert.Equal(3, exitCode)
    })

[<Fact>]
let ``Exit code 2 takes priority over exit code 1``(): Task =
    WithTempDir(fun tempDir -> task {
        do! (tempDir / "test.txt").WriteAllTextAsync "// IgnoreTODO-Start\n// TODO fix this"
        let! exitCode = scanSimple tempDir
        Assert.Equal(2, exitCode)
    })
