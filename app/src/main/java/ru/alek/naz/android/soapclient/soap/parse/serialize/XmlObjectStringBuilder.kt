package ru.sportmaster.android.tools.soap.parse.serialize

import ru.sportmaster.android.tools.soap.parse.*
import ru.sportmaster.android.tools.soap.parse.format.FieldFormat
import ru.sportmaster.android.tools.soap.parse.format.NoFieldFormat
import ru.sportmaster.android.tools.xml.format.XmlStringBuilder
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.*

class XmlObjectStringBuilder(
    private val fieldFormat: FieldFormat = NoFieldFormat,
    private val datePattern: String = "yyyy-MM-dd HH:mm:ss.SSS",
    private val doubleFormat: DoubleFormat = NoDoubleFormat
) {
    companion object {
        fun append(
            builder: XmlStringBuilder,
            data: XmlObject,
            namespace: String = "",
            fieldFormat: FieldFormat,
            datePattern: String = "yyyy-MM-dd HH:mm:ss.SSS",
            doubleFormat: DoubleFormat = NoDoubleFormat
        ) {

            val xmlStringBuilder = builder.copy(
                namespace = namespace
            )
            val soapParser = XmlObjectStringBuilder(
                fieldFormat = fieldFormat,
                datePattern = datePattern,
                doubleFormat = doubleFormat
            )
            soapParser.append(xmlStringBuilder, data)
        }
    }

    private val dateFormat = SimpleDateFormat(datePattern)
        .also {
            // check is pattern correct
            it.format(Date())
        }

    fun append(writer: XmlStringBuilder, data: XmlObject, rootTagNamespace: String = "") {
        val soapRootElement = data.getSoapRootElement()
        val soapObjectElement = data.getSoapObjectElement()

        val name = soapObjectElement.name
        val orderedFields = soapObjectElement.orderedFields

        var tagName = getTagName(data.javaClass, name)
        if (rootTagNamespace.isNotEmpty()) {
            tagName = "$rootTagNamespace:$tagName"
        }

        writeObject(writer, tagName, orderedFields, data)

    }

    private fun writeObject(
        writer: XmlStringBuilder,
        tagName: String,
        orderedFields: Array<String>,
        data: XmlObject
    ) {

        writer.openTag(tagName)

        for (orderFieldName in orderedFields) {
            val field = data.javaClass.getDeclaredField(orderFieldName)

            field.isAccessible = true

            val type = field.type
            val tagName = getTagName(field)
            val value = field.get(data)

            if (value == null) continue

            if (value is XmlObject) {
                val objectElement = value.getSoapObjectElement()
                val name = objectElement.name
                val valueObjectOrderedFields = objectElement.orderedFields
                val valueObjectTagName = if (name.isEmpty()) {
                    formatFieldName(field)
                } else {
                    name
                }
                writeObject(writer, valueObjectTagName, valueObjectOrderedFields, value)
            } else if (value is List<*>) {
                val soapArrayElement = field.getSoapArrayElement()
                val itemTagName = soapArrayElement?.itemName ?: ""

                writeArray(writer, tagName, value, itemTagName)
            } else {
                val writeValue = valueToString(field, value)
                if (writeValue.isEmpty()) {
                    writer.emptyTag(tagName)
                } else {
                    writer.writeTag(tagName, writeValue)
                }
            }

        }
        writer.closeTag(tagName)
    }

    private fun valueToString(field: Field, value: Any?): String {
        if (value is Date) {
            val dateFormatField = field.getDateFormat()
            if (dateFormatField != null) {
                return SimpleDateFormat(dateFormatField.pattern).format(value)
            }

            return dateFormat.format(value)
        }
        if (value?.javaClass?.isEnum == true) {
            if (value is HasCode) {
                return value.code()
            }

            return value.toString()
        }
        if (value is Double) {
            return doubleFormat.format(value)
        }
        return value?.toString() ?: ""
    }

    private fun writeArray(
        writer: XmlStringBuilder,
        name: String,
        value: List<*>,
        itemTagName: String = ""
    ) {
        if (value.isEmpty()) {
            writer.emptyTag(name)
        } else {
            writer.openTag(name)
            for (item in value) {
                if (item is XmlObject) {
                    val soapElement = item.getSoapObjectElement()

                    val itemElementName = soapElement.name
                    val itemOrderedFields = soapElement.orderedFields
                    val itemTagName = if (itemElementName.isEmpty()){
                        formatClassName(item.javaClass)
                    }else{
                        itemElementName
                    }

                    writeObject(writer, itemTagName, itemOrderedFields, item)
                } else {
                    if (itemTagName.isEmpty()) {
                        val simpleName = item?.javaClass?.simpleName ?: "NULL"
                        throw RuntimeException("Unknown how write $simpleName")
                    }

                    writer.writeTag(itemTagName, item?.toString() ?: "")
                }
            }
            writer.closeTag(name)
        }
    }

    private fun formatValueToWrite(value: Any?, type: Class<*>): String {
        if (value == null) {
            return nullValueToWrite(type)
        }
        return value.toString()
    }

    private fun nullValueToWrite(type: Class<*>): String {
        return ""
    }

    private fun getTagName(
        field: Field
    ): String {
        val annotation = field.getSoapElement()

        val name = annotation?.name ?: ""
        if (name.isNotEmpty()) {
            return name
        }
        return formatFieldName(field)
    }

    private fun formatFieldName(field: Field): String {
        val fieldName = field.name ?: throw IllegalStateException("field.name is NULL")
        return fieldFormat.formatToTag(fieldName)
    }

    private fun getTagName(javaClass: Class<Any>, overrideName: String = ""): String {
        if (overrideName.isNotEmpty()) {
            return overrideName
        }
        return formatClassName(javaClass)
    }

    private fun formatClassName(javaClass: Class<Any>): String {
        return fieldFormat.formatToTag(javaClass.simpleName)
    }

}

internal fun Field.getSoapElement(): SoapElement? {
    return this.getAnnotation(SoapElement::class.java)
}

internal fun Field.getDateFormat(): DateFormatField? {
    return this.getAnnotation(DateFormatField::class.java)
}

internal fun Field.getSoapArrayElement(): SoapArrayElement? {
    return this.getAnnotation(SoapArrayElement::class.java)
}