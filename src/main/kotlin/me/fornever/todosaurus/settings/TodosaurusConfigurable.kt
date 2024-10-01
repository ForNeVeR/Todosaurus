package me.fornever.todosaurus.settings

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

class TodosaurusConfigurable : Configurable {
    private val settingsDialog: TodosaurusSettingsDialog = TodosaurusSettingsDialog()

    override fun createComponent(): JComponent
        = settingsDialog.createPanel()

    override fun isModified(): Boolean {
        val settings = TodosaurusSettings.getInstance()

        return settingsDialog.numberPattern != settings.state.numberPattern ||
            settingsDialog.descriptionTemplate != settings.state.descriptionTemplate
    }

    override fun apply() {
        val settings = TodosaurusSettings.getInstance()

        settings.state.numberPattern = settingsDialog.numberPattern
        settings.state.descriptionTemplate = settingsDialog.descriptionTemplate
    }

    override fun reset() {
        val settings = TodosaurusSettings.getInstance()

        // TODO: Default values
        settingsDialog.numberPattern = settings.state.numberPattern
        settingsDialog.descriptionTemplate = settings.state.descriptionTemplate
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String
        = "Todosaurus"
}
