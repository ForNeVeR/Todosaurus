// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.gitHub.labels.ui.wizard

import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.issues.ui.wizard.IssueOptions
import me.fornever.todosaurus.core.issues.ui.wizard.IssueOptionsFactory
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardContext
import me.fornever.todosaurus.gitHub.GITHUB_TASK_REPOSITORY_NAME

class LabelsOptionsFactory(private val scope: CoroutineScope) : IssueOptionsFactory {
    override val trackerId: String
        get() = GITHUB_TASK_REPOSITORY_NAME

    override fun createIssueOptions(project: Project, model: TodosaurusWizardContext<ToDoItem.New>): IssueOptions
        = LabelsOptions(scope, project, model)
}
