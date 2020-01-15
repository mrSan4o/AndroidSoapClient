package ru.sportmaster.android.tools.soap.send

import org.apache.commons.io.IOUtils
import ru.sportmaster.android.tools.soap.parse.XmlObject
import ru.sportmaster.android.tools.soap.parse.deserialize.EnvelopeXmlObjectBuilder
import ru.sportmaster.android.tools.soap.parse.serialize.Envelope
import ru.sportmaster.android.tools.soap.parse.serialize.EnvelopeXmlStringBuilder
import ru.sportmaster.android.tools.soap.parse.serialize.getSoapRootElement
import ru.sportmaster.android.tools.xml.parse.ComplexXmlElement
import ru.sportmaster.android.tools.xml.parse.XmlStringJsoupParser
import ru.sportmaster.android.tools.xml.parse.XmlStringParser
import ru.sportmaster.android.tools.xml.parse.prettyString
import timber.log.Timber
import java.nio.charset.StandardCharsets
import kotlin.reflect.KClass

class SoapParseClient(
    webServicesUrl: String,
    basicAuth: BasicAuth? = null,
    private val envelopeXmlStringBuilder: EnvelopeXmlStringBuilder,
    private val envelopeXmlObjectBuilder: EnvelopeXmlObjectBuilder,
    private val xmlStringParser: XmlStringParser = XmlStringJsoupParser(),
    private val logger: Logger = TimberLogger
) {


    private val client = SoapActionClient(webServicesUrl, basicAuth)


    fun <T : XmlObject, R : XmlObject> invoke(request: T, returnClass: KClass<R>): R? {
        val envelope = Envelope(body = request)
        val soapRootElement = envelope.getSoapRootElement()
        val methodName = soapRootElement.method

        val requestXml = envelopeXmlStringBuilder.build(envelope)
        logger("<<< request [$methodName] :\n$requestXml")
        val responseXml = invokeOnXml(methodName, requestXml)
        logger(">>> response [$methodName] RAW:\n$responseXml")
        val element = xmlStringParser.parse(responseXml) as ComplexXmlElement
        Timber.d(">>> response [$methodName] :\n${element.prettyString()}")

        return envelopeXmlObjectBuilder.build(element, returnClass)
    }

    private fun logger(message: String) {
        logger.log(message)
    }

    private fun invokeOnXml(methodName: String, requestXml: String): String {
        return client.invoke(methodName, requestXml.toByteArray()) {
            IOUtils.toString(it, StandardCharsets.UTF_8) ?: ""
        }
    }

    interface Logger {
        fun log(message: String)
    }

    private object TimberLogger : Logger {
        override fun log(message: String) {
            Timber.d(message)
        }
    }
}

