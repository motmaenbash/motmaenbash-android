package nu.milad.motmaenbash.utils

import android.util.LruCache
import nu.milad.motmaenbash.models.Alert
import nu.milad.motmaenbash.services.UrlGuardService.UrlAnalysisResult
import nu.milad.motmaenbash.services.UrlGuardService.UrlAnalysisResult.SuspiciousUrl
import java.net.IDN

object UrlUtils {

    private val IPV4_REGEX = Regex(
        """^(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}$"""
    )

    private val SHAPARAK_HOST_REGEX = Regex(
        """^([a-z0-9-]+\.)+shaparak\.ir$""",
        RegexOption.IGNORE_CASE
    )

    /**
     * Common multi-part public suffixes (eTLD+1 awareness) including Iranian (.ir)
     * and common international second-level domains.
     */
    private val MULTI_PART_PUBLIC_SUFFIXES = setOf(
        // Iran (.ir)
        "co.ir", "ac.ir", "gov.ir", "org.ir", "net.ir", "sch.ir", "id.ir",
        // Common country-code second-level domains
        "co.uk", "org.uk", "ac.uk", "gov.uk", "me.uk", "net.uk",
        "com.au", "net.au", "org.au", "edu.au", "gov.au",
        "com.tr", "org.tr", "gov.tr", "edu.tr", "net.tr",
        "co.AE", "net.ae", "org.ae", "gov.ae", "ac.ae", "co.ae",
        "com.sa", "org.sa", "gov.sa", "edu.sa",
        "com.pk", "org.pk", "gov.pk", "edu.pk",
        "co.in", "net.in", "org.in", "gov.in", "ac.in",
        "co.jp", "ne.jp", "or.jp", "ac.jp", "go.jp",
        "co.kr", "ne.kr", "or.kr", "ac.kr", "go.kr",
        "com.cn", "net.cn", "org.cn", "gov.cn", "edu.cn",
        "com.br", "net.br", "org.br", "gov.br", "edu.br",
        "co.za", "org.za", "gov.za", "ac.za",
        "co.nz", "net.nz", "org.nz", "govt.nz",
        "com.ru", "org.ru", "net.ru",
        "com.de", "co.de",
        "com.es", "org.es", "gob.es",
        "com.fr", "asso.fr", "gouv.fr",
        "co.il", "org.il", "gov.il", "ac.il",
        "com.my", "org.my", "gov.my", "edu.my",
        "com.sg", "org.sg", "gov.sg", "edu.sg",
        "com.hk", "org.hk", "gov.hk", "edu.hk",
        "com.tw", "org.tw", "gov.tw", "edu.tw",
        "com.ua", "org.ua", "gov.ua", "edu.ua",
        "com.eg", "org.eg", "gov.eg", "edu.eg",
        "com.ng", "org.ng", "gov.ng", "edu.ng",
        "com.ar", "org.ar", "gob.ar", "edu.ar",
        "com.mx", "org.mx", "gob.mx", "edu.mx",
        "co.id", "or.id", "go.id", "ac.id",
        "co.th", "or.th", "go.th", "ac.th",
        "com.vn", "org.vn", "gov.vn", "edu.vn",
        "com.ph", "org.ph", "gov.ph", "edu.ph"
    )

    /**
     * Sensitive Iranian financial and government targets frequently targeted by
     * IDN homograph / lookalike attacks.
     */
    private val PROTECTED_HOMOGRAPH_TARGETS = setOf(
        "shaparak.ir",
        "cbi.ir",
        "bmi.ir",
        "mellat.ir",
        "bankmellat.ir",
        "tejaratbank.ir",
        "bsi.ir",
        "bpi.ir",
        "parsian-bank.ir",
        "pasargad.ir",
        "enbank.ir",
        "sb24.ir",
        "samanbank.ir",
        "keshavarzi.ir",
        "bki.ir",
        "bank-maskan.ir",
        "refah-bank.ir",
        "sepah.ir",
        "banksepah.ir",
        "postbank.ir",
        "blu.ir",
        "blubank.com",
        "zarinpal.com",
        "idpay.ir",
        "zibal.ir",
        "payping.ir",
        "nextpay.org",
        "adliran.ir",
        "my.gov.ir",
        "epolice.ir",
        "najaja.ir"
    )

