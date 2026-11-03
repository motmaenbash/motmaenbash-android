package nu.milad.motmaenbash.utils

import android.net.http.X509TrustManagerExtensions
import android.util.LruCache
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Checks that a payment gateway serves a certificate that chains up to one of Android's
 * system root certificates.
 *
 * A root certificate the user has installed (for example one handed out to bank customers)
 * can issue a valid-looking certificate for any website, and browsers such as Chrome trust
 * these roots. So a gateway page secured by such a root may be intercepted even though the
 * browser shows a padlock.
 */
object GatewayCertificateChecker {

    enum class Status {
        /** The certificate chains up to a system root certificate. */
        TRUSTED,

        /** The certificate is only trusted because of a certificate the user installed. */
        USER_INSTALLED_ROOT,

        /** The certificate isn't trusted at all or doesn't belong to the host. */
        UNTRUSTED,

        /** The check couldn't be done, e.g. there is no connection. */
        UNKNOWN
    }

    private const val PORT = 443
    private const val TIMEOUT_MS = 8_000
    private const val CACHE_DURATION_MS = 5 * 60 * 1000L

    // Failed checks are retried sooner, but not on every page event
    private const val UNKNOWN_CACHE_DURATION_MS = 30 * 1000L

    // host -> (status, expiry time)
    private val cache = LruCache<String, Pair<Status, Long>>(32)

    /**
     * Connects to the host and checks its certificate chain.
     * This blocks, so it must be called off the main thread.
     */
    fun check(host: String): Status {
        cache.get(host)?.let { (status, expiresAt) ->
            if (System.currentTimeMillis() < expiresAt) return status
        }

        val status = try {
            checkCertificateChain(host)
        } catch (_: Exception) {
            Status.UNKNOWN
        }

        val cacheDuration =
            if (status == Status.UNKNOWN) UNKNOWN_CACHE_DURATION_MS else CACHE_DURATION_MS
        cache.put(host, status to System.currentTimeMillis() + cacheDuration)
        return status
    }

    private fun checkCertificateChain(host: String): Status {
        // Trust both system and user-installed roots, then look at which one the chain ends at
        val keyStore = KeyStore.getInstance("AndroidCAStore").apply { load(null) }
        val trustManager =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                .apply { init(keyStore) }
                .trustManagers
                .filterIsInstance<X509TrustManager>()
                .first()
        val trustManagerExtensions = X509TrustManagerExtensions(trustManager)
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(trustManager), null)
        }

        val peerCertificates = try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, PORT), TIMEOUT_MS)
                socket.soTimeout = TIMEOUT_MS

                val sslSocket =
                    sslContext.socketFactory.createSocket(socket, host, PORT, true) as SSLSocket
                sslSocket.use {
                    sslSocket.startHandshake()
                    if (!HttpsURLConnection.getDefaultHostnameVerifier()
                            .verify(host, sslSocket.session)
                    ) {
                        return Status.UNTRUSTED
                    }
                    sslSocket.session.peerCertificates
                        .filterIsInstance<X509Certificate>()
                        .toTypedArray()
                }
            }
        } catch (e: IOException) {
            return if (e.hasCause<CertificateException>()) Status.UNTRUSTED else Status.UNKNOWN
        }

        val trustedChain =
            trustManagerExtensions.checkServerTrusted(peerCertificates, "GENERIC", host)
        return if (trustedChain.any { trustManagerExtensions.isUserAddedCertificate(it) }) {
            Status.USER_INSTALLED_ROOT
        } else {
            Status.TRUSTED
        }
    }

    private inline fun <reified T : Throwable> Throwable.hasCause(): Boolean =
        generateSequence(this) { it.cause }.any { it is T }
}
