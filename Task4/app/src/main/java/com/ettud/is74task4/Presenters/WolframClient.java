package com.ettud.is74task4.Presenters;

import android.annotation.TargetApi;
import android.os.Build;

import com.ettud.is74task4.Models.database.DatabaseContext;
import com.ettud.is74task4.Models.database.Definition;
import com.ettud.is74task4.Models.database.Source_;
import com.ettud.is74task4.Models.database.Term;
import com.ettud.is74task4.Models.database.TermSource;
import com.ettud.is74task4.Models.database.TermSource_;
import com.ettud.is74task4.Models.database.TermSynonym;
import com.ettud.is74task4.Models.database.TermSynonym_;
import com.squareup.moshi.Json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class WolframClient {
    private IWolframApi mWolframApi;
    private DatabaseContext mDatabaseContext;

    public WolframClient(){
        mDatabaseContext = new DatabaseContext();
        mWolframApi = new Retrofit.Builder()
                .baseUrl("http://api.wolframalpha.com/v2/")
                .addConverterFactory(MoshiConverterFactory.create())
                .build().create(IWolframApi.class);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public Optional<TermSynonym> getDefinition(String term) {
        Response<WolframQueryResult> response = null;
        try {
            response = mWolframApi.get(term, "Q3QYRQ-A9Q6PXK4U3", "json").execute();
        } catch (IOException e) {
            return Optional.empty();
        } catch(com.squareup.moshi.JsonDataException e){
            return Optional.empty();
        }
        if (response.isSuccessful()) {
            WolframQueryResult result = response.body();
            if(result != null){
                if(result.getQueryresult() != null){
                    return analyze(term, result);
                }
            }
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.N)
    private Optional<TermSynonym> analyze(String input, WolframQueryResult result){
        Optional<TermSynonym> ret;
        ret = analyzeIfWord(input, result.getQueryresult());
        if(ret.isPresent())
            return ret;
        return analyzeIfMovie(input, result.getQueryresult());
    }

    @TargetApi(Build.VERSION_CODES.N)
    private Optional<TermSynonym> analyzeIfWord(String termName, Queryresult result){
        if(result.getPods() != null){
            if(!result.getPods().isEmpty()){
                Pod definitionPod = null;
                for (Pod pod: result.getPods()) {
                    if(pod.getTitle() != null){
                        if(pod.getTitle().equals("Definitions")){
                            definitionPod = pod;
                            break;
                        }
                    }
                }
                if(definitionPod == null){
                    return Optional.empty();
                }
                if(definitionPod.getSubpods() == null){
                    return Optional.empty();
                }
                if(definitionPod.getSubpods().isEmpty()){
                    return Optional.empty();
                }
                if(definitionPod.getSubpods().get(0).getPlaintext() == null){
                    return Optional.empty();
                }
                ArrayList<String> definitions = new ArrayList<>();
                String[] splitted = definitionPod.getSubpods().get(0).getPlaintext().split("(\\s\\|\\s)|\\n");
                for(int i = 0; i < splitted.length; i++){
                    switch(i%3){
                        case 0: //number
                            break;
                        case 1: //part of speech
                            break;
                        case 2: //definition
                            definitions.add(splitted[i]);
                            break;
                    }
                }
                String correctTermName = termName;
                if(result.getAssumptions() != null){
                    if(result.getAssumptions().getWord() != null){
                        correctTermName = result.getAssumptions().getWord();
                    }
                }

                //получаем source:
                com.ettud.is74task4.Models.database.Source source = mDatabaseContext.sources.query().equal(Source_.name, "WolframAlpha").build().findFirst();
                if(source == null){
                    source = new com.ettud.is74task4.Models.database.Source();
                    source.name = "WolframAlpha";
                    source.priority = 0;
                }

                String[] synonyms;
                if(termName.equals(correctTermName)){
                    synonyms = new String[]{termName, correctTermName};
                }
                else{
                    synonyms = new String[]{termName};
                }
                Optional<TermSource> termSource = mDatabaseContext.addDefinition(source, synonyms, (String[])definitions.toArray());
                if(!termSource.isPresent())
                    return Optional.empty();
                TermSynonym termSynonym = null;
                for (TermSynonym iTermSynonym: termSource.get().term.getTarget().termSynonyms) {
                    if(iTermSynonym.name.equals(termName)){
                        termSynonym = iTermSynonym;
                        break;
                    }
                }
                return Optional.ofNullable(termSynonym);
            }
        }
        return Optional.empty();
    }

    @TargetApi(Build.VERSION_CODES.N)
    private Optional<TermSynonym> analyzeIfMovie(String movieName, Queryresult result){
        if(result.getPods() != null){
            if(!result.getPods().isEmpty()){
                Pod movieBasicInformationPod = null;
                for (Pod pod: result.getPods()) {
                    if(pod.getTitle() != null){
                        if(pod.getTitle().equals("Basic movie information")){
                            movieBasicInformationPod = pod;
                            break;
                        }
                    }
                }
                if(movieBasicInformationPod == null){
                    return Optional.empty();
                }
                if(movieBasicInformationPod.getSubpods() == null){
                    return Optional.empty();
                }
                if(movieBasicInformationPod.getSubpods().isEmpty()){
                    return Optional.empty();
                }
                if(movieBasicInformationPod.getSubpods().get(0).getPlaintext() == null){
                    return Optional.empty();
                }

                String movieDefinition = null;
                Pattern yearPattern = Pattern.compile("\\nrelease date\\s*|\\s*\\d\\d/\\d\\d/(\\d\\d\\d\\d)");
                Matcher yearMatcher = yearPattern.matcher(movieBasicInformationPod.getSubpods().get(0).getPlaintext());
                boolean nounAdded = false;
                if (yearMatcher.find()) {
                    movieDefinition = movieName + " is a " +yearMatcher.group(1);
                    nounAdded = true;
                }
                Pattern genresPattern = Pattern.compile("\\ngenres\\s*(\\|\\s*([^\\|\\n]+))+\\n");
                Matcher genresMatcher = genresPattern.matcher(movieBasicInformationPod.getSubpods().get(0).getPlaintext());
                if (genresMatcher.find()) {
                    if(movieDefinition == null){
                        movieDefinition = movieName + " is a ";
                    }
                    for(int i = 1; i < genresMatcher.groupCount(); i++){
                        if(i%2 == 1)
                            continue;
                        movieName += genresMatcher.group(i) + " ";
                    }
                }
                Pattern directorPattern = Pattern.compile("\\ndirector\\s*\\|\\s*([^\\|\\n]+)\\n");
                Matcher directorMatcher = directorPattern.matcher(movieBasicInformationPod.getSubpods().get(0).getPlaintext());
                if (directorMatcher.find()) {
                    if(movieDefinition == null){
                        movieDefinition = movieName + " is a movie by " + directorMatcher.group(1);
                    }
                    else{
                        if(nounAdded) {
                            movieDefinition += " by " + directorMatcher.group(1);
                        }
                        else{
                            movieDefinition += " movie by " + directorMatcher.group(1);
                        }
                    }
                    nounAdded = true;
                }
                if(!nounAdded){
                    movieDefinition += "movie";
                }
                movieDefinition += ".";

                //получаем source:
                com.ettud.is74task4.Models.database.Source source = mDatabaseContext.sources.query().equal(Source_.name, "WolframAlpha").build().findFirst();
                if(source == null){
                    source = new com.ettud.is74task4.Models.database.Source();
                    source.name = "WolframAlpha";
                    source.priority = 0;
                }


                Optional<TermSource> termSource = mDatabaseContext.addDefinition(source, new String[]{movieName}, new String[]{movieDefinition});
                if(!termSource.isPresent())
                    return Optional.empty();
                TermSynonym termSynonym = null;
                for (TermSynonym iTermSynonym: termSource.get().term.getTarget().termSynonyms) {
                    if(iTermSynonym.name.equals(movieName)){
                        termSynonym = iTermSynonym;
                        break;
                    }
                }
                return Optional.ofNullable(termSynonym);

                /*//получаем termSynonym
                TermSynonym termSynonym = mDatabaseContext.termSynonyms.query().equal(TermSynonym_.name, movieName).build().findFirst();

                //получаем term
                Term term = null;
                boolean termWasCreated = false;
                if(termSynonym == null){
                    term = new Term();
                    mDatabaseContext.terms.put(term);
                    termWasCreated = true;

                    termSynonym = new TermSynonym();
                    termSynonym.name = movieName;
                    termSynonym.term.setTarget(term);
                    mDatabaseContext.termSynonyms.put(termSynonym);
                }
                else{
                    if(termSynonym.term != null) {
                        term = termSynonym.term.getTarget();
                    }
                    if(term == null){
                        term = new Term();
                        mDatabaseContext.terms.put(term);
                        termWasCreated = true;

                        termSynonym.term.setTarget(term);
                        mDatabaseContext.termSynonyms.put(termSynonym);
                    }
                }

                //получаем termSource:
                TermSource termSource = null;;
                boolean termSourceWasCreated = false;
                if(!termWasCreated){
                    termSource = mDatabaseContext.termSources.query().equal(TermSource_.sourceId, source.id).build().findFirst();
                }
                if(termWasCreated || termSource == null){
                    termSource = new TermSource();
                    termSource.term.setTarget(term);
                    termSource.source.setTarget(source);
                    termSourceWasCreated = true;
                }

                //получаем список существующих definition:
                Definition definition = null;
                if(!termSourceWasCreated){
                    if(termSource.definitions.size() >= 1){
                        definition = termSource.definitions.get(0);
                        termSource.definitions.removeAll(termSource.definitions.subList(1, termSource.definitions.size()-1));
                    }
                }
                if(definition == null){
                    definition = new Definition();
                    definition.termSource.setTarget(termSource);
                }
                definition.text = movieDefinition;
                mDatabaseContext.definitions.put(definition);
                return Optional.of(termSynonym);*/
            }
        }
        return Optional.empty();
    }

    private interface IWolframApi{
        @GET("query")
        Call<WolframQueryResult> get(@Query("input") String input, @Query("appid") String appid, @Query("output") String output);
    }

    public static class Assumptions {

        @Json(name = "type")
        private String type;
        @Json(name = "word")
        private String word;
        @Json(name = "template")
        private String template;
        @Json(name = "count")
        private Integer count;
        @Json(name = "values")
        private List<Value> values = null;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public List<Value> getValues() {
            return values;
        }

        public void setValues(List<Value> values) {
            this.values = values;
        }

    }

    public static class Definitions {

        @Json(name = "word")
        private String word;
        @Json(name = "desc")
        private String desc;

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

    }

    public static class Img {

        @Json(name = "src")
        private String src;
        @Json(name = "alt")
        private String alt;
        @Json(name = "title")
        private String title;
        @Json(name = "width")
        private Integer width;
        @Json(name = "height")
        private Integer height;

        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }

        public String getAlt() {
            return alt;
        }

        public void setAlt(String alt) {
            this.alt = alt;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

    }

    public static class Pod {

        @Json(name = "title")
        private String title;
        @Json(name = "scanner")
        private String scanner;
        @Json(name = "id")
        private String id;
        @Json(name = "position")
        private Integer position;
        @Json(name = "error")
        private Boolean error;
        @Json(name = "numsubpods")
        private Integer numsubpods;
        @Json(name = "subpods")
        private List<Subpod> subpods = null;
        @Json(name = "primary")
        private Boolean primary;
        @Json(name = "states")
        private List<State> states = null;
        @Json(name = "definitions")
        private Definitions definitions;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getScanner() {
            return scanner;
        }

        public void setScanner(String scanner) {
            this.scanner = scanner;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getPosition() {
            return position;
        }

        public void setPosition(Integer position) {
            this.position = position;
        }

        public Boolean getError() {
            return error;
        }

        public void setError(Boolean error) {
            this.error = error;
        }

        public Integer getNumsubpods() {
            return numsubpods;
        }

        public void setNumsubpods(Integer numsubpods) {
            this.numsubpods = numsubpods;
        }

        public List<Subpod> getSubpods() {
            return subpods;
        }

        public void setSubpods(List<Subpod> subpods) {
            this.subpods = subpods;
        }

        public Boolean getPrimary() {
            return primary;
        }

        public void setPrimary(Boolean primary) {
            this.primary = primary;
        }

        public List<State> getStates() {
            return states;
        }

        public void setStates(List<State> states) {
            this.states = states;
        }

        public Definitions getDefinitions() {
            return definitions;
        }

        public void setDefinitions(Definitions definitions) {
            this.definitions = definitions;
        }

    }

    public static class Queryresult {

        @Json(name = "success")
        private Boolean success;
        @Json(name = "error")
        private Boolean error;
        @Json(name = "numpods")
        private Integer numpods;
        @Json(name = "datatypes")
        private String datatypes;
        @Json(name = "timedout")
        private String timedout;
        @Json(name = "timedoutpods")
        private String timedoutpods;
        @Json(name = "timing")
        private Double timing;
        @Json(name = "parsetiming")
        private Double parsetiming;
        @Json(name = "parsetimedout")
        private Boolean parsetimedout;
        @Json(name = "recalculate")
        private String recalculate;
        @Json(name = "id")
        private String id;
        @Json(name = "host")
        private String host;
        @Json(name = "server")
        private String server;
        @Json(name = "related")
        private String related;
        @Json(name = "version")
        private String version;
        @Json(name = "pods")
        private List<Pod> pods = null;
        @Json(name = "assumptions")
        private Assumptions assumptions;
        @Json(name = "sources")
        private List<Source> sources = null;

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public Boolean getError() {
            return error;
        }

        public void setError(Boolean error) {
            this.error = error;
        }

        public Integer getNumpods() {
            return numpods;
        }

        public void setNumpods(Integer numpods) {
            this.numpods = numpods;
        }

        public String getDatatypes() {
            return datatypes;
        }

        public void setDatatypes(String datatypes) {
            this.datatypes = datatypes;
        }

        public String getTimedout() {
            return timedout;
        }

        public void setTimedout(String timedout) {
            this.timedout = timedout;
        }

        public String getTimedoutpods() {
            return timedoutpods;
        }

        public void setTimedoutpods(String timedoutpods) {
            this.timedoutpods = timedoutpods;
        }

        public Double getTiming() {
            return timing;
        }

        public void setTiming(Double timing) {
            this.timing = timing;
        }

        public Double getParsetiming() {
            return parsetiming;
        }

        public void setParsetiming(Double parsetiming) {
            this.parsetiming = parsetiming;
        }

        public Boolean getParsetimedout() {
            return parsetimedout;
        }

        public void setParsetimedout(Boolean parsetimedout) {
            this.parsetimedout = parsetimedout;
        }

        public String getRecalculate() {
            return recalculate;
        }

        public void setRecalculate(String recalculate) {
            this.recalculate = recalculate;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public String getRelated() {
            return related;
        }

        public void setRelated(String related) {
            this.related = related;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public List<Pod> getPods() {
            return pods;
        }

        public void setPods(List<Pod> pods) {
            this.pods = pods;
        }

        public Assumptions getAssumptions() {
            return assumptions;
        }

        public void setAssumptions(Assumptions assumptions) {
            this.assumptions = assumptions;
        }

        public List<Source> getSources() {
            return sources;
        }

        public void setSources(List<Source> sources) {
            this.sources = sources;
        }

    }

    public static class Source {

        @Json(name = "url")
        private String url;
        @Json(name = "text")
        private String text;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

    }

    public static class State {

        @Json(name = "count")
        private Integer count;
        @Json(name = "value")
        private String value;
        @Json(name = "delimiters")
        private String delimiters;
        @Json(name = "states")
        private List<State_> states = null;
        @Json(name = "name")
        private String name;
        @Json(name = "input")
        private String input;

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getDelimiters() {
            return delimiters;
        }

        public void setDelimiters(String delimiters) {
            this.delimiters = delimiters;
        }

        public List<State_> getStates() {
            return states;
        }

        public void setStates(List<State_> states) {
            this.states = states;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

    }

    public static  class State_ {

        @Json(name = "name")
        private String name;
        @Json(name = "input")
        private String input;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

    }

    public static class Subpod {

        @Json(name = "title")
        private String title;
        @Json(name = "img")
        private Img img;
        @Json(name = "plaintext")
        private String plaintext;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Img getImg() {
            return img;
        }

        public void setImg(Img img) {
            this.img = img;
        }

        public String getPlaintext() {
            return plaintext;
        }

        public void setPlaintext(String plaintext) {
            this.plaintext = plaintext;
        }

    }

    public static class Value {

        @Json(name = "name")
        private String name;
        @Json(name = "desc")
        private String desc;
        @Json(name = "input")
        private String input;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

    }

    public static class WolframQueryResult {

        @Json(name = "queryresult")
        private Queryresult queryresult;

        public Queryresult getQueryresult() {
            return queryresult;
        }

        public void setQueryresult(Queryresult queryresult) {
            this.queryresult = queryresult;
        }

    }
}

