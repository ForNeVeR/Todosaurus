package me.fornever.todosaurus.views

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import me.fornever.todosaurus.MyBundle
import java.awt.event.ActionEvent
import javax.swing.Action

data class CreateIssueModel(
    val title: String,
    val description: String
)

class CreateIssueDialog(
    parentScope: CoroutineScope,
    private val repositories: Array<RepositoryModel>,
    private val initialData: CreateIssueModel
) : DialogWrapper(null) {

    private val scope = CoroutineScope(parentScope.coroutineContext)

    init {
        isModal = false
        title = MyBundle.message("createIssueDialog.title")
        init()
    }

    lateinit var repositoryChooser: RepositoryChooser
    lateinit var issueTitleField: JBTextField
    lateinit var issueDescriptionField: JBTextArea

    override fun createCenterPanel() = panel {
        row(MyBundle.message("createIssueDialog.chooseRepository")) {
            repositoryChooser = RepositoryChooser(repositories).also {
                cell(it)
            }
        }
        row(MyBundle.message("createIssueDialog.issueTitle")) {
            issueTitleField = textField().text(initialData.title).component
        }
        row(MyBundle.message("createIssueDialog.issueDescription")) {
            issueDescriptionField = textArea().text(initialData.description).component
        }
    }

    override fun createActions(): Array<Action> = arrayOf(CreateIssueAction())

    override fun dispose() {
        scope.cancel()
        super.dispose()
    }

    private inner class CreateIssueAction : DialogWrapperAction(MyBundle.message("createIssueDialog.createIssue")) {

        override fun isEnabled() =
            repositoryChooser.selectedItem != null

        override fun doAction(e: ActionEvent?) {
            // TODO: Create the issue
            // TODO: Replace the TODO number in the original text
        }
    }
}

