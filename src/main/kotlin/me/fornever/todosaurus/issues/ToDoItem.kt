// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issues

import com.intellij.openapi.editor.RangeMarker
import com.intellij.util.concurrency.annotations.RequiresReadLock
import com.intellij.util.concurrency.annotations.RequiresWriteLock
import me.fornever.todosaurus.settings.TodosaurusSettings

class ToDoItem(val toDoRange: RangeMarker) {
    private companion object {
        val newItemPattern: Regex
            = Regex("\\b(?i)TODO(?-i)\\b:?(?!\\[.*?])") // https://regex101.com/r/lDDqm7/2

        fun formReportedItemPattern(issueNumber: String): String {
            if (issueNumber.all { it.isDigit() })
                return "TODO[#${issueNumber}]:"

            return "TODO[${issueNumber}]:"
        }
    }

    private val text: String
        get() = toDoRange
            .document
            .getText(toDoRange.textRange)

    val title: String
        get() = text
            .substringBefore('\n')
            .replace(newItemPattern, "")
            .trim()

    val description: String
        get() = (if (text.contains("\n")) text.substringAfter('\n') + "\n" else "") +
            TodosaurusSettings.getInstance().state.descriptionTemplate

    @get:RequiresReadLock
    val issueNumber: String?
        get() {
            if (isNew)
                return null

            return text
                .substringAfter("[")
                .substringBefore("]")
                .replace("#", "")
        }

    @RequiresWriteLock
    fun markAsReported(issueNumber: String) {
        if (!isNew)
            return

        val previousText = text
        val newText = previousText.replace(newItemPattern, formReportedItemPattern(issueNumber))
        toDoRange.document.replaceString(toDoRange.startOffset, toDoRange.endOffset, newText)
    }

    val isNew: Boolean
        get() = newItemPattern.containsMatchIn(text)
}
