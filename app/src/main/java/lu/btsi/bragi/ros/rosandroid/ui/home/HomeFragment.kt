package lu.btsi.bragi.ros.rosandroid.ui.home

import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.google.zxing.integration.android.IntentIntegrator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import lu.btsi.bragi.ros.rosandroid.R
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionState
import lu.btsi.bragi.ros.rosandroid.databinding.FragmentHomeBinding
import java.text.DateFormat
import java.util.*

/**
 * Created by gillesbraun on 13/03/2017.
 */
@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val binding by viewBinding(FragmentHomeBinding::bind)
    private val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {

                viewModel.connectionState.onEach {
                    updateStatus(it)
                }.launchIn(this)

            }
        }

        binding.buttonConnectBarcode.setOnClickListener {
            buttonConnectBarcodePressed()
        }
        binding.buttonConnectIp.setOnClickListener {
            buttonConnectIPPressed()
        }
    }

    private fun updateStatus(state: ConnectionState) {
        when(state) {
            is ConnectionState.Connected -> {
                binding.connectionStatus.text = getString(R.string.home_isconnected, state.host)
            }
            ConnectionState.Disconnected -> {
                binding.connectionStatus.text = getString(R.string.home_isconnected, "not connected")
            }
            is ConnectionState.Failed -> {
                val dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG)
                val error = state.error.javaClass.simpleName + if (state.error.message != null) ": " + state.error.message else ""
                binding.connectionStatus.text = getString(R.string.home_notconnected, dateFormat.format(Date())) + "\n$error"
            }
        }
    }

    private fun buttonConnectIPPressed() {
        MaterialDialog.Builder(requireContext())
            .title(R.string.home_dialog_title)
            .inputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_CLASS_TEXT)
            .input(
                getString(R.string.home_dialog_hint),
                viewModel.getHost(),
                false
            ) { _, input -> viewModel.connect(input.toString()) }
            .show()
    }

    private fun buttonConnectBarcodePressed() {
        val integrator = IntentIntegrator(activity)
        integrator.initiateScan()
    }

}