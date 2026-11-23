package nu.milad.motmaenbash.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlUtilsTest {

    @Test
    fun extractDomain_multiPartPublicSuffixes_returnsRegistrableDomain() {
        assertEquals("phishing.co.ir", UrlUtils.extractDomain("https://phishing.co.ir/login"))
        assertEquals("phishing.co.ir", UrlUtils.extractDomain("https://sub.phishing.co.ir/path"))
        assertEquals("my.gov.ir", UrlUtils.extractDomain("https://service.my.gov.ir"))
        assertEquals("uni.ac.ir", UrlUtils.extractDomain("http://portal.uni.ac.ir/index.html"))
        assertEquals("example.co.uk", UrlUtils.extractDomain("https://www.example.co.uk/test"))
    }

    @Test
    fun extractDomain_standardDomainsAndPortsAndIpv4_handlesCorrectly() {
        assertEquals("example.com", UrlUtils.extractDomain("https://www.example.com:8080/path"))
        assertEquals("shaparak.ir", UrlUtils.extractDomain("https://bpm.shaparak.ir/pgwchannel"))
        assertEquals("192.168.1.1", UrlUtils.extractDomain("http://192.168.1.1:8080/login"))
        assertEquals("evil.com", UrlUtils.extractDomain("https://shaparak.ir@evil.com/pay"))
    }

    @Test
    fun isShaparakSubdomain_validAndSpoofedUrls() {
        assertTrue(UrlUtils.isShaparakSubdomain("https://bpm.shaparak.ir/pgwchannel/startpay.mellat"))
        assertTrue(UrlUtils.isShaparakSubdomain("bpm.shaparak.ir/pgwchannel"))
        assertTrue(UrlUtils.isShaparakSubdomain("https://sep.shaparak.ir"))

        // Reject root domain, plain HTTP, userinfo (@) spoofing, port, and homographs
        assertFalse(UrlUtils.isShaparakSubdomain("https://shaparak.ir"))
        assertFalse(UrlUtils.isShaparakSubdomain("http://bpm.shaparak.ir"))
        assertFalse(UrlUtils.isShaparakSubdomain("https://bpm.shaparak.ir@evil.com/pay"))
        assertFalse(UrlUtils.isShaparakSubdomain("https://bpm.shaparak.ir:8443/pay"))
        assertFalse(UrlUtils.isShaparakSubdomain("https://evil.com/https://bpm.shaparak.ir"))
        assertFalse(UrlUtils.isShaparakSubdomain("https://bpm.shɑpɑrɑk.ir"))
    }

    @Test
    fun isHomographPhishingUrl_detectsLookalikeDomains() {
        // Latin small letter alpha (ɑ) and Cyrillic small letter a (а)
        assertTrue(UrlUtils.isHomographPhishingUrl("https://bpm.shɑpɑrɑk.ir/pay"))
        assertTrue(UrlUtils.isHomographPhishingUrl("https://shаpаrаk.ir"))
        assertTrue(UrlUtils.isHomographPhishingUrl("https://mellаt.ir"))

        // Legitimate domains should not be flagged as homographs
        assertFalse(UrlUtils.isHomographPhishingUrl("https://bpm.shaparak.ir/pay"))
        assertFalse(UrlUtils.isHomographPhishingUrl("https://mellat.ir"))
        assertFalse(UrlUtils.isHomographPhishingUrl("https://motmaenbash.ir"))
    }
}
