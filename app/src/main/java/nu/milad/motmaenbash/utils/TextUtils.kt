package nu.milad.motmaenbash.utils

import android.util.Log
import java.text.Normalizer
import java.util.regex.Pattern

object TextUtils {


    private val URL_PATTERN = Pattern.compile(
        "(?<![a-zA-Z0-9@])" +                                                // Prevent matches after @ to exclude emails
                "(?![\\w.-]+@)" +                                               // Negative lookahead to exclude emails
                "(?:" + "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*" +  // Scheme
                "[-a-zA-Z0-9+&@#/%=~_|#]|" +                           // Path and query params
                "\\b(?:[a-zA-Z0-9-]+\\.)+" +                           // Subdomains
                "[a-zA-Z]{2,26}" +                                     // Top-level domain
                "(?::\\d{1,5})?" +                                     // Optional port
                "(?:/[-a-zA-Z0-9@:%_\\+.~#?&/=]*)?\\b" +               // Optional path
                ")", Pattern.CASE_INSENSITIVE
    )


    /**
     * Checks if a message contains any links (URLs).
     *
     * @param messageThe message string.
     * @return True if the message contains links, false otherwise.
     */
    fun isMessageContainingLink(message: String): Boolean {
        return URL_PATTERN.matcher(message).find()
    }


    /**
     * Extracts all links (URLs) from a message.
     *
     * @param message The message string.
     * @return A list of extracted links.
     */
    fun extractLinks(message: String): List<String> {
        val matcher = URL_PATTERN.matcher(message)
        val links = mutableListOf<String>()
        while (matcher.find()) {
            links.add(matcher.group())
        }
        return links
    }


    /**
     * Removes all links from a message.
     *
     * @param message The message string.
     * @return The message with all links removed.
     */

    fun removeLinks(message: String): String {
        return message.replace(URL_PATTERN.toRegex(), "")
    }


    fun removeShortWords(message: String, minLength: Int = 1): String {
        val words =
            message.split(Regex("\\p{Z}+")) // Use \p{Z} for broader Unicode whitespace matching

        val filteredWords = words.filter { it.isNotEmpty() && it.length >= minLength }

        return filteredWords.joinToString(" ")
    }

    fun extractPhrases(message: String, minPhraseLength: Int = 1, maxPhraseLength: Int) {

        Log.d("Normlze kwd ", "extractPhrases:" + message.split("\\s+".toRegex()))


        return message.split("\\s+".toRegex()).filter { it.isNotBlank() }.let { words ->
            words.indices.flatMap { i ->
                (minPhraseLength..maxPhraseLength).filter { i + it <= words.size }
                    .map { words.subList(i, i + it).joinToString(" ") }
            }
        }
    }


    fun arabicToPersian(input: String): String {
        val arabicToPersianMap = mapOf(
            'ي' to 'ی',
            'ى' to 'ی',
            'ك' to 'ک',
            'ة' to 'ه',
            'أ' to 'ا',
            'إ' to 'ا',
            'آ' to 'ا', //todo: check
            'ٱ' to 'ا',
            'ٲ' to 'ا',
            'ٳ' to 'ا',
            'ﭐ' to 'ا',
            'ﭑ' to 'ا',
            'ؤ' to 'و',
            'ئ' to 'ی'
        )
        return input.map { char ->
            arabicToPersianMap[char] ?: char
        }.joinToString("")
    }


    fun cleanMessage(message: String): String {
//        val symbolsToRemove = "+_)(*&^%$#@!~=-`\":';|}{\][?></.,٪؟؛:،×٪﷼٫٬!"
//        val words = message.split(Regex("\\p{Z}+"))
//        val filteredWords = words.filter { word ->
//            word.isNotEmpty() &&
//                    word.length >= minLength &&
//                    !word.all { it.isDigit() } && // Remove numeric-only words
//                    !word.any { it in symbolsToRemove } // Remove words containing specified symbols
//        }
//        return filteredWords.joinToString(" ")
        return message
    }

    fun normalizeWhitespace(text: String): String {
        return text.trim().replace("\\s+".toRegex(), " ")
    }


    /**
     * Removes all punctuation characters from the input text.
     * Only alphanumeric characters and whitespace are retained.
     *
     * @param text The input text from which punctuation will be removed.
     * @return The text with punctuation removed.
     *
     * Sample Usage:
     * val input1 = "Hello, world!"       // Output: "Hello world"
     * val input2 = "¡Hola, mundo!"       // Output: "Hola mundo"
     * val input3 = "Bonjour, le monde!"  // Output: "Bonjour le monde"
     * val input4 = "你好，世界！"         // Output: "你好 世界"
     */
    fun removePunctuation(text: String): String {
        return text.replace("[^\\w\\s]".toRegex(), "")
    }

    /**
     * Removes diacritical marks (accents) from the input text.
     * This is useful for text normalization where accents are not required.
     *
     * @param text The input text from which diacritics will be removed.
     * @return The text with diacritics removed.
     *
     * Sample Usage:
     * val input1 = "naïve"               // Output: "naive"
     * val input2 = "mañana"              // Output: "manana"
     * val input3 = "école"               // Output: "ecole"
     * val input4 = "straße"             // Output: "strasse"
     * val input5 = "你好"                // Output: "你好" (no diacritics to remove)
     */
    fun removeDiacritics(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFKD).replace("\\p{M}".toRegex(), "")
    }

//    fun removeDiacritics(text: String): String {
//        return Normalizer.normalize(text, Normalizer.Form.NFKD)
//            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
//    }
}