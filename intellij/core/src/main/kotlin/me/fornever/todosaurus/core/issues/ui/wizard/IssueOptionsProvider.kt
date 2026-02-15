// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues.ui.wizard

import com.intellij.openapi.project.Project
import com.intellij.tasks.TaskRepositoryType
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardContext

object IssueOptionsProvider {
    private val cachedFactories
        = IssueOptionsFactory
            .EP_NAME
            .extensionList
            .groupBy { it.trackerId }

    fun provideAll(project: Project, model: TodosaurusWizardContext<ToDoItem.New>): List<IssueOptions> {
        val repositoryType = TaskRepositoryType
            .getRepositoryTypes()
            .firstOrNull { it.name == model.connectionDetails.issueTracker?.title }
                ?: return emptyList()

        return cachedFactories[repositoryType.name]
            ?.map { it.createIssueOptions(project, model) }
                ?: emptyList()
    }
}
