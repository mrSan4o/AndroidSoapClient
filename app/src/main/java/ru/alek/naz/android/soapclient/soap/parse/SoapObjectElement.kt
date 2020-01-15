package ru.sportmaster.android.tools.soap.parse

@Target(AnnotationTarget.CLASS)
@Retention
annotation class SoapObjectElement(
    val name: String = "",
    val orderedFields: Array<String>
)