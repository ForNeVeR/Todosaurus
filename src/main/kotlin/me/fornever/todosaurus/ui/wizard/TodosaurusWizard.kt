package me.fornever.todosaurus.ui.wizard

import com.intellij.ide.IdeBundle
import com.intellij.ide.wizard.AbstractWizard
import com.intellij.ide.wizard.CommitStepCancelledException
import com.intellij.ide.wizard.CommitStepException
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.JBCardLayout.SwipeDirection
import com.intellij.ui.mac.touchbar.Touchbar
import com.intellij.util.containers.toArray
import com.intellij.util.ui.UIUtil
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import javax.swing.*


class TodosaurusWizard(title: String, project: Project, private val finalAction: suspend () -> WizardResult)
    : AbstractWizard<TodosaurusStep>(title, project) {
    private val stepsToIndexes: Object2IntMap<Any> = Object2IntOpenHashMap()
    private val indexesToSteps: Int2ObjectMap<TodosaurusStep> = Int2ObjectOpenHashMap()
    private val dynamicSteps: Int2ObjectMap<TodosaurusStep> = Int2ObjectOpenHashMap()

    var nextButtonName: String? = null

    init {
        isModal = false
        helpButton.isVisible = false
    }

    override fun show() {
        init()
        super.show()
    }

    override fun addStep(step: TodosaurusStep, index: Int) {
        stepsToIndexes.put(step.id, index)
        indexesToSteps.put(index, step)

        super.addStep(step, index)

        step.addStepListener(object: TodosaurusStep.Listener {
            override fun stateChanged() {
                updateButtons()
            }

            override fun doNextAction() {
                if (nextButton.isEnabled) {
                    doNextAction()
                }
            }
        })
    }

    override fun doPreviousAction() {
        val currentStep = indexesToSteps.get(myCurrentStep)

        try {
            currentStep.commitPrevious()
        }
        catch (exception: CommitStepCancelledException) {
            return
        }
        catch (exception: CommitStepException) {
            return Messages.showErrorDialog(contentPane, exception.message)
        }

        myCurrentStep = getPreviousStep(myCurrentStep)
        updateStep(SwipeDirection.BACKWARD)
    }

    override fun doNextAction() {
        val currentStep = indexesToSteps.get(myCurrentStep)

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

        if (currentStep !is DynamicStepProvider || dynamicSteps.containsKey(nextStepIndex)) {
            myCurrentStep = nextStepIndex
            return updateStep(SwipeDirection.FORWARD)
        }

        val nextStep = indexesToSteps.get(nextStepIndex)
        val dynamicStep = currentStep.createDynamicStep()

        dynamicStep.previousId = currentStep.id
        currentStep.nextId = dynamicStep.id

        if (nextStep.id != currentStep.id) {
            dynamicStep.nextId = nextStep.id
            nextStep.previousId = dynamicStep.id
        }

        addStep(dynamicStep)

        myCurrentStep = mySteps.size - 1
        dynamicSteps.put(myCurrentStep, dynamicStep)

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

    override fun getNextStep(stepIndex: Int): Int {
        val step = indexesToSteps.get(stepIndex)
        return stepsToIndexes.getInt(step.nextId)
    }

    override fun getPreviousStep(stepIndex: Int): Int {
        val step = indexesToSteps.get(stepIndex)
        return stepsToIndexes.getInt(step.previousId)
    }

    override fun updateStep() {
        super.updateStep()
        updateButtons()

        currentStepObject
            .preferredFocusedComponent
            ?.let {
                IdeFocusManager
                    .findInstanceByComponent(window)
                    .requestFocus(it, true)
            }
    }

    override fun updateButtons() {
        super.updateButtons()

        val currentStep = indexesToSteps.get(myCurrentStep)
        previousButton.isEnabled = currentStep.previousId != null
        nextButton.isEnabled = currentStep.isComplete() && !isLastStep || isLastStep && canFinish()
    }

    override fun createSouthPanel(): JComponent {
        if (useDialogWrapperSouthPanel()) {
            return super.createSouthPanel()
        }

        val panel = JPanel(BorderLayout())

        if (style == DialogStyle.COMPACT) {
            panel.setBorder(BorderFactory.createEmptyBorder(4, 15, 4, 15))
        }

        val buttonPanel = JPanel()

        if (SystemInfo.isMac) {
            panel.add(buttonPanel, BorderLayout.EAST)
            buttonPanel.setLayout(BoxLayout(buttonPanel, BoxLayout.X_AXIS))

            if (!EditorColorsManager.getInstance().isDarkEditor) { // is it analogue of isUnderDarcula?
                helpButton.putClientProperty("JButton.buttonType", "help")
            }

            val touchbarButtons: MutableList<JButton> = ArrayList()
            val leftPanel = JPanel()

            if (ApplicationInfo.contextHelpAvailable()) {
                leftPanel.add(helpButton)
                touchbarButtons.add(helpButton)
            }

            leftPanel.add(cancelButton)
            touchbarButtons.add(cancelButton)
            panel.add(leftPanel, BorderLayout.WEST)

            val principalTouchbarButtons: MutableList<JButton> = ArrayList()

            if (mySteps.size > 1 || mySteps[0] is DynamicStepProvider) {
                buttonPanel.add(Box.createHorizontalStrut(5))
                buttonPanel.add(previousButton)
                principalTouchbarButtons.add(previousButton)
            }

            buttonPanel.add(Box.createHorizontalStrut(5))
            buttonPanel.add(nextButton)
            principalTouchbarButtons.add(nextButton)
            Touchbar.setButtonActions(panel, touchbarButtons, principalTouchbarButtons, nextButton)
        }
        else {
            panel.add(buttonPanel, BorderLayout.CENTER)

            val layout = GroupLayout(buttonPanel).also {
                it.autoCreateGaps = true
            }

            buttonPanel.setLayout(layout)

            val horizontalGroup: GroupLayout.SequentialGroup = layout.createSequentialGroup()
            val verticalGroup: GroupLayout.ParallelGroup = layout.createParallelGroup()
            val buttons: MutableCollection<Component> = ArrayList(5)
            val helpAvailable = ApplicationInfo.contextHelpAvailable()

            fillComponentGroups(horizontalGroup, verticalGroup, null, Box.createHorizontalGlue())

            if (mySteps.size > 1 || mySteps[0] is DynamicStepProvider) {
                fillComponentGroups(horizontalGroup, verticalGroup, buttons, previousButton)
            }

            fillComponentGroups(horizontalGroup, verticalGroup, buttons, nextButton, cancelButton)

            if (helpAvailable) {
                val leftPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))

                if (ApplicationInfo.contextHelpAvailable()) {
                    leftPanel.add(helpButton)
                    panel.add(leftPanel, BorderLayout.WEST)
                }
            }

            layout.setHorizontalGroup(horizontalGroup)
            layout.setVerticalGroup(verticalGroup)
            layout.linkSize(*buttons.toArray(emptyArray()))
        }

        previousButton.isEnabled = false

        previousButton.addActionListener {
            doPreviousAction()
        }

        nextButton.addActionListener {
            proceedToNextStep()
        }

        cancelButton.addActionListener {
            doCancelAction()
        }

        return panel
    }

    private fun fillComponentGroups(
        horizontalGroup: GroupLayout.Group,
        verticalGroup: GroupLayout.Group,
        collection: MutableCollection<in Component>?,
        vararg components: Component) {
        for (component in components) {
            horizontalGroup.addComponent(component)
            verticalGroup.addComponent(component)
            collection?.add(component)
        }
    }

    override fun updateButtons(lastStep: Boolean, canGoNext: Boolean, firstStep: Boolean) {
        if (lastStep) {
            if (nextButtonName != null) {
                nextButton.text = nextButtonName
            }
            else if (mySteps.size > 1) {
                nextButton.mnemonic = 67
                nextButton.text = UIUtil.removeMnemonic(IdeBundle.message("button.create"))
            }
            else {
                nextButton.text = IdeBundle.message("button.ok")
            }
        }
        else {
            nextButton.text = UIUtil.removeMnemonic(IdeBundle.message("button.wizard.next"))
            nextButton.mnemonic = 78
        }

        nextButton.setEnabled(canGoNext)

        if (nextButton.isEnabled && !ApplicationManager.getApplication().isUnitTestMode) {
            rootPane?.setDefaultButton(nextButton)
        }

        previousButton.isEnabled = !firstStep
        previousButton.isVisible = !firstStep
    }

    override fun canGoNext(): Boolean
        = indexesToSteps.get(myCurrentStep).isComplete()

    override fun isLastStep(): Boolean
        = indexesToSteps.get(myCurrentStep).nextId == null

    override fun canFinish(): Boolean
        = mySteps.all { it.isComplete() }

    override fun dispose() {
        super.dispose()

        for (step in mySteps) {
            Disposer.dispose(step)
        }
    }

    override fun getHelpID(): String?
        = null
}
