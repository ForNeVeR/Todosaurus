package me.fornever.todosaurus.views

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import java.net.URI
import javax.swing.JList

data class RepositoryModel(val url: URI) {
    val displayName: String
        get() = url.path.removePrefix("/")
}

class RepositoryChooser(repos: Array<RepositoryModel>) : ComboBox<RepositoryModel>(repos) {
    init {
        renderer = object : SimpleListCellRenderer<RepositoryModel?>() {
            override fun customize(
                list: JList<out RepositoryModel?>,
                value: RepositoryModel?,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean
            ) {
                text = value?.displayName
            }
        }
    }
}
