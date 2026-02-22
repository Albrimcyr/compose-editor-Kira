package example.feb.ui.elements

import androidx.compose.runtime.Composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import example.feb.ui.AppColors
import example.feb.ui.AppShapes
import java.util.UUID


@Composable
fun MainContent(
    selectedId: UUID?,
    title: String,
    content: String,
    onContentChange: (String) -> Unit,
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

            // -------------------------------------------------------------------------------
            // TEXT AREA

            var fieldValue by remember(selectedId) { mutableStateOf(TextFieldValue(content)) }

            LaunchedEffect(selectedId, content) {
                if (fieldValue.text != content) fieldValue = TextFieldValue(content)
            }

            BasicTextField(
                value = fieldValue,
                onValueChange = { next ->
                    fieldValue = next
                    onContentChange(next.text)
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).fillMaxHeight(),
                )

        }

    }
}