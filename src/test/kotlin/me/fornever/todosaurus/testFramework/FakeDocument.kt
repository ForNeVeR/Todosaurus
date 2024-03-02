package me.fornever.todosaurus.testFramework

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.util.Key

class FakeDocument(private var text: String) : Document {
    override fun <T : Any?> getUserData(key: Key<T>): T? {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
        TODO("Not yet implemented")
    }

    override fun getImmutableCharSequence(): CharSequence
        = text

    override fun getLineCount(): Int {
        TODO("Not yet implemented")
    }

    override fun getLineNumber(offset: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getLineStartOffset(line: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getLineEndOffset(line: Int): Int {
        TODO("Not yet implemented")
    }

    override fun insertString(offset: Int, s: CharSequence) {
        TODO("Not yet implemented")
    }

    override fun deleteString(startOffset: Int, endOffset: Int) {
        TODO("Not yet implemented")
    }

    override fun replaceString(startOffset: Int, endOffset: Int, s: CharSequence) {
        text = text.replaceRange(startOffset, endOffset, s)
    }

    override fun isWritable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getModificationStamp(): Long {
        TODO("Not yet implemented")
    }

    override fun createRangeMarker(startOffset: Int, endOffset: Int, surviveOnExternalChange: Boolean): RangeMarker {
        TODO("Not yet implemented")
    }

    override fun createGuardedBlock(startOffset: Int, endOffset: Int): RangeMarker {
        TODO("Not yet implemented")
    }

    override fun setText(text: CharSequence) {
        TODO("Not yet implemented")
    }
}
