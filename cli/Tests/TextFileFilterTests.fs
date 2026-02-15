// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module Todosaurus.Tests.TextFileFilterTests

open System.Text
open System.Threading.Tasks
open TruePath.SystemIo
open Xunit

open Todosaurus.Cli
open Todosaurus.Tests.TestFramework

[<Fact>]
let ``ASCII text file is detected as text``(): Task =
    WithTempDir(fun tempDir -> task {
        let filePath = tempDir / "test.txt"
        do! filePath.WriteAllTextAsync "Hello, World!"
        let! result = TextFileFilter.IsTextFile filePath
        Assert.True result
    })

[<Fact>]
let ``Empty file is detected as text``(): Task =
    WithTempDir(fun tempDir -> task {
        let filePath = tempDir / "empty.txt"
        do! filePath.WriteAllTextAsync ""
        let! result = TextFileFilter.IsTextFile filePath
        Assert.True result
    })

[<Fact>]
let ``File with NUL byte is detected as binary``(): Task =
    WithTempDir(fun tempDir -> task {
        let filePath = tempDir / "binary.bin"
        do! filePath.WriteAllBytesAsync[| 0x48uy; 0x65uy; 0x00uy; 0x6Cuy |]
        let! result = TextFileFilter.IsTextFile filePath
        Assert.False result
    })

[<Fact>]
let ``NUL byte beyond 8000 bytes is detected as text``(): Task =
    WithTempDir(fun tempDir -> task {
        let filePath = tempDir / "large.txt"
        let content = Array.create 8001 0x41uy
        content[8000] <- 0x00uy
        do! filePath.WriteAllBytesAsync content
        let! result = TextFileFilter.IsTextFile filePath
        Assert.True result
    })

[<Fact>]
let ``UTF-8 BOM file is detected as text``(): Task =
    WithTempDir(fun tempDir -> task {
        let filePath = tempDir / "bom.txt"
        let bom = [| 0xEFuy; 0xBBuy; 0xBFuy |]
        let text = Encoding.UTF8.GetBytes("Hello")
        do! filePath.WriteAllBytesAsync(Array.append bom text)
        let! result = TextFileFilter.IsTextFile filePath
        Assert.True result
    })
