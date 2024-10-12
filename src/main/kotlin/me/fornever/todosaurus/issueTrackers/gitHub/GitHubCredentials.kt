package me.fornever.todosaurus.issueTrackers.gitHub

import me.fornever.todosaurus.issueTrackers.IssueTrackerCredentials
import org.jetbrains.plugins.github.api.GithubServerPath

class GitHubCredentials(override val username: String, val apiToken: String?, override val serverPath: GithubServerPath) : IssueTrackerCredentials