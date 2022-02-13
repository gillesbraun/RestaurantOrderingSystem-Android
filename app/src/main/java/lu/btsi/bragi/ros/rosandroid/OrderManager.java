package lu.btsi.bragi.ros.rosandroid;

import org.jooq.types.UInteger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import java8.util.stream.StreamSupport;
import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.StateFlowKt;
import lu.btsi.bragi.ros.models.message.Message;
import lu.btsi.bragi.ros.models.message.MessageType;
import lu.btsi.bragi.ros.models.pojos.Order;
import lu.btsi.bragi.ros.models.pojos.Product;
import lu.btsi.bragi.ros.models.pojos.ProductPriceForOrder;
import lu.btsi.bragi.ros.models.pojos.Table;
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager;

/**
 * Created by Gilles Braun on 14.03.2017.
 */

@Singleton
public class OrderManager {
    private final ConnectionManager connectionManager;
    private final WaiterManager waiterManager;
    private Order order;
    private final MutableStateFlow<List<ProductPriceForOrder>> _products = StateFlowKt.MutableStateFlow(Collections.emptyList());

    @Inject
    OrderManager(ConnectionManager connectionManager, WaiterManager waiterManager) {
        this.connectionManager = connectionManager;
        this.waiterManager = waiterManager;
    }

    public void sendToServer() {
        order.setWaiter(waiterManager.getWaiter().getValue());
        Message<Order> orderMessage = new Message<>(MessageType.Create, order, Order.class);
        connectionManager.send(orderMessage);
        order = null;
    }

    public void addProductToOrder(Product product, long quantity) {
        if(isProductInOrder(product)) {
            StreamSupport.stream(order.getProductPriceForOrder())
                    .filter(ppfo -> ppfo.getProductId().equals(product.getId()))
                    .findFirst().ifPresent(ppfo -> {
                ppfo.setQuantity(UInteger.valueOf(ppfo.getQuantity().longValue() + quantity));
            });
        } else {
            ProductPriceForOrder productPriceForOrder = new ProductPriceForOrder();
            productPriceForOrder.setProductId(product.getId());
            productPriceForOrder.setProduct(product);
            productPriceForOrder.setPricePerProduct(product.getPrice());
            productPriceForOrder.setQuantity(UInteger.valueOf(quantity));

            if(order.getProductPriceForOrder() == null) {
                order.setProductPriceForOrder(new ArrayList<>());
            }
            order.getProductPriceForOrder().add(productPriceForOrder);
        }
    }

    private boolean isProductInOrder(Product product) {
        return order != null && order.getProductPriceForOrder() != null && StreamSupport.stream(order.getProductPriceForOrder())
                .anyMatch(ppfo -> ppfo.getProductId().equals(product.getId()));
    }

    public boolean hasOpenOrder() {
        return order != null;
    }

    public boolean orderHasProducts() {
        return order != null && order.getProductPriceForOrder() != null && order.getProductPriceForOrder().size() > 0;
    }

    public void createNew() {
        order = new Order();
    }

    public void setTable(Table table) {
        order.setTable(table);
        order.setTableId(table.getId());
    }

    public Table getTable() {
        return order == null ? null : order.getTable();
    }

    public Order getOrder() {
        return order;
    }

    public void removeProductFromOrder(Product product) {
        if(order != null && order.getProductPriceForOrder() != null) {
            StreamSupport.stream(order.getProductPriceForOrder())
                    .filter(ppfo -> ppfo.getProduct().equals(product))
                    .findFirst()
                    .ifPresent(ppfo -> order.getProductPriceForOrder().remove(ppfo));
        }
    }

    public void changeQuantity(Product product, int quantity) {
        if(order != null && order.getProductPriceForOrder() != null) {
            StreamSupport.stream(order.getProductPriceForOrder())
                    .filter(ppfo -> ppfo.getProduct().equals(product))
                    .filter(ppfo -> quantity < 0 && ppfo.getQuantity().longValue() + quantity > 0 || quantity > 0)
                    .findFirst()
                    .ifPresent(ppfo -> ppfo.setQuantity(UInteger.valueOf(ppfo.getQuantity().longValue() + quantity)));
        }
    }

    public void clear() {
        order = null;
    }
}
