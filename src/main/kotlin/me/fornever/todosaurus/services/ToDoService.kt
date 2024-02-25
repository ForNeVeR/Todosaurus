package me.fornever.todosaurus.services

import com.intellij.ide.todo.SmartTodoItemPointer
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement

@Service
class ToDoService {

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
        // TODO: Create an issue
    }
}
