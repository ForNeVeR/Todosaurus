package me.fornever.todosaurus.models

import me.fornever.todosaurus.services.ToDoItem
import org.jetbrains.plugins.github.authentication.accounts.GithubAccount

data class GetIssueModel(
	val repository: RepositoryModel?,
    val account: GithubAccount?,
	val toDoItem: ToDoItem
)
