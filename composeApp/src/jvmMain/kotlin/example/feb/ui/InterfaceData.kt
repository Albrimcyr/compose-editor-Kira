package example.feb.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AppShapes {
    val rounded12 = RoundedCornerShape(12.dp)
}

data class AppColors(
    val selectionColor: Color,
    val sidebarColor: Color,
    val activeTextColor: Color,
    val grayedTextColor: Color
)

private val DarkColors = AppColors(
    selectionColor = Color(0xFF303338),
    sidebarColor = Color(0xFF1A1E24),
    activeTextColor = Color(0xFFFFFFFF),
    grayedTextColor = Color(0xFFACACAC)
)

private val LightColors = AppColors(
    selectionColor = Color(0xFFBCC6D5),
    sidebarColor = Color(0xFFA2B9E2),
    activeTextColor = Color(0xFF000000),
    grayedTextColor = Color(0xFF363636)
)

// simple implementation, instead of a new Theme, just for clarity. With some prop drilling. Change later? Will see.
fun colorsFor(isDarkTheme: Boolean): AppColors =
    if (isDarkTheme) DarkColors else LightColors