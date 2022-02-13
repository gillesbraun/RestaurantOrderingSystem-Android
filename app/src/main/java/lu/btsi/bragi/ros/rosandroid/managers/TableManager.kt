package lu.btsi.bragi.ros.rosandroid.managers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import lu.btsi.bragi.ros.models.message.Message
import lu.btsi.bragi.ros.models.message.MessageGet
import lu.btsi.bragi.ros.models.pojos.Table
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionState
import lu.btsi.bragi.ros.rosandroid.di.ApplicationScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TableManager @Inject constructor(
    private val connectionManager: ConnectionManager,
    @ApplicationScope private val coroutineScope: CoroutineScope,
) {

    private val _allTables = MutableStateFlow<List<Table>>(emptyList())
    val allTables = _allTables.asStateFlow()

    init {
        coroutineScope.launch {
            connectionManager.connectionState.filterIsInstance<ConnectionState.Connected>()
                .first()
            loadTables()
        }
    }

    private fun loadTables() {
        connectionManager.sendWithAction(MessageGet(Table::class.java)) { response ->
            _allTables.update { Message<Table>(response).payload }
        }
    }

}