// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.gitHub

import com.intellij.tasks.TaskRepositoryType
import me.fornever.todosaurus.core.issueTrackers.IssueTrackerFactory

class GitHubTrackerFactory : IssueTrackerFactory {

    override val trackerId: String
        get() = GITHUB_TASK_REPOSITORY_NAME

    override fun createTracker(repositoryType: TaskRepositoryType<*>) =
        GitHub(repositoryType.icon, repositoryType.name)
}
