package lu.btsi.bragi.ros.rosandroid;

import org.jooq.types.UInteger;

import java.util.ArrayList;

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
        ProductPriceForOrder productPriceForOrder = new ProductPriceForOrder();
        productPriceForOrder.setProductId(product.getId());
        productPriceForOrder.setPricePerProduct(product.getPrice());
        productPriceForOrder.setQuantity(quantity);

        if(order.getProductPriceForOrder() == null) {
            order.setProductPriceForOrder(new ArrayList<>());
        }
        order.getProductPriceForOrder().add(productPriceForOrder);
        return ourInstance;
    }

    public boolean hasOpenOrder() {
        return order != null;
    }

    public OrderManager createNew() {
        order = new Order();
        return ourInstance;
    }

    public OrderManager setTable(Table table) {
        order.setTable(table);
        return ourInstance;
    }

    public Table getTable() {
        return order.getTable();
    }
}
