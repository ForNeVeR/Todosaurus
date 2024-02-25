package me.fornever.todosaurus.models

import com.intellij.openapi.editor.RangeMarker
import org.jetbrains.plugins.github.authentication.accounts.GithubAccount

data class CreateIssueModel(
    val selectedRepository: RepositoryModel?,
    val selectedAccount: GithubAccount?,
    val title: String,
    val description: String,
    val textRangeMarker: RangeMarker
)
