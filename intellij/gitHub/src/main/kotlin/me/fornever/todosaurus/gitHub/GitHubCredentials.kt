// SPDX-FileCopyrightText: 2024-2026 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.gitHub

import me.fornever.todosaurus.core.issueTrackers.IssueTrackerCredentials
import org.jetbrains.plugins.github.api.GithubServerPath
import org.jetbrains.plugins.github.authentication.accounts.GithubAccount

class GitHubCredentials(
    override val username: String,
    val apiToken: String?,
    override val serverPath: GithubServerPath,
    val account: GithubAccount? = null
) : IssueTrackerCredentials()
