// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module Todosaurus.Tests.ConfigurationTests

open System.Collections.Immutable
open System.Threading.Tasks
open Todosaurus.Cli
open Todosaurus.Tests.TestFramework
open TruePath
open TruePath.SystemIo
open Xunit

// --- ReadConfig tests ---

[<Fact>]
let ``ReadConfig with no config file returns empty config``(): Task =
    WithTempDir(fun tempDir -> task {
        let! result = Configuration.ReadConfig(None, tempDir)
        match result with
        | Ok config ->
            Assert.Equal(None, config.TrackerUrl)
            Assert.True(config.Exclusions.IsEmpty)
        | Error msg -> failwith $"Expected Ok but got Error: %s{msg}"
    })

[<Fact>]
let ``ReadConfig with explicit missing file returns error``(): Task =
    WithTempDir(fun tempDir -> task {
        let missingPath = tempDir / "nonexistent.toml"
        let! result = Configuration.ReadConfig(Some missingPath, tempDir)
        match result with
        | Error msg ->
            Assert.Contains("not found", msg)
        | Ok _ -> failwith "Expected Error but got Ok"
    })

[<Fact>]
let ``ReadConfig with valid config file parses tracker and exclusions``(): Task =
    WithTempDir(fun tempDir -> task {
        let configPath = tempDir / "todosaurus.toml"
        do! configPath.WriteAllTextAsync """
exclusions = ["build/**", "*.generated.cs"]

[tracker]
url = "https://github.com/owner/repo"
"""
        let! result = Configuration.ReadConfig(Some configPath, tempDir)
        match result with
        | Ok config ->
            Assert.Equal(Some "https://github.com/owner/repo", config.TrackerUrl)
            Assert.Equal(2, config.Exclusions.Length)
            Assert.Equal(LocalPathPattern "build/**", config.Exclusions[0])
            Assert.Equal(LocalPathPattern "*.generated.cs", config.Exclusions[1])
        | Error msg -> failwith $"Expected Ok but got Error: %s{msg}"
    })

[<Fact>]
let ``ReadConfig with empty config file returns empty config``(): Task =
    WithTempDir(fun tempDir -> task {
        let configPath = tempDir / "todosaurus.toml"
        do! configPath.WriteAllTextAsync ""
        let! result = Configuration.ReadConfig(Some configPath, tempDir)
        match result with
        | Ok config ->
            Assert.Equal(None, config.TrackerUrl)
            Assert.True(config.Exclusions.IsEmpty)
        | Error msg -> failwith $"Expected Ok but got Error: %s{msg}"
    })

[<Fact>]
let ``ReadConfig with invalid TOML returns error``(): Task =
    WithTempDir(fun tempDir -> task {
        let configPath = tempDir / "todosaurus.toml"
        do! configPath.WriteAllTextAsync "[tracker\ninvalid toml"
        let! result = Configuration.ReadConfig(Some configPath, tempDir)
        match result with
        | Error msg ->
            Assert.Contains("Error reading configuration file", msg)
        | Ok _ -> failwith "Expected Error but got Ok"
    })

[<Fact>]
let ``ReadConfig with invalid tracker URL returns error``(): Task =
    WithTempDir(fun tempDir -> task {
        let configPath = tempDir / "todosaurus.toml"
        do! configPath.WriteAllTextAsync """
[tracker]
url = "https://gitlab.com/owner/repo"
"""
        let! result = Configuration.ReadConfig(Some configPath, tempDir)
        match result with
        | Error msg ->
            Assert.Contains("Invalid tracker URL", msg)
        | Ok _ -> failwith "Expected Error but got Ok"
    })

[<Fact>]
let ``ReadConfig with owner/repo format returns error``(): Task =
    WithTempDir(fun tempDir -> task {
        let configPath = tempDir / "todosaurus.toml"
        do! configPath.WriteAllTextAsync """
[tracker]
url = "owner/repo"
"""
        let! result = Configuration.ReadConfig(Some configPath, tempDir)
        match result with
        | Error msg ->
            Assert.Contains("Invalid tracker URL", msg)
        | Ok _ -> failwith "Expected Error but got Ok"
    })

// --- ApplyExclusions tests ---

[<Fact>]
let ``ApplyExclusions with empty list returns all files``() =
    let config = { Configuration.Empty(AbsolutePath "/tmp") with Exclusions = System.Collections.Immutable.ImmutableArray.Empty }
    let files = [ LocalPath "a.fs"; LocalPath "b.fs" ]
    let result = Configuration.ApplyExclusions(files, config)
    Assert.Equal(files, result)

[<Fact>]
let ``ApplyExclusions filters matching glob patterns``() =
    let config = {
        Configuration.Empty(AbsolutePath "/tmp") with
            Exclusions = ImmutableArray.Create(LocalPathPattern "*.generated.cs")
    }
    let files = [ LocalPath "a.fs"; LocalPath "b.generated.cs"; LocalPath "c.fs" ]
    let result = Configuration.ApplyExclusions(files, config)
    Assert.Equal([ LocalPath "a.fs"; LocalPath "c.fs" ], result)

[<Fact>]
let ``ApplyExclusions with directory glob filters nested files``() =
    let config = {
        Configuration.Empty(AbsolutePath "/tmp") with
            Exclusions = ImmutableArray.Create(LocalPathPattern "build/**")
    }
    let files = [ LocalPath "src/a.fs"; LocalPath "build/output.dll"; LocalPath "build/sub/file.txt" ]
    let result = Configuration.ApplyExclusions(files, config)
    Assert.Equal([ LocalPath "src/a.fs" ], result)

// --- Scan integration with exclusions ---

// IgnoreTODO-Start
[<Fact>]
let ``Scan integration: excluded file with TODO does not affect exit code``(): Task =
    WithTempDir(fun tempDir -> task {
        let! log = RunWithLoggerCollector(fun () -> task {
            let configPath = tempDir / "todosaurus.toml"
            do! configPath.WriteAllTextAsync """
exclusions = ["ignored.txt"]
"""
            do! (tempDir / "clean.txt").WriteAllTextAsync "no issues here"
            do! (tempDir / "ignored.txt").WriteAllTextAsync "// TODO should be ignored"
            let! configResult = Configuration.ReadConfig(Some configPath, tempDir)
            let config =
                match configResult with
                | Ok c -> c
                | Error msg -> failwith $"Config error: %s{msg}"
            let checker = { new GitHubClient.IIssueChecker with
                member _.CheckIssue(_, _, _) = task { return GitHubClient.Open }
            }
            let! exitCode = ScanCommand.Scan(tempDir, config, fun () -> checker)
            Assert.Equal(0, exitCode)
        })
        Assert.Empty log.Warnings
        Assert.Empty log.Errors
    })
// IgnoreTODO-End