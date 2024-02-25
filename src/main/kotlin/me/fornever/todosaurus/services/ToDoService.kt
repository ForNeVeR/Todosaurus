package me.fornever.todosaurus.services

import com.intellij.ide.todo.SmartTodoItemPointer
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import kotlinx.coroutines.CoroutineScope
import me.fornever.todosaurus.views.CreateIssueDialog

@Service
class ToDoService(private val scope: CoroutineScope) {

    companion object {
        fun getInstance(): ToDoService = service()
    }

    private val newToDoItemPattern = "TODO:".toRegex(RegexOption.IGNORE_CASE)

    fun hasNewToDoItem(element: SmartTodoItemPointer): Boolean {
        val range = element.rangeMarker
        val text = range.document.getText(range.textRange)
        return newToDoItemPattern.containsMatchIn(text)
    }

    fun createIssue(psiElement: PsiElement) {
        CreateIssueDialog(scope).show() // TODO: Pass the text and any required context
    }
}
