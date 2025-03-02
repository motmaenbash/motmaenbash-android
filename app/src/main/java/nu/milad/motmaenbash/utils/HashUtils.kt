package nu.milad.motmaenbash.utils

import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.security.MessageDigest

object HashUtils {

    /**
     * Generates the MD5 hash of a given string.
     *
     * @param input The string to hash.
     * @return The MD5 hash as a hexadecimal string.
     */
    fun generateMD5(input: String): String {
        return generateHash(input, "MD5")
    }


    /**
     * Generates the SHA-1 hash of a given string.
     *
     * @param input The string to hash.
     * @return The SHA-1 hash as a hexadecimal string.
     */
    fun generateSHA1(input: String): String {
        return generateHash(input, "SHA-1")
    }

    /**
     * Generates the SHA-256 hash of a given string.
     *
     * @param input The string to hash.
     * @return The SHA-256 hash as a hexadecimal string.
     */
    fun generateSHA256(input: String): String {
        return generateHash(input, "SHA-256")
    }

    /**
     * Generates the SHA-512 hash of a given string.
     *
     * @param input The string to hash.
     * @return The SHA-512 hash as a hexadecimal string.
     */
    fun generateSHA512(input: String): String {
        return generateHash(input, "SHA-512")
    }

    /**
     * Generates a hash of a given string with the specified algorithm.
     *
     * @param input The string to hash.
     * @param algorithm The hashing algorithm to use (e.g., "MD5", "SHA-1").
     * @return The hash as a hexadecimal string.
     */
    private fun generateHash(input: String, algorithm: String): String {


        val digest = MessageDigest.getInstance(algorithm)

        digest.reset()
        val hashBytes = digest.digest(input.toByteArray())
        val hashString = BigInteger(1, hashBytes).toString(16)
        // Pad with leading zeros (adjust padding based on algorithm)
        val paddingLength = when (algorithm) {
            "MD5", "SHA-1" -> 32
            "SHA-256" -> 64
            "SHA-512" -> 128
            else -> 0 // Handle unknown algorithms
        }
        return hashString.padStart(paddingLength, '0')


    }


    /**
     * Calculates the SHA-1 hash of the provided byte array.
     *
     * @param data The byte array to hash.
     * @return The SHA-1 hash as a Base64 encoded string.
     */
    fun calculateSHA1FromBytes(data: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-1")
        val digest = md.digest(data)
        return Base64.encodeToString(digest, Base64.DEFAULT)
    }


    /**
     * Calculates the SHA-1 hash of the provided file.
     *
     * @param file The file to hash.
     * @return The SHA-1 hash as a Base64 encoded string.
     */
    fun calculateSHA1FromFile(file: File): String {
        val md = MessageDigest.getInstance("SHA-1")
        val fis = FileInputStream(file)
        val buffer = ByteArray(8192)
        var bytesRead = fis.read(buffer)
        while (bytesRead != -1) {
            md.update(buffer, 0, bytesRead)
            bytesRead = fis.read(buffer)
        }
        fis.close()
        val digest = md.digest()
        return Base64.encodeToString(digest, Base64.DEFAULT)
    }
}
