// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.toDoItemTests

import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.settings.TodosaurusSettings
import me.fornever.todosaurus.core.testFramework.FakeRangeMarker
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class ToReportedTests(private val newItem: String) {
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
    fun `Should mark ToDo item as reported`() {
        // Arrange
        val expected = "TODO[#1]:"
        val sut = ToDoItem.fromRange(FakeRangeMarker(newItem), TodosaurusSettings.State.defaultState)

        if (sut !is ToDoItem.New)
            return fail()

        // Act
        sut.toReported("1")

        // Assert
        assertTrue(sut.toDoRange.document.text.contains(expected))
    }

    @Test
    fun `Should provide issue number after report`() {
        // Arrange
        val sut = ToDoItem.fromRange(FakeRangeMarker(newItem), TodosaurusSettings.State.defaultState)

        if (sut !is ToDoItem.New)
            return fail()

        // Act
        val actual = sut.toReported("1")

        // Assert
        assertEquals("1", actual.issueNumber)
    }
}
