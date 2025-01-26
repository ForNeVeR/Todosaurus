// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.wizard

import com.intellij.ide.wizard.Step
import com.intellij.ide.wizard.StepListener
import com.intellij.openapi.Disposable
import com.intellij.util.EventDispatcher
import javax.swing.Icon

abstract class TodosaurusWizardStep : Step, Disposable {
    interface Listener : StepListener {
        fun doNextAction()
    }

    enum class CommitType {
        Previous,
        Next,
        Finish
    }

    abstract val id: String

    var nextId: String? = null
    var previousId: String? = null

    private val eventDispatcher: EventDispatcher<Listener> = EventDispatcher.create(Listener::class.java)

    abstract fun isComplete(): Boolean

    fun addStepListener(stepListener: Listener)
        = eventDispatcher.addListener(stepListener)

    fun fireStateChanged()
        = eventDispatcher.multicaster.stateChanged()

    fun commitPrevious()
        = commit(CommitType.Previous)

    override fun _commit(finishChosen: Boolean)
        = commit(if (finishChosen) CommitType.Finish else CommitType.Next)

    override fun getIcon(): Icon?
        = null

    override fun _init()
    { }

    open fun commit(commitType: CommitType?)
    { }

    override fun dispose()
    { }
}
