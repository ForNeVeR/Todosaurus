// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues

import com.intellij.ide.todo.TodoConfiguration
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.RangeMarker
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.util.concurrency.annotations.RequiresWriteLock
import me.fornever.todosaurus.core.settings.TodosaurusSettings

sealed class ToDoItem private constructor(val text: String, protected val todosaurusSettings: TodosaurusSettings.State) {
    companion object {
        private val newItemPattern: Regex
            = Regex("\\b(?i)TODO(?-i)\\b:?(?!\\[.*?])") // https://regex101.com/r/lDDqm7/2

        fun containsToDo(psiElement: PsiElement): Boolean {
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

        fun extractFrom(psiElement: PsiElement, todosaurusSettings: TodosaurusSettings.State): Array<ToDoItem> {
            if (psiElement !is PsiComment)
                return emptyArray()

            val psiFile = psiElement.containingFile
                ?: return emptyArray()

            val document = PsiDocumentManager.getInstance(psiElement.project).getDocument(psiFile)
                ?: return emptyArray()

            val commentaryText = psiElement.text

            if (commentaryText.isBlank())
                return emptyArray()

            val toDoPatterns = TodoConfiguration
                .getInstance()
                .todoPatterns
                .mapNotNull { it.pattern?.toRegex() }

            val toDoItems = mutableListOf<ToDoItem>()
            var lineOffset = psiElement.textRange.startOffset

            for (commentaryLine in commentaryText.lineSequence()) {
                val toDoPattern = toDoPatterns.firstOrNull { it.containsMatchIn(commentaryLine) }

                if (toDoPattern != null) {
                    val toDoMatch: MatchResult? = toDoPattern.find(commentaryLine, 0)

                    if (toDoMatch != null)
                        toDoItems.add(create(
                            toDoMatch.value,
                            document,
                            lineOffset,
                            lineOffset + commentaryLine.length,
                            todosaurusSettings
                        ))
                }

                lineOffset += commentaryLine.length + 1
            }

            return toDoItems.toTypedArray()
        }

        fun fromRange(toDoRange: RangeMarker, todosaurusSettings: TodosaurusSettings.State): ToDoItem {
            val text = ReadAction.compute<String, Nothing> { toDoRange.document.getText(toDoRange.textRange) }

            return create(text, toDoRange.document, toDoRange.textRange.startOffset, toDoRange.textRange.endOffset, todosaurusSettings)
        }

        private fun create(text: String, document: Document, startIndex: Int, endIndex: Int, todosaurusSettings: TodosaurusSettings.State): ToDoItem {
            val isNew = newItemPattern.containsMatchIn(text)

            if (isNew)
                return New(document.createRangeMarker(startIndex, endIndex), todosaurusSettings)

            val issueNumber = text
                .substringAfter("[")
                .substringBefore("]")
                .replace("#", "")

            return Reported(text, issueNumber, todosaurusSettings)
        }
    }

    var title: String = text
        .substringBefore('\n')
        .replace(newItemPattern, "")
        .trim()

    var description: String = (if (text.contains("\n")) text.substringAfter('\n') + "\n" else "") +
        todosaurusSettings.descriptionTemplate

    class Reported(text: String, val issueNumber: String, todosaurusSettings: TodosaurusSettings.State) : ToDoItem(text, todosaurusSettings)
    class New(val toDoRange: RangeMarker, todosaurusSettings: TodosaurusSettings.State) : ToDoItem(toDoRange.document.getText(toDoRange.textRange), todosaurusSettings) {
        @RequiresWriteLock
        fun toReported(issueNumber: String): Reported {
            val previousText = text
            val newText = previousText.replace(newItemPattern, formReportedItemPattern(issueNumber))
            toDoRange.document.replaceString(toDoRange.startOffset, toDoRange.endOffset, newText)
            return Reported(newText, issueNumber, todosaurusSettings)
        }

        private fun formReportedItemPattern(issueNumber: String): String
        // TODO[#134]: Allow to customize template for issue number. This is difficult task because the "newItemPattern" is now linked to a regular [.*?] pattern
            = "TODO${todosaurusSettings.numberPattern}".replace(TodosaurusSettings.ISSUE_NUMBER_REPLACEMENT, issueNumber)
    }
}
