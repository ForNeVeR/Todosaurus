// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.git.ui.wizard

import com.intellij.openapi.project.Project
import com.intellij.ui.UserActivityWatcher
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.ComponentPredicate
import git4idea.GitUtil
import git4idea.remote.GitConfigureRemotesDialog
import me.fornever.todosaurus.core.TodosaurusCoreBundle
import me.fornever.todosaurus.core.git.GitBasedPlacementDetails
import me.fornever.todosaurus.core.git.GitHostingRemote
import me.fornever.todosaurus.core.git.GitHostingRemoteProvider
import me.fornever.todosaurus.core.git.ui.controls.GitHostingRemoteComboBox
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardContext
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardStep
import me.fornever.todosaurus.core.ui.wizard.memoization.MemorableStep
import javax.swing.JComponent

class ChooseGitHostingRemoteStep(private val project: Project, private val model: TodosaurusWizardContext<*>) : TodosaurusWizardStep(),
    MemorableStep {

    override val id: String = ChooseGitHostingRemoteStep::class.java.name

    private val gitHostingRemotePicker: GitHostingRemoteComboBox = GitHostingRemoteComboBox()

    override fun _init() {
        super._init()

        model.placementDetails = GitBasedPlacementDetails()

        updateHostingRemotePicker()
    }

    override fun createComponent(): JComponent = panel {
        panel {
            row {
                label(TodosaurusCoreBundle.message("wizard.steps.chooseGitHostingRemote.remoteUrl.title"))
            }

            row {
                gitHostingRemotePicker.also {
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

            row {
                link(TodosaurusCoreBundle.message("wizard.steps.chooseGitHostingRemote.remoteUrl.notFound.link")) {
                    if (tryAddRemote())
                        updateHostingRemotePicker()
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
        = gitHostingRemotePicker

    override fun isComplete(): Boolean {
        val placementDetails = model.placementDetails as? GitBasedPlacementDetails
            ?: return false

        return placementDetails.remote != null
    }

    private fun updateIssuePlacementDetails() {
        val placementDetails = model.placementDetails as? GitBasedPlacementDetails ?: return
        placementDetails.remote = gitHostingRemotePicker.selectedItem as? GitHostingRemote
    }

    private fun tryAddRemote(): Boolean {
        val remotesDialog = GitConfigureRemotesDialog(project, GitUtil.getRepositoryManager(project).repositories)
        return remotesDialog.showAndGet() || !remotesDialog.isShowing
    }

    private fun updateHostingRemotePicker() {
        val selectedItem = gitHostingRemotePicker.selectedItem as? GitHostingRemote

        gitHostingRemotePicker.removeAllItems()

        val gitHostingRemotes = GitHostingRemoteProvider
            .getInstance(project)
            .provideAll(model.connectionDetails)

        gitHostingRemotes
            .forEach {
                gitHostingRemotePicker.addItem(it)
            }

        if (selectedItem != null) {
            // Index of "selectedItem" may be changed after updating
            val selectedIndex = gitHostingRemotes.indexOfFirst { it.ownerAndName == selectedItem.ownerAndName }

            if (selectedIndex != -1)
                gitHostingRemotePicker.selectedIndex = selectedIndex
        }
    }
}
