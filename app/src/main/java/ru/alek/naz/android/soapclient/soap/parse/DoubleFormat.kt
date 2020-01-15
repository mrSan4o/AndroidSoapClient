package ru.sportmaster.android.tools.soap.parse

import java.math.BigDecimal
import java.math.RoundingMode

interface DoubleFormat {
    fun format(value: Double): String
}
object NoDoubleFormat : DoubleFormat{
    override fun format(value: Double): String {
        return value.toString()
    }
}
object Round2DoubleFormat : DoubleFormat {
    override fun format(value: Double): String {
        if (value == 0.0){
            return "0"
        }
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toString()
    }
}