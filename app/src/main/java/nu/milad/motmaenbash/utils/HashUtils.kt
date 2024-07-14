package nu.milad.motmaenbash.utils

import java.math.BigInteger
import java.security.MessageDigest

object HashUtils {

    // Generate MD5 hash of a string
    fun md5(input: String): String {
        return hashString(input, "MD5")
    }

    // Generate SHA-1 hash of a string
    fun sha1(input: String): String {
        return hashString(input, "SHA-1")
    }

    // Generate hash of a string with specified algorithm
    private fun hashString(input: String, algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm)
        digest.reset()
        val hashBytes = digest.digest(input.toByteArray())
        val hashString = BigInteger(1, hashBytes).toString(16)
        // Pad with leading zeros
        return hashString.padStart(32, '0')
    }
}
