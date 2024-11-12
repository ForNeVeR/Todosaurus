// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.toDoItemTests

import me.fornever.todosaurus.issues.ToDoItem
import me.fornever.todosaurus.testFramework.FakeRangeMarker
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class IsNewTests(private val newItem: String) {
    companion object {
        @JvmStatic
        @Parameters
        fun newItems()
            = arrayOf(
                "TODO",
                "todo",
                "text Todo",
                "ToDo",
                "Todo:text",
                "ToDo Text",
                "Todo:Text",
                "TODO    Text")
    }

    @Test
    fun `ToDo item should be new`() {
        // Arrange
        val sut = ToDoItem(FakeRangeMarker(newItem))

        // Act & Assert
        assertTrue(sut.isNew)
    }
}
