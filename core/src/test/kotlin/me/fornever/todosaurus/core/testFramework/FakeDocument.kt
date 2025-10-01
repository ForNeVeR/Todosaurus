// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.testFramework

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.Key

class FakeDocument(private var text: String) : Document {
    override fun <T : Any?> getUserData(key: Key<T>) = error("Not implemented.")
    override fun <T : Any?> putUserData(key: Key<T>, value: T?) = error("Not implemented.")
    override fun getImmutableCharSequence() = text

    private fun lineStarts(): IntArray {
        if (text.isEmpty())
            return intArrayOf(0)

        val starts = ArrayList<Int>(32)
        starts.add(0)

        for (index in text.indices.filter { text[it] == '\n' }) {
            val next = index + 1

            if (next <= text.length)
                starts.add(next)
        }

        return starts.toIntArray()
    }

    override fun getLineCount()
        = lineStarts().size

    override fun getLineNumber(offset: Int): Int {
        if (offset < 0 || offset > text.length)
            throw IndexOutOfBoundsException("offset = $offset, length = ${text.length}")

        val starts = lineStarts()
        val lineNumber = starts.binarySearch(offset)

        return if (lineNumber >= 0) lineNumber else -lineNumber - 2
    }

    override fun getLineStartOffset(line: Int): Int {
        val starts = lineStarts()

        if (line !in starts.indices)
            throw IndexOutOfBoundsException("line = $line, lineCount = ${starts.size}")

        return starts[line]
    }

    override fun getLineEndOffset(line: Int): Int {
        val starts = lineStarts()

        if (line !in starts.indices)
            throw IndexOutOfBoundsException("line = $line, lineCount = ${starts.size}")

        return if (line == starts.lastIndex) text.length else starts[line + 1] - 1
    }

    override fun insertString(offset: Int, s: CharSequence) = error("Not implemented.")
    override fun deleteString(startOffset: Int, endOffset: Int) = error("Not implemented.")
    override fun replaceString(startOffset: Int, endOffset: Int, s: CharSequence) {
        text = text.replaceRange(startOffset, endOffset, s)
    }

    override fun isWritable() = error("Not implemented.")
    override fun getModificationStamp() = error("Not implemented.")
    override fun createRangeMarker(startOffset: Int, endOffset: Int, surviveOnExternalChange: Boolean)
        = FakeRangeMarker(text)

    override fun createGuardedBlock(startOffset: Int, endOffset: Int) = error("Not implemented.")
    override fun setText(text: CharSequence)  = error("Not implemented.")
}
