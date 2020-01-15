package ru.sportmaster.android.tools.soap.send

import android.util.Base64
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class SoapActionClient(
    private val webServicesUrl: String,
    private val basicAuth: BasicAuth? = null
) {

    var connectTimeout = 30_000
    var readTimeout = 30_000

    operator fun <R> invoke(
        webServiceMethodName: String,
        xmlByteArray: ByteArray,
        responseMapper: (InputStream) -> R
    ): R {
        val aThis = this

        val url = URL(webServicesUrl)
        var urlConnection: HttpURLConnection? = null
        try {
            urlConnection = (url.openConnection() as HttpURLConnection)
                .apply {
                    requestMethod = "POST"
//                    setRequestProperty("Content-type", "application/soap+xml; charset=utf-8")
                    setRequestProperty("Content-type", "text/xml; charset=utf-8")
                    setRequestProperty("SOAPAction", "$webServicesUrl#$webServiceMethodName")
                    addBasicAuthentication(this)
                    doOutput = true
                    connectTimeout = aThis.connectTimeout
                    readTimeout = aThis.readTimeout
                }

            val reqStream = urlConnection.outputStream
            reqStream.write(xmlByteArray)

            if (urlConnection.responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
                throw IllegalStateException(
                    "Error response[${urlConnection.responseCode}]: " + read(
                        urlConnection.errorStream
                    )
                )
            }

            return responseMapper.invoke(urlConnection.inputStream)
        } finally {
            urlConnection?.disconnect()
        }
    }

    private fun addBasicAuthentication(con: HttpURLConnection) {
        basicAuth?.let {
            con.setRequestProperty(
                "Authorization",
                "Basic " + encodeUsernamePassword(it)
            )
        }
    }

    private fun encodeUsernamePassword(basicAuth: BasicAuth): String {
        val username = basicAuth.username
        val password = basicAuth.password
        return Base64.encodeToString(
            "$username:$password".toByteArray(StandardCharsets.UTF_8)
            , Base64.NO_WRAP
        )
    }

    @Throws(IOException::class)
    private fun read(inputStream: InputStream): String {
        return inputStream.bufferedReader().use { it.readText() }
    }
}

data class BasicAuth(
    val username: String,
    val password: String
)
