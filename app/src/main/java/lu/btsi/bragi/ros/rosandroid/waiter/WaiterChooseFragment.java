package lu.btsi.bragi.ros.rosandroid.waiter;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.List;

import lu.btsi.bragi.ros.models.message.Message;
import lu.btsi.bragi.ros.models.message.MessageException;
import lu.btsi.bragi.ros.models.message.MessageGet;
import lu.btsi.bragi.ros.models.pojos.Waiter;
import lu.btsi.bragi.ros.rosandroid.Config;
import lu.btsi.bragi.ros.rosandroid.MainActivity;
import lu.btsi.bragi.ros.rosandroid.R;
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager;
import lu.btsi.bragi.ros.rosandroid.connection.MessageCallback;

/**
 * Created by gillesbraun on 13/03/2017.
 */

public class WaiterChooseFragment extends Fragment {

    public WaiterChooseFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_waiter_choose, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.actionbar_waiter_choose);

        ConnectionManager.getInstance().sendWithAction(new MessageGet<>(Waiter.class), new MessageCallback() {
            @Override
            public void handleAnswer(String message) {
                try {
                    List<Waiter> waiters = new Message<Waiter>(message).getPayload();
                    ListView listViewWaiters = (ListView) view.findViewById(R.id.waiter_choose_listView);
                    ListAdapter adapter = new ArrayAdapter<>(view.getContext(), R.layout.single_waiter, R.id.single_waiter_label, waiters);
                    listViewWaiters.setAdapter(adapter);
                    listViewWaiters.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Config.getInstance().setWaiter(waiters.get(position));
                            ((MainActivity) getActivity()).pushFragment(new WaiterHomeFragment());
                        }
                    });
                } catch (MessageException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
