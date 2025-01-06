// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.wizardTests

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.fornever.todosaurus.testFramework.FakeProject
import me.fornever.todosaurus.ui.wizard.TodosaurusWizardBuilder
import org.junit.Assert
import org.junit.Test

class TodosaurusWizardBuilderTests {
    @Test
    fun `Should link steps properly`() {
        // Arrange
        val sut = TodosaurusWizardBuilder(FakeProject(), CoroutineScope(Dispatchers.IO))
        val firstStep = FakeStep("1")
        val secondStep = FakeStep("2")
        val lastStep = FakeStep("3")

        // Act
        sut.addStep(firstStep)
            .addStep(secondStep)
            .addStep(lastStep)

        // Assert
        Assert.assertEquals(secondStep.id, firstStep.nextId)
        Assert.assertEquals(null, firstStep.previousId)
        Assert.assertEquals(lastStep.id, secondStep.nextId)
        Assert.assertEquals(firstStep.id, secondStep.previousId)
        Assert.assertEquals(null, lastStep.nextId)
        Assert.assertEquals(secondStep.id, lastStep.previousId)
    }
}
