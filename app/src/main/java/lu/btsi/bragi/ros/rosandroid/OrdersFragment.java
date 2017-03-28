package lu.btsi.bragi.ros.rosandroid;

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

import org.jooq.types.UInteger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import lu.btsi.bragi.ros.models.message.Message;
import lu.btsi.bragi.ros.models.message.MessageException;
import lu.btsi.bragi.ros.models.message.MessageGetQuery;
import lu.btsi.bragi.ros.models.message.Query;
import lu.btsi.bragi.ros.models.message.QueryParam;
import lu.btsi.bragi.ros.models.message.QueryType;
import lu.btsi.bragi.ros.models.pojos.Location;
import lu.btsi.bragi.ros.models.pojos.Order;
import lu.btsi.bragi.ros.rosandroid.connection.BroadcastCallback;
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

/**
 * Created by gillesbraun on 17/03/2017.
 */

public class OrdersFragment extends Fragment implements BroadcastCallback {
    @BindView(R.id.orders_recyclerView)
    RecyclerView recyclerView;
    private List<Order> orders;

    @BindString(R.string.actionbar_orders)
    String actionbarStr;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        ButterKnife.bind(this, view);
        recyclerView.setAdapter(new OrdersRecyclerView(new ArrayList<>(), getContext()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ConnectionManager.getInstance().addBroadcastCallback(this);
        return view;
    }

    private void loadData() {
        Message messageQuery = new MessageGetQuery<>(
                Order.class,
                new Query(QueryType.Open_Orders_For_Location,
                        new QueryParam("location", UInteger.class, Config.getInstance().getLocation().getId())));
        ConnectionManager.getInstance().sendWithAction(messageQuery, m -> {
            try {
                orders = new Message<Order>(m).getPayload();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    recyclerView.setAdapter(new OrdersRecyclerView(orders, getContext()));
                }, 200);
                updateOrientation();
            } catch (MessageException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateOrientation();
    }

    private void updateOrientation() {
        RecyclerView.LayoutManager layoutManager;
        if(getActivity() == null)
            return;
        if (getActivity().getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
            layoutManager = new GridLayoutManager(getContext(), 2);
        } else {
            layoutManager = new GridLayoutManager(getContext(), 1);
        }
        if(recyclerView != null) {
            recyclerView.setLayoutManager(layoutManager);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Location location = Config.getInstance().getLocation();
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(String.format(
                Config.getInstance().getLocale(view.getContext()),
                actionbarStr,
                location != null ? location.getDescription() : ""));
        loadData();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity)getActivity()).setMenuEditLocationVisibility(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).setMenuEditLocationVisibility(true);
        OrderManager.getInstance().clear();
        ((MainActivity)getActivity()).updateFabVisibility();
    }

    @Override
    public void handleBroadCast() {
        loadData();
    }
}
