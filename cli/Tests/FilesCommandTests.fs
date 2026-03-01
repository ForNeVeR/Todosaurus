// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module Todosaurus.Tests.FilesCommandTests

open System.Threading.Tasks
open Todosaurus.Cli
open Todosaurus.Tests.TestFramework
open TruePath
open TruePath.SystemIo
open Xunit

let private runGit (workingDirectory: AbsolutePath) (args: string seq): Task =
    task {
        let! _ = Shell.RunProcess(workingDirectory, LocalPath "git", args)
        return ()
    }

let private assertNoGitDir(dir: AbsolutePath) =
    let gitPath = (dir / ".git")

    if gitPath.Exists() then
        failwith $"Unexpected .git found in temp directory %s{dir.Value}. Manual cleanup required."

[<Fact>]
let ``Non-Git directory with text file lists it``(): Task =
    WithTempDir(fun tempDir -> task {
        assertNoGitDir tempDir
        do! (tempDir / "hello.txt").WriteAllTextAsync "Hello"
        let ctx = LoggerContext.Create()
        let! files = FilesCommand.ListEligibleFiles(ctx, tempDir)
        Assert.Equal<LocalPath>([| LocalPath "hello.txt" |], files)
    })

[<Fact>]
let ``Non-Git directory with binary file excludes it``(): Task =
    WithTempDir(fun tempDir -> task {
        assertNoGitDir tempDir
        do! (tempDir / "data.bin").WriteAllBytesAsync [| 0x00uy; 0x01uy; 0x02uy |]
        let ctx = LoggerContext.Create()
        let! files = FilesCommand.ListEligibleFiles(ctx, tempDir)
        Assert.Empty files
    })

[<Fact>]
let ``Non-Git directory with mixed files lists only text``(): Task =
    WithTempDir(fun tempDir -> task {
        assertNoGitDir tempDir
        do! (tempDir / "readme.txt").WriteAllTextAsync "Read me"
        do! (tempDir / "image.bin").WriteAllBytesAsync [| 0x89uy; 0x50uy; 0x00uy |]
        do! (tempDir / "code.fs").WriteAllTextAsync "let x = 1"
        let ctx = LoggerContext.Create()
        let! files = FilesCommand.ListEligibleFiles(ctx, tempDir)
        let names = files |> Seq.map _.Value
        Assert.Equal<string>([ "code.fs"; "readme.txt" ], names)
    })

[<Fact>]
let ``Git repo lists tracked and untracked-not-ignored text files, excludes binary``(): Task =
    WithTempDir(fun tempDir -> task {
        assertNoGitDir tempDir
        do! runGit tempDir [ "init" ]
        do! runGit tempDir [ "config"; "user.email"; "test@test.com" ]
        do! runGit tempDir [ "config"; "user.name"; "Test" ]

        // Tracked files (should appear):
        do! (tempDir / "tracked.txt").WriteAllTextAsync "tracked"
        do! (tempDir / "tracked.bin").WriteAllBytesAsync [| 0x00uy; 0x01uy |]
        do! runGit tempDir [ "add"; "tracked.txt"; "tracked.bin" ]
        do! runGit tempDir [ "commit"; "-m"; "initial" ]

        // Untracked-not-ignored text file (should appear):
        do! (tempDir / "new-file.txt").WriteAllTextAsync "new"

        // Ignored file (should not appear):
        do! (tempDir / ".gitignore").WriteAllTextAsync "ignored.txt\n"
        do! (tempDir / "ignored.txt").WriteAllTextAsync "ignored"
        do! runGit tempDir [ "add"; ".gitignore" ]
        do! runGit tempDir [ "commit"; "-m"; "add gitignore" ]

        let ctx = LoggerContext.Create()
        Env.SetIsCiOverride(Some false)
        try
            let! files = FilesCommand.ListEligibleFiles(ctx, tempDir)
            let names = files |> Seq.map _.Value
            Assert.Contains(".gitignore", names)
            Assert.Contains("tracked.txt", names)
            Assert.Contains("new-file.txt", names)
            Assert.DoesNotContain("tracked.bin", names)
            Assert.DoesNotContain("ignored.txt", names)
        finally
            Env.SetIsCiOverride(None)
    })

[<Fact>]
let ``CI mode excludes untracked files from Git repo scan``(): Task =
    WithTempDir(fun tempDir -> task {
        assertNoGitDir tempDir
        do! runGit tempDir [ "init" ]
        do! runGit tempDir [ "config"; "user.email"; "test@test.com" ]
        do! runGit tempDir [ "config"; "user.name"; "Test" ]

        // Tracked file:
        do! (tempDir / "tracked.txt").WriteAllTextAsync "tracked"
        do! runGit tempDir [ "add"; "tracked.txt" ]
        do! runGit tempDir [ "commit"; "-m"; "initial" ]

        // Untracked-not-ignored file (should be excluded in CI mode):
        do! (tempDir / "untracked.txt").WriteAllTextAsync "untracked"

        let ctx = LoggerContext.Create()
        Env.SetIsCiOverride(Some true)
        try
            let! files = FilesCommand.ListEligibleFiles(ctx, tempDir)
            let names = files |> Seq.map _.Value
            Assert.Contains("tracked.txt", names)
            Assert.DoesNotContain("untracked.txt", names)
        finally
            Env.SetIsCiOverride None
    })

[<Fact>]
let ``Nested directories produce relative paths``(): Task =
    WithTempDir(fun tempDir -> task {
        assertNoGitDir tempDir
        let subDir = tempDir / "sub"
        subDir.CreateDirectory()
        do! (subDir / "nested.txt").WriteAllTextAsync "nested"
        do! (tempDir / "root.txt").WriteAllTextAsync "root"
        let ctx = LoggerContext.Create()
        let! files = FilesCommand.ListEligibleFiles(ctx, tempDir)
        Assert.Equal(2, files.Count)
        Assert.Contains(LocalPath "root.txt", files)
        // Path separator may vary by OS
        Assert.Contains((subDir / "nested.txt").RelativeTo tempDir, files)
    })
