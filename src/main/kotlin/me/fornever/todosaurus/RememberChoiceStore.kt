package me.fornever.todosaurus

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

// TODO[#38]: Remember last selected account
@Service(Service.Level.PROJECT)
class RememberChoiceStore {
    companion object {
        fun getInstance(project: Project): RememberChoiceStore = project.service()
    }

    suspend fun remember() {

    }

    suspend fun forget() {

    }
}
