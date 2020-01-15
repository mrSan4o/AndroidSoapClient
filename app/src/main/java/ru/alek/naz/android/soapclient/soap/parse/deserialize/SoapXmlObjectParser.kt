package ru.sportmaster.android.tools.soap.parse.deserialize

import ru.sportmaster.android.tools.soap.parse.SoapElement
import ru.sportmaster.android.tools.soap.parse.XmlObject
import ru.sportmaster.android.tools.soap.parse.format.NoTagFormat
import ru.sportmaster.android.tools.soap.parse.format.TagFormat
import ru.sportmaster.android.tools.xml.parse.ComplexXmlElement
import timber.log.Timber
import kotlin.reflect.KClass

/**
 * строит на основе полей класса returnClass: KClass<out XmlObject> объект по значениям из multiElement: MultiElement
 *
 * */
class SoapXmlObjectParser(
    private val tagFormat: TagFormat = NoTagFormat
) : AbstractXmlObjectParser() {

    override fun parseObject(
        multiElement: ComplexXmlElement,
        returnClass: KClass<out XmlObject>
    ): Any {

        val primaryConstructor = findPrimaryConstructor(returnClass)

        val arguments = ArrayList<Any?>()

        primaryConstructor.parameters.forEach { parameter ->
            val parameterName = parameter.name
            val parameterType = parameter.type

            val property = findProperty(returnClass, parameterName!!, parameterType)

            val propertyAnnotation = property.findJavaAnnotation(SoapElement::class.java)
            val findElement = multiElement.childs.find {
                isMatch(it.name, property.name, propertyAnnotation)
            }
            if (findElement != null) {
                arguments.add(getValue(findElement, property))
            } else {
                arguments.add(null)
            }
        }
        Timber.d("Create ${returnClass.simpleName} args: $arguments\nmultiElement: $multiElement\n")
        val args = arguments.toArray()
        return primaryConstructor.call(*args)
    }

    private fun isMatch(
        elementName: String,
        propertyName: String,
        propertyAnnotation: SoapElement?
    ): Boolean {
        if (propertyAnnotation != null) {

            return propertyAnnotation.name == elementName.cutNamespace()
        }

        return tagFormat.parseFromTag(elementName.cutNamespace()) == propertyName
    }
}

