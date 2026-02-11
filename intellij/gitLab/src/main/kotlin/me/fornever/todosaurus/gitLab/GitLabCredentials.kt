// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.gitLab

import me.fornever.todosaurus.core.issueTrackers.IssueTrackerCredentials
import org.jetbrains.plugins.gitlab.api.GitLabServerPath

class GitLabCredentials(
    override val username: String,
    val apiToken: String?,
    override val serverPath: GitLabServerPath
) : IssueTrackerCredentials()
