// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues.ui.wizard

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.tasks.TaskRepositoryType
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardContext

interface IssueOptionsFactory {
    companion object {
        val EP_NAME = ExtensionPointName<IssueOptionsFactory>("me.fornever.todosaurus.issueOptionsFactory")
    }

    /**
     * This is supposed to be equal to a [TaskRepositoryType.name] of one of the configured [TaskRepositoryType].
     */
    val trackerId: String

    fun createIssueOptions(project: Project, model: TodosaurusWizardContext<ToDoItem.New>): IssueOptions
}
