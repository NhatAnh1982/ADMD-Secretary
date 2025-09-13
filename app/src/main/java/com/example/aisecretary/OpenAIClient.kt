package com.example.aisecretary

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object OpenAIClient {
    private val client = OkHttpClient.Builder().build()
    private const val OPENAI_API_KEY = "YOUR_OPENAI_API_KEY"

    fun callChatCompletion(prompt: String): String {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val bodyJson = JSONObject()
        bodyJson.put("model", "gpt-4o-mini")
        val messages = org.json.JSONArray()
        val msg = JSONObject()
        msg.put("role", "user")
        msg.put("content", prompt)
        messages.put(msg)
        bodyJson.put("messages", messages)

        val requestBody = bodyJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $OPENAI_API_KEY")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("HTTP " + resp.code + ": " + resp.message)
            val text = resp.body!!.string()
            val json = JSONObject(text)
            val choices = json.getJSONArray("choices")
            if (choices.length() > 0) {
                val first = choices.getJSONObject(0)
                val message = first.getJSONObject("message")
                return message.getString("content")
            }
            return ""
        }
    }
}
