package com.example.trinity.extensions;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.example.trinity.Interfaces.Extensions;
import com.example.trinity.R;
import com.example.trinity.services.broadcasts.CancelCurrentWorkReceiver;
import com.example.trinity.storageAcess.LogoMangaStorageTemp;
import com.example.trinity.storageAcess.PageCacheManager;
import com.example.trinity.valueObject.ChapterManga;
import com.example.trinity.valueObject.Manga;
import com.example.trinity.valueObject.TagManga;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;


import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MangaDexExtension implements Extensions {


    private int updateOffSet = 0;
    private int UpdateTotalItens = 0;
    private int limit = 27;
    private String language;
    private String tags = "";
    public static final String HIGH_QUALITY = "data";
    public static final String LOW_QUALITY = "dataSaver";
    private String imageQuality;
    private Context context;


    public MangaDexExtension(String language, String imageQuality) {
        this.language = language;
        this.imageQuality = LOW_QUALITY;
    }

//    public MangaDexExtension(String imageQuality) {
//        this.imageQuality = imageQuality;
//
//    }

    public void setUpdateTotalItens(int updateTotalItens) {
        UpdateTotalItens = updateTotalItens;
    }

    public void search(String title, Handler h) {
        String url = "https://api.mangadex.org/manga?availableTranslatedLanguage[]=" + this.language + "&title=" + title + "&includes[]=cover_art&includes[]=author&limit=" + this.limit + "&offset=" + this.updateOffSet;
//        System.out.println(url);
        URL urlApi = null;
        //System.out.println("Url = "+url);
        if (this.UpdateTotalItens > 0 && this.updateOffSet > this.UpdateTotalItens) {
            Message msg = Message.obtain();
            msg.what = Extensions.RESPONSE_FINAL;
            h.sendMessage(msg);
            return;

        }

        try {
            urlApi = new URL(url);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

        OkHttpClient http = new OkHttpClient.Builder().build();


        Request req = new Request.Builder().url(urlApi).build();

        try (Response resp = http.newCall(req).execute();) {


            ArrayList<Manga> mangaModels = this.responseToValueObject(resp.body().string());
            if (mangaModels.isEmpty()) {
                Message msg = Message.obtain();
                msg.what = RESPONSE_ERROR;
                h.sendMessage(msg);
            }
            this.updateOffSet += 15;
            loadMangaLogo(h, mangaModels);
            //System.out.println(mangaListedModels);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    static private boolean isChapterValid(String chapter) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(chapter);
        return matcher.matches();
    }
    static private String validateNumChapter(String chapter) {
        if (chapter.isEmpty()) {
            chapter = "0";
            return "0";
        }
        if (!isChapterValid(chapter)) {
            StringBuilder newStringChapter = new StringBuilder();
            for (int i = 0; i < chapter.length(); i++) {
                if(chapter.charAt(i) == '.'){
                    newStringChapter.append(""+chapter.charAt(i));
                    continue;
                }
                String stringChar = ""+chapter.charAt(i);

                Pattern pattern = Pattern.compile("\\d+");
                Matcher matcher = pattern.matcher(stringChar);

                if (!matcher.matches()) {
                    continue;
                }
                newStringChapter.append(stringChar);
            }
            chapter = String.valueOf(newStringChapter);
        }
        return chapter;
    }

    public void updates(Handler h) {
        String url = "https://api.mangadex.org/manga?availableTranslatedLanguage[]=" + this.language + "&includes[]=cover_art&includes[]=author&limit=" + this.limit + "&offset=" + this.updateOffSet + tags;
        URL urlApi = null;
//        System.out.println("Url = " + url);
        if (this.UpdateTotalItens > 0 && this.updateOffSet > this.UpdateTotalItens) {
            Message msg = Message.obtain();
            msg.what = RESPONSE_FINAL;
            h.sendMessage(msg);
            return;

        }

        try {
            urlApi = new URL(url);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

        OkHttpClient http = new OkHttpClient.Builder().build();


        Request req = new Request.Builder().url(urlApi).build();

        try (Response resp = http.newCall(req).execute();) {


            ArrayList<Manga> mangaModels = this.responseToValueObject(resp.body().string());
            this.updateOffSet += 27;
            loadMangaLogo(h, mangaModels);
            //System.out.println(mangaListedModels);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public ArrayList<Manga> responseToValueObject(String response) {
        Gson gson = new Gson();
        JsonElement json = gson.fromJson(response, JsonElement.class);
//        System.out.println(response);
        JsonArray data = json.getAsJsonObject().get("data").getAsJsonArray();
//        System.out.println(json.getAsJsonObject().get("total").getAsString());
        int total = json.getAsJsonObject().get("total").getAsInt();
        if(total == 0){
            if(context != null){
                Toast.makeText(context,"Ocorreu um erro ao realizar a requisição", Toast.LENGTH_LONG).show();
            }
            return new ArrayList<>();
        }

        ArrayList<Manga> mangaModels = new ArrayList<>();
        int position = 0;
        for (JsonElement jsonItem : data) {
//            System.out.println(position);
            String mangaId = jsonItem.getAsJsonObject().get("id").getAsString();

            String mangaTitulo = "";
            String mangaCoverArtName = "";
            String mangaDescricao = "";
            double lastChapter = 0;
            try {
                mangaTitulo = jsonItem.getAsJsonObject().get("attributes").getAsJsonObject().get("title").getAsJsonObject().get("pt-br").getAsString();

            } catch (NullPointerException ex) {
                try {
                    mangaTitulo = jsonItem.getAsJsonObject().get("attributes").getAsJsonObject().get("title").getAsJsonObject().get("en").getAsString();

                } catch (NullPointerException exp) {
                    try {
                        mangaTitulo = jsonItem.getAsJsonObject().get("attributes").getAsJsonObject().get("title").getAsJsonObject().get("ja-ro").getAsString();

                    } catch (NullPointerException expt) {
                        try {
                            mangaTitulo = jsonItem.getAsJsonObject().get("attributes").getAsJsonObject().get("title").getAsJsonObject().get("ja").getAsString();
                        } catch (NullPointerException exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            }
            try {

                mangaDescricao = jsonItem.getAsJsonObject().get("attributes").getAsJsonObject().get("description").getAsJsonObject().get("pt-br").getAsString();
            } catch (NullPointerException ex) {
                try {

                    mangaDescricao = jsonItem.getAsJsonObject().get("attributes").getAsJsonObject().get("description").getAsJsonObject().get("en").getAsString();
                } catch (NullPointerException exp) {
                    exp.printStackTrace();
                }
            }

            ArrayList<String> autoresManga = new ArrayList<>();

            ArrayList<TagManga> tagsManga = new ArrayList<>();

            JsonArray relationshipsJson = jsonItem.getAsJsonObject().get("relationships").getAsJsonArray();
            JsonArray tagsJson = jsonItem.getAsJsonObject().get("attributes").getAsJsonObject().get("tags").getAsJsonArray();

            for (JsonElement rel : relationshipsJson) {
                //System.out.println("Tipo: "+rel.getAsJsonObject().get("type").getAsString());
                if (rel.getAsJsonObject().get("type").getAsString() != null ? rel.getAsJsonObject().get("type").getAsString().equals("author") : false) {
                    autoresManga.add(rel.getAsJsonObject().get("attributes").getAsJsonObject().get("name").getAsString());
                } else if (rel.getAsJsonObject().get("type").getAsString() != null ? rel.getAsJsonObject().get("type").getAsString().equals("cover_art") : false) {
                    mangaCoverArtName = rel.getAsJsonObject().get("attributes").getAsJsonObject().get("fileName").getAsString();

                }
            }

            for (JsonElement t : tagsJson) {
                tagsManga.add(new TagManga(t.getAsJsonObject().get("id").getAsString(), t.getAsJsonObject().get("attributes").getAsJsonObject().get("name").getAsJsonObject().get("en").getAsString()));

            }


            if (jsonItem.getAsJsonObject().get("attributes").getAsJsonObject().get("lastChapter").isJsonNull()) {
                lastChapter = 0;

            } else {

                lastChapter  = jsonItem.getAsJsonObject().get("attributes").getAsJsonObject().get("lastChapter").isJsonNull()? 0 : jsonItem.getAsJsonObject().get("attributes").getAsJsonObject().get("lastChapter").getAsString().isEmpty() ?0:Double.parseDouble(validateNumChapter(jsonItem.getAsJsonObject().get("attributes").getAsJsonObject().get("lastChapter").getAsString()));
            }

//            System.out.println("lastChapter: " + lastChapter);
            Manga m = new Manga(mangaId, mangaTitulo, null, autoresManga, mangaDescricao, tagsManga, language);
            m.setLastChapter(lastChapter);
            m.coverName = mangaCoverArtName;
            mangaModels.add(m);

            position++;
        }
        return mangaModels;
    }

    @Override
    public void loadMangaLogo(Handler h, ArrayList<Manga> mangaArrayList) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 4, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), Executors.defaultThreadFactory());
        LogoMangaStorageTemp storageTemp = new LogoMangaStorageTemp(context);
        for (Manga item : mangaArrayList) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient http = new OkHttpClient.Builder().build();

                    URL urlApiImange = null;
                    try {
                        urlApiImange = new URL("https://uploads.mangadex.org/covers/" + item.getId() + "/" + item.coverName + ".256.jpg");
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }

                    Request req = new Request.Builder().url(urlApiImange).build();

                    try (Response resp = http.newCall(req).execute();) {

                        if (resp.isSuccessful() && resp.body().contentType().toString().contains("image/")) {
                            InputStream imageStream = resp.body().byteStream();
                            Bitmap bitImage = BitmapFactory.decodeStream(imageStream);
//                            item.setImage(bitImage);
                            storageTemp.insertLogoManga(bitImage, item.getId());
                            Message msg = Message.obtain();
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("dados", item);
                            msg.setData(bundle);
                            msg.what = RESPONSE_ITEM;
                            h.sendMessage(msg);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }


    }

    public ArrayList<ChapterManga> viewChapters(String mangaId) {
        ArrayList<ChapterManga> chapterMangas = new ArrayList<>();
        int offSet = 0;
        int total = 0;
        int limit = 300;


        while (offSet <= total) {


//            System.out.println("Url: https://api.mangadex.org/manga/"+mangaId+"/feed?includes[]=scanlation_group&limit=300&offset="+offSet+"&translatedLanguage[]="+this.language);
            URL url = null;
            try {
                url = new URL("https://api.mangadex.org/manga/" + mangaId + "/feed?includes[]=scanlation_group&limit=300&offset=" + offSet + "&includeExternalUrl=0&translatedLanguage[]=" + this.language);
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
            OkHttpClient http = new OkHttpClient.Builder().build();

            Request req = new Request.Builder().url(url).build();

            try (Response response = http.newCall(req).execute();) {

                Gson gson = new Gson();

                String s = response.body().string();
                JsonElement json = gson.fromJson(s, JsonElement.class);
                total = json.getAsJsonObject().get("total").getAsInt();
                if(total == 0){
                    if(context != null){
                        Toast.makeText(context,"Ocorreu um erro ao realizar a requisição", Toast.LENGTH_LONG).show();
                    }
                    return new ArrayList<>();
                }

                JsonArray data = json.getAsJsonObject().get("data").getAsJsonArray();
                int pos = 0;
                for (JsonElement chapter : data) {

                    String idChap = chapter.getAsJsonObject().get("id").getAsString();
                    String chapterChap = "";
                    try {
                        chapterChap = !chapter.getAsJsonObject().get("attributes").getAsJsonObject().get("chapter").isJsonNull()?chapter.getAsJsonObject().get("attributes").getAsJsonObject().get("chapter").getAsString():"0";
                    } catch (UnsupportedOperationException ex) {
                        ex.printStackTrace();
                    }

                    String titleChap = "";
                    try {
                        chapter.getAsJsonObject().get("attributes").getAsJsonObject().get("title").getAsJsonNull();
                    } catch (IllegalStateException ex) {
                        titleChap = chapter.getAsJsonObject().get("attributes").getAsJsonObject().get("title").getAsString();
                    }
                    String ChapScan = "";
                    JsonArray relations = chapter.getAsJsonObject().get("relationships").getAsJsonArray();

                    for (JsonElement rel : relations) {
                        if (rel.getAsJsonObject().get("type").getAsString().equals("scanlation_group")) {
                            try {
                                ChapScan = rel.getAsJsonObject().get("attributes").getAsJsonObject().get("name").getAsString();
                            } catch (NullPointerException ex) {
                                ChapScan = "";
                            }
                        }
                    }
                    String date = chapter.getAsJsonObject().get("attributes").getAsJsonObject().get("readableAt").getAsString();
//                    System.out.println("TimeStamp: "+date.split("[+]")[0]+".00Z");
                    Instant instant = Instant.parse(date.split("[+]")[0] + ".00Z");

                    Date dateDate = new Date(instant.toEpochMilli());
                    Calendar mangaDate = Calendar.getInstance();
                    mangaDate.setTime(dateDate);

                    chapterMangas.add(new ChapterManga(idChap, titleChap, chapterChap, ChapScan, date.split("[+]")[0] + ".00Z", false));
                    pos++;
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
//            System.out.println(chapterMangas);
            offSet += 300;


        }
//        MangaShowContentActivity mangaShowContentActivity = (MangaShowContentActivity) context;
//        InfoMangaFragment infoMangaFragment = (InfoMangaFragment) fragment;
//        if (context != null && fragment != null) {
//            mangaShowContentActivity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
////                    infoMangaFragment.setDataSet(chapterMangas);infoMangaFragment.stopLoading();
//                }
//            });
//        }
        return chapterMangas;
    }

    public void getChapterPages(Handler h, String idChapter) {
        URL url = null;
        try {
            url = new URL("https://api.mangadex.org/at-home/server/" + idChapter);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

        OkHttpClient http = new OkHttpClient();

        Request req = new Request.Builder().url(url).build();

        try (Response response = http.newCall(req).execute()) {

            Gson gson = new Gson();
            JsonElement json = gson.fromJson(response.body().string(), JsonElement.class);
//            int total = json.getAsJsonObject().get("total").getAsInt();
//            if(total == 0){
//                if(context != null){
//                    Toast.makeText(context,"Ocorreu um erro ao carregar as páginas do capítuo", Toast.LENGTH_LONG).show();
//                }
//                return;
//            }

            JsonArray imgs;
            try {
                imgs = json.getAsJsonObject().get("chapter").getAsJsonObject().get(imageQuality).getAsJsonArray();
            } catch (NullPointerException ex) {
                ex.printStackTrace();
                return;
            }

            Message msg = Message.obtain();
            msg.what = Extensions.RESPONSE_ITEM;
            Bundle bundle = new Bundle();
            bundle.putInt("numPages", imgs.size());
            msg.setData(bundle);
            h.sendMessage(msg);
            loadChapterPages(h, imgs, json.getAsJsonObject().get("chapter").getAsJsonObject().get("hash").getAsString(), json.getAsJsonObject().get("baseUrl").getAsString());
        } catch (IOException ex) {
            ex.printStackTrace();
            Message msg = Message.obtain();
            msg.what = Extensions.RESPONSE_ERROR;
            h.sendMessage(msg);
        }

    }

    public void loadChapterPages(Handler h, JsonArray array, String hash, String urlBase) {
        int index = 1;
        for (JsonElement s : array) {
            URL urlImage = null;
            try {
                String sFinal = "";
                for (int i = 1; i < s.toString().length() - 1; i++) {
                    sFinal += s.toString().charAt(i);
                }
//                System.out.println(imageQuality);
                urlImage = new URL(urlBase + (imageQuality.equals(HIGH_QUALITY) ? "/data/" : "/data-saver/") + hash + "/" + sFinal);
//                System.out.println(urlBase + (imageQuality.equals(HIGH_QUALITY) ? "/data/" : "/data-saver/") + hash + "/" + sFinal);
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }

            OkHttpClient http = new OkHttpClient();

            Request req = new Request.Builder().url(urlImage).build();

            try (Response response = http.newCall(req).execute();) {
//                System.out.println("tipo do arquivo"+response.body().contentType().toString());
                if (response.body().contentType().toString().contains("image/")) {
                    InputStream ImageInput = response.body().byteStream();

                    if (index == 1) {
                        PageCacheManager.getInstance(context).clearCache();
                    }

                    BitmapFactory.Options op = new BitmapFactory.Options();
                    op.inJustDecodeBounds = false;
                    Bitmap bit = BitmapFactory.decodeStream(ImageInput, null, op);


                    String url = PageCacheManager.getInstance(context).insertBitmapInCache(bit, Integer.toString(index) + ".jpeg");
                    if (bit != null) {
                        bit.recycle();
                    }

                    Message msg = Message.obtain();
                    msg.what = Extensions.RESPONSE_PAGE;
                    Bundle bundle = new Bundle();
                    bundle.putString("img", url);
                    bundle.putInt("index", index);
                    msg.setData(bundle);
                    h.sendMessage(msg);
                }
            } catch (ConnectException ex) {
                Message msg = Message.obtain();
                msg.what = Extensions.RESPONSE_PAGE;
                Bundle bundle = new Bundle();
                Bitmap bit = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.time_out);
                bundle.putParcelable("img", bit);
                bundle.putInt("index", index);
                msg.setData(bundle);
                h.sendMessage(msg);
            } catch (IOException ex) {
                ex.printStackTrace();
            }


            index++;
        }
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Bundle getChapterPages(String idChapter) {
        URL url = null;
        Bundle bundle = new Bundle();

        try {
            url = new URL("https://api.mangadex.org/at-home/server/" + idChapter);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

        OkHttpClient http = new OkHttpClient();

        Request req = new Request.Builder().url(url).build();

        try (Response response = http.newCall(req).execute()) {

            Gson gson = new Gson();
            JsonElement json = gson.fromJson(response.body().string(), JsonElement.class);

            JsonArray imgs = json.getAsJsonObject().get("chapter").getAsJsonObject().get(imageQuality).getAsJsonArray();


            String[] imgsString = new String[imgs.size()];
            int index = 0;
            for (JsonElement element : imgs) {
                imgsString[index] = element.toString();
                index++;
            }

            bundle.putStringArray("imgs", imgsString);
            bundle.putString("hash", json.getAsJsonObject().get("chapter").getAsJsonObject().get("hash").getAsString());
            bundle.putString("baseUrl", json.getAsJsonObject().get("baseUrl").getAsString());

        } catch (IOException ex) {
            ex.printStackTrace();
            Message msg = Message.obtain();
            msg.what = Extensions.RESPONSE_ERROR;
            return null;
        }
        return bundle;
    }

    public void loadUniquePage(String chapterIdApi, int chapterPage, Handler h) {
        Bundle bundle = this.getChapterPages(chapterIdApi);
        Bitmap bitmap = null;
        if (bundle == null) return;
        String urlBase = bundle.getString("baseUrl");
        String[] array = bundle.getStringArray("imgs");
        String hash = bundle.getString("hash");

        assert array != null;
        String s = "";
        try {
            s = array[chapterPage];
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
            return;
        }

        URL urlImage = null;
        try {
            String sFinal = "";
            for (int i = 1; i < s.length() - 1; i++) {
                sFinal += s.charAt(i);
            }
            urlImage = new URL(urlBase + (imageQuality.equals(HIGH_QUALITY) ? "/data/" : "/data-saver/") + hash + "/" + sFinal);
//                System.out.println(urlBase+"/data-saver/"+hash+"/"+s.toString());
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

        OkHttpClient http = new OkHttpClient();

        Request req = new Request.Builder().url(urlImage).build();

        try (Response response = http.newCall(req).execute();) {
//                System.out.println("tipo do arquivo"+response.body().contentType().toString());
            if (response.body().contentType().toString().contains("image/")) {
                InputStream ImageInput = response.body().byteStream();
                BitmapFactory.Options op = new BitmapFactory.Options();
                op.inPreferredColorSpace = ColorSpace.get(ColorSpace.Named.PRO_PHOTO_RGB);
                op.inJustDecodeBounds = false;
                op.inScaled = false;

                //Modificado
                bitmap = BitmapFactory.decodeStream(ImageInput, null, op);

            }

        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        Message msg = Message.obtain();
        msg.what = Extensions.RESPONSE_PAGE;
        Bundle bundle2 = new Bundle();
        bundle2.putParcelable("img", bitmap);
        bundle2.putInt("index", chapterPage + 1);
        msg.setData(bundle2);
        h.sendMessage(msg);

    }

    public Bitmap[] loadChapterPages(String[] array, String hash, String urlBase) {
        int index = 1;
        Bitmap[] bitmaps = new Bitmap[array.length + 2];
        for (String s : array) {
            URL urlImage = null;
            try {
                String sFinal = "";
                for (int i = 1; i < s.length() - 1; i++) {
                    sFinal += s.charAt(i);
                }
                urlImage = new URL(urlBase + (imageQuality.equals(HIGH_QUALITY) ? "/data/" : "/data-saver/") + hash + "/" + sFinal);
//                System.out.println(urlBase+"/data-saver/"+hash+"/"+s.toString());
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }

            OkHttpClient http = new OkHttpClient();

            Request req = new Request.Builder().url(urlImage).build();
            if (CancelCurrentWorkReceiver.isIsWorkDownloadChaptersCanceled()) {
                return bitmaps;
            }
            try (Response response = http.newCall(req).execute();) {
//                System.out.println("tipo do arquivo"+response.body().contentType().toString());
                if (response.body().contentType().toString().contains("image/")) {
                    InputStream ImageInput = response.body().byteStream();
                    BitmapFactory.Options op = new BitmapFactory.Options();
                    op.inJustDecodeBounds = false;
                    op.inScaled = false;
                    op.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    //Modificado
                    Bitmap bit = BitmapFactory.decodeStream(ImageInput, null, op);

                    bitmaps[index] = bit;

                }
            } catch (ConnectException ex) {
                bitmaps[index] = null;
                ex.printStackTrace();
            } catch (IOException ex) {
                bitmaps[index] = null;
                ex.printStackTrace();
            }

            index++;
        }
        return bitmaps;
    }

    public void addTags(ArrayList<String> tags) {
        this.tags = "";
        if (tags.isEmpty()) {
            this.updateOffSet = 0;
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : tags) {
            stringBuilder.append("&").append("includedTags[]=").append(s);
        }
        this.tags = stringBuilder.toString();
    }

    public ArrayList<TagManga> getTags() {
        ArrayList<TagManga> tags = new ArrayList<>();
        String url = "https://api.mangadex.org/manga/tag";
        URL urlTags = null;
        try {
            urlTags = new URL(url);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return tags;
        }

        OkHttpClient http = new OkHttpClient.Builder().build();
        Request reqTag = new Request.Builder().url(urlTags).build();
        try (Response response = http.newCall(reqTag).execute()) {
            Gson gson = new Gson();

            JsonElement json = gson.fromJson(response.body().string(), JsonElement.class);
//            System.out.println(json);
            JsonArray list = json.getAsJsonObject().get("data").getAsJsonArray();
            for (JsonElement element : list) {

                String id = element.getAsJsonObject().get("id").getAsString();
                String name = element.getAsJsonObject().get("attributes").getAsJsonObject().get("name").getAsJsonObject().get("en").getAsString();
                tags.add(new TagManga(id, name));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return tags;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return tags;
        }
        return tags;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public double getMangaStatus(String idApiManga){
        URL url;
        double lastChapter = 0;
        try{
            url = new URL("https://api.mangadex.org/manga/"+idApiManga);
        }catch (MalformedURLException ex){
            ex.printStackTrace();
            return 0;
        }
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(url).build();
        try(Response response = okHttpClient.newCall(request).execute()){
            Gson gson = new Gson();
            JsonElement jsonItem = gson.fromJson(response.body().string(),JsonElement.class);
//            System.out.println(jsonItem);
            if (jsonItem.getAsJsonObject().get("data").getAsJsonObject().get("attributes").getAsJsonObject().get("lastChapter").isJsonNull()) {
                lastChapter = 0;

            } else {
                lastChapter = jsonItem.getAsJsonObject().get("data").getAsJsonObject().get("attributes").getAsJsonObject().get("lastChapter").getAsString().isEmpty() ? 0 : jsonItem.getAsJsonObject().get("data").getAsJsonObject().get("attributes").getAsJsonObject().get("lastChapter").getAsDouble();
            }
            return lastChapter;
        }catch (IOException exception){
            exception.printStackTrace();
            return 0;
        }
    }

    @Override
    public ArrayList<ChapterManga> viewChapters(String mangaId, Handler h) {
        return null;
    }
}


