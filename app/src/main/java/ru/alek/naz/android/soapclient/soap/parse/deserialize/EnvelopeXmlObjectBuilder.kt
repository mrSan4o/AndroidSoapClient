package ru.sportmaster.android.tools.soap.parse.deserialize

import ru.sportmaster.android.tools.soap.parse.XmlObject
import ru.sportmaster.android.tools.xml.parse.ComplexXmlElement
import ru.sportmaster.android.tools.xml.parse.SingleXmlElement
import ru.sportmaster.android.tools.xml.parse.findTag
import ru.sportmaster.android.tools.xml.parse.value
import kotlin.reflect.KClass

class EnvelopeXmlObjectBuilder(
    private val parser: XmlObjectParser
) {

    fun <R : XmlObject> build(envelopeElement: ComplexXmlElement, returnClass: KClass<R>): R? {
        if (!envelopeElement.name.cutNamespace().equals("envelope", true)) {
            throw SoapResponseFormatException()
        }
        val bodyElement = envelopeElement.findTag("body")
        if (bodyElement == null || bodyElement !is ComplexXmlElement) {
            if (bodyElement is SingleXmlElement) {
                return null
            }
            throw SoapResponseFormatException()
        }
        if (bodyElement.childs.size > 1
            || (bodyElement.childs.size == 1 && bodyElement.childs[0] !is ComplexXmlElement)
        ) {
            throw SoapResponseFormatException()
        }
        if (bodyElement.childs.isEmpty()){
            return null
        }
        val bodyValueElement = bodyElement.childs[0] as ComplexXmlElement

        if (bodyValueElement.name.cutNamespace().equals("fault", true)) {
            val faultcode = bodyValueElement.findTag("faultcode").value()
            val faultstring = bodyValueElement.findTag("faultstring").value()
            val detail = bodyValueElement.findTag("detail").value()

            throw SoapException(faultcode, faultstring, detail)
        } else {
            return parser.parse(bodyValueElement, returnClass)
        }

    }

}



class SoapResponseFormatException : RuntimeException("Error soap Envelope format!")

class SoapException(
    val code: String,
    val text: String,
    val detail: String
) : RuntimeException("[$code] '$text':\n$detail")