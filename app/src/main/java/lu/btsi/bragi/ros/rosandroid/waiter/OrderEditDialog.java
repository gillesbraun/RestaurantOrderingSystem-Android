package lu.btsi.bragi.ros.rosandroid.waiter;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.hilt.android.AndroidEntryPoint;
import java8.util.stream.StreamSupport;
import lu.btsi.bragi.ros.models.pojos.Order;
import lu.btsi.bragi.ros.models.pojos.Product;
import lu.btsi.bragi.ros.models.pojos.ProductPriceForOrder;
import lu.btsi.bragi.ros.rosandroid.Config;
import lu.btsi.bragi.ros.rosandroid.MainActivity;
import lu.btsi.bragi.ros.rosandroid.R;
import lu.btsi.bragi.ros.rosandroid.managers.OrderManager;

/**
 * Created by gillesbraun on 15/03/2017.
 */

@AndroidEntryPoint
public class OrderEditDialog extends DialogFragment {
    @BindView(R.id.dialog_order_listView)
    ListView productListView;

    @Inject OrderManager orderManager;
    private Order order;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        order = orderManager.getOrder();
        Dialog dialog = new Dialog(getContext());
        View view = View.inflate(getContext(), R.layout.dialog_order_edit, null);
        dialog.setContentView(view);
        ButterKnife.bind(this, view);

        productListView.setAdapter(new OrderEditAdapter());

        return dialog;
    }

    @OnClick(R.id.dialog_order_buttonClose)
    void closeDialog() {
        dismiss();
    }

    class OrderEditAdapter extends BaseAdapter {
        @BindView(R.id.single_order_textView_price)
        TextView textViewPrice;

        @BindView(R.id.single_order_textView_unitprice)
        TextView textViewUnitPrice;

        @BindView(R.id.single_order_textView_productName)
        TextView textViewProductName;

        @BindView(R.id.single_order_textView_quantity)
        TextView textViewQuantity;

        @BindView(R.id.single_order_button_remove)
        Button buttonRemove;

        @BindView(R.id.single_order_button_increase)
        Button buttonIncrease;

        @BindView(R.id.single_order_button_decrease)
        Button buttonDecrease;

        private ProductPriceForOrder productPriceForOrder;

        @Override
        public int getCount() {
            return order.getProductPriceForOrder().size();
        }

        @Override
        public Object getItem(int position) {
            return order.getProductPriceForOrder().get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = View.inflate(getContext(), R.layout.single_order_edit, null);
            }
            ButterKnife.bind(this, convertView);


            productPriceForOrder = order.getProductPriceForOrder().get(position);
            double price = productPriceForOrder.getPricePerProduct().doubleValue() * productPriceForOrder.getQuantity().longValue();
            String priceStr = String.format(Locale.GERMANY, "%.2f €", price);
            String unitPriceStr = String.format(Config.getInstance().getLocale(getContext()), "%.2f €", productPriceForOrder.getPricePerProduct().doubleValue());

            textViewPrice.setText(priceStr);
            textViewUnitPrice.setText(unitPriceStr);

            textViewQuantity.setText(String.valueOf(productPriceForOrder.getQuantity().longValue()));

            StreamSupport.stream(productPriceForOrder.getProduct().getProductLocalized())
                    .filter(pL -> pL.getLanguageCode().equals(Config.getInstance().getLanguage().getCode()))
                    .findFirst()
                    .ifPresent(pL -> textViewProductName.setText(pL.getLabel()));

            buttonRemove.setTag(productPriceForOrder.getProduct());
            buttonDecrease.setTag(productPriceForOrder.getProduct());
            buttonIncrease.setTag(productPriceForOrder.getProduct());

            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            ((MainActivity)getActivity()).updateFabVisibility();
            if(orderManager.hasOpenOrder() && ! orderManager.orderHasProducts()) {
                dismiss();
            }
        }

        @OnClick(R.id.single_order_button_remove)
        void removeClicked(View view) {
            orderManager.removeProductFromOrder((Product) view.getTag());
            notifyDataSetChanged();
        }

        @OnClick(R.id.single_order_button_increase)
        void increaseQuantityClicked(View view) {
            orderManager.changeQuantity((Product) view.getTag(), 1);
            notifyDataSetChanged();
        }

        @OnClick(R.id.single_order_button_decrease)
        void decreaseQuantityClicked(View view) {
            orderManager.changeQuantity((Product) view.getTag(), -1);
            notifyDataSetChanged();
        }
    }
}
