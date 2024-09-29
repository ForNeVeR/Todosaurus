// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.services

import com.intellij.openapi.editor.RangeMarker
import com.intellij.util.concurrency.annotations.RequiresReadLock
import com.intellij.util.concurrency.annotations.RequiresWriteLock

class ToDoItem(val range: RangeMarker) {
    private companion object {
        val newItemPattern: Regex
            = Regex("\\b(?i)TODO(?-i)\\b:?(?!\\[.*?])") // https://regex101.com/r/lDDqm7/2

        val issueDescriptionTemplate = """
            See the code near this line: ${GitHubService.GITHUB_CODE_URL_REPLACEMENT}

            Also, look for the number of this issue in the project code base.
        """.trimIndent()

        fun formReadyItemPattern(issueNumber: Long): String
            = "TODO[#${issueNumber}]:"
    }

    private val text: String
        get() = range.document
            .getText(range.textRange)

    val title: String
        get() = text
            .substringBefore('\n')
            .replace(newItemPattern, "")
            .trim()

    val description: String
        get() = (if (text.contains("\n")) text.substringAfter('\n') + "\n" else "") +
            issueDescriptionTemplate

    @get:RequiresReadLock
    val issueNumber: Long?
        get() {
            if (isNew)
                return null

            val number = text
                .substringAfter("[")
                .substringBefore("]")
                .replace("#", "")
                .takeIf { it.all { symbol -> symbol.isDigit() } }

            return number?.toLongOrNull()
        }

    @RequiresWriteLock
    fun markAsReported(issueNumber: Long) {
        if (!isNew)
            return

        val previousText = text
        val newText = previousText.replace(newItemPattern, formReadyItemPattern(issueNumber))
        range.document.replaceString(range.startOffset, range.endOffset, newText)
    }

    val isNew: Boolean
        get() = newItemPattern.containsMatchIn(text)
}
