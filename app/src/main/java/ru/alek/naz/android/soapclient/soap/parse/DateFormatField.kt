package ru.sportmaster.android.tools.soap.parse

@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
@Retention
annotation class DateFormatField(
    val pattern: String
)