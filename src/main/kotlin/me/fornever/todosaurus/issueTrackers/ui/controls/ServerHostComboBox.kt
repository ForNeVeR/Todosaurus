package me.fornever.todosaurus.issueTrackers.ui.controls

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import javax.swing.JList

class ServerHostComboBox : ComboBox<String>() {
    init {
        renderer = object : SimpleListCellRenderer<String>() {
            override fun customize(list: JList<out String>, value: String?, index: Int, selected: Boolean, focused: Boolean) {
                if (value == null)
                    return

                text = value
            }
        }
    }
}
