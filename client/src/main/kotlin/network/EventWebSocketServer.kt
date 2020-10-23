package network

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

const val WEB_SOCKET_PORT = 8081

fun main() {
    EventWebSocketServer(InetSocketAddress("localhost", WEB_SOCKET_PORT)).run()
}

class EventWebSocketServer(inetSocketAddress: InetSocketAddress) : WebSocketServer(inetSocketAddress) {

    private val subscribers = mutableListOf<WebSocket?>()

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        conn?.send("Connected")
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        subscribers.remove(conn)
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        if (message == "Subscribe") {
            subscribers.add(conn)
            conn?.send("Subscribed")
        } else {
            subscribers.forEach {
                it?.send(message)
            }
        }
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {

    }

    override fun onStart() {

    }

}