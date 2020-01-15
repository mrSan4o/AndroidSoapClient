package ru.sportmaster.android.tools.soap.parse.deserialize

import ru.sportmaster.android.tools.soap.parse.XmlObject
import ru.sportmaster.android.tools.xml.parse.ComplexXmlElement
import kotlin.reflect.KClass

interface XmlObjectParser {
    fun <R : XmlObject> parse(multiElement: ComplexXmlElement, returnClass: KClass<R>): R
}