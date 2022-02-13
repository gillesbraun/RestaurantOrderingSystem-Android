package lu.btsi.bragi.ros.rosandroid.connection

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

/**
 * Created by gillesbraun on 13/03/2017.
 */
class Client(serverURI: URI?) : WebSocketClient(serverURI) {
    var connectionCallback: ConnectionManager? = null
    var messageCallbackHandler: MessageCallbackHandler? = null

    override fun onOpen(handshakedata: ServerHandshake) {
        connectionCallback?.connectionOpened()
    }

    override fun onMessage(message: String) {
        messageCallbackHandler?.handleMessage(message)
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        connectionCallback?.connectionClosed()
    }

    override fun onError(ex: Exception) {
        connectionCallback?.connectionError(ex)
    }
}