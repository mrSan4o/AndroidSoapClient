package ru.sportmaster.android.tools.soap.parse.format

interface FieldFormat {
    fun formatToTag(field: String): String
}

interface TagFormat{
    fun parseFromTag(tag: String): String
}

object NoTagFormat : TagFormat {
    override fun parseFromTag(tag: String): String {
        return tag
    }
}

object NoFieldFormat : FieldFormat {

    override fun formatToTag(field: String): String {
        return field
    }
}

