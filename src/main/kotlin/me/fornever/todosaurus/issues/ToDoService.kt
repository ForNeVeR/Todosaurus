// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issues

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.fornever.todosaurus.TodosaurusBundle
import me.fornever.todosaurus.issueTrackers.IssueTrackerConnectionDetails
import me.fornever.todosaurus.issueTrackers.IssueTrackerCredentialsProviderFactory
import me.fornever.todosaurus.issueTrackers.IssueTrackerProvider
import me.fornever.todosaurus.issueTrackers.anonymous.AnonymousCredentials
import me.fornever.todosaurus.issueTrackers.ui.wizard.ChooseIssueTrackerStep
import me.fornever.todosaurus.ui.Notifications
import me.fornever.todosaurus.ui.wizard.CreateNewIssueStep
import me.fornever.todosaurus.ui.wizard.TodosaurusWizardBuilder
import me.fornever.todosaurus.ui.wizard.TodosaurusWizardContext
import me.fornever.todosaurus.ui.wizard.WizardResult
import me.fornever.todosaurus.ui.wizard.memoization.UserChoice
import me.fornever.todosaurus.ui.wizard.memoization.UserChoiceStore

@Service(Service.Level.PROJECT)
class ToDoService(private val project: Project, private val scope: CoroutineScope) {
    companion object {
        fun getInstance(project: Project): ToDoService = project.service()
    }

    suspend fun rememberUserChoice(userChoice: UserChoice) {
        val choiceStore = UserChoiceStore.getInstance(project)

        try {
            if (userChoice.credentialsId == AnonymousCredentials.ID)
                error("Saving an anonymous account is not supported")

            choiceStore.rememberChoice(userChoice)
        }
        catch (exception: Exception) {
            Notifications.CreateNewIssue.memoizationWarning(exception, project)
        }
    }

    fun createNewIssue(toDoItem: ToDoItem)
        = scope.launch(Dispatchers.IO) {
            val savedChoice = UserChoiceStore
                .getInstance(project)
                .getChoice()

            if (savedChoice != null) {
                val model = retrieveWizardContextBasedOnUserChoice(toDoItem, savedChoice)

                withContext(Dispatchers.EDT) {
                    TodosaurusWizardBuilder(project, model, scope)
                        .setTitle(TodosaurusBundle.message("action.CreateNewIssue.text"))
                        .addStep(CreateNewIssueStep(model))
                        .setFinalAction { createNewIssue(model) }
                        .build()
                        .show()
                }

                return@launch
            }

            val model = TodosaurusWizardContext(toDoItem)

            withContext(Dispatchers.EDT) {
                TodosaurusWizardBuilder(project, model, scope)
                    .setTitle(TodosaurusBundle.message("action.CreateNewIssue.text"))
                    .addStep(ChooseIssueTrackerStep(project, scope, model))
                    .addStep(CreateNewIssueStep(model))
                    .setFinalAction { createNewIssue(model) }
                    .build()
                    .show()
            }
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
                .createClient(project, credentials, placementDetails)
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
            Notifications.CreateNewIssue.creationFailed(exception, project)

            return WizardResult.Failed
        }
    }

    fun openReportedIssueInBrowser(toDoItem: ToDoItem)
        = scope.launch(Dispatchers.IO) {
            val savedChoice = UserChoiceStore
                .getInstance(project)
                .getChoice()

            if (savedChoice != null) {
                openReportedIssueInBrowser(
                    retrieveWizardContextBasedOnUserChoice(toDoItem, savedChoice))

                return@launch
            }

            val model = TodosaurusWizardContext(toDoItem)

            withContext(Dispatchers.EDT) {
                TodosaurusWizardBuilder(project, model, scope)
                    .setTitle(TodosaurusBundle.message("action.OpenReportedIssueInBrowser.text"))
                    .setFinalButtonName(TodosaurusBundle.message("wizard.steps.chooseGitHostingRemote.openReportedIssueInBrowser.primaryButton.name"))
                    .addStep(ChooseIssueTrackerStep(project, scope, model))
                    .setFinalAction { openReportedIssueInBrowser(model) }
                    .build()
                    .show()
            }
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
                .createClient(project, credentials, placementDetails)
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

    private suspend fun retrieveWizardContextBasedOnUserChoice(toDoItem: ToDoItem, userChoice: UserChoice): TodosaurusWizardContext {
        val issueTrackerType = userChoice.issueTrackerType
            ?: error("Issue tracker type must be specified")

        val credentialsId = userChoice.credentialsId
            ?: error("Credentials identifier must be specified")

        val issueTracker = IssueTrackerProvider
            .getInstance()
            .provide(issueTrackerType)
                ?: error("Unable to find ${issueTrackerType.name} issue tracker")

        val credentials = IssueTrackerCredentialsProviderFactory
            .getInstance(project)
            .create(issueTracker)
            .provide(credentialsId)
                ?: error("Unable to find credentials with \"${credentialsId}\" identifier")

        val connectionDetails = IssueTrackerConnectionDetails()
        connectionDetails.issueTracker = issueTracker
        connectionDetails.credentials = credentials

        return TodosaurusWizardContext(toDoItem, connectionDetails, userChoice.placementDetails)
    }
}
