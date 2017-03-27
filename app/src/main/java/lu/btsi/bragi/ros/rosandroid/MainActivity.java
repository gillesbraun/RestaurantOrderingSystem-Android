package lu.btsi.bragi.ros.rosandroid;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ncapdevi.fragnav.FragNavController;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

import lu.btsi.bragi.ros.rosandroid.connection.ConnectionCallback;
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager;
import lu.btsi.bragi.ros.rosandroid.waiter.OrderEditDialog;
import lu.btsi.bragi.ros.rosandroid.waiter.ReviewOrderDialog;
import lu.btsi.bragi.ros.rosandroid.waiter.WaiterChooseFragment;
import lu.btsi.bragi.ros.rosandroid.waiter.WaiterHomeFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ConnectionCallback {

    List<Fragment> fragments = new ArrayList<>(6);
    private FragNavController fragNavController;
    private FloatingActionButton fab_oderSubmit;
    private MenuItem menu_edit_order, menu_change_location, menu_change_waiter;
    private LanguageObserver languageObserver;

    public MainActivity() {
        ConnectionManager.init(this);

        fragments.add(new HomeFragment());
        fragments.add(new WaiterChooseFragment());
        fragments.add(new OrderLocationChooseFragment());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ConnectionManager.getInstance().initPreferences(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(options)
                .build();
        ImageLoader.getInstance().init(config);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        fab_oderSubmit = (FloatingActionButton)findViewById(R.id.fab_order_submit);
        fab_oderSubmit.setVisibility(View.GONE);
        fab_oderSubmit.setOnClickListener(fabSendOrderPressed);

        fragNavController = new FragNavController(savedInstanceState, getSupportFragmentManager(), R.id.content_main, fragments, FragNavController.TAB1);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        if(fragNavController.isRootFragment()) {
            new MaterialDialog.Builder(this)
                    .title(R.string.dialog_exit_title)
                    .positiveText(R.string.dialog_exit_yes)
                    .onPositive((dialog1, which) ->
                        super.onBackPressed()
                    )
                    .negativeText(R.string.dialog_exit_no)
                    .build()
                    .show();
        } else {
            fragNavController.popFragment();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        menu_edit_order = menu.findItem(R.id.menu_edit_order);
        menu_edit_order.setOnMenuItemClickListener(menuEditOrderPressed);

        menu_change_location = menu.findItem(R.id.menu_change_location);
        menu_change_location.setOnMenuItemClickListener(menuChangeLocationPressed);

        menu_change_waiter = menu.findItem(R.id.menu_change_waiter);
        menu_change_waiter.setOnMenuItemClickListener(menuChangeWaiterPressed);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.menu_clear_cache) {
            ImageLoader.getInstance().clearDiskCache();
            ImageLoader.getInstance().clearMemoryCache();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            fragNavController.switchTab(FragNavController.TAB1);
        } else if (id == R.id.nav_waiter) {
            if(!OrderManager.getInstance().hasOpenOrder()) {
                fragNavController.clearStack();
            }
            fragNavController.switchTab(FragNavController.TAB2);
            if(Config.getInstance().getWaiter() != null) {
                fragNavController.pushFragment(new WaiterHomeFragment());
            }
        } else if (id == R.id.nav_orders_view) {
            fragNavController.switchTab(FragNavController.TAB3);
            if(Config.getInstance().getLocation() != null) {
                fragNavController.pushFragment(new OrdersFragment());
            }
        } else if (id == R.id.nav_change_language) {
            LanguageChooseDialog.showLanguageSelectDialog(this);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void connectionOpened() {
        Log.d("ROS", "Connection established with: " + ConnectionManager.getInstance().getRemoteIPAdress());
    }

    @Override
    public void connectionClosed() {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(getApplicationContext(), "Conn closed", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void connectionError(Exception e) {
        new Handler(Looper.getMainLooper()).post(() -> {
            new MaterialDialog.Builder(this)
                    .title("Error")
                    .content(e.getCause().getClass().getSimpleName() + ": " + e.getMessage())
                    .build()
                    .show();
        });
    }

    public void pushFragment(@Nullable Fragment fragment) {
        fragNavController.pushFragment(fragment);
    }

    public void popFragment() throws UnsupportedOperationException {
        fragNavController.popFragment();
    }

    public void popFragments(int popDepth) throws UnsupportedOperationException {
        fragNavController.popFragments(popDepth);
    }

    public void switchTab(int index) throws IndexOutOfBoundsException {
        fragNavController.switchTab(index);
    }

    public void updateFabVisibility() {
        OrderManager manager = OrderManager.getInstance();
        if(fab_oderSubmit == null || menu_edit_order == null)
            return;
        if(manager.hasOpenOrder() && manager.orderHasProducts()) {
            fab_oderSubmit.setVisibility(View.VISIBLE);
        } else {
            fab_oderSubmit.setVisibility(View.GONE);
        }
        menu_edit_order.setVisible(manager.hasOpenOrder() && manager.orderHasProducts());
    }

    private View.OnClickListener fabSendOrderPressed = view -> {
        ReviewOrderDialog reviewOrderDialog = new ReviewOrderDialog();
        reviewOrderDialog.setCancelable(false);
        fragNavController.showDialogFragment(reviewOrderDialog);
    };

    public void showDialogFragment(@Nullable DialogFragment dialogFragment) {
        fragNavController.showDialogFragment(dialogFragment);
    }

    public void clearStack() {
        fragNavController.clearStack();
    }

    public void replaceFragment(@NonNull Fragment fragment) {
        fragNavController.replaceFragment(fragment);
    }

    public void clearDialogFragment() {
        fragNavController.clearDialogFragment();
    }


    private MenuItem.OnMenuItemClickListener menuEditOrderPressed = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            fragNavController.showDialogFragment(new OrderEditDialog());
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener menuChangeLocationPressed = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Config.getInstance().setLocation(null);
            fragNavController.replaceFragment(new OrderLocationChooseFragment());
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener menuChangeWaiterPressed = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Config.getInstance().setWaiter(null);
            fragNavController.clearStack();
            fragNavController.pushFragment(new WaiterChooseFragment());
            return true;
        }
    };

    // QR Code
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null && scanResult.getContents() != null) {
            ConnectionManager.getInstance().setHost(scanResult.getContents());
        }
    }

    public void setMenuEditLocationVisibility(boolean b) {
        menu_change_location.setVisible(b);
    }

    public void setMenuChangeWaiterVisibility(boolean b) {
        menu_change_waiter.setVisible(b);
    }

    public void setLanguageObserver(LanguageObserver languageObserver) {
        this.languageObserver = languageObserver;
    }

    public void languageChanged() {
        if(languageObserver != null) {
            languageObserver.languageChanged();
        }
    }
}
