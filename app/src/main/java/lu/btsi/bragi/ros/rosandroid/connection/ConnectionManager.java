package lu.btsi.bragi.ros.rosandroid.connection;

import android.content.Context;
import android.content.SharedPreferences;
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
import lu.btsi.bragi.ros.rosandroid.MainActivity;
import lu.btsi.bragi.ros.rosandroid.R;

/**
 * Created by gillesbraun on 13/03/2017.
 */

public class ConnectionManager implements ConnectionCallback, MessageCallbackHandler {
    private SharedPreferences preferences;
    private String host;
    private URI url;
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

    public void initPreferences(MainActivity mainActivity) {
        instance.preferences = mainActivity.getSharedPreferences(mainActivity.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        loadSettings();
    }

    private ConnectionManager(ConnectionCallback connectionCallback) {
        this.connectionCallback = connectionCallback;
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
            if(client != null && message != null && isConnected)
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
        if(host == null || url == null)
            return;
        try {
            client = new Client(url);
            client.setConnectionCallback(this);
            client.setMessageCallbackHandler(this);
            client.connect();
        } catch (Exception e) {
            connectionCallback.connectionError(e);
        }
    }

    private void saveSettings() {
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("host", host);
        edit.apply();
    }

    private void loadSettings() {
        if(preferences != null) {
            setHost(preferences.getString("host", null));
        }
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
    public void connectionError(Exception e) {
        connectionCallback.connectionError(e);
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
            Log.e("ROS", text);
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setHost(String host) {
        this.host = host;
        isConnected = false;
        url = URI.create("ws://"+host+":8887");
        saveSettings();
        newClient();
    }

    public String getHost() {
        return host;
    }
}
