package me.fornever.todosaurus.views

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import me.fornever.todosaurus.MyBundle
import java.awt.event.ActionEvent
import javax.swing.Action

class CreateIssueDialog(parentScope: CoroutineScope) : DialogWrapper(null) {

    private val scope = CoroutineScope(parentScope.coroutineContext)

    init {
        isModal = false
        title = MyBundle.message("createIssueDialog.title")
        init()
    }

    lateinit var issueTitleField: JBTextField
    lateinit var issueDescriptionField: JBTextArea

    override fun createCenterPanel() = panel {
        row(MyBundle.message("createIssueDialog.chooseRepository")) {

        }
        row(MyBundle.message("createIssueDialog.issueTitle")) {
            issueTitleField = textField().component
        }
        row(MyBundle.message("createIssueDialog.issueDescription")) {
            issueDescriptionField = textArea().component
        }
    }

    override fun createActions(): Array<Action> = arrayOf(CreateIssueAction())

    override fun dispose() {
        scope.cancel()
        super.dispose()
    }

    private inner class CreateIssueAction : DialogWrapperAction(MyBundle.message("createIssueDialog.createIssue")) {
        override fun doAction(e: ActionEvent?) {
            // TODO: Create the issue
        }
    }
}

