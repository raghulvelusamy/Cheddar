package co.adrianblan.storyfeed

import androidx.compose.Composable
import androidx.compose.remember
import androidx.ui.core.*
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.geometry.Offset
import androidx.ui.layout.*
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.res.stringResource
import androidx.ui.unit.IntPxPosition
import androidx.ui.unit.dp
import co.adrianblan.hackernews.StoryType
import co.adrianblan.ui.AppTheme

@Composable
fun StoryTypePopup(
    onStoryTypeClick: (StoryType) -> Unit,
    onDismiss: () -> Unit
) {

    DropdownPopup(
        popupProperties = PopupProperties(true, onDismissRequest = onDismiss)
    ) {
        // Popups require reapplication of the app theme
        AppTheme {
            Container(
                padding = EdgeInsets(20.dp),
                constraints = DpConstraints(maxWidth = 200.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colors().background,
                    elevation = 4.dp
                ) {
                    Column {
                        val storyTypes = remember { StoryType.values() }

                        storyTypes
                            .map { storyType ->
                                StoryTypePopupItem(
                                    storyType
                                ) {
                                    onStoryTypeClick(storyType)
                                    onDismiss()
                                }
                            }
                    }
                }
            }
        }
    }
}

@Composable
fun StoryTypePopupItem(storyType: StoryType, onClick: (StoryType) -> Unit) {
    Ripple(bounded = true) {
        Clickable(onClick = { onClick(storyType) }) {
            Container(
                modifier = LayoutWidth.Fill,
                alignment = Alignment.BottomLeft
            ) {
                Text(
                    text = stringResource(storyType.titleStringResource()),
                    style = MaterialTheme.typography().h6,
                    modifier = LayoutPadding(left = 12.dp, right = 12.dp, top = 8.dp, bottom = 6.dp)
                )
            }
        }
    }
}