package nu.milad.motmaenbash.security

import android.content.Context
import android.util.Log
import nu.milad.motmaenbash.BuildConfig
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.security.cert.CertificateException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLPeerUnverifiedException

/**
 * Network security manager for MotmaenBash Android application.
 * Implements certificate pinning, secure connections, and network security policies.
 * 
 * Security features:
 * - Certificate pinning for motmaenbash.ir domains
 * - TLS 1.2+ enforcement
 * - Request/response logging (debug only)
 * - Timeout management
 * - Connection pooling
 * - Retry policies
 * 
 * @author محمدحسین نوروزی (Mohammad Hossein Norouzi)
 * @since 2.0.0
 */
class NetworkSecurityManager private constructor(context: Context) {
    
    companion object {
        private const val TAG = "NetworkSecurity"
        private const val CONNECT_TIMEOUT = 30L
        private const val READ_TIMEOUT = 30L
        private const val WRITE_TIMEOUT = 30L
        private const val MAX_RETRIES = 3
        
        // Certificate pins for motmaenbash.ir (replace with actual pins)
        private const val MOTMAENBASH_PIN = "sha256/YLh1dUR9y6Kja30RrAn7JKnbQG/uEtLMkBgFF2Fuihg="
        private const val BACKUP_PIN = "sha256/C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M="
        
        @Volatile
        private var INSTANCE: NetworkSecurityManager? = null
        
        fun getInstance(context: Context): NetworkSecurityManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkSecurityManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val certificatePinner = CertificatePinner.Builder()
        .add("motmaenbash.ir", MOTMAENBASH_PIN)
        .add("motmaenbash.ir", BACKUP_PIN)
        .add("api.motmaenbash.ir", MOTMAENBASH_PIN)
        .add("api.motmaenbash.ir", BACKUP_PIN)
        .build()
    
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        if (BuildConfig.DEBUG) {
            Log.d(TAG, sanitizeLogMessage(message))
        }
    }.apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.HEADERS
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    
    private val securityInterceptor = Interceptor { chain ->
        val request = chain.request()
        val secureRequest = request.newBuilder()
            .header("User-Agent", "MotmaenBash-Android/${BuildConfig.VERSION_NAME}")
            .header("Accept", "application/json")
            .header("X-Requested-With", "XMLHttpRequest")
            .build()
        
        chain.proceed(secureRequest)
    }
    
    private val retryInterceptor = Interceptor { chain ->
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        
        repeat(MAX_RETRIES) { attempt ->
            try {
                response = chain.proceed(request)
                if (response?.isSuccessful == true) {
                    return@Interceptor response!!
                }
                response?.close()
            } catch (e: IOException) {
                exception = e
                if (attempt == MAX_RETRIES - 1) {
                    throw e
                }
                // Wait before retry
                Thread.sleep(1000L * (attempt + 1))
            }
        }
        
        response ?: throw (exception ?: IOException("Unknown network error"))
    }
    
