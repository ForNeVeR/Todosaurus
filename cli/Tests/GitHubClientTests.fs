// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module Todosaurus.Tests.GitHubClientTests

open System.Threading.Tasks
open Todosaurus.Cli
open Xunit

let private mockChecker(responses: Map<int, GitHubClient.IssueStatus>): GitHubClient.IIssueChecker =
    let mutable callCount = 0
    { new GitHubClient.IIssueChecker with
        member _.CheckIssue(_owner, _repo, issueNumber) =
            task {
                callCount <- callCount + 1
                return
                    match responses |> Map.tryFind issueNumber with
                    | Some status -> status
                    | None -> GitHubClient.NotFound
            }
    }

[<Fact>]
let ``CheckIssues returns statuses for each unique issue``(): Task =
    task {
        let responses = Map.ofList [
            (1, GitHubClient.Open)
            (2, GitHubClient.Closed)
            (3, GitHubClient.NotFound)
        ]
        let checker = mockChecker responses
        let! statuses = GitHubClient.CheckIssues(checker, "owner", "repo", [ 1; 2; 3 ])
        Assert.Equal(3, statuses.Count)
        Assert.Equal(GitHubClient.Open, statuses[1])
        Assert.Equal(GitHubClient.Closed, statuses[2])
        Assert.Equal(GitHubClient.NotFound, statuses[3])
    }

[<Fact>]
let ``CheckIssues deduplicates issue numbers``(): Task =
    task {
        let mutable callCount = 0
        let checker =
            { new GitHubClient.IIssueChecker with
                member _.CheckIssue(_owner, _repo, _issueNumber) =
                    task {
                        callCount <- callCount + 1
                        return GitHubClient.Open
                    }
            }
        let! statuses = GitHubClient.CheckIssues(checker, "owner", "repo", [ 1; 1; 1; 2; 2 ])
        Assert.Equal(2, statuses.Count)
        Assert.Equal(2, callCount)
    }

[<Fact>]
let ``CheckIssues returns empty map for empty input``(): Task =
    task {
        let checker = mockChecker Map.empty
        let! statuses = GitHubClient.CheckIssues(checker, "owner", "repo", [])
        Assert.Empty(statuses)
    }
