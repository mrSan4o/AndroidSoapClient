package ru.sportmaster.android.tools.xml.parse

interface XmlStringParser {
    fun parse(input: String): XmlElement
}