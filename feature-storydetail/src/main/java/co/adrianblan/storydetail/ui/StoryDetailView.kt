package co.adrianblan.storydetail.ui

import android.net.Uri
import androidx.compose.Composable
import androidx.compose.key
import androidx.compose.remember
import androidx.ui.core.Alignment
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Modifier
import androidx.ui.foundation.*
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.layout.*
import androidx.ui.material.IconButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.ArrowBack
import androidx.ui.material.ripple.RippleIndication
import androidx.ui.res.colorResource
import androidx.ui.res.stringResource
import androidx.ui.text.style.TextAlign
import androidx.ui.text.style.TextOverflow
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.Dp
import androidx.ui.unit.TextUnit
import androidx.ui.unit.dp
import androidx.ui.unit.lerp
import co.adrianblan.common.urlSiteName
import co.adrianblan.core.WebPreviewState
import co.adrianblan.domain.Comment
import co.adrianblan.domain.Story
import co.adrianblan.domain.StoryUrl
import co.adrianblan.domain.placeholder
import co.adrianblan.storydetail.*
import co.adrianblan.storydetail.R
import co.adrianblan.ui.*
import co.adrianblan.webpreview.WebPreviewData

private const val toolbarMinHeightDp = 56
private const val toolbarMaxHeightDp = 148


@Composable
fun StoryDetailView(
    viewState: StoryDetailViewState,
    onStoryContentClick: (StoryUrl) -> Unit,
    onCommentUrlClicked: (Uri) -> Unit,
    onBackPressed: () -> Unit
) {

    val scroller = ScrollerPosition()

    CollapsingScaffold(
        scroller = scroller,
        minHeight = toolbarMinHeightDp.dp,
        maxHeight = toolbarMaxHeightDp.dp,
        toolbarContent = { collapsedFraction, height ->
            StoryDetailToolbar(
                viewState = viewState,
                collapsedFraction = collapsedFraction,
                height = height,
                onStoryContentClick = onStoryContentClick,
                onBackPressed = onBackPressed
            )
        },
        bodyContent = {

            when (viewState) {
                is StoryDetailViewState.Success -> {

                    val story = viewState.story

                    when (viewState.commentsState) {
                        is StoryDetailCommentsState.Success ->
                            VerticalScroller(scroller) {
                                Column {

                                    val topInsets =
                                        with(DensityAmbient.current) {
                                            InsetsAmbient.current.top.toDp()
                                        }

                                    Spacer(
                                        modifier = Modifier.preferredHeight(
                                            toolbarMaxHeightDp.dp + topInsets
                                        )
                                    )

                                    if (viewState.story.text != null) {
                                        CommentItem(
                                            text = story.text,
                                            by = story.by,
                                            depthIndex = 0,
                                            storyAuthor = story.by,
                                            onCommentUrlClicked = onCommentUrlClicked
                                        )
                                    }

                                    viewState.commentsState.comments
                                        .map { comment ->
                                            key(comment.comment.id) {
                                                CommentItem(
                                                    comment = comment,
                                                    storyAuthor = story.by,
                                                    onCommentUrlClicked = onCommentUrlClicked
                                                )
                                            }
                                        }

                                    with(DensityAmbient.current) {
                                        Spacer(modifier = Modifier.preferredHeight(InsetsAmbient.current.bottom.toDp() + 8.dp))
                                    }
                                }
                            }
                        is StoryDetailCommentsState.Empty ->
                            CommentsEmptyView()
                        is StoryDetailCommentsState.Loading ->
                            LoadingView()
                        is StoryDetailCommentsState.Error ->
                            ErrorView()
                    }
                }

                is StoryDetailViewState.Loading -> LoadingView()
                is StoryDetailViewState.Error -> ErrorView()
            }
        }
    )
}

