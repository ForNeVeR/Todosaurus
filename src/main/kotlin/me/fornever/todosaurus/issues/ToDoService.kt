// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issues

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.fornever.todosaurus.TodosaurusBundle
import me.fornever.todosaurus.issueTrackers.ui.wizard.ChooseIssueTrackerStep
import me.fornever.todosaurus.settings.TodosaurusSettings
import me.fornever.todosaurus.ui.Notifications
import me.fornever.todosaurus.ui.wizard.CreateNewIssueStep
import me.fornever.todosaurus.ui.wizard.TodosaurusWizardContext
import me.fornever.todosaurus.ui.wizard.TodosaurusWizardBuilder
import me.fornever.todosaurus.ui.wizard.WizardResult

@Service(Service.Level.PROJECT)
class ToDoService(private val project: Project, private val scope: CoroutineScope) {
    companion object {
        fun getInstance(project: Project): ToDoService = project.service()
    }

    private val todosaurusSettings: TodosaurusSettings = TodosaurusSettings.getInstance()

    fun createNewIssue(toDoItem: ToDoItem) {
        // TODO[#38]: Remember last selected account
        // if (rememberChoiceStore.isSaved()) ...

        val model = TodosaurusWizardContext(toDoItem)

        return TodosaurusWizardBuilder(project, scope)
            .setTitle(TodosaurusBundle.message("action.CreateNewIssue.text"))
            .addStep(ChooseIssueTrackerStep(project, scope, model))
            .addStep(CreateNewIssueStep(project, model))
            .setFinalAction { createNewIssue(model) }
            .build()
            .show()
    }

    private suspend fun createNewIssue(model: TodosaurusWizardContext): WizardResult {
        try {
            val issueTracker = model.connectionDetails.issueTracker
                ?: error("Issue tracker must be specified")

            val credentials = model.connectionDetails.credentials
                ?: error("Credentials must be specified")

            val placementDetails = model.placementDetails
                ?: error("Placement details must be specified")

            val newIssue = issueTracker
                .createClient(credentials, placementDetails)
                .createIssue(model.toDoItem)

            @Suppress("UnstableApiUsage")
            writeAction {
                executeCommand(project, "Update TODO Item") {
                    model.toDoItem.markAsReported(newIssue.number)
                }
            }

            Notifications.CreateNewIssue.success(newIssue, project)

            return WizardResult.Success
        }
        catch (exception: Exception) {
            Notifications.CreateNewIssue.failed(exception, project)

            return WizardResult.Failed
        }
    }

    fun openReportedIssueInBrowser(toDoItem: ToDoItem) {
        // TODO[#38]: Remember last selected account
        // if (rememberChoiceStore.isSaved()) ...

        val model = TodosaurusWizardContext(toDoItem)

        return TodosaurusWizardBuilder(project, scope)
            .setTitle(TodosaurusBundle.message("action.OpenReportedIssueInBrowser.text"))
            .setFinalButtonName(TodosaurusBundle.message("wizard.steps.chooseGitHostingRemote.openReportedIssueInBrowser.primaryButton.name"))
            .addStep(ChooseIssueTrackerStep(project, scope, model))
            .setFinalAction { openReportedIssueInBrowser(model) }
            .build()
            .show()
    }

    private suspend fun openReportedIssueInBrowser(model: TodosaurusWizardContext): WizardResult {
        try {
            val issueTracker = model.connectionDetails.issueTracker
                ?: error("Issue tracker must be specified")

            val credentials = model.connectionDetails.credentials
                ?: error("Credentials must be specified")

            val placementDetails = model.placementDetails
                ?: error("Placement details must be specified")

            val issueNumber = readAction { model.toDoItem.issueNumber }
                ?: error("Issue number must be specified")

            val issue = issueTracker
                .createClient(credentials, placementDetails)
                .getIssue(model.toDoItem)
                    ?: error("Issue with number \"${issueNumber}\" not found on ${issueTracker.title}")

            withContext(Dispatchers.IO) {
                BrowserUtil.browse(issue.url, project)
            }

            return WizardResult.Success
        }
        catch (exception: Exception) {
            Notifications.OpenReportedIssueInBrowser.failed(exception, project)

            return WizardResult.Failed
        }
    }
}