    private val client = OkHttpClient.Builder()
        .certificatePinner(certificatePinner)
        .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS))
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(securityInterceptor)
        .addInterceptor(retryInterceptor)
        .addInterceptor(loggingInterceptor)
        .hostnameVerifier { hostname, session ->
            // Additional hostname verification
            val validHosts = listOf(
                "motmaenbash.ir",
                "api.motmaenbash.ir",
                "www.motmaenbash.ir"
            )
            validHosts.contains(hostname)
        }
        .build()
    
    /**
     * Makes a secure GET request to the specified URL.
     * 
     * @param url The URL to request
     * @param headers Optional headers to include
     * @return Response object
     * @throws SecurityException if URL is not allowed
     * @throws IOException if network request fails
     */
    fun makeSecureGetRequest(url: String, headers: Map<String, String>? = null): Response {
        validateUrl(url)
        
        val requestBuilder = Request.Builder()
            .url(url)
            .get()
        
        headers?.forEach { (key, value) ->
            requestBuilder.header(key, value)
        }
        
        val request = requestBuilder.build()
        
        return try {
            val response = client.newCall(request).execute()
            logSecurityEvent("GET request successful", url)
            response
        } catch (e: SSLPeerUnverifiedException) {
            logSecurityEvent("Certificate pinning failed", url)
            throw SecurityException("Certificate pinning validation failed", e)
        } catch (e: CertificateException) {
            logSecurityEvent("Certificate validation failed", url)
            throw SecurityException("Certificate validation failed", e)
        } catch (e: IOException) {
            logSecurityEvent("Network request failed", url)
            throw e
        }
    }
    
    /**
     * Makes a secure POST request to the specified URL.
     * 
     * @param url The URL to request
     * @param body The request body
     * @param headers Optional headers to include
     * @return Response object
     * @throws SecurityException if URL is not allowed
     * @throws IOException if network request fails
     */
    fun makeSecurePostRequest(
        url: String,
        body: RequestBody,
        headers: Map<String, String>? = null
    ): Response {
        validateUrl(url)
        
        val requestBuilder = Request.Builder()
            .url(url)
            .post(body)
        
        headers?.forEach { (key, value) ->
            requestBuilder.header(key, value)
        }
        
        val request = requestBuilder.build()
        
        return try {
            val response = client.newCall(request).execute()
            logSecurityEvent("POST request successful", url)
            response
        } catch (e: SSLPeerUnverifiedException) {
            logSecurityEvent("Certificate pinning failed", url)
            throw SecurityException("Certificate pinning validation failed", e)
        } catch (e: CertificateException) {
            logSecurityEvent("Certificate validation failed", url)
            throw SecurityException("Certificate validation failed", e)
        } catch (e: IOException) {
            logSecurityEvent("Network request failed", url)
            throw e
        }
    }
    
    /**
     * Creates a JSON request body.
     * 
     * @param json The JSON string
     * @return RequestBody for JSON
     */
    fun createJsonRequestBody(json: String): RequestBody {
        return RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            json
        )
    }
    
    /**
     * Validates if the URL is allowed for requests.
     * 
     * @param url The URL to validate
     * @throws SecurityException if URL is not allowed
     */
    private fun validateUrl(url: String) {
        val allowedDomains = listOf(
            "motmaenbash.ir",
            "api.motmaenbash.ir",
            "www.motmaenbash.ir"
        )
        
        try {
            val urlObj = java.net.URL(url)
            
            // Check protocol
            if (urlObj.protocol != "https") {
                throw SecurityException("Only HTTPS URLs are allowed")
            }
            
            // Check domain
            val hostname = urlObj.host.lowercase()
            if (!allowedDomains.any { domain -> 
                hostname == domain || hostname.endsWith(".$domain") 
            }) {
                throw SecurityException("URL domain not allowed: $hostname")
            }
            
        } catch (e: Exception) {
            throw SecurityException("Invalid URL: $url", e)
        }
    }
    
    /**
     * Sanitizes log messages to prevent information disclosure.
     * 
     * @param message The log message to sanitize
     * @return Sanitized log message
     */
    private fun sanitizeLogMessage(message: String): String {
        return message
            .replace(Regex("Authorization: Bearer [A-Za-z0-9\\-._~+/]+=*"), "Authorization: Bearer [REDACTED]")
            .replace(Regex("\"password\"\\s*:\\s*\"[^\"]+\""), "\"password\":\"[REDACTED]\"")
            .replace(Regex("\"token\"\\s*:\\s*\"[^\"]+\""), "\"token\":\"[REDACTED]\"")
            .replace(Regex("\"api_key\"\\s*:\\s*\"[^\"]+\""), "\"api_key\":\"[REDACTED]\"")
    }
    
    /**
     * Logs security-related events.
     * 
     * @param event The security event description
     * @param url The URL involved (optional)
     */
    private fun logSecurityEvent(event: String, url: String? = null) {
        if (BuildConfig.DEBUG) {
            val message = if (url != null) {
                "$event - URL: ${sanitizeUrl(url)}"
            } else {
                event
            }
            Log.d(TAG, message)
        }
    }
    
    /**
     * Sanitizes URL for logging.
     * 
     * @param url The URL to sanitize
     * @return Sanitized URL
     */
    private fun sanitizeUrl(url: String): String {
        return try {
            val urlObj = java.net.URL(url)
            "${urlObj.protocol}://${urlObj.host}${urlObj.path}"
        } catch (e: Exception) {
            "[INVALID_URL]"
        }
    }
    
    /**
     * Checks if the network security manager is properly configured.
     * 
     * @return true if configuration is valid
     */
    fun isConfigurationValid(): Boolean {
        return try {
            // Test certificate pinning configuration
            certificatePinner.findMatchingPins("motmaenbash.ir").isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Gets network security configuration details.
     * 
     * @return Map of configuration details
     */
    fun getSecurityConfiguration(): Map<String, String> {
        return mapOf(
            "tls_version" to "TLS 1.2+",
            "certificate_pinning" to "enabled",
            "hostname_verification" to "enabled",
            "connect_timeout" to "${CONNECT_TIMEOUT}s",
            "read_timeout" to "${READ_TIMEOUT}s"
        )
    }
    
    /**
     * Cleans up resources.
     */
    fun cleanup() {
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
    }
}