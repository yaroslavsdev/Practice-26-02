import java.net.HttpURLConnection
import java.net.URL

// ===========================================
// Задача 6. Клиент для сервера заметок
// ===========================================
// Цель: написать клиент, который тестирует все эндпоинты сервера.
// Перед запуском: запустить Task6_Server.kt
//
// 1: Реализовать request() — универсальную функцию отправки запросов
// 2: В seminar.seminar.seminar.main() выполнить 8 шагов (ниже), вывести код и тело каждого ответа

val BASE = "http://localhost:8080/api/notes"

/** Отправить HTTP-запрос.
 *  @param url    — полный URL
 *  @param method — HTTP-метод
 *  @param body   — JSON-тело (null для GET/DELETE)
 *  @return Pair(statusCode, responseBody)
 */

fun request(url: String, method: String, body: String? = null): Pair<Int, String> {
    try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = method

        if (body != null) {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { it.write(body.toByteArray()) }
        }

        val code = connection.responseCode

        val inputStream = if (code in 200..299) connection.inputStream else connection.errorStream
        val responseBody = inputStream?.bufferedReader()?.use { it.readText() } ?: ""

        connection.disconnect()
        return code to responseBody
    } catch (e: Exception) {
        return 0 to "Ошибка подключения: ${e.message}"
    }
}

fun main() {
    // Шаг 1: получить все заметки
    println("=== 1. GET /api/notes — все заметки ===")
    println(request(BASE, "GET"))

    // Шаг 2: создать новую заметку
    println("\n=== 2. POST /api/notes — создать заметку ===")
    val newNote = """{"title":"Домашка","content":"Сделать задание по сетям","tag":"учёба"}"""
    val (code2, body2) = request(BASE, "POST", newNote)
    println("Код: $code2, Тело: $body2")

    // Шаг 3: получить заметку по id
    println("\n=== 3. GET /api/notes/1 — одна заметка ===")
    println(request("$BASE/1", "GET"))

    // Шаг 4: обновить заметку
    println("\n=== 4. PUT /api/notes/1 — обновить заметку ===")
    val updatedNote = """{"title":"Покупки (обновлено)","content":"Молоко, хлеб, яйца, сыр","tag":"личное"}"""
    println(request("$BASE/1", "PUT", updatedNote))

    // Шаг 5: фильтр по тегу
    println("\n=== 5. GET /api/notes?tag=учёба — фильтр по тегу ===")
    println(request("$BASE?tag=учёба", "GET"))

    // Шаг 6: удалить заметку
    println("\n=== 6. DELETE /api/notes/1 — удалить заметку ===")
    println(request("$BASE/1", "DELETE"))

    // Шаг 7: запросить несуществующую заметку (404)
    println("\n=== 7. GET /api/notes/999 — несуществующая заметка ===")
    println(request("$BASE/999", "GET"))

    // Шаг 8: финальное состояние
    println("\n=== 8. GET /api/notes — финальное состояние ===")
    println(request(BASE, "GET"))
}
