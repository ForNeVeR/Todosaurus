// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.vcs.git.ui.wizard

import com.intellij.openapi.project.Project
import com.intellij.ui.UserActivityWatcher
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.ComponentPredicate
import me.fornever.todosaurus.ui.wizard.MemorableStep
import me.fornever.todosaurus.ui.wizard.TodosaurusContext
import me.fornever.todosaurus.ui.wizard.TodosaurusStep
import me.fornever.todosaurus.vcs.git.GitBasedPlacementDetails
import me.fornever.todosaurus.vcs.git.GitRemote
import me.fornever.todosaurus.vcs.git.GitRemoteProvider
import me.fornever.todosaurus.vcs.git.ui.controls.GitRemoteComboBox
import javax.swing.JComponent

class ChooseGitRemoteStep(private val project: Project, private val model: TodosaurusContext) : TodosaurusStep(), MemorableStep {
    companion object {
        val id: Any = ChooseGitRemoteStep::class.java
    }

    override val id: Any = Companion.id

    private val gitRemotePicker: GitRemoteComboBox = GitRemoteComboBox()

    override fun _init() {
        super._init()

        model.placementDetails = GitBasedPlacementDetails()

        val selectedIndex = gitRemotePicker.selectedIndex

        gitRemotePicker.removeAllItems()

        GitRemoteProvider
            .getInstance(project)
            .provideAll(model.connectionDetails)
            .forEach {
                gitRemotePicker.addItem(it)
            }

        if (selectedIndex > -1)
            gitRemotePicker.selectedIndex = selectedIndex
    }

    override fun getComponent(): JComponent = panel {
        panel {
            row {
                label("Choose a remote repository:")
            }

            row {
                gitRemotePicker.also {
                    cell(it)
                        .enabledIf(object : ComponentPredicate() {
                            override fun addListener(listener: (Boolean) -> Unit)
                                = it.addItemListener {
                                    listener(invoke())
                                }

                            override fun invoke(): Boolean
                                = it.itemCount != 0
                        })
                        .align(Align.FILL)
                }
            }
        }
    }
    .also {
        UserActivityWatcher().also { watcher ->
            watcher.register(it)
            watcher.addUserActivityListener {
                updateIssuePlacementDetails()
                fireStateChanged()
            }
        }
    }

    override fun getPreferredFocusedComponent(): JComponent
        = gitRemotePicker

    override fun isComplete(): Boolean {
        val placementDetails = model.placementDetails as? GitBasedPlacementDetails
            ?: return false

        return placementDetails.remote != null
    }

    private fun updateIssuePlacementDetails() {
        val placementDetails = model.placementDetails as? GitBasedPlacementDetails ?: return
        placementDetails.remote = gitRemotePicker.selectedItem as? GitRemote
    }

    override fun rememberUserChoice() {
        // TODO[#38]: Remember last selected account
    }
}
