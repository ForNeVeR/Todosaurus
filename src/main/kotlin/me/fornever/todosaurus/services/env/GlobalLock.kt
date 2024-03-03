package me.fornever.todosaurus.services.env

import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service

interface GlobalLock {
    suspend fun <T> withReadLock(block: () -> T): T
}

@Service
class IntelliJGlobalLock : GlobalLock {

    companion object {
        fun getInstance(): IntelliJGlobalLock = IntelliJGlobalLock()
    }

    override suspend fun <T> withReadLock(block: () -> T): T = readAction(block)
}
