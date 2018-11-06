package com.ettud.is74task4.Activities;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.ettud.is74task4.App;
import com.ettud.is74task4.Models.Term;
import com.ettud.is74task4.Models.Term_;
import com.ettud.is74task4.Presenters.WikiClient;
import com.ettud.is74task4.R;
import com.ettud.is74task4.RecyclerViewTermAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;

public class WikiActivity extends AppCompatActivity {
    private WikiClient mWikiClient;
    private EditText mEditText;
    private ImageButton mImageButton;
    private RecyclerView mRecyclerView;
    private Box<Term> mTerms;
    private BoxStore mBoxStore;
    private RecyclerViewTermAdapter mRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wiki);
        mEditText = findViewById(R.id.editText);
        mImageButton = findViewById(R.id.imageButton);
        mRecyclerView = findViewById(R.id.recyclerView);
        mWikiClient = new WikiClient();
        mBoxStore = App.getApp().getBoxStore();
        mTerms = mBoxStore.boxFor(Term.class);
        mImageButton.setOnClickListener(v -> search(mEditText.getText().toString()));
        mRecyclerViewAdapter = new RecyclerViewTermAdapter(this, new ArrayList<Term>());
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    private void search(String input) {
        new Thread(() -> {
            List<Term> terms = mTerms.query().contains(Term_.term, input).build().find();
            if (terms.isEmpty()) {
                try {
                    mTerms.put(mWikiClient.getDefinition(input));
                    terms = mTerms.query().contains(Term_.term, input).build().find();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            List<Term> finalTerms = terms;
            runOnUiThread(() -> {
                mRecyclerViewAdapter.data.clear();
                for (Term term : finalTerms) {
                    mRecyclerViewAdapter.data.add(term);
                }
                mRecyclerViewAdapter.notifyDataSetChanged();
            });
        }).start();
    }
}