package lu.btsi.bragi.ros.rosandroid.connection;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import lu.btsi.bragi.ros.models.message.Message;
import lu.btsi.bragi.ros.models.message.MessageType;

/**
 * Created by gillesbraun on 13/03/2017.
 */

public class ConnectionManager implements ConnectionCallback, MessageCallbackHandler {
    private String host = "192.168.0.77";
    private URI url = URI.create("ws://"+host+":8887");
    private Client client;
    private boolean isConnected = false;
    private ConnectionCallback connectionCallback;
    private Map<UUID, MessageCallback> callbackMap = new HashMap<>();
    private final Set<Message> unsentMessages = new HashSet<>();

    private static ConnectionManager instance;
    private boolean queueRunning;

    public static ConnectionManager getInstance() {
        return instance;
    }

    public static void init(ConnectionCallback callback) {
        instance = new ConnectionManager(callback);
    }

    private ConnectionManager(ConnectionCallback connectionCallback) {
        this.connectionCallback = connectionCallback;
        newClient();
    }

    public void sendWithAction(Message message, MessageCallback messageCallback) {
        send(message);
        UUID messageID = message.getMessageID();
        callbackMap.put(messageID, messageCallback);
    }

    public String getRemoteIPAdress() {
        return host;
    }

    public void send(Message message) {
        try {
            client.send(message.toString());
        } catch (WebsocketNotConnectedException e) {
            queueMessageForLater(message);
        }
    }

    private void queueMessageForLater(Message message) {
        unsentMessages.add(message);
        startQueueTimer();
    }

    private void startQueueTimer() {
        if(!queueRunning) {
            queueRunning = true;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    List<Message> sendNext = new ArrayList<>();
                    for(Iterator<Message> it = unsentMessages.iterator(); it.hasNext();) {
                        Message message = it.next();
                        sendNext.add(message);
                        it.remove();
                    }
                    for (Message message : sendNext) {
                        send(message);
                    }
                    queueRunning = false;
                }
            }, 1000);
        }
    }

    private void newClient() {
        client = new Client(url);
        client.setConnectionCallback(this);
        client.setMessageCallbackHandler(this);
        client.connect();
    }

    @Override
    public void connectionOpened() {
        isConnected = true;
        connectionCallback.connectionOpened();
    }

    @Override
    public void connectionClosed() {
        isConnected = false;
        connectionCallback.connectionClosed();
    }

    @Override
    public void handleMessage(String text) {
        boolean isError = Message.messageType(text).equals(MessageType.Error);
        UUID messageUUID = Message.messageUUID(text);
        MessageCallback messageCallback = callbackMap.get(messageUUID);
        if(messageCallback != null) {
            new Handler(Looper.getMainLooper()).post(() -> {
                messageCallback.handleAnswer(text);
                callbackMap.remove(messageUUID);
            });
        } else {
            Log.e("ROS", "SOMTHING WRONG");
            Log.e("ROS", text);
        }
    }
}
