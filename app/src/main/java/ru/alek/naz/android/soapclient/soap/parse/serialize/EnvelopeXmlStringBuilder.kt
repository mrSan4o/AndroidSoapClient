package ru.sportmaster.android.tools.soap.parse.serialize

import ru.sportmaster.android.tools.soap.parse.*
import ru.sportmaster.android.tools.soap.parse.format.FieldFormat
import ru.sportmaster.android.tools.soap.parse.format.NoFieldFormat
import ru.sportmaster.android.tools.xml.format.XmlStringBuilder

class EnvelopeXmlStringBuilder(
    private val bodyNamespace: Namespace = Namespace(
        "",
        ""
    ),
    private val defaultBodyFieldFormat: FieldFormat = NoFieldFormat,
    private val defaultBodyDoubleFormat: DoubleFormat = NoDoubleFormat,
    private val defaultDatePattern: String = "yyyy-MM-dd HH:mm:ss.SSS"
) {
    private val bodyTag = "Body"
    private val headerTag = "Header"

    fun build(envelope: Envelope): String {

        val rootTag = tag(envelope)
        val envelopeAnnotation = envelope.getEnvelopeElement()

        val builder = XmlStringBuilder(
            namespace = envelopeAnnotation.namespace
        )

        val body = envelope.body

        builder.rootTag(
            rootTag,
            arrayOf(
                namespace(envelopeAnnotation),
                namespace(bodyNamespace.name, bodyNamespace.url)
            )
        )

        builder.emptyTag(headerTag)

        builder.openTag(bodyTag)
        append(builder, body)
        builder.closeTag(bodyTag)

        builder.closeTag(rootTag)

        return builder.buildString()
    }

    private fun append(
        builder: XmlStringBuilder,
        body: XmlObject
    ) {
        val tagNamespace = if (bodyNamespace.onlyBodyTagNamespace) {
            ""
        } else {
            bodyNamespace.name
        }
        val xmlStringBuilder = builder.copy(
            namespace = tagNamespace
        )
        val soapParser = XmlObjectStringBuilder(
            fieldFormat = defaultBodyFieldFormat,
            datePattern = defaultDatePattern,
            doubleFormat = defaultBodyDoubleFormat
        )
        val rootTagNamespace = if (bodyNamespace.onlyBodyTagNamespace) {
            bodyNamespace.name
        } else {
            ""
        }
        soapParser.append(xmlStringBuilder, body, rootTagNamespace)
    }

    private fun tag(any: Any) =
        any.javaClass.simpleName

    private fun namespace(
        namespace: String,
        namespaceUrl: String
    ) =
        "xmlns:$namespace=\"$namespaceUrl\""

    private fun namespace(envelopeAnnotation: EnvelopeElement) =
        "xmlns:${envelopeAnnotation.namespace}=\"${envelopeAnnotation.namespaceUrl}\""

}

@EnvelopeElement(
    namespace = "soapenv",
    namespaceUrl = "http://schemas.xmlsoap.org/soap/envelope/"
)
data class Envelope(
    val header: EnvelopeHeader = EnvelopeHeader(),
    val body: XmlObject
) : XmlObject

class EnvelopeHeader

data class Namespace(
    val name: String,
    val url: String,
    val onlyBodyTagNamespace: Boolean = false
)

fun Envelope.getSoapRootElement(): SoapRootElement {
    return this.body.getSoapRootElement()
}

internal fun XmlObject.getSoapRootElement() =
    (this.javaClass.getAnnotation(SoapRootElement::class.java)
        ?: throw IllegalStateException("SoapRootElement annotation not found on class ${javaClass.simpleName}"))

internal fun XmlObject.getSoapObjectElement() =
    (this.javaClass.getAnnotation(SoapObjectElement::class.java)
        ?: throw IllegalStateException("SoapObjectElement annotation not found on class ${javaClass.simpleName}"))

internal fun Envelope.getEnvelopeElement() =
    (this.javaClass.getAnnotation(EnvelopeElement::class.java)
        ?: throw IllegalStateException("EnvelopeElement annotation not found on class ${javaClass.simpleName}"))