package lu.btsi.bragi.ros.rosandroid.ui.waiter.choose_table

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.afollestad.materialdialogs.MaterialDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import lu.btsi.bragi.ros.rosandroid.OrderManager
import lu.btsi.bragi.ros.rosandroid.R
import lu.btsi.bragi.ros.rosandroid.TableManager
import lu.btsi.bragi.ros.rosandroid.WaiterManager
import lu.btsi.bragi.ros.rosandroid.databinding.FragmentWaiterHomeBinding
import javax.inject.Inject

/**
 * Created by Gilles Braun on 14.03.2017.
 */
@AndroidEntryPoint
class WaiterChooseTableFragment : Fragment(R.layout.fragment_waiter_home) {

    @Inject lateinit var tableManager: TableManager
    @Inject lateinit var waiterManager: WaiterManager
    @Inject lateinit var orderManager: OrderManager

    private val binding by viewBinding(FragmentWaiterHomeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                waiterManager.waiter.onEach { waiter ->
                    binding.title.text = getString(R.string.waiterhome_textView_waiterName, waiter?.name ?: "not set")
                }.launchIn(this)
            }
        }
        binding.buttonChooseTable.setOnClickListener { createNewOrder() }
    }

    private fun createNewOrder() {
        val tables = tableManager.allTables.value
        MaterialDialog.Builder(requireContext())
            .title("Select a Table")
            .items(tables)
            .itemsCallbackSingleChoice(-1) { _, _, position, _ ->
                orderManager.createNew()
                orderManager.table = tables[position]
                NavHostFragment.findNavController(this).navigate(
                    WaiterChooseTableFragmentDirections.actionWaiterHomeFragmentToWaiterProductCategoriesFragment()
                )
                true
            }
            .negativeText("Cancel")
            .positiveText("Choose")
            .build()
            .show()
    }
}