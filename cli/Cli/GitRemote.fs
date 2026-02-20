// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module internal Todosaurus.Cli.GitRemote

open System.Text.RegularExpressions
open System.Threading.Tasks
open TruePath

type GitHubRepo = {
    Owner: string
    Repo: string
}

/// <summary>
/// Parse owner/repo from various GitHub URL formats:
/// <list type="bullet">
///   <item><c>https://github.com/owner/repo.git</c>,</item>
///   <item><c>https://github.com/owner/repo</c>,</item>
///   <item><c>git@github.com:owner/repo.git</c>,</item>
///   <item><c>ssh://git@github.com/owner/repo.git</c>.</item>
/// </list>
/// </summary>
let ParseGitHubUrl(url: string): GitHubRepo option =
    let pattern = @"github\.com[:/]([^/]+)/([^/]+?)(?:\.git)?$"
    let m = Regex.Match(url, pattern)
    if m.Success then
        Some { Owner = m.Groups[1].Value; Repo = m.Groups[2].Value }
    else
        None

/// <summary>
/// Parse an explicit tracker argument. Accepts either:
/// <list type="bullet">
///   <item>"owner/repo" format,</item>
///   <item>a full GitHub URL.</item>
/// </list>
/// </summary>
let ParseTrackerArgument(tracker: string): GitHubRepo option =
    let slashPattern = @"^([A-Za-z0-9._-]+)/([A-Za-z0-9._-]+)$"
    let m = Regex.Match(tracker, slashPattern)
    if m.Success then
        Some { Owner = m.Groups[1].Value; Repo = m.Groups[2].Value }
    else
        ParseGitHubUrl tracker

/// <summary>
/// Run <c>git remote get-url origin</c> and parse the result.
/// </summary>
let DiscoverFromOrigin(workingDirectory: AbsolutePath): Task<GitHubRepo option> =
    task {
        try
            let! result = Shell.RunProcess(
                workingDirectory,
                LocalPath "git",
                [ "remote"; "get-url"; "origin" ]
            )
            let url = result.StandardOutput.Trim()
            return ParseGitHubUrl url
        with
        | _ -> return None
    }
