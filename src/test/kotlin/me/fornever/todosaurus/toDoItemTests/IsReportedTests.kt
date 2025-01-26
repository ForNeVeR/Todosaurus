// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.toDoItemTests

import me.fornever.todosaurus.issues.ToDoItem
import me.fornever.todosaurus.settings.TodosaurusSettings
import me.fornever.todosaurus.testFramework.FakeRangeMarker
import org.junit.Assert
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
        Assert.assertFalse(sut is ToDoItem.Reported)
    }
}
