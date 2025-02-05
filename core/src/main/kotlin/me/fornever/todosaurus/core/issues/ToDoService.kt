// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.fornever.todosaurus.core.TodosaurusCoreBundle
import me.fornever.todosaurus.core.issueTrackers.IssueTrackerConnectionDetails
import me.fornever.todosaurus.core.issueTrackers.IssueTrackerProvider
import me.fornever.todosaurus.core.issueTrackers.ui.wizard.ChooseIssueTrackerStep
import me.fornever.todosaurus.core.issues.ui.wizard.CreateNewIssueStep
import me.fornever.todosaurus.core.ui.Notifications
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardBuilder
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardContext
import me.fornever.todosaurus.core.ui.wizard.WizardResult
import me.fornever.todosaurus.core.ui.wizard.memoization.UserChoice
import me.fornever.todosaurus.core.ui.wizard.memoization.UserChoiceStore

@Service(Service.Level.PROJECT)
class ToDoService(private val project: Project, private val scope: CoroutineScope) {
    companion object {
        fun getInstance(project: Project): ToDoService = project.service()
    }

    fun createNewIssue(toDoItem: ToDoItem.New)
        = scope.launch(Dispatchers.IO) {
            val savedChoice = UserChoiceStore
                .getInstance(project)
                .getChoiceOrNull()

            if (savedChoice != null) {
                val model = retrieveWizardContextBasedOnUserChoice(toDoItem, savedChoice)

                withContext(Dispatchers.EDT) {
                    TodosaurusWizardBuilder(project, model, scope)
                        .setTitle(TodosaurusCoreBundle.message("action.CreateNewIssue.text"))
                        .addStep(CreateNewIssueStep(project, model))
                        .setFinalAction { createNewIssue(model) }
                        .build()
                        .show()
                }

                return@launch
            }

            val model = TodosaurusWizardContext(toDoItem)

            withContext(Dispatchers.EDT) {
                TodosaurusWizardBuilder(project, model, scope)
                    .setTitle(TodosaurusCoreBundle.message("action.CreateNewIssue.text"))
                    .addStep(ChooseIssueTrackerStep(project, scope, model))
                    .addStep(CreateNewIssueStep(project, model))
                    .setFinalAction { createNewIssue(model) }
                    .build()
                    .show()
            }
        }

    private suspend fun createNewIssue(model: TodosaurusWizardContext<ToDoItem.New>): WizardResult {
        try {
            val issueTracker = model.connectionDetails.issueTracker
                ?: error("Issue tracker must be specified")

            val credentials = model.connectionDetails.credentials
                ?: error("Credentials must be specified")

            val placementDetails = model.placementDetails
                ?: error("Placement details must be specified")

            val newIssue = issueTracker
                .createClient(project, credentials, placementDetails)
                .createIssue(model.toDoItem)

            @Suppress("UnstableApiUsage")
            writeAction {
                executeCommand(project, "Update TODO Item") {
                    model.toDoItem.toReported(newIssue.number)
                }
            }

            Notifications.CreateNewIssue.success(newIssue, project)

            return WizardResult.Success
        }
        catch (exception: Exception) {
            Notifications.CreateNewIssue.creationFailed(exception, project)

            return WizardResult.Failed
        }
    }

    fun openReportedIssueInBrowser(toDoItem: ToDoItem.Reported)
        = scope.launch(Dispatchers.IO) {
            val savedChoice = UserChoiceStore
                .getInstance(project)
                .getChoiceOrNull()

            if (savedChoice != null) {
                openReportedIssueInBrowser(
                    retrieveWizardContextBasedOnUserChoice(toDoItem, savedChoice))

                return@launch
            }

            val model = TodosaurusWizardContext(toDoItem)

            withContext(Dispatchers.EDT) {
                TodosaurusWizardBuilder(project, model, scope)
                    .setTitle(TodosaurusCoreBundle.message("action.OpenReportedIssueInBrowser.text"))
                    .setFinalButtonName(TodosaurusCoreBundle.message("wizard.steps.chooseGitHostingRemote.openReportedIssueInBrowser.primaryButton.name"))
                    .addStep(ChooseIssueTrackerStep(project, scope, model))
                    .setFinalAction { openReportedIssueInBrowser(model) }
                    .build()
                    .show()
            }
        }

    private suspend fun openReportedIssueInBrowser(model: TodosaurusWizardContext<ToDoItem.Reported>): WizardResult {
        try {
            val issueTracker = model.connectionDetails.issueTracker
                ?: error("Issue tracker must be specified")

            val credentials = model.connectionDetails.credentials
                ?: error("Credentials must be specified")

            val placementDetails = model.placementDetails
                ?: error("Placement details must be specified")

            val issue = issueTracker
                .createClient(project, credentials, placementDetails)
                .getIssue(model.toDoItem)
                    ?: error("Issue with number \"${model.toDoItem.issueNumber}\" not found on ${issueTracker.title}")

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

    private suspend fun <T : ToDoItem> retrieveWizardContextBasedOnUserChoice(toDoItem: T, userChoice: UserChoice): TodosaurusWizardContext<T> {
        val issueTrackerId = userChoice.issueTrackerId
            ?: error("Issue tracker id must be specified")

        val credentialsId = userChoice.credentialsId
            ?: error("Credentials identifier must be specified")

        val issueTracker = IssueTrackerProvider.provideByTrackerId(issueTrackerId)
            ?: error("Unable to find the issue tracker $issueTrackerId.")

        val credentials = issueTracker
            .createCredentialsProvider(project)
            .provide(credentialsId)
                ?: error("Unable to find credentials with \"${credentialsId}\" identifier")

        val connectionDetails = IssueTrackerConnectionDetails()
        connectionDetails.issueTracker = issueTracker
        connectionDetails.credentials = credentials

        return TodosaurusWizardContext(toDoItem, connectionDetails, userChoice.placementDetails)
    }
}
