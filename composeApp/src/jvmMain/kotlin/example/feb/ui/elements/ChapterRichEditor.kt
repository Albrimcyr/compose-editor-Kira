package example.feb.ui.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.Highlight
import androidx.compose.material.icons.outlined.TextDecrease
import androidx.compose.material.icons.outlined.TextIncrease
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.state.HyphenTextState
import com.denser.hyphen.state.rememberHyphenTextState
import com.denser.hyphen.ui.HyphenBasicTextEditor
import com.denser.hyphen.ui.HyphenStyleConfig
import example.feb.presentation.ZoomUiState
import example.feb.ui.AppColors
import example.feb.ui.AppShapes
import java.util.UUID

private const val BASE_FONT_SP = 16 // move to InterfaceData later? Only 1 file uses it.

// ── EDITOR ───────────────────────────────────────────────────────────────────────────────────────────────────────────
@Composable
fun ChapterRichEditor(
    modifier: Modifier = Modifier,
    chapterId: UUID,
    storedMarkdown: String,
    onMarkdownChange: (UUID, String) -> Unit,
    onPlainTextChange: (String) -> Unit,
    colors: AppColors,

    isToolbarVisible: Boolean,
    onToggleToolbar: () -> Unit,

    zoom:  ZoomUiState
) {

    key(chapterId) {
        val state                   = rememberHyphenTextState(initialText = storedMarkdown)
        val editorFocusRequester    = remember { FocusRequester() }

        val fontSizeSp              = BASE_FONT_SP * zoom.zoomPercent / 100

        val editorStyleConfig       = remember(zoom.zoomPercent) {
            HyphenStyleConfig(
                h1Style = SpanStyle(fontSize = (fontSizeSp + 24).sp),
                h2Style = SpanStyle(fontSize = (fontSizeSp + 18).sp),
                h3Style = SpanStyle(fontSize = (fontSizeSp + 12).sp),
                h4Style = SpanStyle(fontSize = (fontSizeSp + 6).sp),
                h5Style = SpanStyle(fontSize = (fontSizeSp + 3).sp),
                h6Style = SpanStyle(fontSize = (fontSizeSp + 1).sp),
            )
        }

        Column(modifier = modifier) {

            if (isToolbarVisible) {
                Surface(color = colors.toolbarColor) {
                    EditorToolbar(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        state                   = state,
                        colors                  = colors,
                        editorFocusRequester    = editorFocusRequester,
                        onToggleToolbar         = onToggleToolbar,
                        zoom                    = zoom,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                HyphenBasicTextEditor(
                    state               = state,
                    modifier            = Modifier.fillMaxSize().focusRequester(editorFocusRequester),
                    textStyle           = TextStyle(fontSize = fontSizeSp.sp, color = colors.blackColor),
                    cursorBrush         = SolidColor(colors.blackColor),
                    styleConfig         = editorStyleConfig,
                    onTextChange        = onPlainTextChange,
                    onMarkdownChange    = { markdown -> onMarkdownChange(chapterId, markdown) },
                )
            }

        }
    }
}


// ── EDITOR TOOLBAR ───────────────────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun EditorToolbar(
    modifier: Modifier,
    state: HyphenTextState,
    colors: AppColors,
    editorFocusRequester: FocusRequester,
    onToggleToolbar: () -> Unit,
    zoom: ZoomUiState,
) {

    Row(
        modifier                = modifier.heightIn(min = 32.dp),
        horizontalArrangement   = Arrangement.spacedBy(4.dp),
        verticalAlignment       = Alignment.CenterVertically,
    ) {

        // ── ZOOM CONTROL ─────────────────────────────────────────────────────────────────────────────────────────────
        // [ A- | 150% | A+ ]

        Surface(
            color = colors.whiteColor,
            shape = AppShapes.rounded6
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {

                ToolbarIconButton(
                    icon                = Icons.Outlined.TextDecrease,
                    contentDescription  = "decrease editor zoom",
                    colors              = colors,
                    enabled             = !zoom.isAtMin,
                    onClick             = zoom.onDecrease,
                )

                Box(modifier = Modifier.width(45.dp), contentAlignment = Alignment.Center) {
                    Text(text = "${zoom.zoomPercent}%", color = colors.blackColor)
                }

                ToolbarIconButton(
                    icon                = Icons.Outlined.TextIncrease,
                    contentDescription  = "increase editor zoom",
                    colors              = colors,
                    enabled             = !zoom.isAtMax,
                    onClick             = zoom.onIncrease,
                )
            }
        }

        // ── HEADING ──────────────────────────────────────────────────────────────────────────────────────────────────

        ToolbarTextToggleButton(
            label               = headingButtonLabel(state),
            contentDescription  = "cycle heading size",
            isActive            = currentHeading(state) != null,
            colors              = colors,
            onClick             = { cycleHeading(state, editorFocusRequester) },
        )

        // ── INLINE STYLES ────────────────────────────────────────────────────────────────────────────────────────────

        ToolbarToggleButton(
            icon                = Icons.Outlined.FormatBold,
            contentDescription  = "bold",
            isActive            = state.hasStyle(MarkupStyle.Bold),
            colors              = colors,
            onClick             = { toggleStyle(state, MarkupStyle.Bold, editorFocusRequester) },
        )

        ToolbarToggleButton(
            icon                = Icons.Outlined.FormatItalic,
            contentDescription  = "italic",
            isActive            = state.hasStyle(MarkupStyle.Italic),
            colors              = colors,
            onClick             = { toggleStyle(state, MarkupStyle.Italic, editorFocusRequester) },
        )

        ToolbarToggleButton(
            icon                = Icons.Outlined.FormatUnderlined,
            contentDescription  = "underline",
            isActive            = state.hasStyle(MarkupStyle.Underline),
            colors              = colors,
            onClick             = { toggleStyle(state, MarkupStyle.Underline, editorFocusRequester) },
        )

        ToolbarToggleButton(
            icon                = Icons.Outlined.FormatStrikethrough,
            contentDescription  = "strikethrough",
            isActive            = state.hasStyle(MarkupStyle.Strikethrough),
            colors              = colors,
            onClick             = { toggleStyle(state, MarkupStyle.Strikethrough, editorFocusRequester) },
        )

        ToolbarToggleButton(
            icon                = Icons.Outlined.Code,
            contentDescription  = "inline code",
            isActive            = state.hasStyle(MarkupStyle.InlineCode),
            colors              = colors,
            onClick             = { toggleStyle(state, MarkupStyle.InlineCode, editorFocusRequester) },
        )

        ToolbarToggleButton(
            icon                = Icons.AutoMirrored.Outlined.FormatListBulleted,
            contentDescription  = "bulleted list",
            isActive            = state.hasStyle(MarkupStyle.BulletList),
            colors              = colors,
            onClick             = { toggleStyle(state, MarkupStyle.BulletList, editorFocusRequester) },
        )

        ToolbarToggleButton(
            icon                = Icons.Outlined.FormatListNumbered,
            contentDescription  = "numbered list",
            isActive            = state.hasStyle(MarkupStyle.OrderedList),
            colors              = colors,
            onClick             = { toggleStyle(state, MarkupStyle.OrderedList, editorFocusRequester) },
        )

        ToolbarToggleButton(
            icon                = Icons.Outlined.Highlight,
            contentDescription  = "highlight",
            isActive            = state.hasStyle(MarkupStyle.Highlight),
            colors              = colors,
            onClick             = { toggleStyle(state, MarkupStyle.Highlight, editorFocusRequester) },
        )

        ToolbarToggleButton(
            icon                = Icons.Outlined.FormatQuote,
            contentDescription  = "blockquote",
            isActive            = state.hasStyle(MarkupStyle.Blockquote),
            colors              = colors,
            onClick             = { toggleStyle(state, MarkupStyle.Blockquote, editorFocusRequester) },
        )

        Spacer(modifier = Modifier.weight(1f))

        ToolbarActionButton(
            icon                = Icons.Outlined.Visibility,
            contentDescription  = "hide toolbar",
            colors              = colors,
            onClick             = onToggleToolbar,
        )
    }
}

// ── BUTTONS ──────────────────────────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun ToolbarToggleButton(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    colors: AppColors,
    onClick: () -> Unit,
) {
    Surface(
        shape    = AppShapes.rounded6,
        color    = if (isActive) colors.toggledColor else colors.sidebarColor,
        modifier = Modifier.size(28.dp),
    ) {
        IconButton(
            modifier = Modifier.focusProperties { canFocus = false },
            onClick  = onClick,
        ) {
            Icon(
                imageVector        = icon,
                modifier           = Modifier.size(16.dp),
                contentDescription = contentDescription,
                tint               = colors.activeTextColor,
            )
        }
    }
}

