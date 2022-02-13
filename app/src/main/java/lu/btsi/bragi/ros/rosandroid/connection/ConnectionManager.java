package lu.btsi.bragi.ros.rosandroid.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;
import lu.btsi.bragi.ros.models.message.Message;
import lu.btsi.bragi.ros.models.message.MessageType;
import lu.btsi.bragi.ros.rosandroid.R;

/**
 * Created by gillesbraun on 13/03/2017.
 */

@Singleton
public class ConnectionManager implements ConnectionCallback, MessageCallbackHandler {
    private SharedPreferences preferences;
    private String host;
    private URI url;
    private Client client;

    @Deprecated
    private final List<ConnectionCallback> connectionCallbackList = new ArrayList<>();

    private final Map<UUID, MessageCallback> callbackMap = new HashMap<>();
    private final List<BroadcastCallback> broadcastCallbacks = new ArrayList<>();
    private final LinkedBlockingQueue<Message<?>> unsentMessages = new LinkedBlockingQueue<>();

    private static ConnectionManager instance;
    private boolean tryReconnect = false;

    private final MutableStateFlow<ConnectionState> _connectionState = StateFlowKt.MutableStateFlow(ConnectionState.Disconnected.INSTANCE);

    public static ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    public void initPreferences(Context context) {
        preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        loadSettings();
    }

    @Inject ConnectionManager() {
        runQueue();
        instance = this;
    }

    public void sendWithAction(Message message, MessageCallback messageCallback) {
        send(message);
        UUID messageID = message.getMessageID();
        callbackMap.put(messageID, messageCallback);
    }

    public String getRemoteIPAdress() {
        return host;
    }

    public void send(Message<?> message) {
        try {
            Log.d("ConnectionManager", "SEND: " + message.toString());
            client.send(message.toString());
        } catch (Exception e) {
            queueMessageForLater(message);
        }
    }

    private void queueMessageForLater(Message<?> message) {
        unsentMessages.add(message);
    }

    private void runQueue() {
        Thread runner = new Thread(() -> {
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                ArrayList<Message<?>> sendNext = new ArrayList<>();
                unsentMessages.drainTo(sendNext);
                if (!sendNext.isEmpty()) {
                    Log.d("ConnectionManager", "retrying to send messages");
                }
                for (Message message : sendNext) {
                    send(message);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "ConnectionManager queue retry thread");
        runner.start();
    }

    private void newClient() {
        if(host == null || url == null)
            return;
        if (client != null) {
            client.close();
            client.setConnectionCallback(null);
            client.setMessageCallbackHandler(null);
        }
        _connectionState.setValue(ConnectionState.Disconnected.INSTANCE);
        try {
            client = new Client(url);
            client.setConnectionCallback(this);
            client.setMessageCallbackHandler(this);
            client.connect();
        } catch (Exception e) {
            for (ConnectionCallback connectionCallback : connectionCallbackList) {
                connectionCallback.connectionError(e);
            }
        }
    }

    private void saveSettings() {
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("host", host);
        edit.apply();
    }

    private void loadSettings() {
        if(preferences != null) {
            String host = preferences.getString("host", null);
            if(host != null)
                connect(host);
        }
    }

    @Override
    public void connectionOpened() {
        _connectionState.setValue(new ConnectionState.Connected(host));
        tryReconnect = true;
        for (ConnectionCallback connectionCallback : connectionCallbackList) {
            connectionCallback.connectionOpened();
        }
    }

    @Override
    public void connectionClosed() {
        _connectionState.setValue(ConnectionState.Disconnected.INSTANCE);
        for (ConnectionCallback connectionCallback : connectionCallbackList) {
            connectionCallback.connectionClosed();
        }
        if(tryReconnect) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    newClient();
                }
            }, 2000);
        }
    }

    @Override
    public void connectionError(Exception e) {
        _connectionState.setValue(new ConnectionState.Failed(e));
        for (ConnectionCallback connectionCallback : connectionCallbackList) {
            connectionCallback.connectionError(e);
        }
    }

    @Override
    public void handleMessage(String text) {
        Log.d("ConnectionManager", "RECV: " + text);
        boolean isError = Message.messageType(text).equals(MessageType.Error);
        UUID messageUUID = Message.messageUUID(text);
        MessageCallback messageCallback = callbackMap.get(messageUUID);
        if(messageCallback != null) {
            new Handler(Looper.getMainLooper()).post(() -> {
                messageCallback.handleAnswer(text);
                callbackMap.remove(messageUUID);
            });
        } else {
            MessageType messageType = Message.messageType(text);
            if(messageType.equals(MessageType.Error)) {
                Log.e("ROS", text);
            } else if(messageType.equals(MessageType.Broadcast)) {
                for (BroadcastCallback broadcastCallback : broadcastCallbacks) {
                    broadcastCallback.handleBroadCast();
                }
            }
        }
    }

    public StateFlow<ConnectionState> getConnectionState() {
        return _connectionState;
    }

    public void connect(String host) {
        tryReconnect = false;
        if(host == null)
            return;
        if (host.contains(":")) {
            url = URI.create("ws://" + host);
            String[] split = host.split(":");
            if (split.length > 0)
                this.host = split[0];
        } else {
            url = URI.create("ws://" + host + ":8887");
            this.host = host;
        }
        saveSettings();
        newClient();
    }

    public String getHost() {
        return host;
    }

    public void addBroadcastCallback(BroadcastCallback broadcastCallback) {
        broadcastCallbacks.add(broadcastCallback);
    }

    public void removeBroadcastCallback(BroadcastCallback broadcastCallback) {
        broadcastCallbacks.remove(broadcastCallback);
    }

    @Deprecated
    public void addConnectionCallback(ConnectionCallback connectionCallback) {
        this.connectionCallbackList.add(connectionCallback);
    }

    @Deprecated
    public void removeConnectionCallback(ConnectionCallback connectionCallback) {
        this.connectionCallbackList.remove(connectionCallback);
    }
}
