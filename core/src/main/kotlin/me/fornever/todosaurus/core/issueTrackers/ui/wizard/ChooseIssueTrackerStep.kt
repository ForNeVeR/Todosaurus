// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issueTrackers.ui.wizard

import com.intellij.collaboration.api.ServerPath
import com.intellij.openapi.application.EDT
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.tasks.config.TaskRepositoriesConfigurable
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.JBColor
import com.intellij.ui.UserActivityWatcher
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.ComponentPredicate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.fornever.todosaurus.core.TodosaurusCoreBundle
import me.fornever.todosaurus.core.issueTrackers.IssueTracker
import me.fornever.todosaurus.core.issueTrackers.IssueTrackerCredentials
import me.fornever.todosaurus.core.issueTrackers.IssueTrackerProvider
import me.fornever.todosaurus.core.issueTrackers.TestConnectionResult
import me.fornever.todosaurus.core.issueTrackers.anonymous.AnonymousCredentials
import me.fornever.todosaurus.core.issueTrackers.ui.controls.IssueTrackerComboBox
import me.fornever.todosaurus.core.issueTrackers.ui.controls.IssueTrackerCredentialsComboBox
import me.fornever.todosaurus.core.issueTrackers.ui.controls.ServerHostComboBox
import me.fornever.todosaurus.core.ui.wizard.DynamicStepProvider
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardContext
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardStep
import me.fornever.todosaurus.core.ui.wizard.memoization.MemorableStep
import java.awt.event.ItemEvent
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel

