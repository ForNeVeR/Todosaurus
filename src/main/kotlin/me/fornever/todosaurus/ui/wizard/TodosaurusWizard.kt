package me.fornever.todosaurus.ui.wizard

import com.intellij.ide.wizard.AbstractWizardEx
import com.intellij.ide.wizard.CommitStepCancelledException
import com.intellij.ide.wizard.CommitStepException
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.JBCardLayout.SwipeDirection
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TodosaurusWizard(title: String, project: Project, steps: MutableList<TodosaurusStep>, private val finalAction: suspend () -> WizardResult)
    : AbstractWizardEx(title, project, steps) {
    companion object {
        private const val CREATE_BUTTON_NAME: String = "Create"
        private const val BUTTON_NAME_PROPERTY: String = "text"
    }

    private val stepIndexes: Object2IntMap<Any> = Object2IntOpenHashMap()

    init {
        isModal = false
        helpButton.isVisible = false

        steps.forEachIndexed { index, step ->
            stepIndexes.put(step.id, index)
        }
    }

    fun setFinalNameButton(newName: String) {
        nextButton.addPropertyChangeListener {
            // Dirty hack to change text of next button :p
            if (it.propertyName == BUTTON_NAME_PROPERTY && it.newValue == CREATE_BUTTON_NAME) {
                nextButton.text = newName
                repaint()
            }
        }
    }

    override fun doNextAction() {
        val currentStep = mySteps[myCurrentStep] as TodosaurusStep

        try {
            currentStep._commit(false)
        }
        catch (exception: CommitStepCancelledException) {
            return
        }
        catch (exception: CommitStepException) {
            return Messages.showErrorDialog(contentPane, exception.message)
        }

        if (this.isLastStep) {
            return doOKAction()
        }

        val nextStepIndex = getNextStep(myCurrentStep)
        val nextStep = mySteps[nextStepIndex] as TodosaurusStep

        if (currentStep is OptionalStepProvider) {
            val optionalStepId = currentStep.chooseOptionalStepId()
            currentStep.nextId = optionalStepId
            nextStep.previousId = optionalStepId
        }

        myCurrentStep = nextStepIndex
        updateStep(SwipeDirection.FORWARD)
    }

    override fun doOKAction() {
        if (!this.okAction.isEnabled)
            return

        applyFields()

        CoroutineScope(Dispatchers.IO).launch {
            val result = finalAction()

            if (result == WizardResult.Success) {
				withContext(Dispatchers.EDT) {
					close(0)
				}
            }
        }
    }

    override fun getHelpID(): String?
        = null
}
