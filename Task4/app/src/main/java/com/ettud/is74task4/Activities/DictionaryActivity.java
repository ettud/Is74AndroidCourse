package com.ettud.is74task4.Activities;

import android.annotation.TargetApi;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import com.ettud.is74task4.Models.TermViewModel;
import com.ettud.is74task4.Models.database.DatabaseContext;
import com.ettud.is74task4.Models.database.Definition;
import com.ettud.is74task4.Models.database.TermSource;
import com.ettud.is74task4.Models.database.TermSynonym;
import com.ettud.is74task4.Models.database.TermSynonym_;
import com.ettud.is74task4.Presenters.WikiClient;
import com.ettud.is74task4.Presenters.WolframClient;
import com.ettud.is74task4.R;
import com.ettud.is74task4.RecyclerViewTermAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DictionaryActivity extends AppCompatActivity {
    private WikiClient mWikiClient;
    private AutoCompleteTextView mAutoCompleteTextView ;
    private ImageButton mImageButton;
    private RecyclerView mRecyclerView;
    private DatabaseContext mDatabaseContext;
    private RecyclerViewTermAdapter mRecyclerViewAdapter;
    private Optional<String> lastSearchTerm;

    private static final String savedInstanceState_lastSearchTermKey = "lastSearchTerm";

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        mAutoCompleteTextView = findViewById(R.id.autoCompleteTextView);

        mImageButton = findViewById(R.id.imageButton);
        mImageButton.setOnClickListener(v -> search(Optional.of(mAutoCompleteTextView.getText().toString())));

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerViewAdapter = new RecyclerViewTermAdapter(new ArrayList<>());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        mAutoCompleteTextView.setOnEditorActionListener((v, actionId, event) -> {
            switch (actionId) {
                case EditorInfo.IME_ACTION_DONE:
                    search(Optional.of(mAutoCompleteTextView.getText().toString()));
                    return true;
                default:
                    return false;
            }
        });

        if(savedInstanceState != null){
            lastSearchTerm = Optional.ofNullable(savedInstanceState.getString(savedInstanceState_lastSearchTermKey));
            search(lastSearchTerm);
        }
        if(lastSearchTerm == null){
            lastSearchTerm =  Optional.empty();
        }

        mWikiClient = new WikiClient();
        mDatabaseContext = new DatabaseContext();
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(savedInstanceState_lastSearchTermKey, lastSearchTerm.orElse(null));
        super.onSaveInstanceState(outState);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void search(Optional<String> input) {
        if (input.isPresent()) {
            lastSearchTerm = input;
            new Thread(() -> {
                List<TermSynonym> termSynonyms = null;
                boolean internetAccess = false;
                ConnectivityManager connectivityManager = ((ConnectivityManager)getSystemService(this.CONNECTIVITY_SERVICE));
                if(connectivityManager != null){
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    if(networkInfo != null){
                        internetAccess = networkInfo.isConnected();
                    }
                }
                if(internetAccess){
                    Optional<TermSynonym> wolframTermSynonim = new WolframClient().getDefinition(input.get());
                    //Optional<TermSynonym> wolframTermSynonim = Optional.empty();
                    Optional<TermSynonym> wikiTermSynonim = mWikiClient.getDefinition(input.get());
                    if(!wolframTermSynonim.isPresent() && !wikiTermSynonim.isPresent()){
                        runOnUiThread(() -> mRecyclerViewAdapter.setTermSynonims(null));
                        return;
                    }
                    TermSynonym termSynonym = null;
                    if(wolframTermSynonim.isPresent()){
                        termSynonym = wolframTermSynonim.get();
                    }
                    if(termSynonym == null) {
                        if (wikiTermSynonim.isPresent()) {
                            termSynonym = wikiTermSynonim.get();
                        }
                    }

                    if(termSynonym != null){
                        termSynonyms = new ArrayList<TermSynonym>();
                        termSynonyms.add(termSynonym);
                    }
                }
                if(termSynonyms == null){
                    List<TermSynonym> tempTermSynonyms = mDatabaseContext.termSynonyms.query().contains(TermSynonym_.name, input.get()).build().find();
                    if(tempTermSynonyms.isEmpty()) {
                        runOnUiThread(() -> mRecyclerViewAdapter.setTermSynonims(null));
                        return;
                    }
                    termSynonyms = new ArrayList<TermSynonym>();
                    for (TermSynonym tempTermSynonym : tempTermSynonyms) {
                        boolean termAlreadyAdded = false;
                        for (TermSynonym termSynonym : termSynonyms) {
                            if(termSynonym.term.getTargetId() == tempTermSynonym.term.getTargetId()){
                                termAlreadyAdded = true;
                                break;
                            }
                        }
                        if(!termAlreadyAdded){
                            termSynonyms.add(tempTermSynonym);
                        }
                    }
                }
                List<TermViewModel> termViewModels = new ArrayList<>();
                for (TermSynonym termSynonym : termSynonyms) {
                    TermViewModel theTermViewModel = null;
                    for (TermViewModel termViewModel: termViewModels) {
                        if(termViewModel.term.equals(termSynonym.name)){
                            theTermViewModel = termViewModel;
                            break;
                        }
                        else{
                            if(termViewModel.synonims.contains(termSynonym.name)){
                                theTermViewModel = termViewModel;
                                break;
                            }
                        }
                    }
                    if(theTermViewModel == null){
                        theTermViewModel = new TermViewModel();
                        theTermViewModel.term = termSynonym.name;
                        termViewModels.add(theTermViewModel);
                    }
                    else{
                        theTermViewModel.addSynonim(termSynonym.name);
                    }
                    for (TermSource termSource: termSynonym.term.getTarget().termSources) {
                        TermViewModel.TermSourceModel termSourceModel = theTermViewModel.addSource(termSource.source.getTarget().name);
                        for (Definition definition : termSource.definitions) {
                            termSourceModel.addDefinition(definition.text);
                        }
                    }
                }
                runOnUiThread(() -> mRecyclerViewAdapter.setTermSynonims(termViewModels));
            }).start();
        } else {
            new Thread(() -> runOnUiThread(() -> mRecyclerViewAdapter.clear())).start();
        }
    }
}