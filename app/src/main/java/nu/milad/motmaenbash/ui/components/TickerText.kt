package nu.milad.motmaenbash.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun TickerText(
    texts: List<String>,
    color: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    animationDuration: Int = 3000, // Time each text is displayed in milliseconds
    transitionDuration: Int = 500, // Fade animation duration in milliseconds
) {
    if (texts.isEmpty()) return

    val currentIndex = remember { mutableIntStateOf(0) }
    val visible = remember { mutableStateOf(true) }

    LaunchedEffect(key1 = true) {
        while (true) {
            delay(animationDuration.toLong())
            visible.value = false
            delay(transitionDuration.toLong())
            currentIndex.intValue = (currentIndex.intValue + 1) % texts.size
            visible.value = true
            delay(transitionDuration.toLong())
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible.value) 1f else 0f,
        animationSpec = tween(durationMillis = transitionDuration),
        label = "alpha"
    )

    Text(
        text = texts[currentIndex.intValue],
        color = color.copy(alpha = alpha),
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}