package me.fornever.todosaurus.issueTrackers

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import me.fornever.todosaurus.issueTrackers.gitHub.GitHub
import me.fornever.todosaurus.issueTrackers.gitHub.GitHubCredentialsProvider
import me.fornever.todosaurus.issueTrackers.youTrack.YouTrack
import me.fornever.todosaurus.issueTrackers.youTrack.YouTrackCredentialsProvider

@Service(Service.Level.PROJECT)
class IssueTrackerCredentialsProviderFactory(private val project: Project) {
    companion object {
        fun getInstance(project: Project): IssueTrackerCredentialsProviderFactory = project.service()
    }

    fun create(issueTracker: IssueTracker): IssueTrackerCredentialsProvider
        = when (issueTracker) {
            is GitHub -> GitHubCredentialsProvider(project)
            is YouTrack -> YouTrackCredentialsProvider()
            else -> error("Issue tracker ${issueTracker.title} not supported")
        }
}
