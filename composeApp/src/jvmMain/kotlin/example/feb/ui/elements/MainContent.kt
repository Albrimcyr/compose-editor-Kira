package example.feb.ui.elements

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.TextDecrease
import androidx.compose.material.icons.outlined.TextIncrease
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import example.feb.ui.AppColors
import example.feb.ui.AppShapes
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.UUID
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.isUnspecified
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import example.feb.domain.text.ContentStats
import org.jetbrains.skia.Surface


@Composable
fun MainContent(
    selectedId: UUID?,
    title: String,
    content: String, // normalized HTML
    onContentChange: (UUID, String) -> Unit,
    isDarkTheme: Boolean,
    colors: AppColors,
    onToggleTheme: () -> Unit,
    contentStats: ContentStats,
    onCloseChapter: () -> Unit,
) {

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        if (selectedId == null) {
            Text(text = "Select a chapter")
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
                        color = colors.sidebarColor)
                {

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


                // ── EDITOR AREA ──────────────────────────────────────────────────────────────────────────────────────────

                Surface(
                    modifier = Modifier.weight(1f, fill = true).fillMaxWidth(),
                )
                {
                    ChapterRichEditor(
                        chapterId = selectedId,
                        storedHtml = content,
                        onHtmlChange = onContentChange,
                        colors = colors,
                    )
                }

                // ── STATS AREA ───────────────────────────────────────────────────────────────────────────────────────────

                Surface(modifier = Modifier.fillMaxWidth(), color = colors.extraColor ) {
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