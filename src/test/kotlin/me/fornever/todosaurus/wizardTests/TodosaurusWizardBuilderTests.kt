package me.fornever.todosaurus.wizardTests

import me.fornever.todosaurus.testFramework.FakeProject
import me.fornever.todosaurus.ui.wizard.TodosaurusStep
import me.fornever.todosaurus.ui.wizard.TodosaurusWizardBuilder
import org.junit.Assert
import org.junit.Test

class TodosaurusWizardBuilderTests {
    @Test
    fun `Should link steps properly`() {
        // Arrange
        val sut = TodosaurusWizardBuilder(FakeProject())
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

    @Test
    fun `Should add optional steps properly`() {
        // Arrange
        val sut = TodosaurusWizardBuilder(FakeProject())
        val optionalSteps = mutableListOf<TodosaurusStep>(FakeStep("2"), FakeStep("3"), FakeStep("4"))
        val step = FakeOptionalStepProvider("1", optionalSteps)

        // Act
        sut.addStep(step)

        // Assert
        optionalSteps.forEach { expected ->
            Assert.assertTrue(sut.steps.contains(expected))
        }
    }

    @Test
    fun `Parent step next id should not be null`() {
        // Arrange
        val sut = TodosaurusWizardBuilder(FakeProject())
        val optionalSteps = mutableListOf<TodosaurusStep>(FakeStep("2"), FakeStep("3"), FakeStep("4"))
        val step = FakeOptionalStepProvider("1", optionalSteps)

        // Act
        sut.addStep(step)

        // Assert
        Assert.assertNotEquals(null, step.nextId)
    }

    @Test
    fun `Previous step id for optional steps should be equals parent step id`() {
        // Arrange
        val sut = TodosaurusWizardBuilder(FakeProject())
        val optionalSteps = mutableListOf<TodosaurusStep>(FakeStep("2"), FakeStep("3"), FakeStep("4"))
        val step = FakeOptionalStepProvider("1", optionalSteps)

        // Act
        sut.addStep(step)

        // Assert
        optionalSteps.forEach {
            Assert.assertEquals(step.id, it.previousId)
        }
    }

    @Test
    fun `Next step id for optional steps should be equals last step`() {
        // Arrange
        val sut = TodosaurusWizardBuilder(FakeProject())
        val optionalSteps = mutableListOf<TodosaurusStep>(FakeStep("2"), FakeStep("3"), FakeStep("4"))
        val firstStep = FakeOptionalStepProvider("1", optionalSteps)
        val lastStep = FakeStep("5")

        // Act
        sut.addStep(firstStep)
            .addStep(lastStep)

        // Assert
        optionalSteps.forEach {
            Assert.assertEquals(lastStep.id, it.nextId)
        }
    }

    @Test
    fun `Previous step id for last step should be null if previous steps was optional`() {
        // Arrange
        val sut = TodosaurusWizardBuilder(FakeProject())
        val optionalSteps = mutableListOf<TodosaurusStep>(FakeStep("2"), FakeStep("3"), FakeStep("4"))
        val firstStep = FakeOptionalStepProvider("1", optionalSteps)
        val lastStep = FakeStep("5")

        // Act
        sut.addStep(firstStep)
            .addStep(lastStep)

        // Assert
        Assert.assertEquals(null, lastStep.previousId)
    }
}