class ChooseIssueTrackerStep(private val project: Project, private val scope: CoroutineScope, private val model: TodosaurusWizardContext)
    : TodosaurusWizardStep(), DynamicStepProvider, MemorableStep {

    override val id: String = ChooseIssueTrackerStep::class.java.name

    private var issueTrackerPicker: IssueTrackerComboBox = IssueTrackerComboBox()
    private var serverHostPicker: ServerHostComboBox = ServerHostComboBox()
    private var credentialsPicker: IssueTrackerCredentialsComboBox = IssueTrackerCredentialsComboBox()
    private var connectAnonymouslyCheckBox: JBCheckBox = JBCheckBox(TodosaurusCoreBundle.message("wizard.steps.chooseIssueTracker.account.connectAnonymously"))
    private var testConnectionResultLabel: JLabel = JLabel()
    private lateinit var testConnectionButton: JButton

    private var cachedServerPaths: Map<String, ServerPath>? = null
    private var cachedCredentials: Map<String, List<IssueTrackerCredentials>?>? = null

    init {
        credentialsPicker.addItemListener {
            if (it.stateChange == ItemEvent.SELECTED)
                clearTestConnectionResult()
        }

        serverHostPicker.addItemListener {
            if (it.stateChange == ItemEvent.SELECTED)
                updateCredentialsPicker(it.item as? String)
        }

        issueTrackerPicker.addItemListener {
            if (it.stateChange == ItemEvent.SELECTED)
                updateServerHostsPicker(it.item as? IssueTracker)
        }

        updateIssueTrackers()
        updateConnectionDetails()
    }

    override fun getComponent(): JComponent = panel {
        panel {
            row {
                label(TodosaurusCoreBundle.message("wizard.steps.chooseIssueTracker.issueTracker.title"))
            }

            row {
                issueTrackerPicker.also {
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
                comment(TodosaurusCoreBundle.message("wizard.steps.chooseIssueTracker.issueTracker.description"))
            }
        }

        // For some reason `label` cannot be hidden using `visibleIf` if they are attached to `serverHostPicker` row
        panel {
            row {
                label(TodosaurusCoreBundle.message("wizard.steps.chooseIssueTracker.serverHost.title"))
            }

            row {
                serverHostPicker.also {
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
                link(TodosaurusCoreBundle.message("wizard.steps.chooseIssueTracker.serverHost.notFound.link")) {
                    if (tryAddServer())
                        updateServerHostsPicker(issueTrackerPicker.selectedItem as? IssueTracker)
                }
            }
        }
        .visibleIf(object : ComponentPredicate() {
            override fun addListener(listener: (Boolean) -> Unit)
                = issueTrackerPicker.addItemListener {
                    listener(invoke())
                }

            override fun invoke(): Boolean
                = model.connectionDetails.issueTracker != null
        })

        panel {
            row {
                label(TodosaurusCoreBundle.message("wizard.steps.chooseIssueTracker.account.title"))
            }

            row {
                credentialsPicker.also {
                    cell(it)
                        .enabledIf(object : ComponentPredicate() {
                            override fun addListener(listener: (Boolean) -> Unit) {
                                it.addItemListener {
                                    listener(invoke())
                                }

                                connectAnonymouslyCheckBox.addActionListener {
                                    listener(invoke())
                                }
                            }

                            override fun invoke(): Boolean
                                = it.itemCount != 0 && !connectAnonymouslyCheckBox.isSelected
                        })
                        .align(Align.FILL)
                }
            }

            row {
                cell(connectAnonymouslyCheckBox)
            }
            .enabledIf(object : ComponentPredicate() {
                override fun addListener(listener: (Boolean) -> Unit)
                    = credentialsPicker.addItemListener {
                        listener(invoke())
                    }

                override fun invoke(): Boolean
                    = model.connectionDetails.isComplete()
            })
        }
        .visibleIf(object : ComponentPredicate() {
            override fun addListener(listener: (Boolean) -> Unit)
                = serverHostPicker.addItemListener {
                    listener(invoke())
                }

            override fun invoke(): Boolean
                = model.connectionDetails.issueTracker != null && model.connectionDetails.serverHost != null
        })

        row {
            testConnectionButton = button(TodosaurusCoreBundle.message("wizard.steps.chooseIssueTracker.testConnection.title")) {
                val button = it.source as? JButton ?: return@button

                val defaultIcon = button.icon
                button.icon = AnimatedIcon.Default()
                issueTrackerPicker.isEnabled = false
                serverHostPicker.isEnabled = false
                credentialsPicker.isEnabled = false
                connectAnonymouslyCheckBox.isEnabled = false
                button.isEnabled = false

                scope.launch(Dispatchers.IO) {
                    val result = checkConnection()

                    withContext(Dispatchers.EDT) {
                        if (result is TestConnectionResult.Failed) {
                            testConnectionResultLabel.text = result.reason ?: TodosaurusCoreBundle.message("wizard.steps.chooseIssueTracker.testConnection.unexpectedError")
                            testConnectionResultLabel.foreground = JBColor.RED
                        }
                        else {
                            testConnectionResultLabel.text = TodosaurusCoreBundle.message("wizard.steps.chooseIssueTracker.testConnection.success")
                            testConnectionResultLabel.foreground = JBColor.GREEN
                        }

                        button.icon = defaultIcon
                        issueTrackerPicker.isEnabled = true
                        serverHostPicker.isEnabled = true
                        credentialsPicker.isEnabled = !connectAnonymouslyCheckBox.isSelected
                        connectAnonymouslyCheckBox.isEnabled = true
                        button.isEnabled = true
                    }
                }
            }
            .visibleIf(object : ComponentPredicate() {
                override fun addListener(listener: (Boolean) -> Unit) {
                    credentialsPicker.addItemListener {
                        listener(invoke())
                    }
                }

                override fun invoke(): Boolean
                    = model.connectionDetails.isComplete()
            })
            .enabledIf(object : ComponentPredicate() {
                override fun addListener(listener: (Boolean) -> Unit) {
                    connectAnonymouslyCheckBox.addActionListener {
                        listener(invoke())
                    }
                }

                override fun invoke(): Boolean
                    = !connectAnonymouslyCheckBox.isSelected
            })
            .component
        }

        row {
            cell(testConnectionResultLabel)
        }
    }
    .also {
        UserActivityWatcher().also { watcher ->
            watcher.register(it)
            watcher.addUserActivityListener {
                updateConnectionDetails()
                fireStateChanged()
            }
        }
    }

    override fun getPreferredFocusedComponent(): JComponent
        = issueTrackerPicker

    override fun isComplete(): Boolean
        = model.connectionDetails.isComplete()

    private suspend fun checkConnection(): TestConnectionResult {
        val issueTracker = model.connectionDetails.issueTracker ?: return TestConnectionResult.Failed("Issue tracker not selected")
        val account = model.connectionDetails.credentials ?: return TestConnectionResult.Failed("Credentials not selected")
        return issueTracker.checkConnection(account)
    }

    private fun updateConnectionDetails() {
        model.connectionDetails.issueTracker = issueTrackerPicker.selectedItem as? IssueTracker
        model.connectionDetails.serverHost = serverHostPicker.selectedItem as? String

        val serverPath = cachedServerPaths?.getOrDefault(model.connectionDetails.serverHost, null)

        when {
            connectAnonymouslyCheckBox.isSelected && serverPath != null -> model.connectionDetails.credentials = AnonymousCredentials(serverPath)
            else -> model.connectionDetails.credentials = credentialsPicker.selectedItem as? IssueTrackerCredentials
        }
    }

    private fun tryAddServer(): Boolean {
        val configurable = TaskRepositoriesConfigurable(project)
        return ShowSettingsUtil.getInstance().editConfigurable(project, configurable)
    }

    private fun updateIssueTrackers()
        = IssueTrackerProvider
            .provideAll()
            .forEach {
                issueTrackerPicker.addItem(it)
            }

    private fun updateCredentialsPicker(serverHost: String?) {
        if (serverHost == null)
            return

        credentialsPicker.removeAllItems()
        clearTestConnectionResult()

        cachedCredentials
            ?.getValue(serverHost)
            ?.forEach {
                credentialsPicker.addItem(it)
            }
    }

    private fun updateServerHostsPicker(issueTracker: IssueTracker?) {
        if (issueTracker == null)
            return

        serverHostPicker.removeAllItems()
        clearTestConnectionResult()

        scope.launch(Dispatchers.IO) {
            // TODO[#135]: Add loading spinner for "serverHostPicker"
            val credentials = issueTracker.createCredentialsProvider(project).provideAll()

            withContext(Dispatchers.EDT) {
                cachedServerPaths = credentials.associate {
                    it.serverPath.toURI().host to it.serverPath
                }

                cachedCredentials = credentials.groupBy {
                    it.serverPath.toURI().host
                }

                cachedServerPaths?.forEach {
                    serverHostPicker.addItem(it.key)
                }
            }
        }
    }

    private fun clearTestConnectionResult() {
        testConnectionResultLabel.text = ""
    }

    override fun createDynamicStep(): TodosaurusWizardStep
        = model.connectionDetails.issueTracker?.createChooseRemoteStep(project, model)
                ?: error("Cannot create specific step for ${model.connectionDetails.issueTracker?.title}")
}
