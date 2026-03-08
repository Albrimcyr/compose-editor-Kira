package example.feb.ui.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.TextDecrease
import androidx.compose.material.icons.outlined.TextIncrease
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import example.feb.ui.AppColors
import example.feb.ui.AppShapes
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.UUID

// ── EDITOR ───────────────────────────────────────────────────────────────────────────────────────────────────────────
@Composable
fun ChapterRichEditor(
    modifier: Modifier = Modifier,
    chapterId: UUID,
    storedHtml: String,
    onHtmlChange: (UUID, String) -> Unit,
    colors: AppColors,
) {
    key(chapterId) {

        val richTextState = rememberRichTextState()
        val editorFocusRequester = remember { FocusRequester() }

        var loadedHtmlCache by remember { mutableStateOf("") }
        var isExternalUpdate by remember { mutableStateOf(false) }

        LaunchedEffect(storedHtml) {
            if (storedHtml != loadedHtmlCache) {
                isExternalUpdate = true
                richTextState.setHtml(storedHtml)
                loadedHtmlCache = richTextState.toHtml()
            }
        }

        LaunchedEffect(Unit) {
            snapshotFlow { richTextState.annotatedString }
                .distinctUntilChanged()
                .collectLatest {
                    if (isExternalUpdate) {
                        isExternalUpdate = false
                        return@collectLatest
                    }
                    val html = richTextState.toHtml()
                    if (html != loadedHtmlCache) {
                        loadedHtmlCache = html
                        onHtmlChange(chapterId, html)
                    }
                }
        }

        Column (modifier = modifier) {

            Surface(color = colors.toolbarColor) {
                EditorToolbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    state = richTextState,
                    colors = colors,
                    editorFocusRequester = editorFocusRequester
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                BasicRichTextEditor(
                    state = richTextState,
                    modifier = Modifier.fillMaxSize().focusRequester(editorFocusRequester),
                )
            }

        }
    }
}


// ── EDITOR TOOLBAR ───────────────────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun EditorToolbar(
    modifier: Modifier,
    state: RichTextState,
    colors: AppColors,
    editorFocusRequester: FocusRequester,
) {

    val spanStyle = state.currentSpanStyle

    val isBold          = spanStyle.fontWeight == FontWeight.Bold
    val isItalic        = spanStyle.fontStyle == FontStyle.Italic
    val isUnderline     = spanStyle.textDecoration?.contains(TextDecoration.Underline) == true
    val isStrike        = spanStyle.textDecoration?.contains(TextDecoration.LineThrough) == true
    val isCode          = state.isCodeSpan
    val isOrderedList   = state.isOrderedList
    val isUnorderedList = state.isUnorderedList

    val baseFontSize = 16.sp
    val step         = 2f
    val min          = 10f
    val max          = 100f

    fun currentFontSizeSp(): Float {
        val fs = state.currentSpanStyle.fontSize
        return if (fs.isUnspecified) baseFontSize.value else fs.value
    }

    fun applyFontSize(sp: Float) {
        val current = currentFontSizeSp()
        val target = sp.coerceIn(min, max)

        if (target == current) return // no reset on min/max size.

        state.toggleSpanStyle(SpanStyle(fontSize = target.sp))
    }


    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {

        ToolbarIconButton(
            icon = Icons.Outlined.TextDecrease,
            contentDescription = "decrease font size",
            colors = colors,
            onClick = {
                applyFontSize(currentFontSizeSp() - step) }
        )

        ToolbarIconButton(
            icon = Icons.Outlined.TextIncrease,
            contentDescription = "increase font size",
            colors = colors,
            onClick = {
                applyFontSize(currentFontSizeSp() + step) }
        )

        ToolbarToggleButton(
            icon = Icons.Outlined.FormatBold,
            contentDescription = "bold",
            isActive = isBold,
            colors = colors,
            onClick = { state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                editorFocusRequester.requestFocus() }
        )

        ToolbarToggleButton(
            icon = Icons.Outlined.FormatItalic,
            contentDescription = "italic",
            isActive = isItalic,
            colors = colors,
            onClick = { state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                editorFocusRequester.requestFocus() }
        )

        ToolbarToggleButton(
            icon = Icons.Outlined.FormatUnderlined,
            contentDescription = "underline",
            isActive = isUnderline,
            colors = colors,
            onClick = { safeClick(editorFocusRequester) {
                state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))} }
        )

        ToolbarToggleButton(
            icon = Icons.Outlined.FormatStrikethrough,
            contentDescription = "strikethrough",
            isActive = isStrike,
            colors = colors,
            onClick = { safeClick(editorFocusRequester) {
                state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))} }
        )

        ToolbarToggleButton(
            icon = Icons.Outlined.Code,
            contentDescription = "code or similar",
            isActive = isCode,
            colors = colors,
            onClick = { state.toggleCodeSpan()
                editorFocusRequester.requestFocus() }
        )

        ToolbarToggleButton(
            icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
            contentDescription = "Bulleted list",
            isActive = isUnorderedList,
            colors = colors,
            onClick = { state.toggleUnorderedList()
                editorFocusRequester.requestFocus() }
        )

        ToolbarToggleButton(
            icon = Icons.Outlined.FormatListNumbered,
            contentDescription = "Numbered list",
            isActive = isOrderedList,
            colors = colors,
            onClick = { state.toggleOrderedList()
                editorFocusRequester.requestFocus() }
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
        shape = AppShapes.rounded6,
        color = if (isActive) colors.toggledColor else colors.sidebarColor,
        modifier = Modifier.size(28.dp)
    ) {
        IconButton(
            modifier = Modifier.focusProperties { canFocus = false },
            onClick = onClick)
        {
            Icon(
                imageVector = icon,
                modifier = Modifier.size(16.dp),
                contentDescription = contentDescription,
                tint = colors.activeTextColor,
            )
        }
    }
}


@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    contentDescription: String,
    colors: AppColors,
    onClick: () -> Unit,
) {
    Surface(
        shape = AppShapes.rounded6,
        color = colors.sidebarColor,
        modifier = Modifier.size(28.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.focusProperties { canFocus = false })
        {
            Icon(
                imageVector = icon,
                modifier = Modifier.size(16.dp),
                contentDescription = contentDescription,
                tint = colors.activeTextColor,
            )
        }
    }
}

private inline fun safeClick(
    focusRequester: FocusRequester,
    action: () -> Unit
) {
    try { action() } catch (_: Throwable) { }
    focusRequester.requestFocus()
}
