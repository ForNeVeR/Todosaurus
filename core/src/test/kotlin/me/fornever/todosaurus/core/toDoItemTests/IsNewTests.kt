// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.toDoItemTests

import me.fornever.todosaurus.core.testFramework.FakeRangeMarker
import me.fornever.todosaurus.issues.ToDoItem
import me.fornever.todosaurus.settings.TodosaurusSettings
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
        val sut = ToDoItem(TodosaurusSettings.State.defaultState, FakeRangeMarker(newItem))

        // Act & Assert
        assertTrue(sut.isNew)
    }
}
