package ru.alek.naz.android.soapclient

import org.junit.Assert
import org.junit.Test
import ru.sportmaster.android.tools.soap.parse.format.UpperSlashFieldFormat
import ru.sportmaster.android.tools.soap.parse.format.UpperSlashTagFormat
import java.text.SimpleDateFormat
import java.util.*

class TestFieldFormat {

    @Test
    fun test_formatToTag() {

        Assert.assertEquals(
            "SIMPLE_CAMEL_CASE",
            UpperSlashFieldFormat.formatToTag("simpleCamelCase")
        )
    }

    @Test
    fun test_parseFromTag() {

        Assert.assertEquals(
            "simpleCamelCase",
            UpperSlashTagFormat.parseFromTag("SIMPLE_CAMEL_CASE")
        )
    }

    @Test
    fun test_dateFormat() {
        val date = TestUtils.buildDate(2019, 10, 11, 10, 25, 37)
        assertDateFormat(
            "2019-10-11 10:25:37.000 MSK",
            "yyyy-MM-dd HH:mm:ss.SSS z",
            date
        )
        assertDateFormat(
            "2019-10-11T10:25:37",
            "yyyy-MM-dd'T'HH:mm:ss",
            date
        )
        assertDateFormat(
            "2019-10-11T10:25:37+03:00",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            date
        )
    }

    private fun assertDateFormat(expected: String, pattern: String, date: Date) {

        val format = SimpleDateFormat(pattern).format(date)
        println("date $date : $format")
        Assert.assertEquals(expected, format)
    }

}
