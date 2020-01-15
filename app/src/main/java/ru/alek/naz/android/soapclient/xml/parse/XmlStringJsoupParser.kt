package ru.sportmaster.android.tools.xml.parse

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import ru.sportmaster.android.tools.soap.parse.deserialize.cutNamespace

class XmlStringJsoupParser : XmlStringParser {

    override fun parse(input: String): XmlElement {
        val document = Jsoup.parse(input, "", Parser.xmlParser())

        val rootElement = document.children().first()!!

        return toXmlElement(rootElement)
    }

    private fun toXmlElement(element: Element): XmlElement {
        val tagName = element.tagName().cutNamespace()

        val children = element.children()
        if (children.size == 0) {
            return SingleXmlElement(tagName, element.text())
        }

        return ComplexXmlElement(tagName, children.map { toXmlElement(it) })
    }
}