package ru.alek.naz.android.soapclient.parse

import org.junit.Assert
import org.junit.Test
import ru.sportmaster.android.tools.soap.parse.Round2DoubleFormat

class Round2DoubleFormatTest{

    @Test
    fun test(){
        Assert.assertEquals("0", Round2DoubleFormat.format(0.0))
        Assert.assertEquals("12.23", Round2DoubleFormat.format(12.23))
        Assert.assertEquals("12.23", Round2DoubleFormat.format(12.2311))
        Assert.assertEquals("12.24", Round2DoubleFormat.format(12.2361))
        Assert.assertEquals("12.00", Round2DoubleFormat.format(12.0).also { println(it)  })
    }
}