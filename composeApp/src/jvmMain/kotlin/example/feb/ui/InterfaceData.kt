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
    val dividerColor: Color,
    val toggledColor: Color,
    val blackColor: Color,
    val whiteColor: Color,
    val mainDividerColor: Color,
    val deleteColor: Color,
)

private val DarkColors = AppColors(
    selectionColor = Color(0xFF2D2F39),
    sidebarColor = Color(0xFF161A23),
    activeTextColor = Color(0xFFFFFFFF),
    activeInvertedTextColor = Color(0xFFFFFFFF),
    grayedTextColor = Color(0xFF717479),
    dividerColor = Color(0xFF2D2F39),
    mainDividerColor = Color(0xFF161A23),
    toggledColor = Color(0xFF723B43),
    whiteColor = Color(0xFFFFFFFF),
    blackColor = Color(0xFF000000),
    deleteColor = Color(0xFFFFE6E6),
)

private val LightColors = AppColors(
    selectionColor = Color(0xFFBECBD5),
    sidebarColor = Color(0xFFE2EAF1),
    activeTextColor = Color(0xFF000000),
    activeInvertedTextColor = Color(0xFF000000),
    grayedTextColor = Color(0xFF747474),
    dividerColor = Color(0xFFBECBD5),
    mainDividerColor = Color(0xFFBECBD5),
    toggledColor = Color(0xFFE3B9D6),
    whiteColor = Color(0xFFFFFFFF),
    blackColor = Color(0xFF000000),
    deleteColor = Color(0xFFFFE6E6),
)

// simple implementation, instead of a new Theme, just for clarity. With some prop drilling. Change later? Once all the colors are 100%.
fun colorsFor(isDarkTheme: Boolean): AppColors =
    if (isDarkTheme) DarkColors else LightColors