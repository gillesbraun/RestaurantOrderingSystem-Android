package lu.btsi.bragi.ros.rosandroid.managers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import lu.btsi.bragi.ros.models.message.Message
import lu.btsi.bragi.ros.models.message.MessageType
import lu.btsi.bragi.ros.models.pojos.Order
import lu.btsi.bragi.ros.models.pojos.Product
import lu.btsi.bragi.ros.models.pojos.ProductPriceForOrder
import lu.btsi.bragi.ros.models.pojos.Table
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager
import org.jooq.types.UInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Gilles Braun on 14.03.2017.
 */
@Singleton
class OrderManager @Inject internal constructor(
    private val connectionManager: ConnectionManager,
    private val waiterManager: WaiterManager
) {
    private val _products = MutableStateFlow<List<ProductPriceForOrder>>(emptyList())
    val products = _products.asStateFlow()

    private val _table = MutableStateFlow<Table?>(null)
    val table = _table.asStateFlow()

    fun sendToServer() {
        val order = Order().apply {
            waiter = waiterManager.waiter.value
            table = _table.value
            tableId = _table.value!!.id
        }
        _products.value = emptyList()
        val orderMessage = Message(MessageType.Create, order, Order::class.java)
        connectionManager.send(orderMessage)
    }

    fun addProduct(product: Product, quantity: Int) {
        if (isProductInOrder(product)) {
            changeQuantity(product, quantity)
        } else {
            val productPriceForOrder = ProductPriceForOrder()
            productPriceForOrder.productId = product.id
            productPriceForOrder.product = product
            productPriceForOrder.pricePerProduct = product.price
            productPriceForOrder.quantity = UInteger.valueOf(quantity)
            _products.update {
                it + productPriceForOrder
            }
        }
    }

    private fun isProductInOrder(product: Product): Boolean {
        return _products.value.any { it.productId == product.id }
    }

    fun createNew() {
        _products.value = emptyList()
    }

    fun setTable(table: Table) {
        _table.value = table
    }

    fun removeProductFromOrder(product: Product) {
        _products.update { list ->
            list.filter { it.productId != product.id }
        }
    }

    fun changeQuantity(product: Product, quantity: Int) {
        val index = _products.value
            .indexOfFirst { it.productId == product.id }
            .takeIf { it != -1 }
        if (index != null) {
            _products.update { originalList ->
                val list = originalList.toMutableList()
                val toUpdate = list[index]
                val newQuantity = toUpdate.quantity.toLong() + quantity
                toUpdate.quantity = UInteger.valueOf(newQuantity)
                if (newQuantity > 0) {
                    list[index] = toUpdate
                }
                list
            }
        }
    }

}