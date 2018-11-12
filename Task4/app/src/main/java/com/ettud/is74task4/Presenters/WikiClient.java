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
import com.ettud.is74task4.Models.database.Source;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class WikiClient {
    private IWikiApi mWikiApi;
    private DatabaseContext mDatabaseContext;

    public WikiClient(){
        mDatabaseContext = new DatabaseContext();
        mWikiApi = new Retrofit.Builder()
                .baseUrl("https://en.wikipedia.org/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build().create(IWikiApi.class);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public Optional<TermSynonym> getDefinition(String termName) {
        String result;
        Response<String> response = null;
        try {
            response = mWikiApi.get("json", 1, "query", "extracts", true, true, true, termName).execute();
        } catch (IOException e) {
            return Optional.empty();
        }
        if (response.isSuccessful()) {
            result = response.body();
            Pattern pattern = Pattern.compile("\"title\":\\s*\"(.+)\",\\s*\"extract\":\\s*\"(.+)\"\\s*\\}");
            Matcher matcher = pattern.matcher(result);
            if (matcher.find()) {
                //получаем source:
                Source source = mDatabaseContext.sources.query().equal(Source_.name, "Wikipedia").build().findFirst();
                if(source == null){
                    source = new Source();
                    source.name = "Wikipedia";
                    source.priority = 1;
                    mDatabaseContext.sources.put(source);
                }

                String anotherTerm = matcher.group(1);
                String[] definitions;
                if(anotherTerm.equals(termName)){
                    definitions = new String[]{ termName };
                }
                else{
                    definitions = new String[]{ termName, anotherTerm };
                }

                Optional<TermSource> termSource = mDatabaseContext.addDefinition(source, definitions, new String[]{matcher.group(2)});
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

    private interface IWikiApi{
        @GET("w/api.php")
        Call<String> get(@Query("format") String format, @Query("exsentences") int exsentences,
                         @Query("action") String action, @Query("prop") String prop,
                         @Query("exintro") boolean exintro, @Query("explaintext") boolean explaintext,
                         @Query("redirects") boolean redirects, @Query("titles") String titles);
    }
}
