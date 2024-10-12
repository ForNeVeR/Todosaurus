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
class IssueNumberTests(private val source: String, private val expected: String?) {
    companion object {
        @JvmStatic
        @Parameters
        fun newItems()
            = arrayOf(
                arrayOf("TODO[#111]:", "111"),
                arrayOf("todo[#112]:", "112"),
                arrayOf("text Todo[#113]:", "113"),
                arrayOf("ToDo[#114]:", "114"),
                arrayOf("Todo[#115]:text", "115"),
                arrayOf("ToDo[#116]: Text", "116"),
                arrayOf("Todo[#117]:Text", "117"),
                arrayOf("TODO[#118]:    Text", "118"),
                arrayOf("TODO", null),
                arrayOf("todo", null),
                arrayOf("text Todo", null),
                arrayOf("ToDo 119", null),
                arrayOf("Todo[120]:text", "120"),
                arrayOf("ToDo[#121]: Text", "121"),
                arrayOf("Todo[#122]:Text", "122"),
                arrayOf("TODO[#123]:    Text", "123"),
                arrayOf("TODO [124]", null),
                arrayOf("todo [125]", null),
                arrayOf("text Todo [#126]", null),
                arrayOf("ToDo[a127]:", null),
                arrayOf("Todo[c128b]:text", null),
                arrayOf("ToDo[129d]: Text", null),
                arrayOf("ToDo[1a30]:", null),
                arrayOf("Todo[13c1]:text", null),
                arrayOf("ToDo[1b3c2]: Text", null)
            )
    }

    @Test
    fun `Should returns issue number properly`() {
        // Arrange
        val sut = ToDoItem(FakeRangeMarker(source))

        // Act & Assert
        Assert.assertEquals(expected, sut.issueNumber)
    }
}
