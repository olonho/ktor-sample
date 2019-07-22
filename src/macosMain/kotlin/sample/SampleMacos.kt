package sample

import io.ktor.client.*
import io.ktor.client.engine.curl.Curl
import io.ktor.client.request.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import kotlin.native.SharedImmutable
import kotlin.native.ThreadLocal
import kotlin.native.concurrent.*

fun fetchSimple(url: String) = runBlocking {
    val client = HttpClient()
    val result = client.get<String>(url)
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

val backgroundWorker = Worker.start()
@ThreadLocal
val jsonParser = Json(JsonConfiguration.Stable)
@SharedImmutable
val mailbox = AtomicReference<JsonElement?>(null)

fun fetchBackground(url: String) {
    val future = backgroundWorker.execute(TransferMode.SAFE, { url }) {
        val client = HttpClient(Curl)
        val result = runBlocking {
            client.get<String>(it)
        }
        mailbox.value = (try {
            jsonParser.parseJson(result)
        } catch (e: SerializationException) {
            JsonPrimitive("<error: ${e.message}>")
        }).freeze()
        client.close()
        "OK"
    }
    println(future.result)
    println(mailbox.value)
}

data class UserId(val userId: String, val id: Int, val title: String, val completed: Boolean)

fun fetchObject(url: String) = runBlocking {
    val client = HttpClient() {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }
    val result = client.get<UserId>(url)
    println("result : $result")
    client.close()
}

fun main() {
    fetchSimple("https://jsonplaceholder.typicode.com/todos/1")
    //fetchBackground("https://jsonplaceholder.typicode.com/todos/2")
    fetchObject("https://jsonplaceholder.typicode.com/todos/3")
}