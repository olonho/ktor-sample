package sample

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*

fun main() = runBlocking {
    val client = HttpClient()
    val result = client.get<String>("https://jsonplaceholder.typicode.com/todos/1")
    println("Result: $result")
    val jsonParser = Json(JsonConfiguration.Stable)
    val json = try {
        jsonParser.parseJson(result)
    } catch (e: SerializationException) {
        JsonPrimitive("<error: ${e.message}>")
    }
    println("JSON : $json id = ${json.jsonObject["id"]}")
    client.close()
}
