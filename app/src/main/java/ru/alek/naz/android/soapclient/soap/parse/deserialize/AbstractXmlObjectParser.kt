package ru.sportmaster.android.tools.soap.parse.deserialize

import ru.sportmaster.android.tools.soap.parse.DateFormatField
import ru.sportmaster.android.tools.soap.parse.XmlObject
import ru.sportmaster.android.tools.xml.parse.ComplexXmlElement
import ru.sportmaster.android.tools.xml.parse.SingleXmlElement
import ru.sportmaster.android.tools.xml.parse.XmlElement
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

abstract class AbstractXmlObjectParser : XmlObjectParser {

    override fun <R : XmlObject> parse(multiElement: ComplexXmlElement, returnClass: KClass<R>): R {

        val instance = this.parseObject(multiElement, returnClass)
        return instance as R

    }

    abstract fun parseObject(
        multiElement: ComplexXmlElement,
        returnClass: KClass<out XmlObject>
    ): Any

    protected fun findProperty(
        returnClass: KClass<out XmlObject>,
        name: String,
        type: KType
    ): KProperty1<out XmlObject, Any?> {
        return findProperty(
            returnClass.declaredMemberProperties,
            name,
            type
        )
            ?: throw IllegalStateException("Not found ${type.javaType} $name")
    }

    protected fun findProperty(
        returnClass: KClass<out XmlObject>,
        name: String
    ): KProperty1<out XmlObject, Any?>? {
        return findProperty(returnClass.declaredMemberProperties, name)
    }

    protected fun findProperty(
        properties: Collection<KProperty1<out XmlObject, *>>,
        name: String,
        type: KType? = null
    ): KProperty1<out XmlObject, Any?>? {
        return properties
            .find {
                it.name == name
                        && (type == null || it.returnType.javaType == type.javaType)
            }

    }

    protected fun findPrimaryConstructor(returnClass: KClass<out XmlObject>) =
        (returnClass.primaryConstructor
            ?: throw IllegalStateException("Not found primaryConstructor for ${returnClass.simpleName}"))

    protected fun getValue(element: XmlElement, field: KProperty1<out XmlObject, Any?>): Any? {
        val type = field.returnType

        if (isCollectionType(type)) {
            return when (element) {
                is ComplexXmlElement -> {
                    val parameterType = field.firstActualTypeArgument()
                        ?: throw IllegalStateException("Error list declaration ${field.toLogString()}")

                    parseArray(
                        element,
                        parameterType,
                        field.javaField?.annotations,
                        field.toLogString()
                    )
                }
                is SingleXmlElement -> {
                    emptyList<Any>()
                }
                else -> throw IllegalStateException("")
            }
        }
        if (isXmlObjectType(type)) {
            if (element is ComplexXmlElement) {
                return parseObject(
                    element,
                    type.jvmErasure as KClass<out XmlObject>
                )
            }

            return null
        }
        return when (element) {
            is SingleXmlElement -> {
                val value = element.value

                parseValue(
                    type.javaType,
                    field.javaField?.annotations,
                    value
                )
            }
            else -> throw IllegalStateException(
                "NOT FOUND for Element $element. ${field.toLogString()}"
            )
        }
    }

    private fun parseArray(
        element: ComplexXmlElement,
        arrayItemType: Type,
        annotations: Array<Annotation>?,
        fieldInfoString: String
    ): java.util.ArrayList<out Any?> {
        if (isXmlObjectParameterType(arrayItemType)) {
            val returnClass = (arrayItemType as Class<out XmlObject>).kotlin

            val list = ArrayList<XmlObject>()
            for (child in element.childs) {
                if (child is ComplexXmlElement) {
                    list.add(this.parseObject(child, returnClass) as XmlObject)
                } else {
                    throw IllegalStateException("Error in formatToTag Element: $child.\n $fieldInfoString")
                }
            }
            return list
        } else {
            val list = ArrayList<Any?>()
            for (child in element.childs) {
                if (child is SingleXmlElement) {
                    list.add(parseValue(arrayItemType, annotations, child.value))
                } else {
                    throw IllegalStateException("Error in formatToTag Element: $child.\n $fieldInfoString")
                }
            }
            return list
        }
    }

    private fun parseValue(type: Type, annotations: Array<Annotation>?, value: String): Any? {
        return castTo(type as Class<*>, annotations, value)
    }

    private fun isXmlObjectParameterType(parameterType: Type) =
        XmlObject::class.java.isAssignableFrom(parameterType as Class<*>)

    private fun isCollectionType(type: KType) =
        Collection::class.java.isAssignableFrom(type.jvmErasure.java)

    private fun isXmlObjectType(type: KType) =
        XmlObject::class.java.isAssignableFrom(type.jvmErasure.java)

    private fun castTo(type: Class<*>, annotations: Array<Annotation>?, value: String): Any? {
        if (XmlObject::class.java.isAssignableFrom(type)) {
            if (value.isEmpty()) {
                return type.newInstance()
            }
            throw IllegalStateException("How create ${type.simpleName} by $value")
        }
        if (Int::class == type || type.toString() == "int" || Integer::class.java == type) {
            if (value.isEmpty()) {
                return 0
            }
            return value.toInt()
        }
        if (Long::class == type
            || type.toString() == "long"
            || java.lang.Long::class.java == type
        ) {
            if (value.isEmpty()) {
                return 0L
            }
            return value.toLong()
        }
        if (Double::class == type
            || type.toString() == "double"
            || java.lang.Double::class.java == type
        ) {
            if (value.isEmpty()) {
                return 0.0
            }
            return value.toDouble()
        }

        if (Boolean::class.java == type
            || type.toString() == "boolean"
            || java.lang.Boolean::class.java == type
        ) {
            if (value.isEmpty()) {
                return false
            }
            return value.toBoolean()
        }
        if (Date::class.java == type) {
            if (value.isNotEmpty()) {

                val formatField = annotations
                    ?.find { it.javaClass == DateFormatField::javaClass }
                    ?.let { it as DateFormatField }

                if (formatField != null) {
                    val pattern = formatField.pattern
                    return SimpleDateFormat(pattern).format(value)
                }
            }

            return null
        }
        return value
    }

}

internal fun <A : Annotation> KProperty1<*, *>.findJavaAnnotation(
    annotationClass: Class<A>
): A? {
    return this.javaField?.annotations?.find { it.annotationClass.java == annotationClass } as A?
}

internal fun String.cutNamespace(): String {
    val dd = this.indexOf(':')
    if (dd > -1) {
        return this.substring(dd + 1)
    }
    return this
}

private fun KProperty1<*, *>.firstActualTypeArgument(): Type? {
    return (this.javaField?.genericType as ParameterizedType).actualTypeArguments[0]
}

private fun KProperty1<*, *>.toLogString() =
    "field ${this.returnType} ${this.javaClass.simpleName}.${this.name}"