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

    override fun getLineCount() = error("Not implemented.")
    override fun getLineNumber(offset: Int) = error("Not implemented.")
    override fun getLineStartOffset(line: Int) = error("Not implemented.")
    override fun getLineEndOffset(line: Int) = error("Not implemented.")
    override fun insertString(offset: Int, s: CharSequence) = error("Not implemented.")
    override fun deleteString(startOffset: Int, endOffset: Int) = error("Not implemented.")
    override fun replaceString(startOffset: Int, endOffset: Int, s: CharSequence) {
        text = text.replaceRange(startOffset, endOffset, s)
    }

    override fun isWritable() = error("Not implemented.")
    override fun getModificationStamp() = error("Not implemented.")
    override fun createRangeMarker(startOffset: Int, endOffset: Int, surviveOnExternalChange: Boolean) =
        error("Not implemented.")

    override fun createGuardedBlock(startOffset: Int, endOffset: Int) = error("Not implemented.")
    override fun setText(text: CharSequence)  = error("Not implemented.")
}
