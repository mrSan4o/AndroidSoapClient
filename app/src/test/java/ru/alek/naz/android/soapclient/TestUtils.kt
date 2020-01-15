package ru.alek.naz.android.soapclient

import org.junit.Assert
import org.junit.ComparisonFailure
import ru.alek.naz.android.soapclient.parse.serialize.normilizeForTest
import java.io.File
import java.util.*

object TestUtils {
    fun readFileText(name:String):String{

        val file = File("src\\test\\res\\$name")

        return file.readText()
    }

    fun readTextWithoutComments(name: String): String {

        val text = readFileText(name)

        var temp = text
        var start = temp.indexOf("<!--")
        var end = temp.indexOf("-->")
        while (start > 0 && end > 0) {
            temp = temp.substring(0, start) + temp.substring(end + 3)
            start = temp.indexOf("<!--")
            end = temp.indexOf("-->")
        }
        return temp
    }

    fun buildDate(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        sec: Int,
        msc: Int = 0
    ): Date {
        return Calendar.getInstance().let {
            it.set(Calendar.YEAR, year)
            it.set(Calendar.MONTH, month - 1)
            it.set(Calendar.DAY_OF_MONTH, day)
            it.set(Calendar.HOUR_OF_DAY, hour)
            it.set(Calendar.MINUTE, minute)
            it.set(Calendar.SECOND, sec)
            it.set(Calendar.MILLISECOND, msc)

            return it.time
        }

    }
    fun assertEqualsString(expect: String, actual: String) {
        println(actual)
        val expectFormat = expect.normilizeForTest()
        val actualFormat = actual.normilizeForTest()

        var i = -1
        for (expectChar in expectFormat) {
            i++
            val actualChar = actualFormat[i]

            if (actualChar != expectChar) {
                val expectpart = expectFormat.substring(i)
                val actualpart = actualFormat.substring(i)
                throw ComparisonFailure("diff in index=$i\n" +
                        "   $expectpart\n" +
                        "   $actualpart", expectFormat, actualFormat)
            }
        }

        Assert.assertEquals(expectFormat, actualFormat)
    }

}