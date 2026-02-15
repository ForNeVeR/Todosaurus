// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.toDoItemTests

import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.settings.TodosaurusSettings
import me.fornever.todosaurus.core.testFramework.FakeRangeMarker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class IssueNumberTests {
    companion object {
        @JvmStatic
        private fun items()
            = arrayOf(
                Arguments.of("TODO[#111]:", "111"),
                Arguments.of("todo[#112]:", "112"),
                Arguments.of("text Todo[#113]:", "113"),
                Arguments.of("ToDo[#114]:", "114"),
                Arguments.of("Todo[#115]:text", "115"),
                Arguments.of("ToDo[#116]: Text", "116"),
                Arguments.of("Todo[#117]:Text", "117"),
                Arguments.of("TODO[#118]:    Text", "118"),
                Arguments.of("Todo[120]:text", "120"),
                Arguments.of("ToDo[#121]: Text", "121"),
                Arguments.of("Todo[#122]:Text", "122"),
                Arguments.of("TODO[#123]:    Text", "123"),
                Arguments.of("ToDo[a127]:", "a127"),
                Arguments.of("Todo[c128b]:text", "c128b"),
                Arguments.of("ToDo[129d]: Text", "129d"),
                Arguments.of("ToDo[1a30]:", "1a30"),
                Arguments.of("Todo[13c1]:text", "13c1"),
                Arguments.of("ToDo[1b3c2]: Text", "1b3c2")
            )
    }

    @ParameterizedTest
    @MethodSource("items")
    fun `Should returns issue number properly`(source: String, expected: String?) {
        // Arrange
        val sut = ToDoItem.fromRange(FakeRangeMarker(source), TodosaurusSettings.State.defaultState)

        // Act & Assert
        if (sut !is ToDoItem.Reported)
            return fail()

        assertEquals(expected, sut.issueNumber)
    }
}
