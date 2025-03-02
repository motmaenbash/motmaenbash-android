package nu.milad.motmaenbash.utils

object UrlUtils {

    /**
     * Validates if the given URL matches the expected pattern.
     *
     * @param url The URL to validate.
     * @return True if the URL is valid, false otherwise.
     */
    fun validateUrl(url: String): Boolean {
        if (url.isEmpty()) {
            return false
        }

        var cleanedUrl = removeQueryString(url)

        cleanedUrl = removeUrlPrefixes(cleanedUrl)

        val regex = Regex(
            "^(https?:\\/\\/)?(www\\.)?([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}(\\/[^\\s]*)?$"
        )
        return regex.matches(cleanedUrl)
    }

    /**
     * Checks if the given URL is a subdomain of shaparak.ir but not shaparak.ir itself.
     *
     * @param url The URL to check.
     * @return True if the URL is a shaparak subdomain, false otherwise.
     */
    fun isShaparakSubdomain(url: String): Boolean {

        val regex =
            Regex("""^(https?://)?([a-zA-Z0-9-]+\.)+shaparak\.ir(/.*)?$""", RegexOption.IGNORE_CASE)

        return regex.matches(url)

    }

    /**
     * Extracts the base domain from the given URL.
     *
     * @param url The URL to extract the domain from.
     * @return The base domain of the URL.
     */
    fun extractDomain(url: String): String {
        var domain = removeUrlPrefixes(url)
        // Extract the domain up to the first '/' (if any)
        val slashIndex = domain.indexOf('/')
        if (slashIndex > 0) {
            domain = domain.substring(0, slashIndex)
        }
        // Split by '.' and take the last two parts (if available)
        val parts = domain.split('.')
        return if (parts.size > 1) {
            parts.takeLast(2).joinToString(".")
        } else {
            domain
        }
    }

    /**
     * Removes common URL prefixes like http://, https://, and www. from the given URL.
     *
     * @param url The URL to clean.
     * @return The URL without the common prefixes.
     */
    fun removeUrlPrefixes(url: String): String {
        var cleanedUrl = url
        cleanedUrl = cleanedUrl.replaceFirst("^(http://|https://)".toRegex(), "")
        cleanedUrl = cleanedUrl.replaceFirst("^www\\.".toRegex(), "")
        return cleanedUrl
    }

    /**
     * Removes all query strings from the given URL.
     *
     * @param url The URL to process.
     * @return The URL without the query string.
     */
    fun removeQueryString(url: String): String {
        val queryIndex = url.indexOf('?')
        return if (queryIndex > 0) {
            url.substring(0, queryIndex)
        } else {
            url
        }
    }
}