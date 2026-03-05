package seminar

import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager



// ===========================================
// Задача 1. HTTP-запросы через HttpURLConnection
// ===========================================
// Цель: научиться отправлять GET и POST запросы, читать ответ и статус-код.
// API: https://jsonplaceholder.typicode.com

// 1: Отправить GET /posts/1, вывести статус-код и тело ответа
// 2: Отправить POST /posts с JSON-телом, вывести статус-код и тело
// 3: Отправить GET /posts/9999, обработать ошибку (код != 2xx)

// Подсказки:
//   val connection = URL("...").openConnection() as HttpURLConnection
//   connection.requestMethod = "GET"             — задать метод
//   connection.doOutput = true                   — разрешить отправку тела
//   connection.setRequestProperty("Content-Type", "application/json") — заголовок
//   connection.outputStream.write(json.toByteArray())                 — записать тело
//   connection.responseCode                      — получить статус-код
//   connection.inputStream.bufferedReader().readText()  — прочитать тело ответа
//   connection.errorStream                       — поток ошибок (при коде 4xx/5xx)
//   connection.disconnect()                      — закрыть соединение




fun disableSslVerification() {
    val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, trustAll, SecureRandom())
    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
    HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
}

const val BASE_URL_OLD = "https://jsonplaceholder.typicode.com"

fun main() {
    disableSslVerification()

    // === GET запрос ===
    println("=== GET /posts/1 ===")
    val getUrl = URL("${BASE_URL_OLD}/posts/1")
    val getConn = getUrl.openConnection() as HttpURLConnection
    getConn.requestMethod = "GET"

    println("Код: ${getConn.responseCode}")
    val getBody = getConn.inputStream.bufferedReader().readText()
    println("Тело: $getBody")
    getConn.disconnect()

    // 2: POST /posts
    println("\n=== POST /posts ===")
    val secondUrl = URL("${BASE_URL_OLD}/posts")
    val secondConnection = secondUrl.openConnection() as HttpURLConnection

    secondConnection.requestMethod = "POST"
    secondConnection.doOutput = true

    val json = """
        {
            "title": "Hello from T-Bank!",
            "body": "hello there",
            "userId": 1
        }
    """.trimIndent()
    secondConnection.setRequestProperty("Content-Type", "application/json")
    secondConnection.outputStream.write(json.toByteArray())

    println("Код: ${secondConnection.responseCode}")
    val getSecondBody = secondConnection.inputStream.bufferedReader().readText()
    println("Тело: $getSecondBody")
    secondConnection.disconnect()

    // 3: GET /posts/9999 (обработка ошибки)
    println("\n=== GET /posts/9999 ===")
    val thirdUrl = URL("$BASE_URL_OLD/posts/9999")
    val thirdConnection = thirdUrl.openConnection() as HttpURLConnection

    thirdConnection.requestMethod = "GET"

    val code = thirdConnection.responseCode
    println("Код: $code")

    if (code in 200..299) {
        val getBody3 = thirdConnection.inputStream.bufferedReader().readText()
        println("Тело: $getBody3")
    } else {
        val errorBody = thirdConnection.errorStream?.bufferedReader()?.readText() ?: "Нет данных об ошибке"
        println("Ошибка! Ресурс не найден или недоступен.")
        println("Тело ошибки: $errorBody")
    }
    thirdConnection.disconnect()
}