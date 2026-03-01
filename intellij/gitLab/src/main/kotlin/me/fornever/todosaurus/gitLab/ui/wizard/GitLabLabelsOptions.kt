// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.gitLab.ui.wizard

import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import me.fornever.todosaurus.core.issues.labels.IssueLabel
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.issues.labels.LabelsOptions
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardContext
import me.fornever.todosaurus.gitLab.GitLabClient

class GitLabLabelsOptions(
    scope: CoroutineScope,
    private val project: Project,
    private val model: TodosaurusWizardContext<ToDoItem.New>) : LabelsOptions(scope, project, model) {

    override suspend fun provideLabels(): Iterable<IssueLabel> {
        val issueTracker = model.connectionDetails.issueTracker
            ?: error("Issue tracker must be specified")

        val credentials = model.connectionDetails.credentials
            ?: error("Credentials must be specified")

        val placementDetails = model.placementDetails
            ?: error("Placement details must be specified")

        val client = issueTracker.createClient(project, credentials, placementDetails)

        if (client !is GitLabClient)
            error("Only ${GitLabClient::class.java} is supported")

        return client
            .getLabels()
            .map { IssueLabel(it.id, it.name, it.description, it.color) }
    }
}
