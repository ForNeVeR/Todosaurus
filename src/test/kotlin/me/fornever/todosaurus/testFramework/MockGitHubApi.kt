package me.fornever.todosaurus.testFramework

import me.fornever.todosaurus.services.env.GitHubApi
import org.jetbrains.plugins.github.api.GithubApiRequest
import org.jetbrains.plugins.github.api.data.GithubIssue

class MockGitHubApi : GitHubApi {
    override suspend fun createIssue(token: String, request: GithubApiRequest.Post<GithubIssue>): GithubIssue =
        error("Not implemented.")
}
