package lu.btsi.bragi.ros.rosandroid;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import lu.btsi.bragi.ros.models.message.Message;
import lu.btsi.bragi.ros.models.message.MessageException;
import lu.btsi.bragi.ros.models.message.MessageGet;
import lu.btsi.bragi.ros.models.pojos.Location;
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager;

/**
 * Created by gillesbraun on 13/03/2017.
 */

public class StaffModeFragment extends Fragment {
    @BindView(R.id.order_location_listView)
    ListView listViewLocations;

    private List<Location> locations;

    public StaffModeFragment() {
        loadData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders_choose_location, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if(locations == null) {
            loadData();
        }
    }

    private void loadData() {
        ConnectionManager.getInstance().sendWithAction(new MessageGet<>(Location.class), m -> {
            try {
                locations = new Message<Location>(m).getPayload();
                listViewLocations.setAdapter(new LocationAdapter(getContext(), locations));
            } catch (MessageException e) {
                e.printStackTrace();
            }
        });
    }

    @OnItemClick(R.id.order_location_listView)
    void locationClick(int position) {
        if(locations != null) {
            Config.getInstance().setLocation(locations.get(position));
            Toast.makeText(getContext(), "Location changed to: %s", Toast.LENGTH_LONG).show();
            ((MainActivity)getActivity()).pushFragment(new OrdersFragment());
        }
    }

    private class LocationAdapter extends ArrayAdapter<Location> {

        LocationAdapter(@NonNull Context context, @NonNull List<Location> objects) {
            super(context, R.layout.single_location, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.single_location, parent, false);
            }
            TextView locationLabel = ButterKnife.findById(convertView, R.id.single_location_label);

            Location location = getItem(position);
            locationLabel.setText(location.getDescription());

            return convertView;
        }
    }
}
