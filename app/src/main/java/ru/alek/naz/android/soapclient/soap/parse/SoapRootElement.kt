package ru.sportmaster.android.tools.soap.parse

@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@Retention
annotation class SoapRootElement(
    val method: String
)