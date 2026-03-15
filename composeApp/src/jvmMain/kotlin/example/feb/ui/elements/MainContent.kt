package example.feb.ui.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.VerticalAlignBottom
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import example.feb.presentation.ContentStats
import example.feb.presentation.ZoomUiState
import example.feb.ui.AppColors
import example.feb.ui.AppShapes
import java.util.UUID


@Composable
fun MainContent(
    selectedId: UUID?,
    title: String,
    content: String,
    onContentChange: (UUID, String) -> Unit,
    onPlainTextChange: (String) -> Unit,
    colors: AppColors,
    contentStats: ContentStats,
    onCloseChapter: () -> Unit,

    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,

    isToolbarVisible: Boolean,
    onToggleToolbar: () -> Unit,

    zoom:  ZoomUiState,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,

) {

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {

        if (selectedId == null) {
            Text(text = "Add or Select a chapter", style = MaterialTheme.typography.headlineMedium)
            return@Box
        }

        Surface(color = colors.grayedTextColor) {

            Column(modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            )

            {

                // ── HEADER ────────────────d───────────────────────────────────────────────────────────────────────────────

                Surface(modifier = Modifier
                            .height(64.dp),
                        color = colors.sidebarColor,
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                ) {

                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = colors.activeTextColor
                                )
                            }

                        if (!isToolbarVisible) {
                            Surface(modifier = Modifier,
                                    shape = AppShapes.rounded12,
                                    color = colors.sidebarColor
                            ) {
                                IconButton(onClick = onToggleToolbar) {
                                    Icon(
                                        imageVector = Icons.Outlined.VerticalAlignBottom,
                                        contentDescription = "show toolbar",
                                        tint = colors.whiteColor,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }

                        Surface(modifier = Modifier,
                                shape = AppShapes.rounded12,
                                color = colors.sidebarColor
                        ) {
                            IconButton(onClick = onCloseChapter){
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "close chapter",
                                    tint = colors.whiteColor,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                        // disabled temporarily, more color work needed.

    //                Surface(modifier = Modifier.padding(horizontal = 10.dp), shape = AppShapes.rounded12){
    //                    IconButton(onClick = onToggleTheme){
    //                        Icon(
    //                            imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
    //                            contentDescription = "toggle theme",
    //                            tint = colors.blackColor,
    //                            modifier = Modifier.size(36.dp)
    //                        )
    //                    }
    //                }

                }


                // ── EDITOR AREA ──────────────────────────────────────────────────────────────────────────────────────

                Surface(
                    modifier = Modifier.weight(1f, fill = true).fillMaxWidth(),
                )
                {
                    ChapterRichEditor(
                        chapterId = selectedId,
                        storedMarkdown = content,
                        onMarkdownChange = onContentChange,
                        onPlainTextChange = onPlainTextChange,
                        isToolbarVisible = isToolbarVisible,
                        colors = colors,
                        onToggleToolbar = onToggleToolbar,
                        zoom = zoom,
                        onZoomIn  = onZoomIn,
                        onZoomOut = onZoomOut,
                    )
                }

                // ── STATS AREA ───────────────────────────────────────────────────────────────────────────────────────

                Surface(modifier = Modifier
                            .fillMaxWidth(),
                        color = colors.sidebarColor,
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 8.dp,
                            bottomEnd = 8.dp)
                ) {
                    Row(modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically ) {

                        Text(
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            text = "Characters: " + contentStats.chars.toString(),
                            fontSize = 12.sp,
                            color = colors.grayedTextColor,
                            overflow = TextOverflow.Ellipsis,
                        )

                        VerticalDivider(color = colors.dividerColor)

                        Text(
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            text = "Words: " + contentStats.words.toString(),
                            fontSize = 12.sp,
                            color = colors.grayedTextColor,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }


            }

        }

    }
}