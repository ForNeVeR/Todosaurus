// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issueTrackers

import com.intellij.tasks.TaskRepositoryType
import com.intellij.util.containers.toArray

object IssueTrackerProvider {

    fun provideAll(): Array<IssueTracker>
        = TaskRepositoryType
            .getRepositoryTypes()
            .mapNotNull { createTracker(it) }
            .toArray(emptyArray())

    fun provideByRepositoryName(repositoryName: String): IssueTracker? =
        TaskRepositoryType
            .getRepositoryTypes()
            .firstOrNull { it.name == repositoryName }
            ?.let { createTracker(it)  }

    private fun createTracker(repository: TaskRepositoryType<*>): IssueTracker? {
        val trackerId = repository.name
        for (factory in IssueTrackerFactory.EP_NAME.extensionList) {
            if (factory.trackerId == trackerId) {
                return factory.createTracker(repository)
            }
        }

        return null
    }
}