@Composable
fun StoryDetailToolbar(
    viewState: StoryDetailViewState,
    collapsedFraction: Float,
    height: Dp,
    onStoryContentClick: (StoryUrl) -> Unit,
    onBackPressed: () -> Unit
) {

    Stack {
        IconButton(
            onClick = onBackPressed,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                asset = Icons.Default.ArrowBack,
                tint = MaterialTheme.colors.onBackground
            )
        }

        val titleCollapsedLeftOffset =
            remember(collapsedFraction) { lerp(0.dp, 48.dp, collapsedFraction) }
        val titleCollapsedTopOffset =
            remember(collapsedFraction) { lerp(48.dp, 0.dp, collapsedFraction) }

        val titleFontSize: TextUnit =
            lerp(
                MaterialTheme.typography.h6.fontSize,
                MaterialTheme.typography.subtitle1.fontSize,
                collapsedFraction
            )

        val titleMaxLines = remember(collapsedFraction) { if (collapsedFraction >= 0.85f) 1 else 3 }
        val imageSize = remember(collapsedFraction) { lerp(80.dp, 40.dp, collapsedFraction) }

        when (viewState) {
            is StoryDetailViewState.Loading ->
                Surface(
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.padding(16.dp)
                        .padding(top = titleCollapsedTopOffset)
                ) {
                    Column {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .preferredHeight(20.dp)
                        ) {
                            ShimmerView()
                        }
                        Spacer(modifier = Modifier.preferredHeight(6.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .preferredHeight(20.dp)
                        ) {
                            ShimmerView()
                        }
                    }
                }
            is StoryDetailViewState.Success -> {

                val story: Story = viewState.story
                val webPreviewState: WebPreviewState? = viewState.webPreviewState
                val webPreview: WebPreviewData? = (webPreviewState as? WebPreviewState.Success)
                    ?.webPreview

                val titleRightOffset =
                    if (story.url != null) imageSize + 12.dp
                    else 0.dp

                Column(
                    modifier = Modifier.padding(
                        start = 16.dp + titleCollapsedLeftOffset,
                        end = 12.dp + titleRightOffset,
                        bottom = 8.dp,
                        top = 8.dp + titleCollapsedTopOffset
                    )
                        .fillMaxWidth()
                        .preferredHeight(height)
                        .gravity(Alignment.CenterStart)
                        .wrapContentHeight(Alignment.CenterVertically)
                ) {

                    Text(
                        text = story.title,
                        style = MaterialTheme.typography.h6.copy(
                            fontSize = titleFontSize,
                            color = MaterialTheme.colors.onBackground
                        ),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = titleMaxLines
                    )

                    val siteName: String? = webPreview?.siteName ?: story.url?.url?.urlSiteName()

                    if (siteName != null) {
                        Text(
                            text = siteName,
                            style = MaterialTheme.typography.subtitle2.copy(
                                color = MaterialTheme.colors.onPrimary.copy(alpha = textSecondaryAlpha)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (story.url != null) {
                    Box(modifier = Modifier.gravity(Alignment.BottomEnd)) {
                        StoryDetailImage(
                            story = story,
                            webPreviewState = webPreviewState,
                            imageSize = imageSize,
                            onStoryContentClick = onStoryContentClick
                        )
                    }
                }
            }
            is StoryDetailViewState.Error -> {
            }
        }
    }
}

@Composable
private fun StoryDetailImage(
    story: Story,
    webPreviewState: WebPreviewState?,
    imageSize: Dp,
    onStoryContentClick: (StoryUrl) -> Unit
) {

    Box(
        modifier = Modifier.clickable(
            onClick = { story.url?.let { onStoryContentClick(it) } }
        )
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(
                start = 8.dp,
                top = 8.dp,
                end = 16.dp,
                bottom = 8.dp
            )
                .preferredSize(imageSize)

        ) {
            Stack {
                Surface(
                    color = colorResource(R.color.contentMuted),
                    modifier = Modifier.fillMaxSize()
                ) {}

                when (webPreviewState) {
                    is WebPreviewState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            ShimmerView()
                        }
                    }
                    is WebPreviewState.Success -> {
                        val webPreview = webPreviewState.webPreview

                        val imageUrl =
                            webPreview.imageUrl ?: webPreview.iconUrl
                            ?: webPreview.favIconUrl

                        UrlImage(imageUrl)
                    }
                    is WebPreviewState.Error -> {
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun StoryDetailPreview() {
    AppTheme {
        val viewState =
            StoryDetailViewState.Success(
                story = Story.placeholder,
                webPreviewState = null,
                commentsState = StoryDetailCommentsState.Success(
                    listOf(
                        FlatComment(Comment.placeholder, 0),
                        FlatComment(Comment.placeholder, 1),
                        FlatComment(Comment.placeholder, 2),
                        FlatComment(Comment.placeholder, 0)
                    )
                )
            )
        StoryDetailView(
            viewState = viewState,
            onStoryContentClick = {},
            onCommentUrlClicked = {},
            onBackPressed = {}
        )
    }
}

@Preview
@Composable
fun CommentsEmptyView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        padding = 32.dp,
        gravity = ContentGravity.Center
    ) {
        Text(
            text = stringResource(id = R.string.comments_empty),
            style = MaterialTheme.typography.h6.copy(textAlign = TextAlign.Center)
        )
    }
}