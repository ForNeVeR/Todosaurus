package me.fornever.todosaurus

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class RememberChoiceService {
    companion object {
        fun getInstance(project: Project): RememberChoiceService = project.service()
    }

    suspend fun remember() {

    }

    suspend fun forget() {

    }
}
