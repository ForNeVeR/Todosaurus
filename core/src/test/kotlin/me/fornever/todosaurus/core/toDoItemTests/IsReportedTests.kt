// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.toDoItemTests

import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.settings.TodosaurusSettings
import me.fornever.todosaurus.core.testFramework.FakeRangeMarker
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class IsReportedTests {

    @ParameterizedTest
    @ValueSource(strings = [
        "TODO[#2342]:",
        "Todo[#2123]"
    ])
    fun `ToDo item should be reported`(readyItem: String) {
        // Arrange
        val sut = ToDoItem(TodosaurusSettings.State.defaultState, FakeRangeMarker(readyItem))

        // Act & Assert
		assertFalse(sut.isNew)
    }
}
