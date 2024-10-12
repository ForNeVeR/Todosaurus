package me.fornever.todosaurus.issues

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.fornever.todosaurus.issueTrackers.ui.wizard.ChooseIssueTrackerStep
import me.fornever.todosaurus.ui.Notifications
import me.fornever.todosaurus.ui.wizard.CreateNewIssueStep
import me.fornever.todosaurus.ui.wizard.TodosaurusContext
import me.fornever.todosaurus.ui.wizard.TodosaurusWizardBuilder
import me.fornever.todosaurus.ui.wizard.WizardResult
import me.fornever.todosaurus.vcs.git.ui.wizard.ChooseGitRemoteStep

@Service(Service.Level.PROJECT)
class ToDoService(private val project: Project) {
    companion object {
        fun getInstance(project: Project): ToDoService = project.service()
    }

    fun createNewIssue(toDoItem: ToDoItem) {
        val model = TodosaurusContext(toDoItem)

        TodosaurusWizardBuilder(project)
            .setTitle("Create New Issue")
            .addStep(ChooseIssueTrackerStep(project, model))
            .addStep(CreateNewIssueStep(project, model))
            .setFinalAction {
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

                    return@setFinalAction WizardResult.Success
                }
                catch (exception: Exception) {
                    Notifications.CreateNewIssue.failed(project)

                    return@setFinalAction WizardResult.Failed
                }
            }
            .build()
            .show()
    }

    fun openReportedIssueInBrowser(toDoItem: ToDoItem) {
        val model = TodosaurusContext(toDoItem)

        TodosaurusWizardBuilder(project)
            .setTitle("Open Issue In Browser")
            .setFinalButtonName("Open")
            .addStep(ChooseIssueTrackerStep(project, model))
            .setFinalAction {
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

                    return@setFinalAction WizardResult.Success
                }
                catch (exception: Exception) {
                    Notifications.OpenReportedIssueInBrowser.failed(exception, project)

                    return@setFinalAction WizardResult.Failed
                }
            }
            .build()
            .show()
    }
}
