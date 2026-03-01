// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module Todosaurus.Tests.GitRemoteTests

open Todosaurus.Cli
open Xunit

[<Theory>]
[<InlineData("https://github.com/ForNeVeR/Todosaurus.git", "ForNeVeR", "Todosaurus")>]
[<InlineData("https://github.com/ForNeVeR/Todosaurus", "ForNeVeR", "Todosaurus")>]
[<InlineData("git@github.com:ForNeVeR/Todosaurus.git", "ForNeVeR", "Todosaurus")>]
[<InlineData("ssh://git@github.com/ForNeVeR/Todosaurus.git", "ForNeVeR", "Todosaurus")>]
let ``ParseGitHubUrl extracts owner and repo``(url: string, expectedOwner: string, expectedRepo: string): unit =
    let result = GitRemote.ParseGitHubUrl url
    match result with
    | Some repo ->
        Assert.Equal(expectedOwner, repo.Owner)
        Assert.Equal(expectedRepo, repo.Repo)
    | None -> failwith $"Expected Some but got None for URL: %s{url}"

[<Fact>]
let ``ParseGitHubUrl returns None for non-GitHub URL``(): unit =
    let result = GitRemote.ParseGitHubUrl "https://gitlab.com/user/project.git"
    Assert.True result.IsNone
