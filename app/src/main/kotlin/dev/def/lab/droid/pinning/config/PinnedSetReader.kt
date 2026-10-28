package dev.def.lab.droid.pinning.config

import java.io.IOException
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

data class PinnedSet(val host: String, val digests: List<String>)

object PinnedSetReader {
    fun read(parser: XmlPullParser, host: String): PinnedSet? {
        val digests = mutableListOf<String>()
        var hostSeen = false
        var inDomain = false
        var inPinSet = false
        return try {
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> when (parser.name) {
                        "domain" -> {
                            if (parser.nextText().trim() == host) {
                                hostSeen = true
                                inDomain = true
                            }
                        }
                        "pin-set" -> inPinSet = inDomain
                        "pin" -> {
                            val digest = parser.nextText().trim()
                            if (inPinSet && digest.isNotEmpty()) {
                                digests += digest
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> when (parser.name) {
                        "domain-config" -> {
                            inDomain = false
                            inPinSet = false
                        }
                    }
                }
                event = parser.next()
            }
            if (hostSeen && digests.isNotEmpty()) PinnedSet(host, digests) else null
        } catch (failure: XmlPullParserException) {
            null
        } catch (failure: IOException) {
            null
        }
    }
}
