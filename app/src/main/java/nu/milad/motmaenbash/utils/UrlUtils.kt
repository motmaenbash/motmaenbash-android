package nu.milad.motmaenbash.utils

import android.util.LruCache
import nu.milad.motmaenbash.services.UrlGuardService.UrlAnalysisResult
import nu.milad.motmaenbash.services.UrlGuardService.UrlAnalysisResult.SuspiciousUrl

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

        var cleanedUrl = removeQueryStringAndFragment(url)

        cleanedUrl = removeUrlPrefixes(cleanedUrl)

        val regex = Regex(
            """^([a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)+)(/[^\s]*)?$"""
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


    fun extractAndCacheDomain(cache: LruCache<String, String>, url: String): String {
        return cache.get(url) ?: extractDomain(url).also { cache.put(url, it) }
    }

    /**
     * Removes common URL prefixes like http://, https://, and www. from the given URL.
     *
     * @param url The URL to clean.
     * @return The URL without the common prefixes.
     */
    fun removeUrlPrefixes(url: String): String {
        var processedUrl = url

        processedUrl = processedUrl.replaceFirst("^(http://|https://)".toRegex(), "")
        processedUrl = processedUrl.replaceFirst("^www\\.".toRegex(), "")

        return processedUrl
    }

    /**
     * Removes all query strings from the given URL.
     *
     * @param url The URL to process.
     * @return The URL without the query string.
     */
    fun removeQueryStringAndFragment(url: String): String {
        var processedUrl = url


        // Remove query string
        val queryIndex = processedUrl.indexOf('?')
        if (queryIndex > 0) {
            processedUrl = processedUrl.substring(0, queryIndex)
        }

        // Remove fragment identifier (#) and everything after it
        val fragmentIndex = processedUrl.indexOf('#')
        if (fragmentIndex > 0) {
            processedUrl = processedUrl.substring(0, fragmentIndex)
        }

        processedUrl = processedUrl.trimEnd('/')

        return processedUrl
    }

    fun analyzeUrl(
        url: String,
        databaseHelper: DatabaseHelper
    ): UrlAnalysisResult {
        try {
            val normalizedUrl = removeUrlPrefixes(url.trim().lowercase())

            return when {
                isShaparakSubdomain(normalizedUrl) -> UrlAnalysisResult.VerifiedPaymentGatewayUrl(
                    normalizedUrl
                )

                else -> {
                    val (isFlagged, threatType, urlMatch) = databaseHelper.isUrlFlagged(
                        normalizedUrl
                    )
                    if (isFlagged) {
                        SuspiciousUrl(normalizedUrl, threatType, urlMatch)
                    } else {
                        UrlAnalysisResult.NeutralUrl
                    }
                }
            }
        } catch (_: Exception) {
            return UrlAnalysisResult.NeutralUrl
        }
    }
}