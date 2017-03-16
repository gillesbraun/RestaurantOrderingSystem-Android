package lu.btsi.bragi.ros.rosandroid.connection;

/**
 * Created by gillesbraun on 13/03/2017.
 */

public interface ConnectionCallback {
    void connectionOpened();

    void connectionClosed();

    void connectionError(Exception e);
}
