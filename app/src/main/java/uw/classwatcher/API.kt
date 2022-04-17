package uw.classwatcher

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

inline fun <reified T> Gson.fromJson(json: String): T = fromJson<T>(json, object : TypeToken<T>() {}.type)

class API(private val key: String) {
    private val gson = Gson()
    private val client = OkHttpClient()

    private inline fun <reified T> request(endpoint: String): T {
        val request = Request.Builder()
            .url("https://openapi.data.uwaterloo.ca${endpoint}")
            .header("accept", "application/json")
            .header("x-api-key", key)
            .build()

        val result = client.newCall(request).execute().use { response ->
            if (response.isSuccessful) response.body!!.string() else throw IOException("Unexpected code $response")
        }

        return gson.fromJson(result)
    }

    fun getClassSchedules(term: String, subject: String, catalogNumber: String) =
        request<List<io.swagger.client.models.ClassValue>>("/v3/ClassSchedules/${term}/${subject}/${catalogNumber}")
}