package ru.alek.naz.android.soapclient

import ru.sportmaster.android.tools.soap.parse.SoapObjectElement
import ru.sportmaster.android.tools.soap.parse.SoapRootElement
import ru.sportmaster.android.tools.soap.parse.XmlObject

@SoapRootElement(method = "single")
@SoapObjectElement(
    orderedFields = ["id", "sum", "text", "flag", "list", "inner"]
)
data class XmlSingleObject(
    val id: Long,
    val sum: Double,
    val text: String,
    val flag: Boolean,
    val list: List<XmlListItemObject>,
    val inner: DoubleInnerXmlObject

) : XmlObject

@SoapObjectElement(orderedFields = ["name", "count"])
data class XmlInnerObject (
    val name: String,
    val count: Int
): XmlObject

@SoapObjectElement(orderedFields = ["text", "hack"])
data class XmlListItemObject(
    val text: String,
    val hack: Boolean
): XmlObject

@SoapObjectElement(orderedFields = ["inner"])
data class DoubleInnerXmlObject(
    val inner: XmlInnerObject
): XmlObject


@SoapRootElement(
    method = "test"
)
@SoapObjectElement(orderedFields = arrayOf("name", "count"))
data class XmlRootObject (
    val name: String,
    val count: Int
): XmlObject

@SoapRootElement(
    method = "test"
)
@SoapObjectElement(orderedFields = arrayOf("name", "count"))
data class XmlRootObject2 (
    val name: String,
    val count: Int
): XmlObject