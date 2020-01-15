package ru.sportmaster.android.tools.soap.parse.deserialize

import ru.sportmaster.android.tools.soap.parse.SoapElement
import ru.sportmaster.android.tools.soap.parse.XmlObject
import ru.sportmaster.android.tools.soap.parse.format.NoTagFormat
import ru.sportmaster.android.tools.soap.parse.format.TagFormat
import ru.sportmaster.android.tools.xml.parse.ComplexXmlElement
import timber.log.Timber
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

/**
 * строит на основе значений из multiElement: MultiElement класс returnClass: KClass<out XmlObject>
 * все теги в multiElement: MultiElement должы быть в виде полей в returnClass: KClass<out XmlObject>
 * */
class SoapXmlObjectElementParser(
    private val tagFormat: TagFormat = NoTagFormat
) : AbstractXmlObjectParser() {

    override fun parseObject(
        multiElement: ComplexXmlElement,
        returnClass: KClass<out XmlObject>
    ): Any {

        val primaryConstructor = findPrimaryConstructor(returnClass)

        val properties = ArrayList<KProperty1<out XmlObject, *>>()
        for (element in multiElement.childs) {
            val elementName = element.name

            val property = returnClass.declaredMemberProperties
                .find {
                    isMatch(elementName, it.name, it.findJavaAnnotation(SoapElement::class.java))
                }
                ?: throw IllegalStateException("Not found $elementName in ${returnClass.simpleName}")

            val isConstructorParam = primaryConstructor.parameters
                .any {
                    it.name == property.name
                            && it.type == property.returnType
                }
            if (!isConstructorParam) {
                throw IllegalStateException(
                    "There is no property " +
                            "${property.returnType} ${property.name} " +
                            "in primaryConstructor ${returnClass.simpleName}"
                )
            }
            properties.add(property)
        }

        val arguments = ArrayList<Any?>()
        primaryConstructor.parameters.forEach { parameter ->
            val parameterName = parameter.name
            val parameterType = parameter.type

            val property = findProperty(properties, parameterName!!, parameterType)


            if (property != null) {
                val find = multiElement.childs.find {
                    isMatch(
                        it.name,
                        property.name,
                        property.findJavaAnnotation(SoapElement::class.java)
                    )
                }
                    ?: throw java.lang.IllegalStateException("Can't find ${property.name} in $multiElement")
                arguments.add(getValue(find, property))
            } else {
                arguments.add(null)
            }
        }
        Timber.d("Create ${returnClass.simpleName} args: $arguments\nelement: $multiElement\n")
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
