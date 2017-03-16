package lu.btsi.bragi.ros.rosandroid;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lu.btsi.bragi.ros.models.message.Message;
import lu.btsi.bragi.ros.models.message.MessageException;
import lu.btsi.bragi.ros.models.message.MessageGet;
import lu.btsi.bragi.ros.models.pojos.Language;
import lu.btsi.bragi.ros.rosandroid.connection.ConnectionManager;

/**
 * Created by gillesbraun on 16/03/2017.
 */

public class LanguageChooseFragment extends Fragment {
    @BindView(R.id.language_choose_listView)
    ListView languageListView;

    private List<Language> languages;


    public LanguageChooseFragment() {
        ConnectionManager.getInstance().sendWithAction(new MessageGet<>(Language.class), m -> {
            try {
                languages = new Message<Language>(m).getPayload();
            } catch (MessageException e) {
                e.printStackTrace();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_language_choose, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        languageListView.setAdapter(new ArrayAdapter<>(getContext(), R.layout.single_language, R.id.single_language, languages));
        languageListView.setOnItemClickListener(languageClicked);
    }

    private AdapterView.OnItemClickListener languageClicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Config.getInstance().setLanguage(languages.get(position));
            Toast.makeText(getContext(), "Changed language to "+languages.get(position).getName(), Toast.LENGTH_LONG).show();
        }
    };
}
