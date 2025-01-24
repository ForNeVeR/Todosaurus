// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.testFramework

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.util.Key

class FakeRangeMarker(text: String) : RangeMarker {
    private val document: FakeDocument = FakeDocument(text)

    override fun <T : Any?> getUserData(key: Key<T>) = error("Not implemented.")
    override fun <T : Any?> putUserData(key: Key<T>, value: T?) = error("Not implemented.")
    override fun getStartOffset(): Int = 0

    override fun getEndOffset(): Int = document.textLength

    override fun getDocument(): Document = document

    override fun isValid() = error("Not implemented.")
    override fun setGreedyToLeft(greedy: Boolean) = error("Not implemented.")

    override fun setGreedyToRight(greedy: Boolean) = error("Not implemented.")
    override fun isGreedyToRight() = error("Not implemented.")
    override fun isGreedyToLeft() = error("Not implemented.")
    override fun dispose() {}
}
