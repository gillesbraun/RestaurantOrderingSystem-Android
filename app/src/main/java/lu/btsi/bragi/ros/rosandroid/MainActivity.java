package lu.btsi.bragi.ros.rosandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nostra13.universalimageloader.core.ImageLoader;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager;
import lu.btsi.bragi.ros.rosandroid.databinding.ActivityMainBinding;
import lu.btsi.bragi.ros.rosandroid.managers.OrderManager;
import lu.btsi.bragi.ros.rosandroid.waiter.ReviewOrderDialog;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private LanguageObserver languageObserver;
    private ActivityMainBinding binding;

    @Inject ConnectionManager connectionManager;
    @Inject
    OrderManager orderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        AppBarConfiguration configuration = new AppBarConfiguration.Builder(R.id.homeFragment)
                .setOpenableLayout(drawerLayout)
                .build();
        NavigationUI.setupWithNavController(binding.navView, navController);
        NavigationUI.setupWithNavController(binding.toolbar, navController, configuration);
        NavigationUI.setupActionBarWithNavController(this, navController, configuration);

        binding.fabOrderSubmit.setVisibility(View.GONE);
        binding.fabOrderSubmit.setOnClickListener(fabSendOrderPressed);

        connectionManager.initPreferences(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.menu_clear_cache) {
            ImageLoader.getInstance().clearDiskCache();
            ImageLoader.getInstance().clearMemoryCache();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Deprecated
    public void updateFabVisibility() {
        if(orderManager.hasOpenOrder() && orderManager.orderHasProducts()) {
            binding.fabOrderSubmit.setVisibility(View.VISIBLE);
        } else {
            binding.fabOrderSubmit.setVisibility(View.GONE);
        }
        //menu_edit_order.setVisible(manager.hasOpenOrder() && manager.orderHasProducts());
    }

    private View.OnClickListener fabSendOrderPressed = view -> {
        ReviewOrderDialog reviewOrderDialog = new ReviewOrderDialog();
        reviewOrderDialog.setCancelable(false);
        navController.navigate(R.id.reviewOrderDialog);
    };

    // QR Code
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null && scanResult.getContents() != null) {
            ConnectionManager.getInstance().connect(scanResult.getContents());
        }
    }

    @Deprecated
    public void setLanguageObserver(LanguageObserver languageObserver) {
        this.languageObserver = languageObserver;
    }

    public void languageChanged() {
        if(languageObserver != null) {
            languageObserver.languageChanged();
        }
    }
}
