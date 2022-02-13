package lu.btsi.bragi.ros.rosandroid.waiter;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.hilt.android.AndroidEntryPoint;
import java8.util.stream.StreamSupport;
import lu.btsi.bragi.ros.models.pojos.Order;
import lu.btsi.bragi.ros.models.pojos.ProductPriceForOrder;
import lu.btsi.bragi.ros.rosandroid.Config;
import lu.btsi.bragi.ros.rosandroid.MainActivity;
import lu.btsi.bragi.ros.rosandroid.R;
import lu.btsi.bragi.ros.rosandroid.managers.OrderManager;

/**
 * Created by gillesbraun on 17/03/2017.
 */
@AndroidEntryPoint
public class ReviewOrderDialog extends DialogFragment {
    @BindView(R.id.dialog_order_review_listView)
    ListView listViewProducts;

    @Inject OrderManager orderManager;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext());
        View view = View.inflate(getContext(), R.layout.dialog_order_review, null);
        dialog.setContentView(view);
        ButterKnife.bind(this, view);

        Order order = orderManager.getOrder();

        listViewProducts.setAdapter(new ReviewOrderAdapter(getContext(), order.getProductPriceForOrder()));

        return dialog;
    }

    @OnClick(R.id.dialog_order_review_button_ok)
    void buttonSendToServerPressed() {
        orderManager.sendToServer();
        dismiss();
        ((MainActivity)getActivity()).updateFabVisibility();
        // pop up to
    }

    @OnClick(R.id.dialog_order_review_button_cancel)
    void buttonCancelPressed() {
        dismiss();
    }

    class ReviewOrderAdapter extends ArrayAdapter<ProductPriceForOrder> {
        @BindView(R.id.single_order_review_textView_productName)
        TextView textViewProductName;

        @BindView(R.id.single_order_review_textView_quantity)
        TextView textViewQuantity;

        @BindView(R.id.single_order_review_textView_price)
        TextView textViewPrice;


        @BindView(R.id.single_order_review_textView_price_total)
        TextView textViewPriceTotal;

        ReviewOrderAdapter(@NonNull Context context, @NonNull List<ProductPriceForOrder> objects) {
            super(context, R.layout.single_order_review_product, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.single_order_review_product, parent, false);
            }
            ButterKnife.bind(this, convertView);
            ProductPriceForOrder ppfo = getItem(position);

            StreamSupport.stream(ppfo.getProduct().getProductLocalized())
                    .filter(pL -> pL.getLanguageCode().equals(Config.getInstance().getLanguage().getCode()))
                    .findFirst()
                    .ifPresent(pL -> textViewProductName.setText(pL.getLabel()));

            textViewQuantity.setText(ppfo.getQuantity().toString());
            String priceStrTotal = String.format(Config.getInstance().getLocale(getContext()), "%.2f€",
                    ppfo.getQuantity().longValue() * ppfo.getPricePerProduct().doubleValue());

            textViewPriceTotal.setText(priceStrTotal);

            String priceStr = String.format(Config.getInstance().getLocale(getContext()), "%.2f€",
                    ppfo.getPricePerProduct().doubleValue());

            textViewPrice.setText(priceStr);

            return convertView;
        }
    }
}
