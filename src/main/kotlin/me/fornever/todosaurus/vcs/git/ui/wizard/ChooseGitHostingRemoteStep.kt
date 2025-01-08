// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.vcs.git.ui.wizard

import com.intellij.openapi.project.Project
import com.intellij.ui.UserActivityWatcher
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.ComponentPredicate
import git4idea.GitUtil
import git4idea.remote.GitConfigureRemotesDialog
import me.fornever.todosaurus.TodosaurusBundle
import me.fornever.todosaurus.ui.wizard.MemorableStep
import me.fornever.todosaurus.ui.wizard.TodosaurusWizardContext
import me.fornever.todosaurus.ui.wizard.TodosaurusWizardStep
import me.fornever.todosaurus.vcs.git.GitBasedPlacementDetails
import me.fornever.todosaurus.vcs.git.GitHostingRemote
import me.fornever.todosaurus.vcs.git.GitHostingRemoteProvider
import me.fornever.todosaurus.vcs.git.ui.controls.GitHostingRemoteComboBox
import javax.swing.JComponent

class ChooseGitHostingRemoteStep(private val project: Project, private val model: TodosaurusWizardContext) : TodosaurusWizardStep(), MemorableStep {
    companion object {
        val id: Any = ChooseGitHostingRemoteStep::class.java
    }

    override val id: Any = Companion.id

    private val gitHostingRemotePicker: GitHostingRemoteComboBox = GitHostingRemoteComboBox()

    override fun _init() {
        super._init()

        model.placementDetails = GitBasedPlacementDetails()

        updateHostingRemotePicker()
    }

    override fun getComponent(): JComponent = panel {
        panel {
            row {
                label(TodosaurusBundle.message("wizard.steps.chooseGitHostingRemote.remoteUrl.title"))
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
                link(TodosaurusBundle.message("wizard.steps.chooseGitHostingRemote.remoteUrl.notFound.link")) {
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

    private fun tryAddRemote(): Boolean
        = GitConfigureRemotesDialog(project, GitUtil.getRepositoryManager(project).repositories)
            .showAndGet()

    private fun updateHostingRemotePicker() {
        val selectedIndex = gitHostingRemotePicker.selectedIndex

        gitHostingRemotePicker.removeAllItems()

        GitHostingRemoteProvider
            .getInstance(project)
            .provideAll(model.connectionDetails)
            .forEach {
                gitHostingRemotePicker.addItem(it)
            }

        if (selectedIndex > -1 && selectedIndex <= gitHostingRemotePicker.itemCount - 1)
            gitHostingRemotePicker.selectedIndex = selectedIndex
    }

    override fun rememberUserChoice() {
        // TODO[#38]: Remember last selected account
    }
}
