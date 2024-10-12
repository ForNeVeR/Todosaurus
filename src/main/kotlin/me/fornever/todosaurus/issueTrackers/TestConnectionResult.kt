package me.fornever.todosaurus.issueTrackers

open class TestConnectionResult {
    class Success : TestConnectionResult()

    class Failed(val reason: String? = "Unexpected error") : TestConnectionResult()
}
