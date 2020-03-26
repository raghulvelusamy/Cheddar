package co.adrianblan.storydetail

import co.adrianblan.hackernews.api.Comment
import co.adrianblan.hackernews.api.Story

sealed class StoryDetailViewState {
    data class Success(
        val story: Story,
        val commentState: StoryDetailCommentsState
    ) : StoryDetailViewState()

    object Loading : StoryDetailViewState()
    data class Error(val throwable: Throwable) : StoryDetailViewState()
}

// A comment in the comment tree that has been flattened into a list
data class FlatComment(
    val comment: Comment,
    val depthIndex: Int
)

sealed class StoryDetailCommentsState {
    data class Success(val comments: List<FlatComment>) : StoryDetailCommentsState()
    object Empty : StoryDetailCommentsState()
    object Loading : StoryDetailCommentsState()
    object Error : StoryDetailCommentsState()
}