@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    contentDescription: String,
    colors: AppColors,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Surface(
        shape    = AppShapes.rounded6,
        color    = colors.sidebarColor,
        modifier = Modifier.size(28.dp),
    ) {
        IconButton(
            onClick  = onClick,
            enabled  = enabled,
            modifier = Modifier.focusProperties { canFocus = false },
        ) {
            Icon(
                imageVector        = icon,
                modifier           = Modifier.size(16.dp),
                contentDescription = contentDescription,
                tint               = if (enabled) colors.activeTextColor else colors.grayedTextColor,
            )
        }
    }
}


@Composable
private fun ToolbarActionButton(
    icon: ImageVector,
    contentDescription: String,
    colors: AppColors,
    onClick: () -> Unit,
) {
    Surface(
        shape    = AppShapes.rounded6,
        color    = colors.toolbarColor,
        modifier = Modifier.size(28.dp),
    ) {
        IconButton(
            onClick  = onClick,
            modifier = Modifier.focusProperties { canFocus = false },
        ) {
            Icon(
                imageVector        = icon,
                modifier           = Modifier.size(24.dp),
                contentDescription = contentDescription,
                tint               = colors.grayedTextColor,
            )
        }
    }
}

@Composable
private fun ToolbarTextToggleButton(
    label: String,
    contentDescription: String,
    isActive: Boolean,
    colors: AppColors,
    onClick: () -> Unit,
) {
    Surface(
        shape    = AppShapes.rounded6,
        color    = if (isActive) colors.toggledColor else colors.sidebarColor,
        modifier = Modifier.width(30.dp).height(28.dp),
    ) {
        IconButton(
            onClick  = onClick,
            modifier = Modifier.focusProperties { canFocus = false },
        ) {
            Text(
                text        = label,
                color       = colors.activeTextColor,
                fontSize    = 12.sp)
        }
    }
}

