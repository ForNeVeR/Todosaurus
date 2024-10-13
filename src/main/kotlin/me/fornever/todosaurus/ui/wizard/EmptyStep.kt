package me.fornever.todosaurus.ui.wizard

import javax.swing.JComponent
import javax.swing.JPanel

class EmptyStep: TodosaurusStep() {
    override val id: Any = EmptyStep::class.java

    override fun getComponent(): JComponent
        = JPanel()

    override fun getPreferredFocusedComponent(): JComponent?
        = null

    override fun isComplete(): Boolean
        = true
}
