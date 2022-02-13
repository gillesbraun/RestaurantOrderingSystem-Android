package lu.btsi.bragi.ros.rosandroid.managers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import lu.btsi.bragi.ros.models.message.Message
import lu.btsi.bragi.ros.models.message.MessageGet
import lu.btsi.bragi.ros.models.pojos.Waiter
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionState
import lu.btsi.bragi.ros.rosandroid.di.ApplicationScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaiterManager @Inject constructor(
    private val connectionManager: ConnectionManager,
    @ApplicationScope private val coroutineScope: CoroutineScope,
) {

    private val _waiter = MutableStateFlow<Waiter?>(null)
    val waiter = _waiter.asStateFlow()

    private val _allWaiters = MutableStateFlow<List<Waiter>>(emptyList())
    val allWaiters = _allWaiters.asStateFlow()

    init {
        coroutineScope.launch {
            connectionManager.connectionState.filterIsInstance<ConnectionState.Connected>()
                .first()
            loadWaiters()
        }
    }

    private fun loadWaiters() {
        connectionManager.sendWithAction(MessageGet(Waiter::class.java)) { response ->
            _allWaiters.update { Message<Waiter>(response).payload }
        }
    }

    fun setWaiter(waiter: Waiter) {
        _waiter.value = waiter
    }

}