package ru.sportmaster.android.tools.soap.parse.format


object UpperSlashTagFormat : TagFormat {
    override fun parseFromTag(tag: String): String {

        val stringBuilder = StringBuilder()

        var i = -1
        var upperCase = false
        for (c in tag) {
            i++

            if (c == '_') {
                upperCase = true
                continue
            }
            val appendChar = if (upperCase) {
                upperCase = false
                c.toUpperCase()
            } else {
                c.toLowerCase()
            }

            stringBuilder.append(appendChar)
        }

        return stringBuilder.toString()
    }

}
object UpperSlashFieldFormat : FieldFormat {

    override fun formatToTag(fieldName: String): String {

        val stringBuilder = StringBuilder()
        var i = 0
        for (c in fieldName) {
            if (c.isUpperCase()) {
                if (i > 0) {
                    stringBuilder.append('_')
                }
            }
            stringBuilder.append(c.toUpperCase())

            i++
        }
        return stringBuilder.toString()
    }

}