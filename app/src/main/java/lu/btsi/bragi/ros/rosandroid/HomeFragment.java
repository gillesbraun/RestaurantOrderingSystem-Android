package lu.btsi.bragi.ros.rosandroid;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.integration.android.IntentIntegrator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionCallback;
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager;

import static lu.btsi.bragi.ros.rosandroid.R.id.home_textView_connection_status;

/**
 * Created by gillesbraun on 13/03/2017.
 */

public class HomeFragment extends Fragment implements ConnectionCallback {
    @BindView(home_textView_connection_status)
    TextView textViewConnectionStatus;

    @BindString(R.string.home_isconnected)
    String strIsConnected;

    @BindString(R.string.home_notconnected)
    String strIsNotConnected;

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

    @Override
    public void connectionOpened() {
        new Handler(Looper.getMainLooper()).post(() -> {
            ConnectionManager manager = ConnectionManager.getInstance();
            String remoteIPAdress = manager.getRemoteIPAdress();
            textViewConnectionStatus.setText(String.format(Config.getInstance().getLocale(getContext()), strIsConnected, remoteIPAdress));
        });
    }

    @Override
    public void connectionClosed() {
        new Handler(Looper.getMainLooper()).post(() -> {
        });
    }

    @Override
    public void connectionError(Exception e) {
        new Handler(Looper.getMainLooper()).post(() -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
            String error = e.getClass().getSimpleName() + (e.getMessage() != null ? ": " + e.getMessage() : "");
            textViewConnectionStatus.setText(e + " " + String.format(Config.getInstance().getLocale(getContext()), strIsNotConnected, dateFormat.format(new Date())));
        });
    }
}
