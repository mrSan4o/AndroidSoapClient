package ru.alek.naz.android.soapclient.parse.deserialize

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.sportmaster.android.tools.soap.TestUtils
import ru.sportmaster.android.tools.xml.parse.*
import timber.log.Timber

class XmlStringJsoupParserTest {
    val parser: XmlStringParser = XmlStringJsoupParser()

    @Before
    fun setUp() {
        Timber.plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                println(message)
            }
        })
    }

    @Test
    fun testIndex() {
        val string = "bbacca111a"

        var n = -1
        for (c in string) {
            n++
            print("$n:$c ")
        }
        println()

        val i = string.indexOf('a')
        println(i)
        val index = string.indexOf('a', i + 1)
        println(index)
    }

    @Test
    fun testSingleParse() {

        assertEquals(
            SingleXmlElement("tag", "111"),
            parser.parse("<tag>111</tag>")
        )
        assertEquals(
            SingleXmlElement("tag", "111"),
            parser.parse("<pkc:tag>111</pkc:tag>")
        )

    }

    @Test
    fun testHardcoreParse() {

        val input = TestUtils.readTextWithoutComments("LOAD_PORTION_for_mrm.xml")
        val parse = parser.parse(input) as ComplexXmlElement


        print(parse)
    }

    @Test
    fun testParseError() {

        val input = TestUtils.readTextWithoutComments("LOAD_PORTION_fail_response.xml")
        val parse = parser.parse(input) as ComplexXmlElement


        print(parse)
    }

    @Test
    fun testEmptyList() {

        val xml = "<Result>" +
                "<code>111</code>" +
                "<errors/>" +
                "</Result>"

        val xmlStringParser = XmlStringByIndexParser()
        val element = xmlStringParser.parse(xml) as ComplexXmlElement

        val tag = element.findTag("errors")

        assertTrue(tag is SingleXmlElement)
    }

    private fun print(multiElement: ComplexXmlElement) {
        println(multiElement.prettyString())
    }

    @Test
    fun testSingleParse_empty() {

        assertEquals(
            SingleXmlElement("tag", ""),
            parser.parse("<tag></tag>")
        )

        assertEquals(
            SingleXmlElement("tag", ""),
            parser.parse("<tag/>")
        )
    }

    @Test
    fun testSingleParse_tagWithAttributes() {

        assertEquals(
            ComplexXmlElement(
                "element",
                childs = listOf(
                    SingleXmlElement("header"),
                    SingleXmlElement("body", "111")
                )
            ),
            parser.parse(
                "<element xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:pi=\"http://xmlns.oracle.com/orawsv/VERA_WEB/PI_WEB_LOAD_MRM_PORTION\">" +
                        "<header/>" +
                        "<body>111</body>" +
                        "</element>"
            )
        )

    }

    @Test
    fun testSingleParse_emptyAndNot() {

        assertEquals(
            ComplexXmlElement(
                "element",
                childs = listOf(
                    SingleXmlElement("header"),
                    SingleXmlElement("body", "111")
                )
            ),
            parser.parse(
                "<element>" +
                        "<header/>" +
                        "<body>111</body>" +
                        "</element>"
            )
        )

    }

    @Test
    fun testObjectParse() {

        assertEquals(
            ComplexXmlElement(
                "object",
                childs = listOf(
                    SingleXmlElement("name", "text"),
                    SingleXmlElement("age", "27"),
                    SingleXmlElement("flag", "true")
                )
            ),
            parser.parse(
                "<object>" +
                        "   <name>text</name>" +
                        "   <age>27</age>" +
                        "   <flag>true</flag>" +
                        "</object>"
            )
        )

    }

    @Test
    fun testObjectListParse() {

        assertEquals(
            ComplexXmlElement(
                "list",
                childs = listOf(
                    ComplexXmlElement(
                        "item",
                        childs = listOf(
                            SingleXmlElement("name", "text1"),
                            SingleXmlElement("value", "111")
                        )
                    ),
                    ComplexXmlElement(
                        "item",
                        childs = listOf(
                            SingleXmlElement("name", "text2"),
                            SingleXmlElement("value", "222")
                        )
                    ),
                    ComplexXmlElement(
                        "item",
                        childs = listOf(
                            SingleXmlElement("name", "text3"),
                            SingleXmlElement("value", "333")
                        )
                    )
                )
            ),
            parser.parse(
                TestUtils.readFileText("test_list.xml")
            )
        )

    }

    @Test
    fun testInnerObjectParse() {

        assertEquals(
            ComplexXmlElement(
                "object",
                childs = listOf(
                    ComplexXmlElement(
                        "inner",
                        childs = listOf(
                            SingleXmlElement("name", "text"),
                            SingleXmlElement("age", "27"),
                            SingleXmlElement("flag", "true")
                        )
                    )
                )
            ),
            parser.parse(
                "<object>" +
                        "<inner>" +
                        "   <name>text</name>" +
                        "   <age>27</age>" +
                        "   <flag>true</flag>" +
                        "</inner>" +
                        "</object>"
            )
        )

    }

    @Test
    fun testParse_usecase() {
        val input = "<?xml version=\"1.0\" ?>\n" +
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                " <soap:Body>\n" +
                "   <soap:Fault>\n" +
                "     <faultcode>soap:Client</faultcode>\n" +
                "     <faultstring>Error processing input</faultstring>\n" +
                "     <detail>\n" +
                "      <OracleErrors xmlns=\"http://xmlns.oracle.com/orawsv/faults\">\n" +
                "       <OracleError>\n" +
                "        <ErrorNumber>ORA-19202</ErrorNumber>\n" +
                "        <Message><![CDATA[Error occurred in XML processing]]></Message>\n" +
                "      </OracleError>\n" +
                "       <OracleError>\n" +
                "        <ErrorNumber>ORA-20000</ErrorNumber>\n" +
                "        <Message><![CDATA[Кассового места вида Касса в РМ Лайт в Активном или Будущем состоянии с кодом 333 не существует.]]></Message>\n" +
                "      </OracleError>\n" +
                "       <OracleError>\n" +
                "        <ErrorNumber>ORA-06512</ErrorNumber>\n" +
                "        <Message><![CDATA[at \"TEST2_OWNER.PK_SHP_LOAD_SALES\", line 70]]></Message>\n" +
                "      </OracleError>\n" +
                "       <OracleError>\n" +
                "        <ErrorNumber>ORA-06512</ErrorNumber>\n" +
                "        <Message><![CDATA[at \"TEST2_OWNER.PK_SHP_LOAD_SALES\", line 158]]></Message>\n" +
                "      </OracleError>\n" +
                "       <OracleError>\n" +
                "        <ErrorNumber>ORA-06512</ErrorNumber>\n" +
                "        <Message><![CDATA[at \"TEST2_OWNER.PK_SHP_LOAD_SALES\", line 789]]></Message>\n" +
                "      </OracleError>\n" +
                "       <OracleError>\n" +
                "        <ErrorNumber>ORA-06512</ErrorNumber>\n" +
                "        <Message><![CDATA[at \"TEST2_OWNER.PK_SHP_LOAD_SALES\", line 12199\n" +
                "\n" +
                "***]]></Message>\n" +
                "      </OracleError>\n" +
                "       <OracleError>\n" +
                "        <ErrorNumber>CMN-E-UNKNOWN\n" +
                "***\n" +
                "31 C</ErrorNumber>\n" +
                "        <Message><![CDATA[Projects\\sm-code\\shop\\s-unload\\pk_shp_load_sales.sp4]]></Message>\n" +
                "      </OracleError>\n" +
                "       <OracleError>\n" +
                "        <ErrorNumber>ORA-06512</ErrorNumber>\n" +
                "        <Message><![CDATA[at \"TEST]]></Message>\n" +
                "      </OracleError>\n" +
                "      </OracleErrors>\n" +
                "     </detail>\n" +
                "   </soap:Fault>\n" +
                " </soap:Body>\n" +
                "</soap:Envelope>"

        val parse = parser.parse(input)

        println(parse.prettyString())
    }

    @Test
    fun testDoubleTag() {
        assertEquals(
            ComplexXmlElement(
                "object",
                childs = listOf(
                    ComplexXmlElement(
                        "data",
                        childs = listOf(
                            ComplexXmlElement(
                                "data",
                                childs = listOf(
                                    SingleXmlElement("name", "text"),
                                    SingleXmlElement("age", "27"),
                                    SingleXmlElement("flag", "true")
                                )
                            )

                        )
                    )
                )
            ),
            parser.parse(
                "<object>" +
                        "<data>" +
                        "   <data>" +
                        "       <name>text</name>" +
                        "       <age>27</age>" +
                        "       <flag>true</flag>" +
                        "   </data>" +
                        "</data>" +
                        "</object>"
            )
        )

    }

    @Test
    fun testDoubleTag2() {

        val input = TestUtils.readFileText("error_response.xml")

        val document = parser.parse(input)

        println(document.prettyString())
    }

    @Test
    fun test_Tag_with_attributes() {

        assertEquals(
            ComplexXmlElement(
                "ClientLevel",
                listOf(
                    SingleXmlElement("BuySumma", "0"),
                    SingleXmlElement("Name", ""),
                    SingleXmlElement("RealCardTypeName", ""),
                    SingleXmlElement("NotExistAnketa", "0")
                )
            ),
            parser.parse(
                """
                            <ClientLevel>
                               <BuySumma>0</BuySumma>
                               <Name xsi:nil="true" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"/>
                               <RealCardTypeName xsi:nil="true" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"/>
                               <NotExistAnketa>0</NotExistAnketa>
                            </ClientLevel>
               """.trimIndent()
            )
        )
    }

    @Test
    fun testParse() {
        val parse = parser.parse(
            TestUtils.readFileText("double_tag.xml")
        )

        println(parse.prettyString())
    }
}