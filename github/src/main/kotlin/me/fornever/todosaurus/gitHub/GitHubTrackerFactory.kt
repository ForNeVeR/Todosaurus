// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.gitHub

import com.intellij.tasks.TaskRepositoryType

class GitHubTrackerFactory : IssueTrackerFactory {

    fun isRecognized(id: String): Boolean =
        repository.name == "GitHub"

    override fun createTracker(tracker: TaskRepositoryType<*>) =
        GitHub(tracker.icon, tracker.name)
}
