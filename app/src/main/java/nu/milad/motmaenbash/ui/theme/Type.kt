package nu.milad.motmaenbash.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import nu.milad.motmaenbash.R


val VazirFontFamily = FontFamily(
    Font(R.font.vazirmatn)
)

val SahelFontFamily = FontFamily(
    Font(R.font.sahel)
)

fun getBaseTextStyle(fontFamily: FontFamily) = TextStyle(
    fontFamily = fontFamily,
    letterSpacing = 0.sp
)


fun getTypography(fontFamily: FontFamily = VazirFontFamily): Typography {
    val baseStyle = getBaseTextStyle(fontFamily)

    return Typography(
        // Title styles
        titleSmall = baseStyle,
        titleMedium = baseStyle,
        titleLarge = baseStyle,

        // Body styles
        bodyLarge = baseStyle.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            lineHeight = 26.sp
        ),
        bodyMedium = baseStyle.copy(
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 26.sp
        ),
        bodySmall = baseStyle.copy(
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 26.sp
        ),

        // Headline styles
        headlineLarge = baseStyle.copy(
            fontWeight = FontWeight.Black,
            fontSize = 30.sp,
            lineHeight = 28.sp
        ),
        headlineMedium = baseStyle.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 28.sp
        ),
        headlineSmall = baseStyle.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 28.sp
        ),

        // Label styles
        labelLarge = baseStyle.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        ),


        )
}
