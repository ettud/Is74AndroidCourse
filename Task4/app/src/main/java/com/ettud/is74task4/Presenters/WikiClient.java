package com.ettud.is74task4.Presenters;

import com.ettud.is74task4.Models.Term;
import com.ettud.is74task4.R;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class WikiClient {
    private IWikiApi mWikiApi;

    public WikiClient(){
        mWikiApi = new Retrofit.Builder()
                .baseUrl("https://en.wikipedia.org/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build().create(IWikiApi.class);
    }

    public Term getDefinition(String term) throws IOException {
        String result;
        Response<String> response = mWikiApi.get("json", 1, "query", "extracts", true, true, true, term).execute();
        if (response.isSuccessful()) {
            result = response.body();
            Term retTerm = new Term();
            Pattern pattern = Pattern.compile("\"title\":\\s*\"(.+)\",\\s*\"extract\":\\s*\"(.+)\"\\s*\\}");
            Matcher matcher = pattern.matcher(result);
            if (matcher.find()) {
                retTerm.term = matcher.group(1);
                retTerm.definition = matcher.group(2);
            }
            return retTerm;
        }
        return null;
    }

    private interface IWikiApi{
        @GET("w/api.php")
        Call<String> get(@Query("format") String format, @Query("exsentences") int exsentences,
                         @Query("action") String action, @Query("prop") String prop,
                         @Query("exintro") boolean exintro, @Query("explaintext") boolean explaintext,
                         @Query("redirects") boolean redirects, @Query("titles") String titles);
    }
    //https://en.wikipedia.org/w/api.php?format=json&exsentences=1&action=query&prop=extracts&exintro&explaintext&redirects=1&titles=Stack%20Overflow
}
