package me.fornever.todosaurus.ui.wizard

import com.intellij.ide.wizard.AbstractWizardStepEx

abstract class TodosaurusStep : AbstractWizardStepEx(null) {
    abstract val id: Any

    var nextId: Any? = null
    var previousId: Any? = null

    override fun getStepId(): Any
        = id

    override fun getNextStepId(): Any?
        = nextId

    override fun getPreviousStepId(): Any?
        = previousId

    override fun commit(commitType: CommitType?)
    { }
}
