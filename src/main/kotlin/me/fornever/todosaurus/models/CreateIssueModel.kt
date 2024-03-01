package me.fornever.todosaurus.models

import me.fornever.todosaurus.services.ToDoItem
import org.jetbrains.plugins.github.authentication.accounts.GithubAccount

data class CreateIssueModel(
    val selectedRepository: RepositoryModel?,
    val selectedAccount: GithubAccount?,
    val toDoItem: ToDoItem
)
