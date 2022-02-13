package lu.btsi.bragi.ros.rosandroid

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import lu.btsi.bragi.ros.models.message.Message
import lu.btsi.bragi.ros.models.message.MessageException
import lu.btsi.bragi.ros.models.message.MessageGet
import lu.btsi.bragi.ros.models.pojos.Language
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager

/**
 * Created by gillesbraun on 16/03/2017.
 */
class LanguageChooseDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adapter = ArrayAdapter<Language>(requireContext(), android.R.layout.simple_list_item_1, android.R.id.text1)

        ConnectionManager.getInstance().sendWithAction(MessageGet(Language::class.java)) { m ->
            try {
                val languages = Message<Language>(m).payload
                adapter.addAll(languages)
            } catch (e: MessageException) {
                e.printStackTrace()
            }
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Choose Language")
            .setAdapter(adapter) { _, i ->
                Config.getInstance().language = adapter.getItem(i)
            }
            .create()
    }

}
