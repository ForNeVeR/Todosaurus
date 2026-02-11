// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issueTrackers

import com.intellij.tasks.TaskRepositoryType

object IssueTrackerProvider {

    fun provideAll(): Sequence<IssueTracker>
        = provideUsing(TaskRepositoryType.getRepositoryTypes())

    fun provideByTrackerId(trackerId: String): IssueTracker? {
        val repositoryTypes = TaskRepositoryType.getRepositoryTypes()

        return repositoryTypes
            .firstOrNull { it.name == trackerId }
            ?.let { createTracker(it) } // For case when TaskRepositoryType.name is equal to repositoryId
                ?: provideUsing(repositoryTypes)
                    .firstOrNull { it.id == trackerId }
    }

    private fun provideUsing(repositoryTypes: List<TaskRepositoryType<*>>): Sequence<IssueTracker>
        = repositoryTypes
            .asSequence()
            .mapNotNull { createTracker(it) }

    private fun createTracker(repositoryType: TaskRepositoryType<*>): IssueTracker? =
        IssueTrackerFactory.EP_NAME
            .extensionList.firstOrNull { it.trackerId == repositoryType.name }
            ?.createTracker(repositoryType)
}
