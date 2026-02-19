package example.feb

import androidx.compose.runtime.Composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.foundation.text.*
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import example.feb.ui.AppColors


@Composable
fun MainContent(
    selectedChapter: Chapter?,
    onContentChange: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selectedChapter == null) {
            Text(
                text = "Select a chapter",
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {

                Text(
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(20.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    text = selectedChapter.title,)

                HorizontalDivider(modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                    color = AppColors.selectionColor,
                )

                BasicTextField(
                    value = selectedChapter.content,
                    onValueChange = onContentChange,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).fillMaxHeight(),

                )

            }
        }
    }
}