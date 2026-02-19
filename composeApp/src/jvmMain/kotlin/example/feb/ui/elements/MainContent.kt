package example.feb.ui.elements

import androidx.compose.runtime.Composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import example.feb.ui.AppColors

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.outlined.LightMode


@Composable
fun MainContent(
    hasSelection: Boolean,
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
        if (!hasSelection) {
            Text(
                text = "Select a chapter",
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {

                Row(){

                    Text(
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(20.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        text = title,
                        color = colors.activeTextColor,)

                    IconButton(onClick = onToggleTheme){
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "toggle theme",
                            tint = colors.grayedTextColor
                        )
                    }

                }

                HorizontalDivider(modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                    color = colors.selectionColor,
                )

                BasicTextField(
                    value = content,
                    onValueChange = onContentChange,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).fillMaxHeight(),

                )

            }
        }
    }
}