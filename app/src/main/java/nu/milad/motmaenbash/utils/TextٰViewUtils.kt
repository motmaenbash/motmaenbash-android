package nu.milad.motmaenbash.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

fun parseBoldTags(raw: String): AnnotatedString {
    val result = buildAnnotatedString {
        var currentIndex = 0
        val regex = "<b>(.*?)</b>".toRegex()
        regex.findAll(raw).forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1

            // Text before <b>
            append(raw.substring(currentIndex, start))

            // Text inside <b>
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(match.groupValues[1])
            }

            currentIndex = end
        }
        // Remaining text after the last tag
        if (currentIndex < raw.length) append(raw.substring(currentIndex))
    }
    return result
}