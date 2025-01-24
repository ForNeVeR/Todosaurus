// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issueTrackers.ui.wizard

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import me.fornever.todosaurus.issueTrackers.gitHub.GitHub
import me.fornever.todosaurus.ui.wizard.TodosaurusWizardContext
import me.fornever.todosaurus.ui.wizard.TodosaurusWizardStep
import me.fornever.todosaurus.vcs.git.ui.wizard.ChooseGitHostingRemoteStep

@Service(Service.Level.PROJECT)
class SpecificIssueTrackerStepFactory(private val project: Project) {
    companion object {
        fun getInstance(project: Project): SpecificIssueTrackerStepFactory = project.service()
    }

    fun create(model: TodosaurusWizardContext): TodosaurusWizardStep?
        = when (model.connectionDetails.issueTracker) {
            is GitHub -> ChooseGitHostingRemoteStep(project, model)
            else -> null
        }
}
