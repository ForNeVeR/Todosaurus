// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.gitLab

import com.intellij.tasks.TaskRepositoryType
import me.fornever.todosaurus.core.issueTrackers.IssueTrackerFactory

class GitLabTrackerFactory : IssueTrackerFactory {

    override val trackerId: String
        get() = GITLAB_TASK_REPOSITORY_NAME

    override fun createTracker(tracker: TaskRepositoryType<*>) =
        GitLab(tracker.icon, tracker.name)
}
