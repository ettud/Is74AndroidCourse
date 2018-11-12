package com.ettud.is74task4.Models;

import java.util.ArrayList;
import java.util.List;

public class TermViewModel {
    public String term;
    public List<String> synonims;
    public List<TermSourceModel> sources;

    public TermViewModel(){
        synonims = new ArrayList<>();
        sources = new ArrayList<>();
    }

    public void addSynonim(String synonimName){
        if(synonims == null){
            synonims = new ArrayList<>();
        }
        synonims.add(synonimName);
    }

    public TermSourceModel addSource(String sourceName){
        if(sources == null){
            sources = new ArrayList<>();
        }
        TermSourceModel termSourceModel= new TermSourceModel();
        termSourceModel.sourceName = sourceName;
        sources.add(termSourceModel);
        return termSourceModel;
    }

    public class TermSourceModel {
        public List<DefinitionModel> definitions;
        public String sourceName;

        public TermSourceModel(){
            definitions = new ArrayList<>();
        }

        public DefinitionModel addDefinition(String definition){
            if(definitions == null){
                definitions = new ArrayList<>();
            }
            DefinitionModel definitionModel= new DefinitionModel(definition);
            definitions.add(definitionModel);
            return definitionModel;
        }

        public class DefinitionModel {
            public DefinitionModel(String text){
                definition = text;
            }

            public String definition;
        }
    }
}
