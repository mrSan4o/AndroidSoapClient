package ru.alek.naz.android.soapclient.parse.deserialize

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ru.sportmaster.android.tools.soap.parse.SoapElement
import ru.sportmaster.android.tools.soap.parse.XmlObject
import ru.sportmaster.android.tools.soap.parse.format.UpperSlashTagFormat
import ru.sportmaster.android.tools.xml.parse.ComplexXmlElement
import ru.sportmaster.android.tools.xml.parse.SingleXmlElement
import timber.log.Timber

class SoapObjectParserTest {
    val parser = SoapXmlObjectParser(tagFormat = UpperSlashTagFormat)

    @Before
    fun setUp() {
        Timber.plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                println(message)
            }
        })
    }

    @Test
    fun testSimpleObject() {

        assertEquals(
            SimpleObject(
                name = "name",
                number = 123,
                flag = true
            ),
            parser.parse(
                multiElement = ComplexXmlElement(
                    name = "element",
                    childs = listOf(
                        SingleXmlElement("name", "name"),
                        SingleXmlElement("number", "123"),
                        SingleXmlElement("flag", "true")

                    )
                ),
                returnClass = SimpleObject::class
            )
        )
    }

    @Test
    fun testSimpleObject_with_TagFormat() {

        assertEquals(
            SimpleObjectNames(
                personalName = "name",
                internalPhoneNumber = 123,
                someExecutionFlag = true
            ),
            parser.parse(
                multiElement = ComplexXmlElement(
                    name = "element",
                    childs = listOf(
                        SingleXmlElement(
                            "pkc:PERSONAL_NAME",
                            "name"
                        ),
                        SingleXmlElement(
                            "pkc:INTERNAL_PHONE_NUMBER",
                            "123"
                        ),
                        SingleXmlElement(
                            "pkc:SOME_EXECUTION_FLAG",
                            "true"
                        )

                    )
                ),
                returnClass = SimpleObjectNames::class
            )
        )
    }

    @Test
    fun testSimpleObject_with_annotation() {

        assertEquals(
            SimpleObjectAnnotation(
                personalName = "name",
                internalPhoneNumber = 123,
                someExecutionFlag = true
            ),
            parser.parse(
                multiElement = ComplexXmlElement(
                    name = "pkc:ELEMENT",
                    childs = listOf(
                        SingleXmlElement(
                            "pkc:PERSONAL_NAME",
                            "name"
                        ),
                        SingleXmlElement("pkc:INTERNAL", "123"),
                        SingleXmlElement(
                            "pkc:FEXECUTION",
                            "true"
                        )

                    )
                ),
                returnClass = SimpleObjectAnnotation::class
            )
        )
    }

    @Test
    fun test_InnerObject() {

        assertEquals(
            ComplexInnerObject(
                header = Header(),
                body = Body(
                    name = "text",
                    sum = 12.34
                )
            ),
            parser.parse(
                multiElement = ComplexXmlElement(
                    name = "soapenv:element",
                    childs = listOf(
                        SingleXmlElement("soapenv:header", ""),
                        ComplexXmlElement(
                            "soapenv:body",
                            childs = listOf(
                                SingleXmlElement(
                                    "name",
                                    "text"
                                ),
                                SingleXmlElement("sum", "12.34")
                            )
                        )

                    )
                ),
                returnClass = ComplexInnerObject::class
            )
        )
    }

    @Test
    fun test_ListObject() {

        assertEquals(
            ComplexListObject(
                code = "321",
                body = listOf(
                    ListItemObject(
                        id = 1L,
                        name = "name1"
                    ),
                    ListItemObject(
                        id = 2L,
                        name = "name2"
                    ),
                    ListItemObject(
                        id = 3L,
                        name = "name3"
                    )
                )
            ),
            parser.parse(
                multiElement = ComplexXmlElement(
                    name = "element",
                    childs = listOf(
                        SingleXmlElement("code", "321"),
                        ComplexXmlElement(
                            "body",
                            childs = listOf(
                                itemElement("1", "name1"),
                                itemElement("2", "name2"),
                                itemElement("3", "name3")
                            )
                        )
                    )
                ),
                returnClass = ComplexListObject::class
            )
        )
    }

    @Test
    fun testComplexListSimpleItem() {

        assertEquals(
            ComplexListItemObject(
                code = "321",
                body = listOf(
                    "111",
                    "222",
                    "333"
                )
            ),
            parser.parse(
                multiElement = ComplexXmlElement(
                    name = "element",
                    childs = listOf(
                        SingleXmlElement("code", "321"),
                        ComplexXmlElement(
                            "body",
                            childs = listOf(
                                singleElement("111"),
                                singleElement("222"),
                                singleElement("333")
                            )
                        )

                    )
                ),
                returnClass = ComplexListItemObject::class
            )
        )
    }

    private fun singleElement(value: String): SingleXmlElement {
        return SingleXmlElement(
            name = "varchar",
            value = value
        )
    }

    private fun itemElement(id: String, name: String): ComplexXmlElement {
        return ComplexXmlElement(
            "item",
            childs = listOf(
                SingleXmlElement("id", id),
                SingleXmlElement("name", name)
            )
        )
    }
}

internal data class SimpleObject(
    val name: String,
    val number: Int,
    val flag: Boolean
) : XmlObject

internal data class SimpleObjectNames(
    val personalName: String,
    val internalPhoneNumber: Int,
    val someExecutionFlag: Boolean
) : XmlObject

internal data class SimpleObjectAnnotation(
    val personalName: String,
    @SoapElement(name = "INTERNAL")
    val internalPhoneNumber: Int,
    @SoapElement(name = "FEXECUTION")
    val someExecutionFlag: Boolean
) : XmlObject

internal data class ComplexListObject(
    val code: String,
    val body: List<ListItemObject> = emptyList()
) : XmlObject

internal data class ComplexListItemObject(
    val code: String,
    val body: List<String> = emptyList()
) : XmlObject

internal data class ListItemObject(
    val id: Long,
    val name: String
) : XmlObject

internal data class ComplexInnerObject(
    val header: Header,
    val body: Body
) : XmlObject

internal class Header : XmlObject {
}

internal data class Body(
    val name: String,
    val sum: Double
) : XmlObject