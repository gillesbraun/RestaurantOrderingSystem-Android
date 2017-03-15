package lu.btsi.bragi.ros.rosandroid;

import org.jooq.types.UInteger;

import java.util.ArrayList;

import java8.util.stream.StreamSupport;
import lu.btsi.bragi.ros.models.message.Message;
import lu.btsi.bragi.ros.models.message.MessageType;
import lu.btsi.bragi.ros.models.pojos.Order;
import lu.btsi.bragi.ros.models.pojos.Product;
import lu.btsi.bragi.ros.models.pojos.ProductPriceForOrder;
import lu.btsi.bragi.ros.models.pojos.Table;
import lu.btsi.bragi.ros.models.pojos.Waiter;
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager;

/**
 * Created by Gilles Braun on 14.03.2017.
 */

public class OrderManager {
    private static final OrderManager ourInstance = new OrderManager();
    private Order order;

    public static OrderManager getInstance() {
        return ourInstance;
    }

    private OrderManager() {}

    public void sendToServer() {
        order.setWaiter(Config.getInstance().getWaiter());
        Message<Order> orderMessage = new Message<>(MessageType.Create, order, Order.class);
        ConnectionManager.getInstance().send(orderMessage);
        order = null;
    }

    public OrderManager addProductToOrder(Product product, UInteger quantity) {
        if(isProductInOrder(product)) {
            StreamSupport.stream(order.getProductPriceForOrder())
                    .filter(ppfo -> ppfo.getProductId().equals(product.getId()))
                    .findFirst().ifPresent(ppfo -> {
                ppfo.setQuantity(UInteger.valueOf(ppfo.getQuantity().longValue() + quantity.longValue()));
            });
        } else {
            ProductPriceForOrder productPriceForOrder = new ProductPriceForOrder();
            productPriceForOrder.setProductId(product.getId());
            productPriceForOrder.setProduct(product);
            productPriceForOrder.setPricePerProduct(product.getPrice());
            productPriceForOrder.setQuantity(quantity);

            if(order.getProductPriceForOrder() == null) {
                order.setProductPriceForOrder(new ArrayList<>());
            }
            order.getProductPriceForOrder().add(productPriceForOrder);
        }
        return ourInstance;
    }

    private boolean isProductInOrder(Product product) {
        return order != null && order.getProductPriceForOrder() != null && StreamSupport.stream(order.getProductPriceForOrder())
                .anyMatch(ppfo -> ppfo.getProductId().equals(product.getId()));
    }

    public boolean hasOpenOrder() {
        return order != null;
    }

    boolean orderHasProducts() {
        return order != null && order.getProductPriceForOrder() != null && order.getProductPriceForOrder().size() > 0;
    }

    public OrderManager createNew() {
        order = new Order();
        return ourInstance;
    }

    public OrderManager setTable(Table table) {
        order.setTable(table);
        order.setTableId(table.getId());
        return ourInstance;
    }

    public Table getTable() {
        return order.getTable();
    }

    public Order getOrder() {
        return order;
    }
}
