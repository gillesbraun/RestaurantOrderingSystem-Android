package lu.btsi.bragi.ros.rosandroid.ui.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionState
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val connectionManager: ConnectionManager
): ViewModel() {

    val connectionState: StateFlow<ConnectionState> = connectionManager.connectionState

    fun getHost() = connectionManager.host

    fun connect(host: String) {
        connectionManager.connect(host)
    }

}