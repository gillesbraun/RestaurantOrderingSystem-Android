package lu.btsi.bragi.ros.rosandroid.waiter;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jooq.types.UInteger;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import java8.util.Optional;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import lu.btsi.bragi.ros.models.pojos.AllergenLocalized;
import lu.btsi.bragi.ros.models.pojos.Product;
import lu.btsi.bragi.ros.models.pojos.ProductAllergen;
import lu.btsi.bragi.ros.models.pojos.ProductLocalized;
import lu.btsi.bragi.ros.rosandroid.Config;
import lu.btsi.bragi.ros.rosandroid.OrderManager;
import lu.btsi.bragi.ros.rosandroid.R;

/**
 * Created by Gilles Braun on 14.03.2017.
 */

public class SingleProductDialog extends DialogFragment {
    private Product product;
    private String language = Config.getInstance().getLanguage().getCode();

    @BindView(R.id.content_product_price)
    TextView priceTextView;

    @BindView(R.id.content_product_title)
    TextView titleTextView;

    @BindView(R.id.content_product_allergen_title)
    TextView allergensTitle;

    @BindView(R.id.content_product_allergen_listView)
    ListView allergenListView;

    @BindView(R.id.content_product_textView_numberOfItems)
    TextView textViewNumberOfItems;

    @BindView(R.id.content_product_button_decreaseItemCount)
    Button buttonDecreaseItemCount;

    private int currentQuantity = 1;

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog showProductSingle = new Dialog(getContext());
        View view = View.inflate(getContext(), R.layout.content_product, null);
        showProductSingle.setContentView(view);
        ButterKnife.bind(this, view);


        Optional<ProductLocalized> productTranslation = StreamSupport.stream(product.getProductLocalized())
                .filter(pL -> pL.getLanguageCode().equals(language))
                .findFirst();

        List<AllergenLocalized> allergens = StreamSupport.stream(product.getProductAllergen())
                .map(ProductAllergen::getAllergen)
                .flatMap(allergen -> StreamSupport.stream(allergen.getAllergenLocalized())
                        .filter(aL -> aL.getLanguageCode().equals(language))
                )
                .collect(Collectors.toList());

        titleTextView.setText(productTranslation.get().getLabel());
        String price = String.format(Locale.GERMANY, "%.2f €", product.getPrice().doubleValue());
        priceTextView.setText(price);

        if(allergens.size() == 0) {
            allergensTitle.setVisibility(View.GONE);
            allergenListView.setVisibility(View.GONE);
        }
        ListAdapter allergenAdapter = new AllergenAdapter(allergens);
        allergenListView.setAdapter(allergenAdapter);
        return showProductSingle;
    }

    @OnClick(R.id.content_product_button_addToOrder)
    public void buttonAddToOrderPressed() {
        OrderManager.getInstance().addProductToOrder(product, UInteger.valueOf(currentQuantity));
        dismiss();
    }

    @OnClick(R.id.content_product_button_increaseItemCount)
    public void buttonIncreasePressed() {
        currentQuantity++;
        updateQuantityLabel();
    }

    @OnClick(R.id.content_product_button_decreaseItemCount)
    public void buttonDecreasePressed() {
        if(currentQuantity > 1) {
            currentQuantity--;
            updateQuantityLabel();
        }
    }

    private void updateQuantityLabel() {
        textViewNumberOfItems.setText(String.valueOf(currentQuantity));
        buttonDecreaseItemCount.setEnabled(currentQuantity > 1);
    }

    private class AllergenAdapter extends BaseAdapter {
        private List<AllergenLocalized> allergens;

        public AllergenAdapter(List<AllergenLocalized> allergens) {
            this.allergens = allergens;
        }

        @Override
        public int getCount() {
            return allergens.size();
        }

        @Override
        public Object getItem(int position) {
            return allergens.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(convertView == null)
                convertView = inflater.inflate(R.layout.single_allergen, parent, false);
            TextView single = (TextView)convertView.findViewById(R.id.single_allergen_label);
            single.setText(allergens.get(position).getLabel());
            return convertView;
        }
    }
}
