package ru.alek.naz.android.soapclient.parse.deserialize

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ru.sportmaster.android.tools.soap.parse.format.UpperSlashTagFormat
import ru.sportmaster.android.tools.xml.parse.ComplexXmlElement
import ru.sportmaster.android.tools.xml.parse.SingleXmlElement
import timber.log.Timber

class SoapObjectXmlElementParserTest {
    val parser = SoapXmlObjectElementParser(tagFormat = UpperSlashTagFormat)

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


}
