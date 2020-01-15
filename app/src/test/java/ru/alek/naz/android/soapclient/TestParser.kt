package ru.alek.naz.android.soapclient

import org.junit.Assert
import org.junit.Before
import org.junit.ComparisonFailure
import org.junit.Test
import ru.alek.naz.android.soapclient.parse.serialize.normilizeForTest
import ru.sportmaster.android.tools.soap.*
import ru.sportmaster.android.tools.soap.parse.XmlObject
import ru.sportmaster.android.tools.soap.parse.deserialize.EnvelopeXmlObjectBuilder
import ru.sportmaster.android.tools.soap.parse.deserialize.SoapXmlObjectElementParser
import ru.sportmaster.android.tools.soap.parse.serialize.*
import ru.sportmaster.android.tools.xml.format.XmlStringBuilder
import ru.sportmaster.android.tools.xml.parse.ComplexXmlElement
import ru.sportmaster.android.tools.xml.parse.XmlStringByIndexParser
import timber.log.Timber

class TestParser {

    @Before
    fun setUp() {
        Timber.plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                println(message)
            }
        })
    }

    @Test
    fun testParse() {
        val parser = XmlObjectStringBuilder()

        val data = xmlObject()

        val builder = XmlStringBuilder()
        parser.append(builder, data)
        val xml: String = builder.buildString()

        println(xml)
        val expect = "<XmlSingleObject>\n" +
                "  <id>11</id>\n" +
                "  <sum>123.44</sum>\n" +
                "  <text>superpupertext</text>\n" +
                "  <flag>true</flag>\n" +
                "  <list>\n" +
                "    <XmlListItemObject>\n" +
                "      <text>item1</text>\n" +
                "      <hack>false</hack>\n" +
                "    </XmlListItemObject>\n" +
                "    <XmlListItemObject>\n" +
                "      <text>item2</text>\n" +
                "      <hack>true</hack>\n" +
                "    </XmlListItemObject>\n" +
                "    <XmlListItemObject>\n" +
                "      <text>item3</text>\n" +
                "      <hack>false</hack>\n" +
                "    </XmlListItemObject>\n" +
                "  </list>\n" +
                "  <inner>\n" +
                "    <inner>\n" +
                "      <name>superinnner</name>\n" +
                "      <count>3</count>\n" +
                "    </inner>\n" +
                "  </inner>\n" +
                "</XmlSingleObject>"


        assertEqualsString(expect, xml)
    }

    @Test
    fun testEmptyList() {

        val xml =
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:pkc=\"http://pkCards/\">\n" +
                    "  <soapenv:Header/>\n" +
                    "  <soapenv:Body>\n" +
                    "<Result>" +
                    "<code>111</code>" +
                    "<errors/>" +
                    "</Result>" +
                    "  </soapenv:Body>\n" +
                    "</soapenv:Envelope>"

        val xmlStringParser = XmlStringByIndexParser()
        val objectBuilder = EnvelopeXmlObjectBuilder(SoapXmlObjectElementParser())
        val element = xmlStringParser.parse(xml) as ComplexXmlElement

        val result = objectBuilder.build(element, Result::class)

        Assert.assertEquals(111, result?.code ?: 0)
        Assert.assertEquals(emptyList<Any>(), result?.errors ?: throw RuntimeException("Fail"))

    }

    data class Result(
        val code: Int,
        val errors: List<ResultError>
    ) : XmlObject

    data class ResultError(val text: String) : XmlObject

    @Test
    fun testEnvelope() {
        val parser = EnvelopeXmlStringBuilder(
            bodyNamespace = Namespace(
                "pkc",
                "http://pkCards/"
            )
        )

        val envelope = Envelope(
            body = XmlRootObject("test", 5)
        )
        val result = parser.build(envelope)

        val expected =
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:pkc=\"http://pkCards/\">\n" +
                    "  <soapenv:Header/>\n" +
                    "  <soapenv:Body>\n" +
                    "    <pkc:XmlRootObject>\n" +
                    "      <pkc:name>test</pkc:name>\n" +
                    "      <pkc:count>5</pkc:count>\n" +
                    "    </pkc:XmlRootObject>\n" +
                    "  </soapenv:Body>\n" +
                    "</soapenv:Envelope>"

        assertEqualsString(expected, result)
    }

    @Test
    fun testEnvelope2() {
        val parser = EnvelopeXmlStringBuilder(
            bodyNamespace = Namespace(
                "pkc",
                "http://pkCards/",
                true
            )
        )

        val envelope = Envelope(
            body = XmlRootObject2("test", 5)
        )
        val result = parser.build(envelope)

        val expected =
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:pkc=\"http://pkCards/\">\n" +
                    "  <soapenv:Header/>\n" +
                    "  <soapenv:Body>\n" +
                    "    <pkc:XmlRootObject2>\n" +
                    "      <name>test</name>\n" +
                    "      <count>5</count>\n" +
                    "    </pkc:XmlRootObject2>\n" +
                    "  </soapenv:Body>\n" +
                    "</soapenv:Envelope>"

        assertEqualsString(expected, result)
    }

    @Test
    fun test_readTextWithoutComments() {
        val text = readLoadPortionXml()
        println(text)
    }

    private fun readLoadPortionXml() =
        TestUtils.readTextWithoutComments("LOAD_PORTION_for_mrm.xml")

    private fun assertEqualsString(expect: String, actual: String) {
        println(actual)
        val expectFormat = expect.normilizeForTest()
        val actualFormat = actual.normilizeForTest()

        var i = -1
        for (expectChar in expectFormat) {
            i++
            val actualChar = actualFormat[i]

            if (actualChar != expectChar) {
                val expectpart = expectFormat.substring(i)
                val actualpart = actualFormat.substring(i)
                throw ComparisonFailure(
                    "diff in index=$i\n" +
                            "   $expectpart\n" +
                            "   $actualpart", expectFormat, actualFormat
                )
            }
        }

        Assert.assertEquals(expectFormat, actualFormat)
    }

    private fun xmlObject(): XmlSingleObject {

        return XmlSingleObject(
            id = 11,
            sum = 123.44,
            text = "superpupertext",
            flag = true,
            list = listOf(
                XmlListItemObject(
                    text = "item1",
                    hack = false
                ),
                XmlListItemObject(
                    text = "item2",
                    hack = true
                ),
                XmlListItemObject(
                    text = "item3",
                    hack = false
                )
            ),
            inner = DoubleInnerXmlObject(
                inner = XmlInnerObject(
                    name = "superinnner",
                    count = 3
                )
            )
        )
    }

}


