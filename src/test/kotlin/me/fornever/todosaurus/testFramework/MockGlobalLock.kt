package me.fornever.todosaurus.testFramework

import me.fornever.todosaurus.services.env.GlobalLock

class MockGlobalLock : GlobalLock {
    override suspend fun <T> withReadLock(block: () -> T): T = block()
}