    private val CONFUSABLE_CHAR_MAP = mapOf(
        // Cyrillic & Greek confusables
        'а' to 'a', 'ɑ' to 'a', 'α' to 'a', 'ạ' to 'a', 'ą' to 'a', 'à' to 'a', 'á' to 'a', 'â' to 'a', 'ã' to 'a', 'ä' to 'a', 'å' to 'a',
        'Ь' to 'b', 'ß' to 'b', 'β' to 'b',
        'с' to 'c', 'ϲ' to 'c', 'ç' to 'c', 'ć' to 'c', 'č' to 'c',
        'ԁ' to 'd', 'đ' to 'd',
        'е' to 'e', 'ẹ' to 'e', 'ė' to 'e', 'ę' to 'e', 'è' to 'e', 'é' to 'e', 'ê' to 'e', 'ë' to 'e', 'ε' to 'e',
        'ɡ' to 'g', 'ğ' to 'g', 'ġ' to 'g',
        'һ' to 'h', 'ħ' to 'h',
        'і' to 'i', 'ї' to 'i', 'ι' to 'i', 'ı' to 'i', 'ì' to 'i', 'í' to 'i', 'î' to 'i', 'ï' to 'i', '1' to 'l', '!' to 'i',
        'ј' to 'j',
        'к' to 'k', 'κ' to 'k', 'ķ' to 'k',
        'ӏ' to 'l', 'ł' to 'l', 'I' to 'l',
        'м' to 'm',
        'п' to 'n', 'ñ' to 'n', 'ń' to 'n', 'η' to 'n',
        'о' to 'o', 'ο' to 'o', 'օ' to 'o', 'ọ' to 'o', 'ò' to 'o', 'ó' to 'o', 'ô' to 'o', 'õ' to 'o', 'ö' to 'o', 'ø' to 'o', '0' to 'o',
        'р' to 'p', 'ρ' to 'p',
        'ԛ' to 'q',
        'г' to 'r', 'ř' to 'r',
        'ѕ' to 's', 'ś' to 's', 'š' to 's', 'ş' to 's',
        'т' to 't', 'τ' to 't', 'ţ' to 't',
        'υ' to 'u', 'ս' to 'u', 'ụ' to 'u', 'ù' to 'u', 'ú' to 'u', 'û' to 'u', 'ü' to 'u',
        'ν' to 'v', 'ѵ' to 'v',
        'ѡ' to 'w', 'ω' to 'w',
        'х' to 'x', 'χ' to 'x',
        'у' to 'y', 'γ' to 'y', 'ý' to 'y', 'ÿ' to 'y',
        'ź' to 'z', 'ž' to 'z', 'ż' to 'z', 'ζ' to 'z'
    )

    /**
     * Validates if the given URL matches the expected pattern.
     *
     * @param url The URL to validate.
     * @return True if the URL is valid, false otherwise.
     */
    fun validateUrl(url: String): Boolean {
        if (url.isBlank()) {
            return false
        }

        val trimmed = url.trim()
        val withoutScheme = trimmed.replaceFirst("^(http://|https://)".toRegex(RegexOption.IGNORE_CASE), "")
        // Reject userinfo (@) in authority to prevent credential spoofing like https://shaparak.ir@evil.com
        val rawAuthority = withoutScheme.substringBefore('/').substringBefore('?').substringBefore('#')
        if (rawAuthority.contains('@')) {
            return false
        }

        val authorityAndPath = removeQueryStringAndFragment(removeUrlPrefixes(trimmed))

        val host = extractHost(trimmed)
        if (host.isEmpty()) {
            return false
        }

        if (IPV4_REGEX.matches(host)) {
            return true
        }

        val regex = Regex(
            """^([a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)+)(:\d{1,5})?(/[^\s]*)?$"""
        )
        val asciiCleaned = normalizeHostInUrl(authorityAndPath)
        return regex.matches(asciiCleaned)
    }

