package me.fornever.todosaurus.settings

import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.Converter
import me.fornever.todosaurus.issueTrackers.IssueTrackerConnectionDetails
import me.fornever.todosaurus.vcs.git.GitRemote
import me.fornever.todosaurus.vcs.git.GitRemoteProvider
import java.net.URI

class GitRemoteConverter : Converter<GitRemote>() {
    private val todosaurusSettings: TodosaurusSettings = TodosaurusSettings.getInstance()

    override fun toString(gitRemote: GitRemote?, context: ConvertContext): String? {
        if (gitRemote == null) {
            return null
        }

        return gitRemote.url.toString()
    }

    override fun fromString(gitRemote: String?, context: ConvertContext): GitRemote? {
        if (gitRemote == null) {
            return null
        }

        val url = URI(gitRemote)

        val connectionDetails = IssueTrackerConnectionDetails().also {
            it.issueTracker = todosaurusSettings.state.issueTracker
            it.serverHost = url.host
            it.credentials = todosaurusSettings.state.credentials
        }

        return GitRemoteProvider
            .getInstance(context.project)
            .provide(connectionDetails, url)
    }
}
