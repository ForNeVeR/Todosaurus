package me.fornever.todosaurus.actions

import com.intellij.ide.todo.TodoPanel
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class CreateIssueAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val toDoPanel = e.getData(TodoPanel.TODO_PANEL_DATA_KEY)
        e.presentation.isEnabledAndVisible = toDoPanel != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        TODO("Not yet implemented")
    }
}
