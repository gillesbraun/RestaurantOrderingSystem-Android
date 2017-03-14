package lu.btsi.bragi.ros.rosandroid.connection;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * Created by gillesbraun on 13/03/2017.
 */

public class Client extends WebSocketClient {
    private ConnectionManager connectionCallback;
    private MessageCallbackHandler messageCallbackHandler;

    Client(URI serverURI) {
        super(serverURI);
        Log.d("ROS", "trying connection to "+ serverURI.toString());
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        connectionCallback.connectionOpened();
    }

    @Override
    public void onMessage(String message) {
        messageCallbackHandler.handleMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        connectionCallback.connectionClosed();
    }

    @Override
    public void onError(Exception ex) {
        Log.d("ROS", "ERROR "+ex.getMessage());
    }

    void setConnectionCallback(ConnectionManager connectionCallback) {
        this.connectionCallback = connectionCallback;
    }

    public void setMessageCallbackHandler(MessageCallbackHandler messageCallbackHandler) {
        this.messageCallbackHandler = messageCallbackHandler;
    }

    public MessageCallbackHandler getMessageCallbackHandler() {
        return messageCallbackHandler;
    }
}
