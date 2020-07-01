package com.leo.overtime.statistics.http

import android.util.Log
import okhttp3.*
import okhttp3.internal.http.HttpHeaders
import okio.Buffer
import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class LoggerInterceptor : Interceptor {

    private val TAG = "NET"
    private val UTF8 = Charset.forName("UTF-8")


    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        logRequest(chain, request)
        return logResponse(chain, request)
    }

    /**
     * Request
     */
    @Throws(IOException::class)
    private fun logRequest(chain: Interceptor.Chain, request: Request) {
        val requestBody = request.body()
        val hasRequestBody = requestBody != null

        val connection = chain.connection()
        val protocol = if (connection != null) connection.protocol() else Protocol.HTTP_1_1
        val requestStartMessage = "--> " + request.method() + ' ' + request.url() + ' ' + protocol
        Log.i(TAG, requestStartMessage)

        if (hasRequestBody) {
            if (requestBody!!.contentType() != null) {
                Log.i(TAG, "Content-Type: " + requestBody.contentType()!!)
            }
            if (requestBody.contentLength() != -1L) {
                Log.i(TAG, "Content-Length: " + requestBody.contentLength())
            }
        }

        val headers = request.headers()
        var i = 0
        val count = headers.size()
        while (i < count) {
            val name = headers.name(i)
            if (!"Content-Type".equals(name, ignoreCase = true) && !"Content-Length".equals(name, ignoreCase = true)) {
                Log.i(TAG, name + ": " + headers.value(i))
            }
            i++
        }

        if (!hasRequestBody) {
            Log.i(TAG, "--> END " + request.method())
        } else if (bodyEncoded(request.headers())) {
            Log.i(TAG, "--> END " + request.method() + " (encoded body omitted)")
        } else {
            val buffer = Buffer()
            requestBody!!.writeTo(buffer)

            if (isPlaintext(buffer)) {
                Log.i(TAG, buffer.readString(UTF8))
                Log.i(TAG, "--> END " + request.method() + " (" + requestBody.contentLength() + "-byte body)")
            } else {
                Log.i(TAG, "--> END " + request.method() + " (binary " + requestBody.contentLength() + "-byte body omitted)")
            }
        }
    }


    /**
     * Response
     */
    @Throws(IOException::class)
    private fun logResponse(chain: Interceptor.Chain, request: Request): Response {
        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            Log.i(TAG, "<-- HTTP FAILED: " + e)
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body()!!
        val contentLength = responseBody.contentLength()
        val url = response.request().url().toString()
        Log.i(TAG, "<-- " + response.code() + ' ' + response.message() + ' ' + url + " (" + tookMs + "ms" + ')')

        val headers = response.headers()
        var i = 0
        val count = headers.size()
        while (i < count) {
            Log.i(TAG, headers.name(i) + ": " + headers.value(i))
            i++
        }

        if (!HttpHeaders.hasBody(response)) {
            Log.i(TAG, "<-- END HTTP")
        } else if (bodyEncoded(response.headers())) {
            Log.i(TAG, "<-- END HTTP (encoded body omitted)")
        } else {
            val source = responseBody.source()
            source.request(java.lang.Long.MAX_VALUE)
            val buffer = source.buffer()


            if (!isPlaintext(buffer)) {
                Log.i(TAG, "")
                Log.i(TAG, "<-- END HTTP (binary " + buffer.size() + "-byte body omitted)")
                return response
            }

            if (contentLength != 0L) {
                Log.i(TAG, "")
                Log.i(TAG, buffer.clone().readString(UTF8))
            }

            Log.i(TAG, "<-- END HTTP (" + buffer.size() + "-byte body)")
        }
        return response
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    private fun isPlaintext(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = if (buffer.size() < 64) buffer.size() else 64
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (e: EOFException) {
            return false // Truncated UTF-8 sequence.
        }

    }


    /**
     * bodyEncoded
     */
    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding = headers.get("Content-Encoding")
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }
}