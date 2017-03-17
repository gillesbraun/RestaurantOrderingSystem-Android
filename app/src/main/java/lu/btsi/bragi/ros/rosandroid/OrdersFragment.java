package lu.btsi.bragi.ros.rosandroid;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lu.btsi.bragi.ros.models.message.Message;
import lu.btsi.bragi.ros.models.message.MessageException;
import lu.btsi.bragi.ros.models.message.MessageGet;
import lu.btsi.bragi.ros.models.pojos.Order;
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

/**
 * Created by gillesbraun on 17/03/2017.
 */

public class OrdersFragment extends Fragment {
    @BindView(R.id.orders_recyclerView)
    RecyclerView recyclerView;
    private List<Order> orders;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    private void loadData() {
        ConnectionManager.getInstance().sendWithAction(new MessageGet<>(Order.class), m -> {
            try {
                orders = new Message<Order>(m).getPayload();
                recyclerView.setAdapter(new OrdersRecyclerView(orders, getContext()));
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
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.actionbar_orders);
        loadData();
    }
}
