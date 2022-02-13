package lu.btsi.bragi.ros.rosandroid.waiter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.hilt.android.AndroidEntryPoint;
import lu.btsi.bragi.ros.models.message.Message;
import lu.btsi.bragi.ros.models.message.MessageException;
import lu.btsi.bragi.ros.models.message.MessageGet;
import lu.btsi.bragi.ros.models.pojos.Table;
import lu.btsi.bragi.ros.rosandroid.MainActivity;
import lu.btsi.bragi.ros.rosandroid.OrderManager;
import lu.btsi.bragi.ros.rosandroid.R;
import lu.btsi.bragi.ros.rosandroid.WaiterManager;
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager;

/**
 * Created by Gilles Braun on 14.03.2017.
 */
@AndroidEntryPoint
public class WaiterHomeFragment extends Fragment {
    @Inject OrderManager orderManager;
    @Inject WaiterManager waiterManager;

    @BindView(R.id.waiterhome_textView_waiterName)
    TextView waiterName;

    @BindString(R.string.waiterhome_textView_waiterName)
    String waiterString;

    private List<Table> tables;

    private void loadData() {
        ConnectionManager.getInstance().sendWithAction(new MessageGet<>(Table.class), m -> {
            try {
                tables = new Message<Table>(m).getPayload();
            } catch (MessageException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.actionbar_waiter_home);
        loadData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_waiter_home, container, false);
        ButterKnife.bind(this, view);
        waiterName.setText(String.format(Locale.GERMAN, waiterString, waiterManager.getWaiter().getValue().getName()));
        if(tables == null) {
            loadData();
        }
        return view;
    }

    @OnClick(R.id.waiterhome_button_createNewOrder)
    public void createNewOrder() {
        new MaterialDialog.Builder(getContext())
                .title("Select a Table")
                .items(tables)
                .itemsCallbackSingleChoice(-1, (dialog, itemView, position, text) -> {
                    if(position < 0 || position >= tables.size())
                        return false;
                    orderManager.createNew();
                    orderManager.setTable(tables.get(position));
                    NavHostFragment.findNavController(this).navigate(
                            WaiterHomeFragmentDirections.actionWaiterHomeFragmentToWaiterProductCategoriesFragment()
                    );
                    return true;
                })
                .negativeText("Cancel")
                .positiveText("Choose")
                .build()
                .show();
    }
}
