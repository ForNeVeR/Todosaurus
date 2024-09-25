package me.fornever.todosaurus.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import kotlinx.coroutines.*
import me.fornever.todosaurus.TodosaurusBundle
import me.fornever.todosaurus.actions.extensions.getToDoTextRange
import me.fornever.todosaurus.actions.extensions.tryActivateThisAction
import me.fornever.todosaurus.services.ToDoItem
import me.fornever.todosaurus.services.ToDoService

class OpenIssueInBrowserAction : AnAction(
    TodosaurusBundle.message("notification.openIssueInBrowser.action.name")
) {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        if (!e.tryActivateThisAction())
            return

        val range = e.getToDoTextRange()
        e.presentation.isEnabled = range != null && ToDoItem(range).issueNumber != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val range = e.getToDoTextRange() ?: return
        val toDoService = ToDoService.getInstance(project)

        CoroutineScope(Dispatchers.IO).launch {
            toDoService.openIssueInBrowser(range)
        }
    }
}
