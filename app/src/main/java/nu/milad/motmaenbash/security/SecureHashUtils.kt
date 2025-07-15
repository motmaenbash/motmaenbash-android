package nu.milad.motmaenbash.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.spec.SecretKeySpec

/**
 * Secure hash utility class for MotmaenBash Android application.
 * Replaces the insecure MD5 implementation with SHA-256 + salt.
 * 
 * Security improvements:
 * - Uses SHA-256 instead of MD5
 * - Implements salt to prevent rainbow table attacks
 * - Secure random number generation
 * - Constant-time hash comparison
 * 
 * @author محمدحسین نوروزی (Mohammad Hossein Norouzi)
 * @since 2.0.0
 */
object SecureHashUtils {
    
    private const val SALT_LENGTH = 32 // 256 bits
    private const val HASH_ALGORITHM = "SHA-256"
    private const val ITERATIONS = 10000 // For PBKDF2 if needed
    
    /**
     * Generates a secure hash with salt using SHA-256 algorithm.
     * 
     * @param input The input string to hash
     * @return Base64-encoded string containing salt + hash
     * @throws SecurityException if hashing fails
     */
    fun generateSecureHash(input: String): String {
        try {
            val salt = generateSalt()
            val sha256 = MessageDigest.getInstance(HASH_ALGORITHM)
            
            // Update digest with salt first
            sha256.update(salt)
            
            // Then hash the input
            val hash = sha256.digest(input.toByteArray(Charsets.UTF_8))
            
            // Combine salt + hash for storage
            val combined = salt + hash
            
            return Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw SecurityException("Failed to generate secure hash", e)
        }
    }
    
    /**
     * Verifies if an input matches a stored hash.
     * 
     * @param input The input string to verify
     * @param storedHash The stored hash to compare against
     * @return true if input matches, false otherwise
     */
    fun verifyHash(input: String, storedHash: String): Boolean {
        return try {
            val decoded = Base64.decode(storedHash, Base64.NO_WRAP)
            
            // Extract salt and hash
            val salt = decoded.sliceArray(0 until SALT_LENGTH)
            val hash = decoded.sliceArray(SALT_LENGTH until decoded.size)
            
            // Recompute hash with same salt
            val sha256 = MessageDigest.getInstance(HASH_ALGORITHM)
            sha256.update(salt)
            val computedHash = sha256.digest(input.toByteArray(Charsets.UTF_8))
            
            // Constant-time comparison to prevent timing attacks
            MessageDigest.isEqual(hash, computedHash)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generates a secure random salt.
     * 
     * @return ByteArray containing random salt
     */
    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val saltBytes = ByteArray(SALT_LENGTH)
        random.nextBytes(saltBytes)
        return saltBytes
    }
    
    /**
     * Generates a secure hash for SMS content specifically.
     * Includes additional normalization for SMS-specific hashing.
     * 
     * @param smsContent The SMS content to hash
     * @param phoneNumber The sender's phone number
     * @return Secure hash of the SMS content
     */
    fun generateSmsHash(smsContent: String, phoneNumber: String): String {
        val normalizedContent = normalizeSmsContent(smsContent)
        val normalizedPhone = normalizePhoneNumber(phoneNumber)
        val combined = "$normalizedContent|$normalizedPhone"
        
        return generateSecureHash(combined)
    }
    
    /**
     * Normalizes SMS content for consistent hashing.
     * 
     * @param content The SMS content to normalize
     * @return Normalized SMS content
     */
    private fun normalizeSmsContent(content: String): String {
        return content.trim()
            .replace(Regex("\\s+"), " ") // Normalize whitespace
            .lowercase() // Convert to lowercase
    }
    
    /**
     * Normalizes phone number for consistent hashing.
     * 
     * @param phoneNumber The phone number to normalize
     * @return Normalized phone number
     */
    private fun normalizePhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace(Regex("[^0-9]"), "") // Remove non-digits
    }
    
    /**
     * Generates a hash for app package information.
     * 
     * @param packageName The package name
     * @param signature The app signature
     * @return Secure hash of the package information
     */
    fun generatePackageHash(packageName: String, signature: String): String {
        val combined = "$packageName|$signature"
        return generateSecureHash(combined)
    }
    
    /**
     * Generates a quick hash for performance-critical operations.
     * Uses single SHA-256 without salt for speed.
     * 
     * @param input The input string to hash
     * @return Base64-encoded SHA-256 hash
     */
    fun generateQuickHash(input: String): String {
        return try {
            val sha256 = MessageDigest.getInstance(HASH_ALGORITHM)
            val hash = sha256.digest(input.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(hash, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw SecurityException("Failed to generate quick hash", e)
        }
    }
    
    /**
     * Generates a secure key for encryption purposes.
     * 
     * @param password The password to derive key from
     * @param salt The salt for key derivation
     * @return SecretKeySpec for encryption
     */
    fun generateSecureKey(password: String, salt: ByteArray): SecretKeySpec {
        val sha256 = MessageDigest.getInstance(HASH_ALGORITHM)
        sha256.update(salt)
        val key = sha256.digest(password.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(key, "AES")
    }
    
    /**
     * Validates hash format and strength.
     * 
     * @param hash The hash to validate
     * @return true if hash is valid format, false otherwise
     */
    fun validateHashFormat(hash: String): Boolean {
        return try {
            val decoded = Base64.decode(hash, Base64.NO_WRAP)
            decoded.size == (SALT_LENGTH + 32) // 32 bytes for SHA-256
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Migrates old MD5 hash to new secure hash.
     * 
     * @param oldHash The old MD5 hash
     * @param originalInput The original input (if available)
     * @return New secure hash or null if migration not possible
     */
    fun migrateFromMD5(oldHash: String, originalInput: String?): String? {
        return if (originalInput != null) {
            generateSecureHash(originalInput)
        } else {
            null // Cannot migrate without original input
        }
    }
}