package com.ettud.is74task4.Models.database;

import android.annotation.TargetApi;
import android.os.Build;

import com.ettud.is74task4.App;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.objectbox.Box;
import io.objectbox.BoxStore;

public class DatabaseContext {
    private BoxStore mBoxStore;

    public Box<Term> terms;
    public Box<Source> sources;
    public Box<Definition> definitions;
    public Box<TermSource> termSources;
    public Box<TermSynonym> termSynonyms;

    public DatabaseContext(){
        mBoxStore = App.getApp().getBoxStore();
        terms = mBoxStore.boxFor(Term.class);
        sources = mBoxStore.boxFor(Source.class);
        definitions = mBoxStore.boxFor(Definition.class);
        termSources = mBoxStore.boxFor(TermSource.class);
        termSynonyms = mBoxStore.boxFor(TermSynonym.class);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public Optional<TermSource> addDefinition(Source source, String[] synonyms, String[] definitionsText) {
        if (definitionsText == null)
            return Optional.empty();
        if (definitionsText.length == 0)
            return Optional.empty();
        List<TermSynonym> termSynonyms = new ArrayList<TermSynonym>();
        for (String synonym : synonyms) {
            if (synonym == null)
                continue;
            if (synonym.isEmpty())
                continue;
            TermSynonym termSynonym = this.termSynonyms.query().equal(TermSynonym_.name, synonym).build().findFirst();
            if (termSynonym == null) {
                termSynonym = new TermSynonym();
                termSynonym.name = synonym;
                this.termSynonyms.put(termSynonym);
            }
            termSynonyms.add(termSynonym);
        }

        List<Term> terms = new ArrayList<Term>();
        for (TermSynonym termSynonym : termSynonyms) {
            Term term = termSynonym.term.getTarget();
            if (term != null) {
                if (!terms.contains(term)) {
                    terms.add(term);
                }
            }
        }

        Term term;
        if (terms.isEmpty()) {
            term = new Term();
            this.terms.put(term);
        }
        else {
            term = terms.get(0);
        }

        for (TermSynonym termSynonym : termSynonyms) {
            if (termSynonym.term.getTarget() != term) {
                termSynonym.term.setTarget(term);
                this.termSynonyms.put(termSynonym);
            }
        }

        List<TermSource> termSources = term.termSources;
        TermSource termSource = null;
        if (termSources != null) {
            for (TermSource iTermSource : termSources) {
                if(iTermSource.source.getTarget() == source){
                    termSource = iTermSource;
                    break;
                }
            }
        }
        if(termSource == null){
            termSource = new TermSource();
            this.termSources.put(termSource);
            termSource.source.setTarget(source);
            termSource.term.setTarget(term);
            this.termSources.put(termSource);
        }

        //получаем список существующих definition:
        //Definition definition = null;
        int newDefinitionsCount = definitionsText.length;
        if(termSource.definitions.size() >= newDefinitionsCount){
            for (int i = 0; (i < definitionsText.length)||(i < termSource.definitions.size()); i++) {
                if(definitionsText[i].isEmpty()){
                    newDefinitionsCount--;
                    continue;
                }
                Definition definition = termSource.definitions.get(i);
                definition.text = definitionsText[i];
                this.definitions.put(definition);
            }
            if(termSource.definitions.size() > newDefinitionsCount) {
                termSource.definitions.removeAll(termSource.definitions.subList(newDefinitionsCount, termSource.definitions.size() - newDefinitionsCount));
            }
        }
        for (int i = termSource.definitions.size(); i < definitionsText.length; i++) {
            if (!definitionsText[i].isEmpty()) {
                Definition definition = new Definition();
                definition.text = definitionsText[i];
                definition.termSource.setTarget(termSource);
                this.definitions.put(definition);
            }
        }
        return Optional.of(termSource);
    }
}
