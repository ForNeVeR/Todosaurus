// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.toDoItemTests

import me.fornever.todosaurus.issues.ToDoItem
import me.fornever.todosaurus.testFramework.FakeRangeMarker
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class TitleTests(private val source: String, private val expected: String) {
    companion object {
        @JvmStatic
        @Parameters
        fun titles() = arrayOf(
            arrayOf("TODO", ""),
            arrayOf("ToDo", ""),
            arrayOf("todo some text", "some text"),
            arrayOf("Todo:text", "text"),
            arrayOf("ToDo Text", "Text"),
            arrayOf("Todo:Text", "Text"),
            arrayOf("TODO    Text", "Text"),
            arrayOf("TODO\nText", "")
        )
    }

    @Test
    fun `Should calculate title properly`() {
        // Arrange
        val sut = ToDoItem(FakeRangeMarker(source))

        // Act & Assert
        Assert.assertEquals(expected, sut.title)
    }
}
