// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.toDoItemTests

import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.settings.TodosaurusSettings
import me.fornever.todosaurus.core.testFramework.FakeRangeMarker
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class IsReportedTests(private val readyItem: String) {
    companion object {
        @JvmStatic
        @Parameters
        fun reportedItems()
            = arrayOf(
                "TODO[#2342]:",
                "Todo[#2123]")
    }

    @Test
    fun `ToDo item should be reported`() {
        // Arrange
        val sut = ToDoItem.fromRange(FakeRangeMarker(readyItem), TodosaurusSettings.State.defaultState)

        // Act & Assert
        assertTrue(sut is ToDoItem.Reported)
    }
}
