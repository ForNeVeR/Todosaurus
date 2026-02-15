// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues

data class UrlReplacement(
    val positionInDescription: IntRange,
    val linesBefore: Int,
    val linesAfter: Int,
    val startLineOffset: Int,
    val endLineOffset: Int,
    val startLineNumber: Int,
    val endLineNumber: Int)

class UrlReplacements(private val toDoItem: ToDoItem.New) {
    companion object {
        val urlReplacementPattern: Regex
            = Regex("\\{(?:URL_REPLACEMENT|URL)(?:(-\\d+)(\\+\\d+)|(\\+\\d+)(-\\d+)|(-\\d+)|(\\+\\d+))?}")
    }

    private var latestDescription: String = ""
    private var offsets: List<UrlReplacement> = emptyList()

    init {
        update()
    }

    fun getLastReplacement(): UrlReplacement?
        = offsets.lastOrNull()

    fun getSelectedReplacement(caretOffset: Int): UrlReplacement?
        = offsets.firstOrNull { it.positionInDescription.contains(caretOffset) }

    fun getAll(): Array<UrlReplacement>
        = offsets.toTypedArray()

    fun isEmpty(): Boolean
        = offsets.isEmpty()

    fun update() {
        val newDescription = toDoItem.description

        if (newDescription == latestDescription)
            return

        latestDescription = newDescription

        if (latestDescription.isEmpty())
            return

        val document = toDoItem.toDoRange.document
        val startTextOffset = toDoItem.toDoRange.startOffset
        val endTextOffset = toDoItem.toDoRange.endOffset

        offsets = urlReplacementPattern
            .findAll(latestDescription)
            .map { match ->
                val firstToken = match.groups[1]?.value ?: match.groups[3]?.value ?: match.groups[5]?.value ?: match.groups[6]?.value
                val secondToken = match.groups[2]?.value ?: match.groups[4]?.value

                var linesBefore = 0
                var linesAfter = 0

                fun parse(token: String) {
                    val linesCount = token.toInt()

                    when {
                        linesCount < 0 -> linesBefore = linesCount
                        else -> linesAfter = linesCount
                    }
                }

                if (firstToken != null)
                    parse(firstToken)

                if (secondToken != null)
                    parse(secondToken)

                val startLineNumber = (document.getLineNumber(startTextOffset) + linesBefore + 1).coerceAtLeast(1)
                val startLineOffset = document.getLineStartOffset(startLineNumber - 1)
                val endLineNumber = (document.getLineNumber(endTextOffset) + linesAfter + 1).coerceAtMost(document.lineCount)
                val endLineOffset = document.getLineEndOffset(endLineNumber - 1)

                UrlReplacement(IntRange(match.range.first, match.range.last + 1), linesBefore, linesAfter, startLineOffset, endLineOffset, startLineNumber, endLineNumber)
            }
            .toList()
    }
}
