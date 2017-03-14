package lu.btsi.bragi.ros.rosandroid.waiter;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java8.util.Optional;
import java8.util.function.Consumer;
import java8.util.function.Predicate;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import lu.btsi.bragi.ros.models.pojos.AllergenLocalized;
import lu.btsi.bragi.ros.models.pojos.Product;
import lu.btsi.bragi.ros.models.pojos.ProductAllergen;
import lu.btsi.bragi.ros.models.pojos.ProductLocalized;
import lu.btsi.bragi.ros.rosandroid.Config;
import lu.btsi.bragi.ros.rosandroid.MainActivity;
import lu.btsi.bragi.ros.rosandroid.R;

/**
 * Created by gillesbraun on 13/03/2017.
 */
public class WaiterProductsFragment extends Fragment implements AdapterView.OnItemClickListener {
    private List<Product> products;
    private String language = Config.getInstance().getLanguage().getCode();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView listView_products = (ListView) view.findViewById(R.id.listView_products);

        ListAdapter adapter = new ProductsAdapter();

        listView_products.setOnItemClickListener(this);
        listView_products.setAdapter(adapter);
    }

    // Product is pressed
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Product product = products.get(position);

        SingleProductDialog productDialog = new SingleProductDialog();
        productDialog.setProduct(product);
        productDialog.show(getFragmentManager(), "singleProductDialog");
/*
        Optional<ProductLocalized> productTranslation = StreamSupport.stream(product.getProductLocalized())
                .filter(pL -> pL.getLanguageCode().equals(language))
                .findFirst();

        List<AllergenLocalized> allergens = StreamSupport.stream(product.getProductAllergen())
                .map(ProductAllergen::getAllergen)
                .flatMap(allergen -> StreamSupport.stream(allergen.getAllergenLocalized())
                        .filter(aL -> aL.getLanguageCode().equals(language))
                )
                .collect(Collectors.toList());

        Dialog showProductSingle = new Dialog(view.getContext());
        showProductSingle.setContentView(R.layout.content_product);
        TextView title = (TextView) showProductSingle.findViewById(R.id.content_product_title);
        TextView priceTextField = (TextView) showProductSingle.findViewById(R.id.content_product_price);

        String price = String.format(Locale.GERMANY, "%.2f €", product.getPrice().doubleValue());
        priceTextField.setText(price);

        ListView allergenListView = (ListView) showProductSingle.findViewById(R.id.content_product_allergen_listView);
        if(allergens.size() == 0) {
            TextView allergensTitle = (TextView)showProductSingle.findViewById(R.id.content_product_allergen_title);
            allergensTitle.setVisibility(View.GONE);
        }
        ListAdapter allergenAdapter = new BaseAdapter() {
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
        };
        allergenListView.setAdapter(allergenAdapter);
        showProductSingle.show();*/
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    class ProductsAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return products.size();
        }

        @Override
        public Object getItem(int position) {
            return products.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(convertView == null)
                convertView = inflater.inflate(R.layout.single_product, parent, false);
            Product product = products.get(position);
            TextView prodName = (TextView) convertView.findViewById(R.id.single_product_name);
            TextView prodPrice = (TextView) convertView.findViewById(R.id.single_product_price);
            StreamSupport.stream(product.getProductLocalized())
                    .filter(pL -> pL.getLanguageCode().equals(language))
                    .findFirst()
                    .ifPresent(t -> prodName.setText(t.getLabel()));
            prodPrice.setText(String.format(Locale.GERMANY, "%.2f €", product.getPrice().doubleValue()));
            return convertView;
        }
    }
}
