// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

module internal Todosaurus.Cli.GitHubClient

open System
open System.Threading.Tasks
open Octokit

type IssueStatus =
    | Open
    | Closed
    | NotFound

[<Interface>]
type IIssueChecker =
    abstract CheckIssue: owner: string * repo: string * issueNumber: int -> Task<IssueStatus>

let CreateClient(ctx: LoggerContext): IIssueChecker =
    let client = GitHubClient(ProductHeaderValue("Todosaurus-CLI"))

    let token =
        let envOpt(x: string | null) =
            match x with
            | null | "" -> None
            | x -> Some x

        Environment.GetEnvironmentVariable("GITHUB_TOKEN")
        |> envOpt
        |> Option.orElseWith(fun() ->
            Environment.GetEnvironmentVariable("GH_TOKEN")
            |> envOpt
        )

    match token with
    | Some t ->
        client.Credentials <- Credentials(t)
    | None ->
        Logger.Warning(ctx, "No GitHub token found (checked GITHUB_TOKEN and GH_TOKEN). API rate limits will be restrictive.")

    { new IIssueChecker with
        member _.CheckIssue(owner, repo, issueNumber) =
            task {
                try
                    let! issue = client.Issue.Get(owner, repo, issueNumber)
                    if issue.State.Value = ItemState.Open then
                        return Open
                    else
                        return Closed
                with
                | :? NotFoundException -> return NotFound
            }
    }

let CheckIssues(
    checker: IIssueChecker,
    owner: string,
    repo: string,
    issueNumbers: int seq
): Task<Map<int, IssueStatus>> =
    task {
        let! data =
            issueNumbers
            |> Seq.distinct
            |> Seq.map(fun issueNumber -> task {
                let! status = checker.CheckIssue(owner, repo, issueNumber)
                return issueNumber, status
            })
            |> Task.WhenAll
        return Map.ofSeq data
    }
