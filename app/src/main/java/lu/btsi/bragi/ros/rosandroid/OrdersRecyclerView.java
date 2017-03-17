package lu.btsi.bragi.ros.rosandroid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.util.stream.StreamSupport;
import lu.btsi.bragi.ros.models.message.Message;
import lu.btsi.bragi.ros.models.message.MessageType;
import lu.btsi.bragi.ros.models.pojos.Order;
import lu.btsi.bragi.ros.models.pojos.ProductLocalized;
import lu.btsi.bragi.ros.models.pojos.ProductPriceForOrder;
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java8.util.stream.Collectors.toList;

/**
 * Created by gillesbraun on 17/03/2017.
 */

class OrdersRecyclerView extends RecyclerView.Adapter<OrdersRecyclerView.OrderViewHolder> {
    private final List<Order> orderList;
    private final Context context;
    private ViewGroup parent;

    OrdersRecyclerView(List<Order> orderList, Context context) {
        super();
        this.orderList = orderList;
        this.context = context;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_order_card, parent, false);
        this.parent = parent;
        return new OrderViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        List<ProductLocalized> localizedList = StreamSupport.stream(order.getProductPriceForOrder())
                .map(ProductPriceForOrder::getProduct)
                .flatMap(
                        product ->
                                StreamSupport.stream(product.getProductLocalized())
                                        .filter(pL -> pL.getLanguageCode().equals(Config.getInstance().getLanguage().getCode()))
                )
                .collect(toList());
        ProductLocalizedWithQuantityAdapter adapter = new ProductLocalizedWithQuantityAdapter(
                context, order.getProductPriceForOrder());

        holder.linearLayoutContainer.removeAllViews();
        for (int i = 0; i < adapter.getCount(); i++) {
            View inflatedSingle = LayoutInflater.from(context).inflate(R.layout.single_order_card_product, parent, false);
            View view = adapter.getView(i, inflatedSingle, parent);
            holder.linearLayoutContainer.addView(view);
        }

        DateFormat dateFormat = android.text.format.DateFormat.getTimeFormat(context);
        String format = dateFormat.format(new Date(order.getCreatedAt().getTime()));
        holder.textViewTime.setText(String.format(Config.getInstance().getLocale(context), holder.strTime, format));

        holder.textViewWaiter.setText(String.format(Config.getInstance().getLocale(context), holder.strWaiter, order.getWaiter().getName()));

        holder.textViewTitle.setText(String.format(Config.getInstance().getLocale(context), holder.strTitle, order.getId().toString()));

        if ((order.getProcessing() == (byte) 1)) {
            holder.checkBoxIsProcessing.setVisibility(VISIBLE);
        } else {
            holder.checkBoxIsProcessing.setVisibility(GONE);
        }

        if (order.getProcessingDone() == (byte) 1) {
            holder.checkBoxIsProcessingDone.setVisibility(VISIBLE);
        } else {
            holder.checkBoxIsProcessingDone.setVisibility(GONE);
        }

        holder.buttonProcessing.setOnClickListener(v -> {
            order.setProcessing((byte) 1);
            ConnectionManager.getInstance().send(new Message<>(MessageType.Update, order, Order.class));
            notifyDataSetChanged();
        });

        holder.buttonProcessingDone.setOnClickListener(v -> {
            order.setProcessingDone((byte) 1);
            ConnectionManager.getInstance().send(new Message<>(MessageType.Update, order, Order.class));
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private class ProductLocalizedWithQuantityAdapter extends ArrayAdapter<ProductPriceForOrder> {
        ProductLocalizedWithQuantityAdapter(@NonNull Context context, @NonNull List<ProductPriceForOrder> objects) {
            super(context, R.layout.single_order_card_product, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.single_order_card_product, parent, false);
            }
            ProductPriceForOrder order = getItem(position);
            TextView productName = ButterKnife.findById(convertView, R.id.single_order_product_textView_name);

            String quantity = order.getQuantity().toString();
            StreamSupport.stream(order.getProduct().getProductLocalized())
                    .filter(pL -> pL.getLanguageCode().equals(Config.getInstance().getLanguage().getCode()))
                    .findFirst()
                    .ifPresent(pL -> productName.setText(quantity + " " + pL.getLabel()));

            return convertView;
        }
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.order_card_title)
        TextView textViewTitle;

        @BindView(R.id.order_card_waiter)
        TextView textViewWaiter;

        @BindView(R.id.order_card_time)
        TextView textViewTime;

        @BindView(R.id.order_card_list_container)
        LinearLayout linearLayoutContainer;

        @BindString(R.string.order_card_time)
        String strTime;

        @BindString(R.string.order_card_waiter)
        String strWaiter;

        @BindString(R.string.order_card_title)
        String strTitle;

        @BindView(R.id.order_card_button_processing)
        Button buttonProcessing;

        @BindView(R.id.order_card_button_processingdone)
        Button buttonProcessingDone;

        @BindView(R.id.order_card_isprocessing)
        CheckBox checkBoxIsProcessing;

        @BindView(R.id.order_card_isprocessingdone)
        CheckBox checkBoxIsProcessingDone;

        OrderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
