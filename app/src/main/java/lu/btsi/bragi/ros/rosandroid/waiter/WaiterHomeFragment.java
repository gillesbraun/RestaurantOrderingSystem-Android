package lu.btsi.bragi.ros.rosandroid.waiter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;
import java.util.Locale;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lu.btsi.bragi.ros.models.message.Message;
import lu.btsi.bragi.ros.models.message.MessageException;
import lu.btsi.bragi.ros.models.message.MessageGet;
import lu.btsi.bragi.ros.models.pojos.Table;
import lu.btsi.bragi.ros.rosandroid.Config;
import lu.btsi.bragi.ros.rosandroid.MainActivity;
import lu.btsi.bragi.ros.rosandroid.OrderManager;
import lu.btsi.bragi.ros.rosandroid.R;
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager;

/**
 * Created by Gilles Braun on 14.03.2017.
 */

public class WaiterHomeFragment extends Fragment {
    @BindView(R.id.waiterhome_textView_waiterName)
    TextView waiterName;

    @BindString(R.string.waiterhome_textView_waiterName)
    String waiterString;

    private List<Table> tables;

    public WaiterHomeFragment() {
        ConnectionManager.getInstance().sendWithAction(new MessageGet<>(Table.class), m -> {
            try {
                tables = new Message<Table>(m).getPayload();
            } catch (MessageException e) {
                e.printStackTrace();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_waiter_home, container, false);
        ButterKnife.bind(this, view);
        waiterName.setText(String.format(Locale.GERMAN, waiterString, Config.getInstance().getWaiter().getName()));
        return view;
    }

    @OnClick(R.id.waiterhome_button_createNewOrder)
    public void createNewOrder() {
        new MaterialDialog.Builder(getContext())
                .title("Select a Table")
                .items(tables)
                .itemsCallbackSingleChoice(-1, (dialog, itemView, position, text) -> {
                    Log.e("ROS", "position: "+position + ". got: "+tables.get(position));
                    OrderManager.getInstance().createNew().setTable(tables.get(position));
                    ((MainActivity)getActivity()).pushFragment(new WaiterProductCategoriesFragment());
                    return true;
                })
                .negativeText("Cancel")
                .positiveText("Choose")
                .build()
                .show();
    }
}
