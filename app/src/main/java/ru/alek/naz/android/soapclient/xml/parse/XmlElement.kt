package ru.sportmaster.android.tools.xml.parse

import ru.sportmaster.android.tools.soap.parse.deserialize.cutNamespace

interface XmlElement {
    val name: String
}

object NoXmlElement : XmlElement {
    override val name = ""
}

data class ComplexXmlElement(
    override val name: String,
    val childs: List<XmlElement> = emptyList()
) : XmlElement

data class SingleXmlElement(
    override val name: String,
    val value: String = ""
) : XmlElement

fun XmlElement?.value(): String {
    return when (this) {
        is SingleXmlElement -> this.value
        is ComplexXmlElement -> {
            this.prettyString()
        }
        else -> this?.toString() ?: ""
    }
}

fun ComplexXmlElement.findTag(tag: String, ignoreCase: Boolean = true): XmlElement? {
    return this.childs.find { it.name.cutNamespace().equals(tag, ignoreCase) }
}

fun XmlElement.prettyString(): String {
    val builder = StringBuilder()
    when (this) {
        is SingleXmlElement -> {
            this.append(builder)
        }
        is ComplexXmlElement -> {
            this.append(builder)
        }

    }

    return builder.toString()
}

private fun ComplexXmlElement.append(builder: StringBuilder, level: Int = 0) {
    level.appendSpaces(builder)
    builder.append(name).append('\n')
    for (child in childs) {
        if (child is SingleXmlElement) {
            child.append(builder, level + 3)
        } else if (child is ComplexXmlElement) {
            child.append(builder, level + 3)
        }
    }
}

private fun SingleXmlElement.append(builder: StringBuilder, level: Int = 0) {
    level.appendSpaces(builder)
    builder
        .append(this.name)
        .append(" : ")
        .append(this.value)
        .append("\n")
}

private fun Int.appendSpaces(builder: StringBuilder) {
    for (i in 1..this) {
        builder.append(' ')
    }
}
