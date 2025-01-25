// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issues

import com.intellij.ide.todo.TodoConfiguration
import com.intellij.openapi.components.serviceOrNull
import com.intellij.openapi.editor.RangeMarker
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.util.concurrency.annotations.RequiresReadLock
import me.fornever.todosaurus.settings.TodosaurusSettings

sealed class ToDoItem private constructor(val text: String) {
    companion object {
        private val newItemPattern: Regex
            = Regex("\\b(?i)TODO(?-i)\\b:?(?!\\[.*?])") // https://regex101.com/r/lDDqm7/2

        fun isToDo(psiElement: PsiElement): Boolean {
            if (psiElement !is PsiComment)
                return false

            val commentaryText = psiElement.text

            if (commentaryText.isBlank())
                return false

            val toDoPatterns = TodoConfiguration
                .getInstance()
                .todoPatterns
                .mapNotNull { it.pattern?.toRegex() }

            return toDoPatterns.stream().anyMatch { it.containsMatchIn(commentaryText) }
        }

        fun extractFrom(psiElement: PsiElement): Sequence<ToDoItem> {
            if (psiElement !is PsiComment)
                return emptySequence()

            val psiFile = psiElement.containingFile
                ?: return emptySequence()

            val document = PsiDocumentManager.getInstance(psiElement.project).getDocument(psiFile)
                ?: return emptySequence()

            val commentaryText = psiElement.text

            if (commentaryText.isBlank())
                return emptySequence()

            val toDoPatterns = TodoConfiguration
                .getInstance()
                .todoPatterns
                .mapNotNull { it.pattern?.toRegex() }

            var baseOffset = psiElement.textRange.startOffset

            return sequence {
                commentaryText
                    .lineSequence()
                    .filter { it.isNotBlank() }
                    .forEach { commentaryLine ->
                        toDoPatterns
                            .filter { it.matches(commentaryLine) }
                            .forEach { toDoPattern ->
                                val toDoMatch: MatchResult? = toDoPattern.find(commentaryLine, 0)

                                if (toDoMatch != null) {
                                    val toDoRange = document.createRangeMarker(baseOffset + toDoMatch.range.first, baseOffset + toDoMatch.range.last + 1)
                                    yield(fromRange(toDoRange))
                                }
                            }

                        baseOffset += commentaryLine.length + 1
                    }
            }
        }

        @RequiresReadLock
        fun fromRange(toDoRange: RangeMarker): ToDoItem {
            val text = toDoRange.document.getText(toDoRange.textRange)
            val isNew = newItemPattern.containsMatchIn(text)

            if (isNew)
                return New(toDoRange)

            val issueNumber = text
                .substringAfter("[")
                .substringBefore("]")
                .replace("#", "")

            return Reported(text, issueNumber)
        }
    }

    protected val settings = serviceOrNull<TodosaurusSettings>()?.state
        ?: TodosaurusSettings.State.defaultState // TODO[#133]: Tests broke if we replaced serviceOrNull with TodosaurusSettings.getInstance

    var title: String = text
        .substringBefore('\n')
        .replace(newItemPattern, "")
        .trim()

    var description: String = (if (text.contains("\n")) text.substringAfter('\n') + "\n" else "") +
        settings.descriptionTemplate

    class Reported(text: String, val issueNumber: String) : ToDoItem(text)
    class New(val toDoRange: RangeMarker) : ToDoItem(toDoRange.document.getText(toDoRange.textRange)) {
        @RequiresReadLock
        fun toReported(issueNumber: String): Reported {
            val previousText = text
            val newText = previousText.replace(newItemPattern, formReportedItemPattern(issueNumber))
            toDoRange.document.replaceString(toDoRange.startOffset, toDoRange.endOffset, newText)
            return Reported(newText, issueNumber)
        }

        private fun formReportedItemPattern(issueNumber: String): String
            // TODO[#134]: Allow to customize template for issue number. This is difficult task because the "newItemPattern" is now linked to a regular [.*?] pattern
            = "TODO${settings.numberPattern}".replace(TodosaurusSettings.ISSUE_NUMBER_REPLACEMENT, issueNumber)
    }
}
