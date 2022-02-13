package lu.btsi.bragi.ros.rosandroid

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.zxing.integration.android.IntentIntegrator
import com.nostra13.universalimageloader.core.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager
import lu.btsi.bragi.ros.rosandroid.databinding.ActivityMainBinding
import lu.btsi.bragi.ros.rosandroid.managers.OrderManager
import lu.btsi.bragi.ros.rosandroid.waiter.ReviewOrderDialog
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    @Deprecated("") private var languageObserver: LanguageObserver? = null

    @Inject lateinit var connectionManager: ConnectionManager
    @Inject lateinit var orderManager: OrderManager

    private lateinit var navController: NavController
    private val binding by viewBinding(ActivityMainBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val configuration = AppBarConfiguration.Builder(R.id.homeFragment)
            .setOpenableLayout(binding.drawerLayout)
            .build()
        NavigationUI.setupWithNavController(binding.navView, navController)
        NavigationUI.setupWithNavController(binding.toolbar, navController, configuration)
        NavigationUI.setupActionBarWithNavController(this, navController, configuration)
        connectionManager.initPreferences(this)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                orderManager.products.onEach {
                    binding.fabOrderSubmit.isVisible = it.isNotEmpty()
                }.launchIn(this)
            }
        }
        binding.fabOrderSubmit.visibility = View.GONE
        binding.fabOrderSubmit.setOnClickListener(fabSendOrderPressed)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_clear_cache) {
            ImageLoader.getInstance().clearDiskCache()
            ImageLoader.getInstance().clearMemoryCache()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private val fabSendOrderPressed = View.OnClickListener { view: View? ->
        val reviewOrderDialog = ReviewOrderDialog()
        reviewOrderDialog.isCancelable = false
        navController.navigate(R.id.reviewOrderDialog)
    }

    // QR Code
    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent)
        if (scanResult != null && scanResult.contents != null) {
            ConnectionManager.getInstance().connect(scanResult.contents)
        }
    }

    @Deprecated("")
    fun setLanguageObserver(languageObserver: LanguageObserver?) {
        this.languageObserver = languageObserver
    }

    fun languageChanged() {
        if (languageObserver != null) {
            languageObserver!!.languageChanged()
        }
    }
}