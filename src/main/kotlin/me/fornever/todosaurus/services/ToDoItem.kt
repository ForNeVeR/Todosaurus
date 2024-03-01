package me.fornever.todosaurus.services

import com.intellij.openapi.editor.RangeMarker

class ToDoItem(val range: RangeMarker) {
    private companion object {
        @JvmStatic
        val newItemPattern: Regex
            = Regex("\\b(?i)TODO(?-i)\\b:?(?!\\[.*\\])") // https://regex101.com/r/lDDqm7/1

        @JvmStatic
        val issueDescriptionTemplate = """
            See the code near this line: ${GitHubService.GITHUB_CODE_URL_REPLACEMENT}

            Also, look for the number of this issue in the project code base.
        """.trimIndent()

        @JvmStatic
        fun formReadyItemPattern(issueNumber: Long): String
            = "TODO[#${issueNumber}]:"
    }

    private var isReady: Boolean = false

    private val text: String =
        range.document
            .getText(range.textRange)

    val title: String
        = text
            .substringBefore('\n')
            .replace(newItemPattern, "")
            .trim()

    val description: String =
        (if (text.contains("\n")) text.substringAfter('\n') + "\n" else "") +
            issueDescriptionTemplate

    fun markAsReady(issueNumber: Long) {
        if (!isNew())
            return

        val previousText = text
        val newText = previousText.replace(newItemPattern, formReadyItemPattern(issueNumber))
        range.document.replaceString(range.startOffset, range.endOffset, newText)

        isReady = true
    }

    fun isNew(): Boolean
        = !isReady && newItemPattern.containsMatchIn(text)
}
