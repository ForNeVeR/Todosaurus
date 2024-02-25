package me.fornever.todosaurus.views

import com.intellij.openapi.project.Project
import com.intellij.openapi.rd.util.withUiContext
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.fornever.todosaurus.TodosaurusBundle
import me.fornever.todosaurus.models.CreateIssueModel
import me.fornever.todosaurus.models.RepositoryModel
import me.fornever.todosaurus.services.GitHubService
import org.jetbrains.plugins.github.authentication.accounts.GithubAccount
import java.awt.event.ActionEvent
import javax.swing.Action

class CreateIssueDialog(
    private val project: Project,
    parentScope: CoroutineScope,
    private val accounts: Array<GithubAccount>,
    private val repositories: Array<RepositoryModel>,
    private val initialData: CreateIssueModel
) : DialogWrapper(null) {

    private val scope = CoroutineScope(parentScope.coroutineContext)

    init {
        isModal = false
        title = TodosaurusBundle.message("createIssueDialog.title")
        init()
    }

    lateinit var repositoryChooser: RepositoryChooser
    lateinit var accountChooser: GitHubAccountChooser
    lateinit var issueTitleField: JBTextField
    lateinit var issueDescriptionField: JBTextArea

    override fun createCenterPanel() = panel {
        row(TodosaurusBundle.message("createIssueDialog.chooseRepository")) {
            repositoryChooser = RepositoryChooser(repositories).also {
                cell(it)
            }
        }
        row(TodosaurusBundle.message("createIssueDialog.chooseAccount")) {
            accountChooser = GitHubAccountChooser(accounts).also {
                cell(it)
            }
        }
        row(TodosaurusBundle.message("createIssueDialog.issueTitle")) {
            issueTitleField = textField().text(initialData.title).component
        }
        row(TodosaurusBundle.message("createIssueDialog.issueDescription")) {
            issueDescriptionField = textArea().text(initialData.description).component
        }
    }

    override fun createActions(): Array<Action> = arrayOf(CreateIssueAction())

    override fun dispose() {
        scope.cancel()
        super.dispose()
    }

    private inner class CreateIssueAction : DialogWrapperAction(TodosaurusBundle.message("createIssueDialog.createIssue")) {

        override fun isEnabled() =
            repositoryChooser.selectedItem != null

        override fun doAction(e: ActionEvent?) {
            val model = CreateIssueModel(
                repositoryChooser.selectedItem as RepositoryModel?,
                accountChooser.selectedItem as GithubAccount?,
                issueTitleField.text,
                issueDescriptionField.text,
                initialData.textRangeMarker
            )
            // TODO: Show errors if repository or account are not selected.
            scope.launch {
                val newIssue = GitHubService.getInstance(project).createIssue(model)
                withUiContext {
                    doOKAction()
                }
                Notifications.issueCreated(newIssue)
                // TODO: Replace the TODO number in the original text
            }
            // TODO: Process IO and unknown errors
        }
    }
}

