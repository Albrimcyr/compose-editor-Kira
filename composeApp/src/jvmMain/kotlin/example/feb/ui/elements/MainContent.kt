package example.feb.ui.elements

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isUnspecified
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor


@Composable
fun MainContent(
    selectedId: UUID?,
    title: String,
    content: String, // normalized HTML
    onContentChange: (UUID, String) -> Unit,
    isDarkTheme: Boolean,
    colors: AppColors,
    onToggleTheme: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(4.dp),
        contentAlignment = Alignment.Center
    ) {

        if (selectedId == null) {
            Text(text = "Select a chapter")
            return@Box
        }


        Column(modifier = Modifier.fillMaxSize()) {

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

            // ── HEADER ───────────────────────────────────────────────────────────────────────────────────────────

                Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(20.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    text = title,
                    color = colors.dividerColor)

                Surface(modifier = Modifier.padding(horizontal = 10.dp), shape = AppShapes.rounded12){
                    IconButton(onClick = onToggleTheme){
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = "toggle theme",
                            tint = if (isDarkTheme) colors.activeInvertedTextColor else colors.activeTextColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

            }

            HorizontalDivider(modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 4.dp),
                color = colors.dividerColor,
                )


            // ── TEXT AREA ────────────────────────────────────────────────────────────────────────────────────────────

            ChapterRichEditor(
                chapterId = selectedId,
                storedHtml = content,
                onHtmlChange = onContentChange,
                colors = colors,
            )

        }

    }
}

// ── EDITOR ───────────────────────────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChapterRichEditor(
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

        val toolbarScroll = rememberScrollState()

        Column(modifier = Modifier.fillMaxSize()) {

            EditorToolbar(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(toolbarScroll)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                state = richTextState,
                colors = colors,
                editorFocusRequester = editorFocusRequester
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
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
    val max          = 32f

    fun currentFontSizeSp(): Float {
        val fs = state.currentSpanStyle.fontSize
        return if (fs.isUnspecified) baseFontSize.value else fs.value
    }

    fun applyFontSize(sp: Float) {
        state.toggleSpanStyle(SpanStyle(fontSize = sp.coerceIn(min, max).sp))
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
        color = if (isActive) colors.selectionColor else colors.sidebarColor,
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
