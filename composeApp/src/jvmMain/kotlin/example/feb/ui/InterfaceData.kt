package example.feb.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AppShapes {
    val rounded12 = RoundedCornerShape(12.dp)
    val rounded6 = RoundedCornerShape(6.dp)
}

data class AppColors(
    val selectionColor: Color,
    val sidebarColor: Color,
    val activeTextColor: Color,
    val grayedTextColor: Color,
    val activeInvertedTextColor: Color,
    val dividerColor: Color
)

// NICE PALETTE (can be used)
//private val DarkColors = AppColors(
//    selectionColor = Color(0xFF303338),
//    sidebarColor = Color(0xFF1A1E24),
//    activeTextColor = Color(0xFFFFFFFF),
//    activeInvertedTextColor = Color(0xFF000000),
//    grayedTextColor = Color(0xFFACACAC)
//)

private val DarkColors = AppColors(
    selectionColor = Color(0xFF2D2F39),
    sidebarColor = Color(0xFF161A23),
    activeTextColor = Color(0xFFFFFFFF),
    activeInvertedTextColor = Color(0xFF000000),
    grayedTextColor = Color(0xFF717479),
    dividerColor = Color(0xFF2D2F39)
)

private val LightColors = AppColors(
    selectionColor = Color(0xFFFFFFFF),
    sidebarColor = Color(0xFFE4E9F1),
    activeTextColor = Color(0xFF000000),
    activeInvertedTextColor = Color(0xFFFFFFFF),
    grayedTextColor = Color(0xFF747474),
    dividerColor = Color(0xFF2D2F39)
)

// simple implementation, instead of a new Theme, just for clarity. With some prop drilling. Change later? Will see.
fun colorsFor(isDarkTheme: Boolean): AppColors =
    if (isDarkTheme) DarkColors else LightColors