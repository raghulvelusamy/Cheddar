package co.adrianblan.storydetail.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
import co.adrianblan.common.urlSiteName
import co.adrianblan.model.Story
import co.adrianblan.model.StoryUrl
import co.adrianblan.model.WebPreviewData
import co.adrianblan.model.WebPreviewState
import co.adrianblan.model.placeholder
import co.adrianblan.model.placeholderLink
import co.adrianblan.model.placeholderPost
import co.adrianblan.storydetail.StoryDetailViewState
import co.adrianblan.ui.AppTheme
import co.adrianblan.ui.ShimmerView
import co.adrianblan.ui.StoryImage
import co.adrianblan.ui.utils.lerp
import kotlin.math.roundToInt


@Composable
internal fun StoryDetailToolbar(
    viewState: StoryDetailViewState,
    collapsedFraction: () -> Float,
    onStoryContentClick: (StoryUrl) -> Unit,
    onBackPressed: () -> Unit
) {

    Box {
        IconButton(
            onClick = onBackPressed,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = null
            )
        }

        when (viewState) {
            is StoryDetailViewState.Loading -> LoadingToolbar(modifier = Modifier.padding(start = 56.dp))
            is StoryDetailViewState.Success -> {
                SuccessToolbar(
                    story = viewState.story,
                    webPreviewState = viewState.webPreviewState,
                    collapsedFraction = collapsedFraction,
                    onStoryContentClick = onStoryContentClick
                )
            }

            is StoryDetailViewState.Error -> {}
        }
    }
}

@Composable
private fun LoadingToolbar(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        ShimmerView(
            Modifier
                .fillMaxWidth()
                .height(16.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        ShimmerView(
            Modifier
                .fillMaxWidth()
                .height(16.dp)
        )
    }
}

@Composable
private fun SuccessToolbar(
    story: Story,
    webPreviewState: WebPreviewState?,
    collapsedFraction: () -> Float,
    onStoryContentClick: (StoryUrl) -> Unit,
) {

    val webPreview: WebPreviewData? = (webPreviewState as? WebPreviewState.Success)
        ?.webPreview

    MotionLayout(
        motionScene =
        MotionScene {
            val titleRef = createRefFor("title")
            val subtitleRef = createRefFor("subtitle")
            val imageRef = createRefFor("image")
            defaultTransition(
                from = constraintSet {
                    createVerticalChain(
                        titleRef.withChainParams(topMargin = 56.dp, bottomMargin = 4.dp),
                        subtitleRef.withChainParams(bottomMargin = 8.dp),
                        chainStyle = ChainStyle.Packed(0.5f)
                    )
                    createHorizontalChain(
                        titleRef.withChainParams(
                            startMargin = 16.dp,
                            endMargin = 16.dp,
                            endGoneMargin = 16.dp
                        ),
                        imageRef.withChainParams(endMargin = 8.dp),
                        chainStyle = ChainStyle.SpreadInside
                    )
                    constrain(titleRef) {
                        width = Dimension.fillToConstraints
                    }
                    constrain(subtitleRef) {
                        start.linkTo(titleRef.start)
                        end.linkTo(titleRef.end)
                        width = Dimension.fillToConstraints
                    }
                    constrain(imageRef) {
                        start.linkTo(titleRef.end)
                        top.linkTo(parent.top, margin = 56.dp)
                        bottom.linkTo(parent.bottom, margin = 8.dp)
                    }
                },
                to = constraintSet {
                    val toolbarMiddleGuideline = createGuidelineFromTop(56.dp / 2)
                    createVerticalChain(
                        toolbarMiddleGuideline.reference,
                        titleRef.withChainParams(topMargin = 8.dp, bottomGoneMargin = 8.dp),
                        subtitleRef.withChainParams(bottomMargin = 8.dp),
                        toolbarMiddleGuideline.reference,
                        chainStyle = ChainStyle.Packed(0f)
                    )
                    createHorizontalChain(
                        titleRef.withChainParams(startMargin = 72.dp, endMargin = 16.dp),
                        imageRef.withChainParams(endMargin = 8.dp),
                        chainStyle = ChainStyle.SpreadInside
                    )
                    constrain(titleRef) {
                        width = Dimension.fillToConstraints
                    }
                    constrain(subtitleRef) {
                        start.linkTo(titleRef.start)
                        end.linkTo(titleRef.end)
                        width = Dimension.fillToConstraints
                    }
                    constrain(imageRef) {
                        top.linkTo(toolbarMiddleGuideline, margin = 8.dp)
                        bottom.linkTo(toolbarMiddleGuideline, margin = 8.dp)
                    }
                }
            )
        },
        progress = collapsedFraction(),
        modifier = Modifier.fillMaxSize()
    ) {


        val titleMaxLines: Int = lerp(3f, 1f, collapsedFraction()).roundToInt()

        Text(
            text = story.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = titleMaxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.layoutId("title")
        )

        val siteName: String? = webPreview?.siteName ?: story.url?.urlSiteName()

        if (siteName != null) {
            Text(
                text = siteName,
                style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onPrimary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.layoutId("subtitle")
            )
        }

        val imageSize = lerp(80.dp, 40.dp, collapsedFraction())
        val storyUrl: StoryUrl? = story.url
        if (storyUrl != null && webPreviewState != null) {
            val onImageClick = remember(storyUrl) { { onStoryContentClick(storyUrl) } }
            StoryImage(
                webPreviewState = webPreviewState,
                onClick = onImageClick,
                modifier = Modifier
                    .size(imageSize)
                    .layoutId("image")
            )
        }
    }
}

@Preview
@Composable
private fun LoadingToolbarPreview() {
    AppTheme {
        LoadingToolbar()
    }
}

@Preview("Toolbar expanded")
@Composable
private fun SuccessToolbarExpandedPreview() {
    AppTheme {
        Box(modifier = Modifier.height(140.dp)) {
            SuccessToolbar(
                story = Story.placeholderLink,
                webPreviewState = WebPreviewState.Success(WebPreviewData.placeholder),
                collapsedFraction = { 0.0f },
                onStoryContentClick = {},
            )
        }
    }
}

@Preview("Toolbar collapsed")
@Composable
private fun SuccessToolbarCollapsedPreview() {
    AppTheme {
        Box(modifier = Modifier.height(56.dp)) {
            SuccessToolbar(
                story = Story.placeholderLink,
                webPreviewState = WebPreviewState.Success(WebPreviewData.placeholder),
                collapsedFraction = { 1.0f },
                onStoryContentClick = {},
            )
        }
    }
}

@Preview("Toolbar collapsed")
@Composable
private fun SuccessToolbarCollapsedPostPreview() {
    AppTheme {
        Box(modifier = Modifier.height(56.dp)) {
            SuccessToolbar(
                story = Story.placeholderPost,
                webPreviewState = null,
                collapsedFraction = { 1.0f },
                onStoryContentClick = {},
            )
        }
    }
}