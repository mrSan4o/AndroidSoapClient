package ru.sportmaster.android.tools.soap.parse

@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@Retention
annotation class EnvelopeElement(
    val namespace: String = "",
    val namespaceUrl: String = "",
    val namespaceOnlyForRoot: Boolean = false
) {

}