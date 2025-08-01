package com.example.botactivity

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


object BackendApi {



    // for using emulator
    //private const val SERVER_URL = "http://10.0.2.2:3000/create-call"

    // for using a physical
  // replace this 192.168.69.227  with your laptop's ip adress
     private const val SERVER_URL = "http://192.168.69.227:3000/create-call"

    private val client = OkHttpClient()

    suspend fun createCallAndGetJoinUrl(): String {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(SERVER_URL)
                .post("".toRequestBody(null))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Failed to get joinUrl from server: ${response.message}. HTTP Code: ${response.code}")
                }

                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    throw Exception("Server returned an empty response.")
                }

                val jsonObject = JSONObject(responseBody)
                return@withContext jsonObject.getString("joinUrl")
            }
        }
    }
}