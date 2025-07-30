package com.example.socialconnect1.realtime

import android.app.Notification
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.util.*


class RealtimeClient {

    private val client = OkHttpClient()

    // Replace this with your actual Supabase project URL and key
    private val supabaseRealtimeUrl =
        "wss://xeuparngwfclppqemmfy.supabase.co/realtime/v1/websocket?apikey=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InhldXBhcm5nd2ZjbHBwcWVtbWZ5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTI2ODc0NDQsImV4cCI6MjA2ODI2MzQ0NH0.RzG2l5anjQwIIfWlU7oc-FyozLJMo8tnFgAyeyYxPGM&vsn=1.0.0"

    private var webSocket: WebSocket? = null

    fun connect(onLikeOrComment: (String, String) -> Unit) {
        val request = Request.Builder().url(supabaseRealtimeUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                println("✅ WebSocket Connected")

                // Subscribe to likes
                ws.send(buildJoinMessage("likes"))
                // Subscribe to comments
                ws.send(buildJoinMessage("comments"))
            }

            override fun onMessage(ws: WebSocket, text: String) {
                if (text.contains("\"type\":\"INSERT\"")) {
                    val json = JSONObject(text)
                    val topic = json.optString("topic")
                    val payload = json.optJSONObject("payload")
                    val newRecord = payload?.optJSONObject("new")

                    val postId = newRecord?.optString("post_id") ?: return

                    when {
                        topic.contains("likes") -> {
                            onLikeOrComment("like", postId)
                        }
                        topic.contains("comments") -> {
                            onLikeOrComment("comment", postId)
                        }
                    }
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                println("❌ WebSocket Failed: ${t.message}")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                println("ℹ️ WebSocket Closed: $reason")
            }
        })
    }
    private fun buildJoinMessage(tableName: String): String {
        val obj = JSONObject()
        obj.put("topic", "realtime:public:$tableName")
        obj.put("event", "phx_join")
        obj.put("payload", JSONObject())
        obj.put("ref", UUID.randomUUID().toString())
        return obj.toString()
    }
    fun disconnect() {
        webSocket?.close(1000, "Closed by app")
        webSocket = null
    }
}
