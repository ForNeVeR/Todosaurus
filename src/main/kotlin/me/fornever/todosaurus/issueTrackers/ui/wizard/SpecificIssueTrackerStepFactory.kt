// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers.ui.wizard

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import me.fornever.todosaurus.issueTrackers.gitHub.GitHub
import me.fornever.todosaurus.ui.wizard.TodosaurusContext
import me.fornever.todosaurus.ui.wizard.TodosaurusStep
import me.fornever.todosaurus.vcs.git.ui.wizard.ChooseGitHostingRemoteStep

@Service(Service.Level.PROJECT)
class SpecificIssueTrackerStepFactory(private val project: Project) {
    companion object {
        fun getInstance(project: Project): SpecificIssueTrackerStepFactory = project.service()
    }

    fun create(model: TodosaurusContext): TodosaurusStep?
        = when (model.connectionDetails.issueTracker) {
            is GitHub -> ChooseGitHostingRemoteStep(project, model)
            else -> null
        }
}