    /**
     * Checks if the given URL is a genuine HTTPS subdomain of shaparak.ir (and not shaparak.ir itself).
     * Strictly validates scheme, rejects userinfo (`@`), ports, and path-spoofing tricks.
     *
     * @param url The URL to check.
     * @return True if the URL is a valid shaparak subdomain, false otherwise.
     */
    fun isShaparakSubdomain(url: String): Boolean {
        val raw = url.trim()
        if (raw.isEmpty()) return false

        // Reject explicit non-https schemes (e.g. http://, javascript:, data:, ftp://)
        val schemeSplit = raw.split("://", limit = 2)
        if (schemeSplit.size == 2) {
            val scheme = schemeSplit[0].lowercase()
            if (scheme != "https") {
                return false
            }
        } else if (raw.contains(':')) {
            // Contains ':' without '://' -> either an explicit port or non-standard scheme
            return false
        }

        val withoutScheme = if (schemeSplit.size == 2) schemeSplit[1] else raw
        val authority = withoutScheme
            .substringBefore('/')
            .substringBefore('?')
            .substringBefore('#')

        // Reject empty authority, userinfo (@), or explicit ports (:)
        if (authority.isEmpty() || authority.contains('@') || authority.contains(':')) {
            return false
        }

        // Reject raw unicode / punycode homographs pretending to be shaparak.ir
        if (authority.any { it.code > 127 }) {
            return false
        }

        val host = authority.lowercase().removePrefix("www.")
        if (host == "shaparak.ir" || host.contains("xn--")) {
            return false
        }

        return SHAPARAK_HOST_REGEX.matches(host)
    }

    /**
     * Extracts the normalized ASCII hostname (without scheme, userinfo, port, path, query, or fragment).
     */
    fun extractHost(url: String): String {
        var working = url.trim()
        if (working.isEmpty()) return ""

        // Strip scheme if present
        working = working.replaceFirst("^[a-zA-Z][a-zA-Z0-9+.-]*://".toRegex(), "")

        // Extract authority up to first /, ?, or #
        val endIndex = working.indexOfFirst { it == '/' || it == '?' || it == '#' }
        if (endIndex >= 0) {
            working = working.substring(0, endIndex)
        }

        // If userinfo is present (user:pass@host), take the actual host after the last '@'
        val atIndex = working.lastIndexOf('@')
        if (atIndex >= 0) {
            working = working.substring(atIndex + 1)
        }

        // Strip port if present (handling IPv6 brackets if any)
        working = if (working.startsWith("[")) {
            working.substringBeforeLast(']') + "]"
        } else {
            working.substringBefore(':')
        }

        working = working.trim().trimEnd('.').lowercase()
        working = working.replaceFirst("^www\\.".toRegex(), "")

        if (working.isEmpty()) return ""

        return try {
            IDN.toASCII(working, IDN.ALLOW_UNASSIGNED).lowercase()
        } catch (_: Exception) {
            working
        }
    }

    /**
     * Extracts the registrable base domain (eTLD+1) from the given URL.
     * Supports multi-part public suffixes such as `.co.ir`, `.gov.ir`, `.ac.ir`, `.co.uk`, etc.,
     * as well as IPv4 literals and port/userinfo stripping.
     *
     * @param url The URL to extract the domain from.
     * @return The registrable base domain of the URL.
     */
    fun extractDomain(url: String): String {
        val host = extractHost(url)
        if (host.isEmpty()) return ""

        // Return IPv4 address as-is instead of taking its last two octets
        if (IPV4_REGEX.matches(host)) {
            return host
        }

        val parts = host.split('.').filter { it.isNotEmpty() }
        if (parts.size <= 2) {
            return parts.joinToString(".")
        }

        val lastTwo = parts.takeLast(2).joinToString(".")
        return if (lastTwo in MULTI_PART_PUBLIC_SUFFIXES && parts.size >= 3) {
            parts.takeLast(3).joinToString(".")
        } else {
            lastTwo
        }
    }

    fun extractAndCacheDomain(cache: LruCache<String, String>, url: String): String {
        return cache.get(url) ?: extractDomain(url).also { cache.put(url, it) }
    }

    /**
     * Removes common URL prefixes like http://, https://, userinfo@, port, and www. from the given URL,
     * and converts internationalized domain names (IDN) to Punycode ASCII.
     *
     * @param url The URL to clean.
     * @return The URL without the common prefixes.
     */
    fun removeUrlPrefixes(url: String): String {
        var processedUrl = url.trim()

        processedUrl = processedUrl.replaceFirst("^(http://|https://)".toRegex(RegexOption.IGNORE_CASE), "")
        processedUrl = processedUrl.replaceFirst("^www\\.".toRegex(RegexOption.IGNORE_CASE), "")

        return normalizeHostInUrl(processedUrl)
    }

