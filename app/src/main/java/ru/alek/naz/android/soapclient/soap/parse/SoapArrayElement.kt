package ru.sportmaster.android.tools.soap.parse

@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
@Retention
annotation class SoapArrayElement(
    val name: String = "",
    val itemName: String
)