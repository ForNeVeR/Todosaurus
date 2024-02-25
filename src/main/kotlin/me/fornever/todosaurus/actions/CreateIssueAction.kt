package me.fornever.todosaurus.actions

import com.intellij.ide.todo.TodoPanel
import com.intellij.ide.todo.nodes.TodoItemNode
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.RangeMarker
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

        val range = e.getToDoTextRange()
        e.presentation.isEnabled = range != null && service.hasNewToDoItem(range)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val range = e.getToDoTextRange() ?: return
        service.createIssue(range)
    }
}

private fun AnActionEvent.getToDoTextRange(): RangeMarker? {
    val tree = getData(PlatformDataKeys.CONTEXT_COMPONENT) as? Tree
    val descriptor = tree?.selectionPath?.let(TreeUtil::getLastUserObject) as? TodoItemNode
    return descriptor?.value?.rangeMarker
}
