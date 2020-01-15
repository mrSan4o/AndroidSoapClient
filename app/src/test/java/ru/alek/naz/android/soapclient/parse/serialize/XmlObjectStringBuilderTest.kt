package ru.alek.naz.android.soapclient.parse.serialize

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ru.alek.naz.android.soapclient.*
import ru.sportmaster.android.tools.soap.parse.*
import ru.sportmaster.android.tools.soap.parse.format.UpperSlashFieldFormat
import ru.sportmaster.android.tools.soap.parse.serialize.Envelope
import ru.sportmaster.android.tools.soap.parse.serialize.EnvelopeXmlStringBuilder
import ru.sportmaster.android.tools.soap.parse.serialize.Namespace
import ru.sportmaster.android.tools.soap.parse.serialize.XmlObjectStringBuilder
import ru.sportmaster.android.tools.xml.format.XmlStringBuilder
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class XmlObjectStringBuilderTest {

    @Before
    fun setUp() {
        Timber.plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                println(message)
            }
        })
    }

    @Test
    fun testDouble() {
        val format = format(12.23111111)
        println("format "+format)
        assertEquals(12.23, format, 0.0001)
    }

    private fun format(value: Double): Double {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    @Test
    fun test_DefaultFormatters() {
        val data = TestData(
            num = 11,
            dtmCreate = TestUtils.buildDate(2019, 11, 11, 15, 35, 2, 539),
            wareId = 111111,
            retailPrice = 123.3456
        )
        val builder = XmlStringBuilder(
            namespace = "pi"
        )

        val soapParser = XmlObjectStringBuilder(
            fieldFormat = UpperSlashFieldFormat,
            doubleFormat = Round2DoubleFormat
        )
        soapParser.append(builder, data)

        TestUtils.assertEqualsString(
            "<pi:TEST_DATA>\n" +
                "   <pi:NUM>11</pi:NUM>\n" +
                "   <pi:DTM_CREATE>2019-11-11 15:35:02.539 MSK</pi:DTM_CREATE>\n" +
                "   <pi:ID_WARE>111111</pi:ID_WARE>\n" +
                "   <pi:RETAIL_PRICE>123.35</pi:RETAIL_PRICE>\n" +
                "</pi:TEST_DATA>",
                builder.buildString()
            )

    }
    @Test
    fun test_Inner_CustomName() {
        val data = RootObject(
            inner = RootInnerObject(
                code = 123,
                name = "test name"
            )
        )

        val xmlObjectStringBuilder = XmlObjectStringBuilder(
            fieldFormat = UpperSlashFieldFormat,
            doubleFormat = Round2DoubleFormat
        )
        val writer = XmlStringBuilder(namespace = "pi")
        xmlObjectStringBuilder.append(writer, data)
        val buildString = writer.buildString()

        println(buildString)
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


        TestUtils.assertEqualsString(expect, xml)
    }

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

        TestUtils.assertEqualsString(expected, result)
    }

    @Test
    fun testEnvelope2() {
        val parser = EnvelopeXmlStringBuilder(
            bodyNamespace = Namespace(
                "pkc",
                "http://pkCards/"
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

        TestUtils.assertEqualsString(expected, result)
    }


    @Test
    fun test_readTextWithoutComments() {
        var text = readLoadPortionXml()
        println(text)
    }

    private fun readLoadPortionXml() =
        TestUtils.readTextWithoutComments("LOAD_PORTION_for_mrm.xml")



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
fun String.normilizeForTest():String = this.replace(" ", "").replace("\n", "").replace("\r", "")

@SoapRootElement(method = "test")
@SoapObjectElement(orderedFields = ["num", "dtmCreate", "wareId", "retailPrice"])
data class TestData(
//    val id: Long,
    val num: Int,
    @DateFormatField(pattern = "yyyy-MM-dd HH:mm:ss.SSS z")
    val dtmCreate: Date,
    @SoapElement(name = "ID_WARE")
    val wareId: Long,
    val retailPrice: Double

) : XmlObject

@SoapRootElement(method = "root")
@SoapObjectElement(orderedFields = ["inner"])
data class RootObject(

    @SoapElement(name = "INNER")
    val inner:RootInnerObject
):XmlObject

@SoapObjectElement(orderedFields = ["code", "name"])
data class RootInnerObject(
    val code:Int,
    val name:String
):XmlObject