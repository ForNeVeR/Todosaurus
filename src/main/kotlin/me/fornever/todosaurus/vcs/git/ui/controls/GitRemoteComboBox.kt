package me.fornever.todosaurus.vcs.git.ui.controls

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import me.fornever.todosaurus.vcs.git.GitRemote
import javax.swing.JList

class GitRemoteComboBox : ComboBox<GitRemote>() {
    init {
        renderer = object : SimpleListCellRenderer<GitRemote?>() {
            override fun customize(
                list: JList<out GitRemote?>,
                value: GitRemote?,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean
            ) {
                text = value?.ownerAndName
            }
        }
    }
}
