package me.fornever.todosaurus.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.RangeMarker
import kotlinx.coroutines.CoroutineScope
import me.fornever.todosaurus.views.CreateIssueDialog
import me.fornever.todosaurus.views.CreateIssueModel

@Service
class ToDoService(private val scope: CoroutineScope) {

    companion object {
        fun getInstance(): ToDoService = service()
    }

    private val newToDoItemPattern = "TODO:".toRegex(RegexOption.IGNORE_CASE)

    fun hasNewToDoItem(range: RangeMarker): Boolean {
        val text = range.document.getText(range.textRange)
        return newToDoItemPattern.containsMatchIn(text)
    }

    fun createIssue(range: RangeMarker) {
        val data = calculateData(range)
        CreateIssueDialog(scope, data).show() // TODO: Pass the text and any required context
    }

    private fun calculateData(range: RangeMarker): CreateIssueModel {
        val text = range.document.getText(range.textRange)
        val title = text.substringBefore('\n')
        val description = text.substringAfter('\n')
        // TODO: Add GitHub text range URL
        return CreateIssueModel(title, description)
    }
}
