package me.fornever.todosaurus.actions

import com.intellij.ide.todo.TodoPanel
import com.intellij.ide.todo.nodes.TodoItemNode
import com.intellij.openapi.actionSystem.*
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.tree.TreeUtil
import me.fornever.todosaurus.services.ToDoService

class CreateIssueAction : AnAction() {

    private val service: ToDoService
        get() = ToDoService.getInstance()

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val toDoPanel = e.getData(TodoPanel.TODO_PANEL_DATA_KEY)
        if (toDoPanel == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        e.presentation.isVisible = true

        val tree = e.getData(PlatformDataKeys.CONTEXT_COMPONENT) as? Tree
        val descriptor = tree?.selectionPath?.let(TreeUtil::getLastUserObject) as? TodoItemNode
        val pointer = descriptor?.value

        e.presentation.isEnabled = pointer != null && service.hasNewToDoItem(pointer)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val psiElement = e.getData(CommonDataKeys.PSI_ELEMENT) ?: return
        service.createIssue(psiElement)
    }
}
