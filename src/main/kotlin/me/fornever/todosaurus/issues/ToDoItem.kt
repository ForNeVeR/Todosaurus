// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issues

import com.intellij.openapi.components.serviceOrNull
import com.intellij.openapi.editor.RangeMarker
import com.intellij.util.concurrency.annotations.RequiresReadLock
import com.intellij.util.concurrency.annotations.RequiresWriteLock
import me.fornever.todosaurus.settings.TodosaurusSettings

class ToDoItem(val toDoRange: RangeMarker) {
    private companion object {
        val newItemPattern: Regex
            = Regex("\\b(?i)TODO(?-i)\\b:?(?!\\[.*?])") // https://regex101.com/r/lDDqm7/2
    }

    private val settings = serviceOrNull<TodosaurusSettings>()?.state
        ?: TodosaurusSettings.State.defaultState // TODO: Tests broke if we replaced serviceOrNull with TodosaurusSettings.getInstance

    private val text: String
        get() = toDoRange
            .document
            .getText(toDoRange.textRange)

    var title: String = text
        .substringBefore('\n')
        .replace(newItemPattern, "")
        .trim()

    var description: String = (if (text.contains("\n")) text.substringAfter('\n') + "\n" else "") +
        settings.descriptionTemplate

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

    private fun formReportedItemPattern(issueNumber: String): String {
        // TODO: Allow to customize template for issue number. This is difficult task because the "newItemPattern" is now linked to a regular [.*?] pattern
        return "TODO${settings.numberPattern}".replace(TodosaurusSettings.ISSUE_NUMBER_REPLACEMENT, issueNumber)
    }
}
