package seminar

import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64

// ===========================================
// Задача 3. JWT — авторизация
// ===========================================
// Цель: понять структуру JWT, собрать и декодировать токен, отправить запрос с Bearer-авторизацией.
// API: https://httpbin.org/bearer (возвращает 200 если есть Bearer, 401 если нет)

// TODO 1: Собрать JWT из трёх частей (header, payload, signature) в Base64URL
// TODO 2: Декодировать JWT обратно — вывести header и payload как JSON
// TODO 3: Отправить GET https://httpbin.org/bearer с заголовком Authorization: Bearer <token>
// TODO 4: Отправить тот же запрос БЕЗ токена — убедиться, что вернулся 401
// TODO 5: Подменить payload (role: student → admin), объяснить почему сервер отвергнет

// Подсказки:
//   Base64.getUrlEncoder().withoutPadding().encodeToString(bytes) — кодирование
//   Base64.getUrlDecoder().decode(string)                        — декодирование
//   JWT = base64(header) + "." + base64(payload) + "." + base64(signature)

// Вопросы после выполнения:
//   - Из каких 3 частей состоит JWT?
//   - Можно ли подменить payload и использовать токен? Почему нет?
//   - Что такое access token и refresh token?



fun main() {
    disableSslVerification()

    val encoder = Base64.getUrlEncoder().withoutPadding()
    val decoder = Base64.getUrlDecoder()

    // 1: Сборка JWT
    println("=== Сборка JWT ===")
    val header = """{"alg":"HS256","typ":"JWT"}"""
    val payload = """{"sub":"1","name":"Ivan Petrov","role":"student","iat":1234567890}"""
    val fakeSignature = "dummysignature"

    val headerEncoded = encoder.encodeToString(header.toByteArray())
    val payloadEncoded = encoder.encodeToString(payload.toByteArray())
    val signatureEncoded = encoder.encodeToString(fakeSignature.toByteArray())

    val token = "$headerEncoded.$payloadEncoded.$signatureEncoded"
    println("Сгенерированный токен: $token")

    // 2: Декодирование JWT
    println("\n=== Декодирование JWT ===")
    val parts = token.split(".")
    if (parts.size == 3) {
        println("Header: ${String(decoder.decode(parts[0]))}")
        println("Payload: ${String(decoder.decode(parts[1]))}")
        println("Signature (raw): ${String(decoder.decode(parts[2]))}")
    }

    // 3: GET /bearer с токеном
    println("\n=== GET /bearer (с токеном) ===")
    val (codeWithToken, bodyWithToken) = sendRequest("https://httpbin.org/bearer", "GET", null, token)
    println("Код: $codeWithToken, Тело: $bodyWithToken")

    // 4: GET /bearer без токена
    println("\n=== GET /bearer (без токена) ===")
    val (codeNoToken, bodyNoToken) = sendRequest("https://httpbin.org/bearer", "GET", null, null)
    println("Код: $codeNoToken (Ожидалось 401)")

    // 5: Подмена payload
    println("\n=== Подмена payload (student -> admin) ===")
    val adminPayload = payload.replace("student", "admin")
    val adminPayloadEncoded = encoder.encodeToString(adminPayload.toByteArray())
    val fakeAdminToken = "$headerEncoded.$adminPayloadEncoded.$signatureEncoded"

    println("Новый токен (fake admin): $fakeAdminToken")
}

fun sendRequest(urlStr: String, method: String, body: String? = null, token: String? = null): Pair<Int, String> {
    val connection = URL(urlStr).openConnection() as HttpURLConnection
    connection.requestMethod = method

    token?.let {
        connection.setRequestProperty("Authorization", "Bearer $it")
    }

    if (body != null) {
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.write(body.toByteArray())
    }

    val code = connection.responseCode
    val response = (if (code in 200..299) connection.inputStream else connection.errorStream)
        ?.bufferedReader()?.readText() ?: ""

    connection.disconnect()
    return code to response
}