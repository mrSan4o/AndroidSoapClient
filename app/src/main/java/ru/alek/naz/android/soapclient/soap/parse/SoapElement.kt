package ru.sportmaster.android.tools.soap.parse

@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
@Retention
annotation class SoapElement(
    val name: String = "",
    val required: Boolean = false
)