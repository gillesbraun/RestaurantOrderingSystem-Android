package lu.btsi.bragi.ros.rosandroid;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.integration.android.IntentIntegrator;

import java.util.Locale;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager;
/**
 * Created by gillesbraun on 13/03/2017.
 */

public class HomeFragment extends Fragment {
    @BindView(R.id.home_textView_connection_status)
    TextView textViewConnectionStatus;

    @BindString(R.string.home_isconnected)
    String strIsConnected;

    @BindString(R.string.home_dialog_hint)
    String strHint;

    private Handler handler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        handler = new Handler();
        runner.run();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRunner();
    }

    private void updateStatus() {
        ConnectionManager manager = ConnectionManager.getInstance();
        if(manager.isConnected()) {
            String remoteIPAdress = manager.getRemoteIPAdress();
            textViewConnectionStatus.setText(String.format(Locale.GERMAN, strIsConnected, remoteIPAdress));
        } else {
            textViewConnectionStatus.setText(R.string.home_notconnected);
        }
    }

    @OnClick(R.id.home_button_connect_ip)
    public void buttonConnectIPPressed() {
        ConnectionManager manager = ConnectionManager.getInstance();
        new MaterialDialog.Builder(getContext())
                .title(R.string.home_dialog_title)
                .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT)
                .input(strHint, manager.getHost(), false, (dialog, input) -> {
                    manager.setHost(input.toString());
                })
                .show();
    }

    @OnClick(R.id.home_button_connect_barcode)
    public void buttonConnectBarcodePressed() {
        IntentIntegrator integrator = new IntentIntegrator(getActivity());
        integrator.initiateScan();
    }

    private void stopRunner() {
        handler.removeCallbacks(runner);
    }

    private Runnable runner = new Runnable() {
        @Override
        public void run() {
            try {
                updateStatus();
            } finally {
                handler.postDelayed(runner, 500);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        OrderManager.getInstance().clear();
        ((MainActivity)getActivity()).updateFabVisibility();
    }
}
