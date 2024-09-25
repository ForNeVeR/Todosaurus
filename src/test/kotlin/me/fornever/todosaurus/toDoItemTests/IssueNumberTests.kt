package me.fornever.todosaurus.toDoItemTests

import me.fornever.todosaurus.services.ToDoItem
import me.fornever.todosaurus.testFramework.FakeRangeMarker
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class IssueNumberTests(private val source: String, private val expected: Long?) {
    companion object {
        @JvmStatic
        @Parameters
        fun newItems()
            = arrayOf(
                arrayOf("TODO[#111]:", 111L),
                arrayOf("todo[#112]:", 112L),
                arrayOf("text Todo[#113]:", 113L),
                arrayOf("ToDo[#114]:", 114L),
                arrayOf("Todo[#115]:text", 115L),
                arrayOf("ToDo[#116]: Text", 116L),
                arrayOf("Todo[#117]:Text", 117L),
                arrayOf("TODO[#118]:    Text", 118L),
                arrayOf("TODO", null),
                arrayOf("todo", null),
                arrayOf("text Todo", null),
                arrayOf("ToDo 119", null),
                arrayOf("Todo[120]:text", 120L),
                arrayOf("ToDo[#121]: Text", 121L),
                arrayOf("Todo[#122]:Text", 122L),
                arrayOf("TODO[#123]:    Text", 123L),
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
