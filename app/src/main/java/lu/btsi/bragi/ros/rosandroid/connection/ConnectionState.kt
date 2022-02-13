package lu.btsi.bragi.ros.rosandroid.connection

sealed class ConnectionState {
    data class Connected(val host: String): ConnectionState()
    object Disconnected: ConnectionState()
    data class Failed(val error: Exception): ConnectionState()
}