// ── HEADING HELPERS ──────────────────────────────────────────────────────────────────────────────────────────────────

private fun currentHeading(state: HyphenTextState): MarkupStyle? = when {
    state.hasStyle(MarkupStyle.H1) -> MarkupStyle.H1
    state.hasStyle(MarkupStyle.H2) -> MarkupStyle.H2
    state.hasStyle(MarkupStyle.H3) -> MarkupStyle.H3
    state.hasStyle(MarkupStyle.H4) -> MarkupStyle.H4
    state.hasStyle(MarkupStyle.H5) -> MarkupStyle.H5
    state.hasStyle(MarkupStyle.H6) -> MarkupStyle.H6
    else                           -> null
}

private fun headingButtonLabel(state: HyphenTextState): String = when (currentHeading(state)) {
    MarkupStyle.H1 -> "H1"
    MarkupStyle.H2 -> "H2"
    MarkupStyle.H3 -> "H3"
    MarkupStyle.H4 -> "H4"
    MarkupStyle.H5 -> "H5"
    MarkupStyle.H6 -> "H6"
    else           -> "H"
}

private fun cycleHeading(state: HyphenTextState, editorFocusRequester: FocusRequester) {
    val current = currentHeading(state)

    val next = when (current) {
        null           -> MarkupStyle.H6
        MarkupStyle.H6 -> MarkupStyle.H5
        MarkupStyle.H5 -> MarkupStyle.H4
        MarkupStyle.H4 -> MarkupStyle.H3
        MarkupStyle.H3 -> MarkupStyle.H2
        MarkupStyle.H2 -> MarkupStyle.H1
        MarkupStyle.H1 -> null
        else           -> null
    }

    if (current != null) state.toggleStyle(current)
    if (next    != null) state.toggleStyle(next)

    editorFocusRequester.requestFocus()
}

private fun toggleStyle(
    state: HyphenTextState,
    style: MarkupStyle,
    editorFocusRequester: FocusRequester,
) {
    state.toggleStyle(style)
    editorFocusRequester.requestFocus()
}
