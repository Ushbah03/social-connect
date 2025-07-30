// NotificationRealtime.kt
package com.example.socialconnect1

import android.util.Log
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.util.*

object NotificationRealtime {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    fun startListening(currentUserId: String, onNotification: (String) -> Unit) {
        val url = "wss://xeuparngwfclppqemmfy.supabase.co/realtime/v1/websocket?apikey=${SessionManager.accessToken}&vsn=1.0.0"
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d("NotifRealtime", "✅ WebSocket Opened")

                // Join the channel for notifications
                val joinMsg = JSONObject().apply {
                    put("topic", "realtime:public:notifications")
                    put("event", "phx_join")
                    put("payload", JSONObject())
                    put("ref", UUID.randomUUID().toString())
                }

                ws.send(joinMsg.toString())
            }

            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    if (json.getString("event") == "INSERT") {
                        val payload = json.getJSONObject("payload")
                        val recipientId = payload.getString("user_id")
                        val type = payload.getString("type")
                        val senderId = payload.getString("sender_id")

                        if (recipientId == currentUserId && senderId != currentUserId) {
                            onNotification("You received a new $type!")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("NotifRealtime", "Parsing error: ${e.message}")
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("NotifRealtime", "WebSocket error: ${t.message}")
            }
        })
    }

    fun stopListening() {
        webSocket?.close(1000, null)
        webSocket = null
    }
}
