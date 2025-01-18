// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.tasks.TaskRepositoryType
import com.intellij.util.containers.toArray
import me.fornever.todosaurus.issueTrackers.gitHub.GitHub

@Service(Service.Level.APP)
class IssueTrackerProvider() {
    companion object {
        fun getInstance(): IssueTrackerProvider = service()
    }

    fun provideAll(): Array<IssueTracker>
        = TaskRepositoryType
            .getRepositoryTypes()
            .mapNotNull { createTracker(it) }
            .toArray(emptyArray())

    fun provide(issueTrackerType: IssueTrackerType): IssueTracker?
        = TaskRepositoryType
            .getRepositoryTypes()
            .firstOrNull { it.name == issueTrackerType.name }
            ?.let { createTracker(it)  }

    private fun createTracker(issueTrackerType: TaskRepositoryType<*>): IssueTracker? {
        val icon = issueTrackerType.icon
        val title = issueTrackerType.name

        return when {
            issueTrackerType.isGitHub() -> GitHub(icon, title)
            else -> null
        }
    }
}
