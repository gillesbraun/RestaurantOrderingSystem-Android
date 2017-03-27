package lu.btsi.bragi.ros.rosandroid.waiter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import java8.util.stream.StreamSupport;
import lu.btsi.bragi.ros.models.pojos.Product;
import lu.btsi.bragi.ros.rosandroid.Config;
import lu.btsi.bragi.ros.rosandroid.LanguageObserver;
import lu.btsi.bragi.ros.rosandroid.MainActivity;
import lu.btsi.bragi.ros.rosandroid.R;

/**
 * Created by gillesbraun on 13/03/2017.
 */
public class WaiterProductsFragment extends Fragment implements AdapterView.OnItemClickListener, LanguageObserver {
    private List<Product> products;
    private ProductsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.actionbar_product_select);

        ListView listView_products = (ListView) view.findViewById(R.id.listView_products);

        adapter = new ProductsAdapter();

        listView_products.setOnItemClickListener(this);
        listView_products.setAdapter(adapter);
    }

    // Product is pressed
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Product product = products.get(position);

        SingleProductDialog productDialog = new SingleProductDialog();
        productDialog.setProduct(product);
        ((MainActivity)getActivity()).showDialogFragment(productDialog);
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
                    .filter(pL -> pL.getLanguageCode().equals(Config.getInstance().getLanguage().getCode()))
                    .findFirst()
                    .ifPresent(t -> prodName.setText(t.getLabel()));
            prodPrice.setText(String.format(Locale.GERMANY, "%.2f €", product.getPrice().doubleValue()));
            return convertView;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).setLanguageObserver(this);
    }

    @Override
    public void languageChanged() {
        adapter.notifyDataSetChanged();
    }
}
