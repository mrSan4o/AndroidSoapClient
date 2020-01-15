package ru.alek.naz.android.soapclient

import android.util.Base64
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import java.nio.charset.StandardCharsets

@Implements(Base64::class)
object Base64Shadow {

    @JvmStatic
    @Implementation
    fun encodeToString(input: ByteArray, flags: Int): String {
        val bytes = java.util.Base64.getEncoder().encode(input)
        return String(bytes, StandardCharsets.UTF_8)
    }

}
