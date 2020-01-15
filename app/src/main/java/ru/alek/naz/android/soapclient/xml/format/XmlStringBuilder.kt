package ru.sportmaster.android.tools.xml.format

class XmlStringBuilder(
    private val stringBuilder: StringBuilder = StringBuilder(),
    private var level: Int = -1,
    private val namespace: String = ""
) {

    private val space = "   "


    fun emptyTag(tag: String) {
        writeSpaces()
        open(tag, true)
        stringBuilder.append('\n')
    }

    fun openTag(tag: String) {
        writeSpaces()
        open(tag)
        stringBuilder.append('\n')

        level++
    }

    fun rootTag(tag: String, namespaces: Array<String>) {
        open(
            tag = tag,
            namespaces = namespaces
        )
        stringBuilder.append('\n')

        level++
    }

    fun writeTag(tag: String, value: String) {
        writeSpaces()
        open(tag)
        stringBuilder.append(value)
        close(tag)
        stringBuilder.append('\n')
    }


    fun closeTag(tag: String) {
        level--
        writeSpaces()

        close(tag)
        stringBuilder.append('\n')
    }

    private fun open(
        tag: String,
        closed: Boolean = false,
        namespaces: Array<String> = emptyArray()
    ) {

        stringBuilder.append("<")
        if (namespace.isNotEmpty()) {
            stringBuilder.append(namespace).append(":")
        }
        stringBuilder.append(tag)
        for (ns in namespaces) {
            stringBuilder.append(' ').append(ns)
        }
        if (closed) {
            stringBuilder.append("/")
        }
        stringBuilder.append(">")

    }

    private fun close(tag: String) {

        stringBuilder.append("</")
        if (namespace.isNotEmpty()) {
            stringBuilder.append(namespace).append(":")
        }
        stringBuilder.append(tag)
        stringBuilder.append(">")
    }

    private fun writeSpaces() {
        for (i in 0..level) {
            stringBuilder.append(space)
        }
    }


    fun buildString(): String {
        return stringBuilder.toString()
    }

    fun copy(namespace: String): XmlStringBuilder =
        XmlStringBuilder(stringBuilder, level, namespace)

}