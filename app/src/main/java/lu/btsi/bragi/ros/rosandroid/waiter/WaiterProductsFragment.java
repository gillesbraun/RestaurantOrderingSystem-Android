package lu.btsi.bragi.ros.rosandroid.waiter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java8.util.stream.StreamSupport;
import lu.btsi.bragi.ros.models.message.Message;
import lu.btsi.bragi.ros.models.message.MessageException;
import lu.btsi.bragi.ros.models.message.MessageGet;
import lu.btsi.bragi.ros.models.pojos.Product;
import lu.btsi.bragi.ros.rosandroid.Config;
import lu.btsi.bragi.ros.rosandroid.LanguageObserver;
import lu.btsi.bragi.ros.rosandroid.MainActivity;
import lu.btsi.bragi.ros.rosandroid.R;
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager;
import lu.btsi.bragi.ros.rosandroid.databinding.FragmentProductsBinding;
import lu.btsi.bragi.ros.rosandroid.databinding.SingleProductBinding;

/**
 * Created by gillesbraun on 13/03/2017.
 */
public class WaiterProductsFragment extends Fragment implements LanguageObserver {
    public static final String EXTRA_PRODUCT_CATEGORY_ID = "EXTRA_PRODUCT_CATEGORY_ID";
    private ProductsAdapter adapter;
    private FragmentProductsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.actionbar_product_select);

        adapter = new ProductsAdapter(this::onProductClicked);

        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycler.setAdapter(adapter);
        ConnectionManager.getInstance().sendWithAction(new MessageGet<>(Product.class), message -> {
            try {
                List<Product> payload = new Message<Product>(message).getPayload();
                adapter.setProducts(payload);
            } catch (MessageException e) {
                e.printStackTrace();
            }
        });
    }

    public void onProductClicked(Product product) {
        SingleProductDialog productDialog = new SingleProductDialog();
        productDialog.setProduct(product);
        ((MainActivity)getActivity()).showDialogFragment(productDialog);
    }

    static class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {
        ProductsAdapter(OnProductsAdapterClicked listener) {
            this.listener = listener;
        }

        interface OnProductsAdapterClicked {
            void onProductClicked(Product product);
        }

        private final ArrayList<Product> products = new ArrayList<>();
        private final OnProductsAdapterClicked listener;

        public void setProducts(List<Product> items) {
            products.clear();
            products.addAll(items);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(
                    SingleProductBinding.inflate(LayoutInflater.from(parent.getContext()))
            );
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final Product product = products.get(position);
            StreamSupport.stream(product.getProductLocalized())
                    .filter(pL -> pL.getLanguageCode().equals(Config.getInstance().getLanguage().getCode()))
                    .findFirst()
                    .ifPresent(t -> holder.binding.singleProductName.setText(t.getLabel()));
            holder.binding.singleProductPrice.setText(String.format(Locale.GERMANY, "%.2f €", product.getPrice().doubleValue()));
            holder.binding.getRoot().setOnClickListener(v -> listener.onProductClicked(product));
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        private static class ViewHolder extends RecyclerView.ViewHolder {
            final SingleProductBinding binding;
            public ViewHolder(SingleProductBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).setLanguageObserver(this);
        ((MainActivity)getActivity()).setMenuChangeWaiterVisibility(true);
    }

    @Override
    public void languageChanged() {
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity)getActivity()).setMenuChangeWaiterVisibility(false);
    }
}
