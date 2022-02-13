package lu.btsi.bragi.ros.rosandroid.ui.waiter.choose

import android.os.Bundle
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import lu.btsi.bragi.ros.models.pojos.Waiter
import lu.btsi.bragi.ros.rosandroid.Config
import lu.btsi.bragi.ros.rosandroid.R
import lu.btsi.bragi.ros.rosandroid.WaiterManager
import lu.btsi.bragi.ros.rosandroid.databinding.FragmentWaiterChooseBinding
import javax.inject.Inject

/**
 * Created by gillesbraun on 13/03/2017.
 */
@AndroidEntryPoint
class WaiterChooseFragment : Fragment(R.layout.fragment_waiter_choose) {

    @Inject
    lateinit var waiterManager: WaiterManager

    private val binding by viewBinding(FragmentWaiterChooseBinding::bind)
    private lateinit var adapter: ArrayAdapter<Waiter>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ArrayAdapter<Waiter>(
            view.context,
            R.layout.single_waiter,
            R.id.single_waiter_label,
            arrayListOf()
        )

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                waiterManager.allWaiters.collect { waiters ->
                    adapter.clear()
                    adapter.addAll(waiters)
                    adapter.notifyDataSetChanged()
                }
            }
        }

        binding.listView.adapter = adapter
        binding.listView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            Config.getInstance().waiter = adapter.getItem(position)
            NavHostFragment.findNavController(this).navigate(
                WaiterChooseFragmentDirections.actionWaiterChooseFragmentToWaiterHomeFragment()
            )
        }
    }
}