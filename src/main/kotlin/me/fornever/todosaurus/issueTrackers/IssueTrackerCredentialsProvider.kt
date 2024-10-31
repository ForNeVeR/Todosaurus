package me.fornever.todosaurus.issueTrackers

interface IssueTrackerCredentialsProvider {
    suspend fun provideAll(): Array<IssueTrackerCredentials>

    suspend fun provide(id: String): IssueTrackerCredentials?
}
