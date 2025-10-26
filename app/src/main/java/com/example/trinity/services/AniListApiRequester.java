package com.example.trinity.services;

import androidx.annotation.WorkerThread;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AniListApiRequester{

    private static final String REQUST_URL = "https://graphql.anilist.co";
    private static final String REQUEST_QUERY_TITLES = "query ($search: String!) {Page {media(search: $search, type: MANGA) {title {english romaji}}}}";

    @WorkerThread
    public ArrayList<String[]> searchTitles(String title) {
        URL urlAPI = null;
        ArrayList<String[]> responseTitles = new ArrayList<>();
        try {
            urlAPI = new URL(REQUST_URL);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient.Builder().build();

        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("query",REQUEST_QUERY_TITLES);
            JSONObject variables = new JSONObject();
            variables.put("search",title);
            jsonObject.put("variables",variables);
        }catch (JSONException e){
            e.printStackTrace();
        }

//        System.out.println(jsonObject.toString());

//        System.out.println("{\n" +
//                "\"query\":"+REQUEST_QUERY_TITLES+",\n" +
//                "\"variables\":"+String.format(REQUEST_VARIABLES,title)+"\n" +
//                "}");


        assert urlAPI != null;
        Request request = new Request.Builder().
                url(urlAPI).
                addHeader("Content-Type","application/json").
                addHeader("Accept","application/json").
                post(RequestBody.create(jsonObject.toString(), MediaType.parse("application/json; charset=utf-8"))).
                build();

        try(Response response = client.newCall(request).execute();){

            Gson gson = new Gson();
            assert response.body() != null;
            String responseString = response.body().string();

            if (!responseString.isEmpty()) {
                JsonElement jsonElement = gson.fromJson(responseString, JsonElement.class);
                JsonArray titles = jsonElement.getAsJsonObject().get("data").
                        getAsJsonObject().get("Page").
                        getAsJsonObject().get("media").getAsJsonArray();
                for(JsonElement element:titles){
                    String stringToNormalizeEnglsih = element.getAsJsonObject().
                            get("title").
                            getAsJsonObject().
                            get("english").toString();

                    String stringToNormalizeRomaji = element.getAsJsonObject().
                            get("title").
                            getAsJsonObject().
                            get("romaji").toString();

                    responseTitles.add(new String[]{stringToNormalizeEnglsih.replace("\"","").equals("null")?null:stringToNormalizeEnglsih.replace("\"",""),stringToNormalizeRomaji.replace("\"","").equals("null")?null:stringToNormalizeRomaji.replace("\"","")});

                    if(element.getAsJsonObject().
                            get("title").
                            getAsJsonObject().
                            get("english").isJsonNull()
                            &&
                            element.getAsJsonObject().
                            get("title").
                            getAsJsonObject().
                            get("english").isJsonNull()){
                        responseTitles.remove(responseTitles.size()-1);
                    }
                    if(responseTitles.size() == 5)break;
                }

            }

        }catch (IOException e){
            e.printStackTrace();
        }
        return responseTitles;
    }


}
