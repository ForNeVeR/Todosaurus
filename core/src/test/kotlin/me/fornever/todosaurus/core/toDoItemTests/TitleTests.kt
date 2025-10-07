// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.toDoItemTests

import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.settings.TodosaurusSettings
import me.fornever.todosaurus.core.testFramework.FakeRangeMarker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class TitleTests {
    companion object {
        @JvmStatic
        fun titles() = arrayOf(
            Arguments.of("TODO", ""),
            Arguments.of("ToDo", ""),
            Arguments.of("todo some text", "some text"),
            Arguments.of("/* todo some text */", "some text"),
            Arguments.of("Todo:text", "text"),
            Arguments.of("ToDo Text", "Text"),
            Arguments.of("// ToDo Text", "Text"),
            Arguments.of("Todo:Text", "Text"),
            Arguments.of("TODO    Text", "Text"),
            Arguments.of("TODO\nText", ""),
            Arguments.of("/* TODO: Text", "Text")
        )
    }

    @ParameterizedTest
    @MethodSource("titles")
    fun `Should calculate title properly`(source: String, expected: String) {
        // Arrange
        val sut = ToDoItem.fromRange(FakeRangeMarker(source), TodosaurusSettings.State.defaultState)

        // Act & Assert
        assertEquals(expected, sut.title)
    }
}
