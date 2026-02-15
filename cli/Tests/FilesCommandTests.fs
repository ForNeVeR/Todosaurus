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
        let! files = FilesCommand.ListEligibleFiles tempDir
        Assert.Equal<LocalPath>([| LocalPath "hello.txt" |], files)
    })

[<Fact>]
let ``Non-Git directory with binary file excludes it``(): Task =
    WithTempDir(fun tempDir -> task {
        assertNoGitDir tempDir
        do! (tempDir / "data.bin").WriteAllBytesAsync [| 0x00uy; 0x01uy; 0x02uy |]
        let! files = FilesCommand.ListEligibleFiles tempDir
        Assert.Empty files
    })

[<Fact>]
let ``Non-Git directory with mixed files lists only text``(): Task =
    WithTempDir(fun tempDir -> task {
        assertNoGitDir tempDir
        do! (tempDir / "readme.txt").WriteAllTextAsync "Read me"
        do! (tempDir / "image.bin").WriteAllBytesAsync [| 0x89uy; 0x50uy; 0x00uy |]
        do! (tempDir / "code.fs").WriteAllTextAsync "let x = 1"
        let! files = FilesCommand.ListEligibleFiles tempDir
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

        let! files = FilesCommand.ListEligibleFiles tempDir
        let names = files |> Seq.map _.Value
        Assert.Contains(".gitignore", names)
        Assert.Contains("tracked.txt", names)
        Assert.Contains("new-file.txt", names)
        Assert.DoesNotContain("tracked.bin", names)
        Assert.DoesNotContain("ignored.txt", names)
    })

[<Fact>]
let ``Nested directories produce relative paths``(): Task =
    WithTempDir(fun tempDir -> task {
        assertNoGitDir tempDir
        let subDir = tempDir / "sub"
        subDir.CreateDirectory()
        do! (subDir / "nested.txt").WriteAllTextAsync "nested"
        do! (tempDir / "root.txt").WriteAllTextAsync "root"
        let! files = FilesCommand.ListEligibleFiles tempDir
        Assert.Equal(2, files.Count)
        Assert.Contains(LocalPath "root.txt", files)
        // Path separator may vary by OS
        Assert.Contains((subDir / "nested.txt").RelativeTo tempDir, files)
    })
