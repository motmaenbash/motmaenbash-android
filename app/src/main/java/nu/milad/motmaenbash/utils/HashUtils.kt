package nu.milad.motmaenbash.utils

import android.util.Base64
import java.io.File
import java.security.MessageDigest

object HashUtils {


    /**
     * Generates the SHA-256 hash of a given string.
     *
     * @param input The string to hash.
     * @return The SHA-256 hash as a hexadecimal string.
     */
    fun generateSHA256(input: String): String {
        return generateHash(input, HashAlgorithms.SHA256)
    }

    /**
     * Generates the SHA-512 hash of a given string.
     *
     * @param input The string to hash.
     * @return The SHA-512 hash as a hexadecimal string.
     */
    fun generateSHA512(input: String): String {
        return generateHash(input, HashAlgorithms.SHA512)
    }

    /**
     * Generates a hash of a given string with the specified algorithm.
     *
     * @param input The string to hash.
     * @param algorithm The hashing algorithm to use (e.g., "SHA256", "SHA512").
     * @return The hash as a hexadecimal string.
     */
    private fun generateHash(input: String, algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm)
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }


    fun generateHashFromBytes(bytes: ByteArray, algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm)
        val hashBytes = digest.digest(bytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }


    /**
     * Calculates the SHA-256 hash of the provided byte array.
     *
     * @param data The byte array to hash.
     * @return The SHA-256 hash as a Base64 encoded string.
     */
    fun calculateSHA256FromBytes(bytes: ByteArray): String {
        val md = MessageDigest.getInstance(HashAlgorithms.SHA256)
        val digest = md.digest(bytes)
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    /**
     * Calculates the SHA-256 hash of the provided byte array as a hexadecimal string.
     *
     * @param data The byte array to hash.
     * @return The SHA-256 hash as a hexadecimal string.
     */
    fun calculateSHA256HexFromBytes(bytes: ByteArray): String {
        val md = MessageDigest.getInstance(HashAlgorithms.SHA256)
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }


    /**
     * Calculates the SHA-256 hash of the provided file.
     *
     * @param file The file to hash.
     * @return The SHA-256 hash as a Base64 encoded string.
     */
    fun calculateSHA256FromFile(file: File): String {
        return Base64.encodeToString(digestFile(file, HashAlgorithms.SHA256), Base64.NO_WRAP)
    }


    /**
     * Calculates the SHA-256 hash of the provided file as a hexadecimal string.
     * The result will match the output of sha256sum command line utility.
     *
     * @param file The file to hash.
     * @return The SHA-256 hash as a hexadecimal string.
     */
    fun calculateSHA256HexFromFile(file: File): String {
        return digestFile(file, HashAlgorithms.SHA256).joinToString("") { "%02x".format(it) }
    }

    /**
     * Calculates message digest for a file using specified algorithm.
     * This implementation matches the behavior of standard command line utilities like sha256sum.
     *
     * @param file The file to digest
     * @param algorithm The hash algorithm to use
     * @return The raw digest bytes
     */
    private fun digestFile(file: File, algorithm: String): ByteArray {
        val md = MessageDigest.getInstance(algorithm)
        file.inputStream().use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }
        }
        return md.digest()
    }
}

object HashAlgorithms {
    const val SHA256 = "SHA-256"
    const val SHA512 = "SHA-512"
}