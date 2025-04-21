package nu.milad.motmaenbash.models

import androidx.compose.ui.graphics.vector.ImageVector

data class InfoItem(
    val question: String,
    val answer: String,
    val icon: ImageVector? = null
)