    private fun normalizeHostInUrl(urlWithoutScheme: String): String {
        val splitIndex = urlWithoutScheme.indexOfFirst { it == '/' || it == '?' || it == '#' }
        val rawAuthority = if (splitIndex >= 0) urlWithoutScheme.substring(0, splitIndex) else urlWithoutScheme
        val remainder = if (splitIndex >= 0) urlWithoutScheme.substring(splitIndex) else ""

        // Strip userinfo if attacker supplied user@host
        val authorityNoUser = if (rawAuthority.contains('@')) {
            rawAuthority.substringAfterLast('@')
        } else {
            rawAuthority
        }

        // Strip port from host if present
        val hostOnly = if (authorityNoUser.startsWith("[")) {
            authorityNoUser
        } else {
            authorityNoUser.substringBefore(':')
        }.trimEnd('.').replaceFirst("^www\\.".toRegex(RegexOption.IGNORE_CASE), "")

        val asciiHost = try {
            if (hostOnly.isNotEmpty()) IDN.toASCII(hostOnly, IDN.ALLOW_UNASSIGNED).lowercase() else hostOnly
        } catch (_: Exception) {
            hostOnly.lowercase()
        }

        return asciiHost + remainder
    }

    /**
     * Removes all query strings and fragments from the given URL.
     *
     * @param url The URL to process.
     * @return The URL without the query string and fragment.
     */
    fun removeQueryStringAndFragment(url: String): String {
        var processedUrl = url

        // Remove query string
        val queryIndex = processedUrl.indexOf('?')
        if (queryIndex >= 0) {
            processedUrl = processedUrl.substring(0, queryIndex)
        }

        // Remove fragment identifier (#) and everything after it
        val fragmentIndex = processedUrl.indexOf('#')
        if (fragmentIndex >= 0) {
            processedUrl = processedUrl.substring(0, fragmentIndex)
        }

        processedUrl = processedUrl.trimEnd('/')

        return processedUrl
    }

    /**
     * Detects IDN homograph / confusable domain attacks targeting sensitive domains
     * (e.g. `shɑpɑrɑk.ir` or `xn--shprk-4qa.ir`).
     */
    fun isHomographPhishingUrl(url: String): Boolean {
        val asciiHost = extractHost(url)
        if (asciiHost.isEmpty()) return false

        val unicodeHost = try {
            IDN.toUnicode(asciiHost, IDN.ALLOW_UNASSIGNED).lowercase()
        } catch (_: Exception) {
            asciiHost
        }

        val isIdnOrNonAscii = asciiHost.contains("xn--") || unicodeHost.any { it.code > 127 }
        if (!isIdnOrNonAscii) return false

        // Map confusable characters in unicodeHost to ASCII skeleton
        val skeletonHost = buildString(unicodeHost.length) {
            for (ch in unicodeHost) {
                append(CONFUSABLE_CHAR_MAP[ch] ?: ch)
            }
        }

        // Flag if the confusable skeleton matches or is a subdomain of any protected target
        if (PROTECTED_HOMOGRAPH_TARGETS.any { target ->
                (skeletonHost == target || skeletonHost.endsWith(".$target")) &&
                        asciiHost != target && !asciiHost.endsWith(".$target")
            }
        ) {
            return true
        }

        // Also flag mixed-script labels (e.g., Latin + Cyrillic/Greek/Arabic in the same domain label)
        val labels = unicodeHost.split('.')
        for (label in labels) {
            val hasLatin = label.any { it in 'a'..'z' }
            val hasNonLatinAlpha = label.any { it.code > 127 && it.isLetter() }
            if (hasLatin && hasNonLatinAlpha) {
                return true
            }
        }

        return false
    }

    fun analyzeUrl(
        url: String,
        databaseHelper: DatabaseHelper
    ): UrlAnalysisResult {
        try {
            val rawTrimmed = url.trim()
            val normalizedUrl = removeUrlPrefixes(rawTrimmed).lowercase()

            return when {
                isHomographPhishingUrl(rawTrimmed) -> SuspiciousUrl(
                    url = normalizedUrl,
                    threatType = Alert.UrlThreatType.PHISHING,
                    urlMatch = 1
                )

                isShaparakSubdomain(rawTrimmed) -> UrlAnalysisResult.VerifiedPaymentGatewayUrl(
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