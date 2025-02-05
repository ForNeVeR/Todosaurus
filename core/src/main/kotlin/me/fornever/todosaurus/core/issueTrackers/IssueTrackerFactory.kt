// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT
package me.fornever.todosaurus.core.issueTrackers

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.tasks.TaskRepositoryType

interface IssueTrackerFactory {
    companion object {
        val EP_NAME = ExtensionPointName<IssueTrackerFactory>("me.fornever.todosaurus.issueTrackerFactory")
    }

    /**
     * This is supposed to be equal to a [TaskRepositoryType.name] of one of the configured [TaskRepositoryType].
     */
    val trackerId: String

    fun createTracker(tracker: TaskRepositoryType<*>): IssueTracker?
}
