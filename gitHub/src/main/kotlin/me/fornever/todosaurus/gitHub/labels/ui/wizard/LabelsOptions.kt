// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.gitHub.labels.ui.wizard

import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import me.fornever.todosaurus.core.git.GitBasedPlacementDetails
import me.fornever.todosaurus.core.git.GitHostingRemote
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.issues.ui.wizard.IssueOptions
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardContext
import me.fornever.todosaurus.gitHub.GitHubClient
import me.fornever.todosaurus.gitHub.TodosaurusGitHubBundle
import me.fornever.todosaurus.gitHub.labels.ui.controls.LabelList
import org.jetbrains.plugins.github.api.data.GithubIssueLabel
import javax.swing.JComponent

class LabelsOptions(
    scope: CoroutineScope,
    private val project: Project,
    private val model: TodosaurusWizardContext<ToDoItem.New>) : IssueOptions {

    private var hostingRemote: GitHostingRemote? = null
    private val labelList: LabelList = LabelList(scope)

    val selectedLabels: List<String>
        get() = labelList.getSelectedTags().map { it.name }

    override fun refresh() {
        val placementDetails = model.placementDetails as? GitBasedPlacementDetails
            ?: return

        if (hostingRemote != placementDetails.remote)
            labelList.fetchTagsUsing(::provideLabels)

        hostingRemote = placementDetails.remote
    }

    override fun createOptionsPanel(): JComponent {
        val panel = panel {
            group(TodosaurusGitHubBundle.message("wizard.steps.createNewIssue.labels.title"), false) {
                row {
                    cell(labelList)
                        .align(Align.FILL)
                }
            }
        }

        refresh()

        return panel
    }

    private suspend fun provideLabels(): Iterable<GithubIssueLabel> {
        val issueTracker = model.connectionDetails.issueTracker
            ?: error("Issue tracker must be specified")

        val credentials = model.connectionDetails.credentials
            ?: error("Credentials must be specified")

        val placementDetails = model.placementDetails
            ?: error("Placement details must be specified")

        val client = issueTracker.createClient(project, credentials, placementDetails)

        if (client !is GitHubClient)
            error("Only ${GitHubClient::class.java} is supported")

        return client.getLabels()
    }
}
