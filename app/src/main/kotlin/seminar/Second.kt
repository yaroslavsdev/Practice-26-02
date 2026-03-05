package seminar

import java.net.HttpURLConnection
import java.net.URL

// ===========================================
// Задача 2. REST — полный CRUD
// ===========================================
// Цель: реализовать все CRUD-операции для ресурса /posts.
// API: https://jsonplaceholder.typicode.com/posts
//
// TODO 1: Реализовать seminar.seminar.sendRequest() — универсальную функцию отправки запросов
// TODO 2: Реализовать 5 CRUD-функций (ниже)
// TODO 3: Вызвать каждую функцию в seminar.seminar.seminar.main() и вывести результат
//
// Вопросы после выполнения:
//   - В чём разница между PUT и PATCH?
//   - Почему POST возвращает 201, а PUT возвращает 200?
//   - Какой метод идемпотентный, а какой нет?

val BASE_URL = "https://jsonplaceholder.typicode.com/posts"

/** Универсальная функция для отправки HTTP-запросов.
 *  @param urlStr  — полный URL
 *  @param method  — HTTP-метод (GET, POST, PUT, DELETE)
 *  @param body    — тело запроса в формате JSON (null для GET/DELETE)
 *  @return Pair(statusCode, responseBody)
 */


fun sendRequest(urlStr: String, method: String, body: String? = null): Pair<Int, String> {
    // 1: открыть соединение, выставить метод,
    //   если body != null — записать тело и заголовок Content-Type,
    //   прочитать ответ (inputStream при 2xx, errorStream иначе),
    //   вернуть пару (код, тело)
    // Реализуй seminar.seminar.sendRequest

    val connection = URL(urlStr).openConnection() as HttpURLConnection
    connection.requestMethod = method

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

/** GET /posts — получить все посты */
fun getPosts(): String {
    // Реализуй seminar.getPosts
    val currentUrl = BASE_URL
    val method = "GET"
    val body: String? = null

    val response = sendRequest(currentUrl, method, body)
    return response.second
}

/** GET /posts/{id} — получить пост по ID */
fun getPost(id: Int): String {
    val (_, body) = sendRequest("$BASE_URL/$id", "GET")
    return body
}

/** POST /posts — создать новый пост. Тело: {"title":"...", "body":"...", "userId":1} */
fun createPost(json: String): String {
    val (code, body) = sendRequest(BASE_URL, "POST", json)
    return "Status: $code, Body: $body"
}

/** PUT /posts/{id} — полностью обновить пост */
fun updatePost(id: Int, json: String): String {
    val (code, body) = sendRequest("$BASE_URL/$id", "PUT", json)
    return "Status: $code, Body: $body"
}

/** DELETE /posts/{id} — удалить пост, вернуть статус-код */
fun deletePost(id: Int): Int {
    val (code, _) = sendRequest("$BASE_URL/$id", "DELETE")
    return code
}

fun main() {
    disableSslVerification()

    println("\n=== GET ALL ===")
    println(getPosts().take(300) + "...")

    println("\n=== GET ONE (id=1) ===")
    println(getPost(1))

    println("\n=== CREATE ===")
    val newPost = """{"title": "New Post", "body": "Content", "userId": 1}"""
    println(createPost(newPost))

    println("\n=== UPDATE (id=1) ===")
    val updatedPost = """{"id": 1, "title": "Updated Title", "body": "New Body", "userId": 1}"""
    println(updatePost(1, updatedPost))

    println("\n=== DELETE (id=1) ===")
    println("Status Code: ${deletePost(1)}")
}