package ru.alek.naz.android.soapclient.parse.deserialize

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.sportmaster.android.tools.soap.TestUtils
import ru.sportmaster.android.tools.soap.parse.XmlObject
import ru.sportmaster.android.tools.xml.parse.ComplexXmlElement
import ru.sportmaster.android.tools.xml.parse.XmlStringByIndexParser

class EnvelopeXmlObjectBuilderTest {

    val deserializer = EnvelopeXmlObjectBuilder(
        parser = SoapXmlObjectParser()
    )

    val xmlParser = XmlStringByIndexParser()

    @Test(expected = SoapException::class)
    fun test_parseFailResponse() {
        val text = TestUtils.readFileText("LOAD_PORTION_fail_response.xml")

        val element = xmlParser.parse(text)

        deserializer.build(element as ComplexXmlElement, ReturnType::class)
    }

    @Test
    fun test_parseSuccessResponse() {
        val text =
            "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                    "    <soap:Body>" +
                    "       <ReturnType>" +
                    "           <name>SUCCESS</name>" +
                    "       </ReturnType>" +
                    "</soap:Body>" +
                    "</soap:Envelope>"

        val element = xmlParser.parse(text)

        val type = deserializer.build(element as ComplexXmlElement, ReturnType::class)

        assertEquals("SUCCESS", type?.name)
    }
    @Test
    fun test_parseEmptyResponse() {
        val text =
            "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                    "    <soap:Body>" +
                    "</soap:Body>" +
                    "</soap:Envelope>"

        val element = xmlParser.parse(text)

        val type = deserializer.build(element as ComplexXmlElement, ReturnType::class)

        assertNull(type)

    }
}

data class ReturnType(
    val name: String
) : XmlObject