package me.fornever.todosaurus.toDoItemTests

import me.fornever.todosaurus.services.ToDoItem
import me.fornever.todosaurus.testFramework.FakeRangeMarker
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class IsReadyTests(private val readyItem: String) {
    companion object {
        @JvmStatic
        @Parameters
        fun readyItems()
            = arrayOf(
                "TODO[#2342]:",
                "Todo[#2123]")
    }

    @Test
    fun `ToDo item should be ready`() {
        // Arrange
        val sut = ToDoItem(FakeRangeMarker(readyItem))

        // Act & Assert
		Assert.assertFalse(sut.isNew())
    }
}
