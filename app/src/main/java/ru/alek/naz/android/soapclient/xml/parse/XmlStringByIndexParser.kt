package ru.sportmaster.android.tools.xml.parse

class XmlStringByIndexParser : XmlStringParser {

    override fun parse(input: String): XmlElement {

        val xml = input.removeTextFormatting().removeXmlVersionTag()

        val xmlTag = findXmlTag(xml)
        if (!xmlTag.isValid()) {
            throw IllegalStateException("There's no xml formatToTag")
        }

        return parseElement(xml, xmlTag)
    }

    private fun parseElement(
        xml: String,
        xmlTag: XmlTag
    ): XmlElement {
        val tag = xmlTag.tag
        val value = xmlTag.substringValue(xml)

        val valueXmlTag = findXmlTag(value)
        if (valueXmlTag.isValid()) {

            val elements = ArrayList<XmlElement>()
            var itemXmlTag = valueXmlTag
            var temp = value
            do {

                elements.add(parseElement(temp, itemXmlTag))
                temp = temp.substring(itemXmlTag.endTagElementIndex())

                itemXmlTag = findXmlTag(temp)
            } while (itemXmlTag.isValid())

            return ComplexXmlElement(tag, elements)
        } else {
            return singleElement(tag, value)
        }
    }

    private fun findXmlTag(
        xml: String,
        startSearchIndex: Int = 0,
        endIndex: Int = xml.length
    ): XmlTag {
        val startTag = xml.absoluteIndexOf('<', startSearchIndex)

        if (startTag == -1 || startSearchIndex >= endIndex) {
            return XmlTag(-1, "")
        }
        val notStartTag = xml.absoluteIndexOf("<![CDATA", startSearchIndex)
        if (notStartTag == startTag) {
            return XmlTag(-1, "")
        }

        val endTagIndex = xml.absoluteIndexOf('>', startSearchIndex, false)
        val endEmptyTag = xml.absoluteIndexOf("/>", startSearchIndex)
        if (endEmptyTag > 0 && endEmptyTag < endTagIndex) {
            val xmlTagName = xml.substringTagName(startTag, endEmptyTag)
            if (!xmlTagName.name.contains('>')) {
                return XmlTag(
                    startTag1Index = startTag,
                    tag = xmlTagName.name,
                    emptyTag = true,
                    endTagElementIndex = xmlTagName.endIndex + 3
                )
            }
        }

        val xmlTagName = xml.substringTagName(startTag, endTagIndex)
        val tagName = xmlTagName.name
        if (tagName.isEmpty()) {
            throw IllegalStateException("Error xml formatToTag :\n$xml")
        }

        val startValueIndex = endTagIndex + 1
        val endTag = "</$tagName>"
        val endValueIndex = xml.absoluteIndexOf(endTag, startSearchIndex, true)

        val tagValue = xml.substring(startValueIndex, endValueIndex)
        if (isValidContain(tagValue, tagName)) {
            return XmlTag(
                startTag,
                tagName,
                startValueIndex = startValueIndex,
                endValueIndex = endValueIndex
            )
        } else {
            return XmlTag(
                startTag,
                tagName,
                startValueIndex = startValueIndex,
                endValueIndex = xml.lastAbsoluteIndexOf(endTag, true)
            )
        }

    }

    private fun isValidContain(tagValue: String, tagName: String): Boolean {
        val openTagCount = tagValue.containsCount("<$tagName>")
        val closeTagCount = tagValue.containsCount("</$tagName>")

        return openTagCount == 0 || openTagCount == closeTagCount
    }

    private fun singleElement(
        tag: String,
        value: String
    ): SingleXmlElement {
        val name = tag.removeNamespace()
        return SingleXmlElement(name, value)
    }

}

private fun String.substringTagName(startTagIndex: Int, endTagIndex: Int): XmlTagName {
    val tagName = this.substring(startTagIndex + 1, endTagIndex)

    val tokens = tagName.split(' ')

    return XmlTagName(
        tokens[0],
        startTagIndex + tagName.length,
        tokens.filterIndexed { index, s -> index != 0 }
    )
}

private fun String.containsCount(token: String): Int {
    var index = this.indexOf(token)
    var count = 0
    while (index != -1) {
        count++
        index = this.indexOf(token, startIndex = index + token.length)
    }
    return count
}

private data class XmlTagName(
    val name: String,
    val endIndex: Int,
    val tokens: List<String>
)

private data class XmlTag(
    val startTag1Index: Int,
    val tag: String,

    val startValueIndex: Int = -1,
    val endValueIndex: Int = -1,
    val emptyTag: Boolean = false,
    val endTagElementIndex: Int = -1
) {
    fun isValid() = startTag1Index > -1 && tag.isNotEmpty()
    fun endTag1Index(): Int = startTag1Index + 1 + tag.length + 1 + if (emptyTag()) {
        1
    } else {
        0
    }

    fun startTagElementIndex(): Int {
        if (emptyTag()) {
            return startTag1Index
        }
        return startValueIndex - tag.length - 2
    }

    fun endTagElementIndex(): Int {
        if (endTagElementIndex != -1) {
            return endTagElementIndex
        }
        if (emptyTag()) {
            return startTag1Index + tag.length + 3
        }
        return endValueIndex + tag.length + 3
    }

    fun substringValue(xml: String): String {
        if (startValueIndex == -1) {
            return ""
        }
        return xml.substring(startValueIndex, endValueIndex)
    }

    private fun emptyTag(): Boolean = startValueIndex == -1 && endValueIndex == -1

    fun substringElement(xml: String): String {
        return xml.substring(startTagElementIndex(), endTagElementIndex())
    }
}

private fun String.absoluteIndexOf(
    string: String,
    startIndex: Int = 0,
    strict: Boolean = false
): Int {
    val index = this.indexOf(string, startIndex)
    if (index == -1) {
        if (strict) {
            throw IllegalStateException("Not Found '$string' in string: $this")
        }
        return index
    }
    return index
}

private fun String.lastAbsoluteIndexOf(
    string: String,
    strict: Boolean = false
): Int {
    val index = this.lastIndexOf(string)
    if (index == -1) {
        if (strict) {
            throw IllegalStateException("Not Found '$string' in string: $this")
        }
        return index
    }
    return index
}

private fun String.absoluteIndexOf(
    string: Char,
    startIndex: Int = 0,
    strict: Boolean = false
): Int {
    val index = this.indexOf(string, startIndex)
    if (index == -1) {
        if (strict) {
            throw IllegalStateException("Not Found '$string' in string: $this")
        }
        return index
    }
    return index
}

private fun String.removeSpaces(): String {
    return this.replace(" ", "")
}

private fun String.removeTextFormatting(): String {
    return this.replace("\r", "").replace("\n", "").replace("\t", "")
}

private fun String.removeXmlVersionTag(): String {
    val startTag = this.indexOf("<?xml")
    if (startTag == -1) {
        return this
    }

    val endTag: Int
    var end = this.indexOf("?>")
    if (end == -1) {
        end = this.indexOf(">")
        if (end == -1) {
            throw RuntimeException("Error format XmlVersionTag")
        }
        endTag = end + 1
    } else {
        endTag = end + 2
    }

    return this.removeRange(startTag, endTag)
}

private fun String.removeNamespace(): String {
    val dd = this.indexOf(':')
    if (dd > 0) {
        return this.substring(dd + 1)
    }
    return this
}