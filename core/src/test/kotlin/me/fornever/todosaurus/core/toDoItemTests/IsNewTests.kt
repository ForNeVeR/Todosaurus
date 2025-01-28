// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.toDoItemTests

import com.intellij.testFramework.junit5.TestApplication
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.settings.TodosaurusSettings
import me.fornever.todosaurus.core.testFramework.FakeRangeMarker
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@TestApplication
class IsNewTests {

    @ParameterizedTest
    @ValueSource(strings = [
        "TODO",
        "todo",
        "text Todo",
        "ToDo",
        "Todo:text",
        "ToDo Text",
        "Todo:Text",
        "TODO    Text"
    ])
    fun `ToDo item should be new`(newItem: String) {
        // Arrange
        val sut = ToDoItem.fromRange(FakeRangeMarker(newItem), TodosaurusSettings.State.defaultState)

        // Act & Assert
        assertTrue(sut is ToDoItem.New)
    }
}
