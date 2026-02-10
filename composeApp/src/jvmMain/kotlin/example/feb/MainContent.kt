package example.feb

import androidx.compose.runtime.Composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
fun MainContent(
    selectedChapter: Chapter?
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (selectedChapter == null) {
            Text(
                text = "Select a chapter",
            )
        } else {
            Text(
                text = "DEMO: ${selectedChapter.title}",
            )
        }
    }
}