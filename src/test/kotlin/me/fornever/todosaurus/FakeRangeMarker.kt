package me.fornever.todosaurus

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.util.Key

class FakeRangeMarker(private val text: String) : RangeMarker {
    private val document: FakeDocument = FakeDocument(text)

    override fun <T : Any?> getUserData(key: Key<T>): T? {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
        TODO("Not yet implemented")
    }

    override fun getStartOffset(): Int
        = 0

    override fun getEndOffset(): Int
        = text.length

    override fun getDocument(): Document
        = document

    override fun isValid(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setGreedyToLeft(greedy: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setGreedyToRight(greedy: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isGreedyToRight(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isGreedyToLeft(): Boolean {
        TODO("Not yet implemented")
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }
}
