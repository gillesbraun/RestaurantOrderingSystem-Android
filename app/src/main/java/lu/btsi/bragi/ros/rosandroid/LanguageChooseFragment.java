package lu.btsi.bragi.ros.rosandroid;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import lu.btsi.bragi.ros.models.message.Message;
import lu.btsi.bragi.ros.models.message.MessageException;
import lu.btsi.bragi.ros.models.message.MessageGet;
import lu.btsi.bragi.ros.models.pojos.Language;
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager;

/**
 * Created by gillesbraun on 16/03/2017.
 */

public class LanguageChooseFragment extends DialogFragment {
    private List<Language> languages;

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getContext())
                .title(R.string.language_choose_title)
                .content(R.string.language_choose_subtitle)
                .items(languages)
                .itemsCallbackSingleChoice(-1, (dialog, itemView, which, text) -> {
                    Config.getInstance().setLanguage(languages.get(which));
                    ((MainActivity)getActivity()).languageChanged();
                    return true;
                })
                .negativeText("Cancel")
                .positiveText("Choose")
                .build();
    }

    public static void showLanguageSelectDialog(MainActivity mainActivity) {
        ConnectionManager.getInstance().sendWithAction(new MessageGet<>(Language.class), m -> {
            try {
                List<Language> languages = new Message<Language>(m).getPayload();
                LanguageChooseFragment languageChooseFragment = new LanguageChooseFragment();
                languageChooseFragment.setLanguages(languages);
                mainActivity.showDialogFragment(languageChooseFragment);
            } catch (MessageException e) {
                e.printStackTrace();
            }
        });
    }
}
