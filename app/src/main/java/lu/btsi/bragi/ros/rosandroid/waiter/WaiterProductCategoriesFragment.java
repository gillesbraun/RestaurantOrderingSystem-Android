package lu.btsi.bragi.ros.rosandroid.waiter;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import java8.util.Comparators;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;
import lu.btsi.bragi.ros.models.message.Message;
import lu.btsi.bragi.ros.models.message.MessageException;
import lu.btsi.bragi.ros.models.message.MessageGet;
import lu.btsi.bragi.ros.models.pojos.Product;
import lu.btsi.bragi.ros.models.pojos.ProductCategory;
import lu.btsi.bragi.ros.models.pojos.Table;
import lu.btsi.bragi.ros.rosandroid.Config;
import lu.btsi.bragi.ros.rosandroid.LanguageObserver;
import lu.btsi.bragi.ros.rosandroid.MainActivity;
import lu.btsi.bragi.ros.rosandroid.OrderManager;
import lu.btsi.bragi.ros.rosandroid.R;
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager;
import lu.btsi.bragi.ros.rosandroid.connection.MessageCallback;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

/**
 * Created by gillesbraun on 13/03/2017.
 */

public class WaiterProductCategoriesFragment extends Fragment implements LanguageObserver {

    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private ProductCategoryRecyclerAdapter adapter;

    @BindView(R.id.productcategories_textView_table)
    TextView tableLabel;

    @BindView(R.id.productcategories_textView_waiter)
    TextView waiterName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_waiter_productcategories, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.actionbar_productcategories_select);
        recyclerView = (RecyclerView) view.findViewById(R.id.main_recycler_view);

        adapter = new ProductCategoryRecyclerAdapter(this::onCategoryClicked);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        ButterKnife.bind(this, view);
        Table table = OrderManager.getInstance().getTable();
        if(table != null) {
            tableLabel.setText(getString(R.string.productcategories_textView_table, table.getId()));
        }
        if(getActivity().getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
            layoutManager = new GridLayoutManager(recyclerView.getContext(), 3);
        } else {
            layoutManager = new GridLayoutManager(recyclerView.getContext(), 2);
        }
        recyclerView.setLayoutManager(layoutManager);
        loadData();
    }

    private void onCategoryClicked(ProductCategory category) {
        WaiterProductsFragment waiterProductsFragment = new WaiterProductsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(WaiterProductsFragment.EXTRA_PRODUCT_CATEGORY_ID, category.getId().intValue());
        waiterProductsFragment.setArguments(bundle);
        ((MainActivity)requireActivity()).pushFragment(waiterProductsFragment);
    }

    private void loadData() {
        ConnectionManager.getInstance().sendWithAction(new MessageGet<>(Product.class), message -> {
            try {
                List<Product> payload = new Message<Product>(message).getPayload();
                adapter.addItems(StreamSupport.stream(payload).map(Product::getProductCategory).collect(Collectors.toList()));
            } catch (MessageException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).updateFabVisibility();
        ((MainActivity)getActivity()).setLanguageObserver(this);
        ((MainActivity)getActivity()).setMenuChangeWaiterVisibility(true);
        waiterName.setText(getString(R.string.productcategories_textView_waiterName, Config.getInstance().getWaiter().getName()));
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
