// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers.ui.wizard

import com.intellij.collaboration.api.ServerPath
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.JBColor
import com.intellij.ui.UserActivityWatcher
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.ComponentPredicate
import kotlinx.coroutines.*
import me.fornever.todosaurus.TodosaurusBundle
import me.fornever.todosaurus.issueTrackers.*
import me.fornever.todosaurus.issueTrackers.anonymous.AnonymousCredentials
import me.fornever.todosaurus.issueTrackers.ui.controls.IssueTrackerComboBox
import me.fornever.todosaurus.issueTrackers.ui.controls.IssueTrackerCredentialsComboBox
import me.fornever.todosaurus.issueTrackers.ui.controls.ServerHostComboBox
import me.fornever.todosaurus.ui.wizard.DynamicStepProvider
import me.fornever.todosaurus.ui.wizard.MemorableStep
import me.fornever.todosaurus.ui.wizard.TodosaurusContext
import me.fornever.todosaurus.ui.wizard.TodosaurusStep
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel

class ChooseIssueTrackerStep(private val project: Project, private val scope: CoroutineScope, private val model: TodosaurusContext)
    : TodosaurusStep(), DynamicStepProvider, MemorableStep {
    companion object {
        val id: Any = ChooseIssueTrackerStep::class.java
    }

    override val id: Any = Companion.id

    private var issueTrackerPicker: IssueTrackerComboBox = IssueTrackerComboBox()
    private var serverHostPicker: ServerHostComboBox = ServerHostComboBox()
    private var credentialsPicker: IssueTrackerCredentialsComboBox = IssueTrackerCredentialsComboBox()
    private var connectAnonymouslyCheckBox: JBCheckBox = JBCheckBox(TodosaurusBundle.message("wizard.steps.chooseIssueTracker.account.connectAnonymously"))
    private var testConnectionResultLabel: JLabel = JLabel()
    private lateinit var testConnectionButton: JButton

    private var cachedServerPaths: Map<String, ServerPath>? = null
    private var cachedCredentials: Map<String, List<IssueTrackerCredentials>?>? = null

    init {
        credentialsPicker.addItemListener {
            testConnectionResultLabel.text = ""
        }

        serverHostPicker.addItemListener { event ->
            val serverHost = event.item as? String ?: return@addItemListener

            credentialsPicker.removeAllItems()
            testConnectionResultLabel.text = ""

            cachedCredentials
                ?.getValue(serverHost)
                ?.forEach {
                    credentialsPicker.addItem(it)
                }
        }

        issueTrackerPicker.addItemListener { event ->
            val issueTracker = event.item as? IssueTracker ?: return@addItemListener

            serverHostPicker.removeAllItems()
            testConnectionResultLabel.text = ""

            val credentials = runBlocking {
                IssueTrackerCredentialsProviderFactory
                    .getInstance(project)
                    .create(issueTracker)
                    .provideAll()
            }

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

        IssueTrackerProvider
            .getInstance(project)
            .provideAll()
            .forEach {
                issueTrackerPicker.addItem(it)
            }

        updateConnectionDetails()
    }

    override fun getComponent(): JComponent = panel {
		panel {
            row {
                label(TodosaurusBundle.message("wizard.steps.chooseIssueTracker.issueTracker.title"))
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
                comment(TodosaurusBundle.message("wizard.steps.chooseIssueTracker.issueTracker.description"))
            }
        }

        // For some reason `label` cannot be hidden using `visibleIf` if they are attached to `serverHostPicker` row
        panel {
            row {
                label(TodosaurusBundle.message("wizard.steps.chooseIssueTracker.serverHost.title"))
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
                label(TodosaurusBundle.message("wizard.steps.chooseIssueTracker.account.title"))
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
            testConnectionButton = button(TodosaurusBundle.message("wizard.steps.chooseIssueTracker.testConnection.title")) {
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
                            testConnectionResultLabel.text = result.reason ?: TodosaurusBundle.message("wizard.steps.chooseIssueTracker.testConnection.unexpectedError")
                            testConnectionResultLabel.foreground = JBColor.RED
                        }
                        else {
                            testConnectionResultLabel.text = TodosaurusBundle.message("wizard.steps.chooseIssueTracker.testConnection.success")
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

    override fun createDynamicStep(): TodosaurusStep
        = SpecificIssueTrackerStepFactory
            .getInstance(project)
            .create(model)
                ?: error("Cannot create specific step for ${model.connectionDetails.issueTracker?.title}")

    override fun rememberUserChoice() {
        // TODO[#38]: Remember last selected account
    }
}
