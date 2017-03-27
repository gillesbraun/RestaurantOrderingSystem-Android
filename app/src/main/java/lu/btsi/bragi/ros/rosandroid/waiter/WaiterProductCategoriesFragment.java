package lu.btsi.bragi.ros.rosandroid.waiter;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import lu.btsi.bragi.ros.models.message.Message;
import lu.btsi.bragi.ros.models.message.MessageException;
import lu.btsi.bragi.ros.models.message.MessageGet;
import lu.btsi.bragi.ros.models.pojos.Product;
import lu.btsi.bragi.ros.models.pojos.ProductCategory;
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

    private List<Product> products;
    private ArrayList<ProductCategory> categories;
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private ProductCategoryRecyclerAdapter adapter;

    @BindView(R.id.productcategories_textView_table)
    TextView tableLabel;

    @BindView(R.id.productcategories_textView_waiter)
    TextView waiterName;

    @BindString(R.string.productcategories_textView_table)
    String tableString;

    @BindString(R.string.productcategories_textView_waiterName)
    String waiterString;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_waiter_productcategories, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.main_recycler_view);
        // set empty adapter and layout to mitigate errors
        recyclerView.setAdapter(new ProductCategoryRecyclerAdapter(new ArrayList<>(), new ArrayList<>(), "", null));
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        ButterKnife.bind(this, view);
        tableLabel.setText(String.format(Locale.GERMAN, tableString, OrderManager.getInstance().getTable().getId()));
        return view;
    }

    public WaiterProductCategoriesFragment() {
        ConnectionManager.getInstance().sendWithAction(new MessageGet<>(Product.class), new MessageCallback() {
            @Override
            public void handleAnswer(String message) {
                try {
                    List<Product> payload = new Message<Product>(message).getPayload();
                    Set<ProductCategory> categories = new HashSet<>();
                    for (Product product : payload) {
                        categories.add(product.getProductCategory());
                    }
                    WaiterProductCategoriesFragment.this.categories = new ArrayList<>(categories);
                    WaiterProductCategoriesFragment.this.products = payload;
                    updateView();
                } catch (MessageException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateView() {
        ((MainActivity)getActivity()).updateFabVisibility();
        if(products != null && categories != null && getView() != null) {
            waiterName.setText(String.format(Locale.GERMAN, waiterString, Config.getInstance().getWaiter().getName()));

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    configurationChange();
                    adapter = new ProductCategoryRecyclerAdapter(categories, products, ConnectionManager.getInstance().getRemoteIPAdress(), (MainActivity) WaiterProductCategoriesFragment.this.getActivity());
                    recyclerView.setAdapter(adapter);
                }
            });
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.actionbar_productcategories_select);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        configurationChange();
    }

    private void configurationChange() {
        View view = getView();
        if(view == null || view.getContext() == null)
            return;
        if(recyclerView != null) {
            if(getActivity().getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
                layoutManager = new GridLayoutManager(getView().getContext(), 3);
            } else {
                layoutManager = new GridLayoutManager(getView().getContext(), 2);
            }
            recyclerView.setLayoutManager(layoutManager);
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
