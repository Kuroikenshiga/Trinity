package com.example.trinity.extensions;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.trinity.Interfaces.Extensions;
import com.example.trinity.Interfaces.PageStorage;
import com.example.trinity.R;
import com.example.trinity.storageAcess.ChapterPageBuffer;
import com.example.trinity.storageAcess.LogoMangaStorageTemp;
import com.example.trinity.storageAcess.PageCacheManager;
import com.example.trinity.valueObject.ChapterManga;
import com.example.trinity.valueObject.Manga;
import com.example.trinity.valueObject.TagManga;
import com.google.gson.JsonArray;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MangaLivreExtension implements Extensions {
    private int currentPage = 1;
    private Context context;
    private MangakakalotExtension.OnMangaLoaded onMangaLoaded;
    private String BASE_URL = "https://";
    private String BASE_URL_SEARCH = "https://mangalivre.blog/?s=";
    private boolean switchStatus = false;
    public MangaLivreExtension(MangakakalotExtension.OnMangaLoaded onMangaLoaded) {
        this.onMangaLoaded = onMangaLoaded;
    }
    public MangaLivreExtension() {

    }
    @Override
    public void updates(Handler h) {
        String url = String.format(!switchStatus?"https://mangalivre.blog/manga/page/%d/":"https://mangalivre.blog/manga/page/%d/?manga_status=completo&orderby=title",currentPage);
//        System.out.println(url);
        URL urlApi;
        try {
            urlApi = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(urlApi).build();
        try (Response response = httpClient.newCall(request).execute()) {
//            System.out.println(response.body().string());
            ArrayList<Manga> mangas = this.responseToValueObject(response.body().string());
            loadMangaLogo(h, mangas);
            currentPage++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadMangaLogo(Handler h, ArrayList<Manga> mangaArrayList) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), Executors.defaultThreadFactory());
        LogoMangaStorageTemp storageTemp = new LogoMangaStorageTemp(context);
        for (Manga item : mangaArrayList) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient httpClient = new OkHttpClient.Builder().build();
                    URL urlApiImage = null;
                    try {
                        urlApiImage = new URL(item.getCoverName());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    Request request = new Request.Builder().url(urlApiImage).header("Referer","https://mangalivre.blog/").build();
                    try (Response response = httpClient.newCall(request).execute()) {
                        if (response.isSuccessful() && response.body().contentType().toString().contains("image/")) {
                            InputStream inputStream = response.body().byteStream();

                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                            String[] arrayStrings = item.getId().split("[/]");
                            storageTemp.insertLogoManga(bitmap, arrayStrings[arrayStrings.length - 1]);
                            Message msg = Message.obtain();
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("dados", item);
                            msg.setData(bundle);
                            msg.what = RESPONSE_ITEM;
                            h.sendMessage(msg);
                            if (executor.getQueue().size() == 1) {
                                Message msgResp = Message.obtain();
                                msgResp.what = RESPONSE_FINAL;
                                h.sendMessage(msgResp);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        Message msg = Message.obtain();
        msg.what = RESPONSE_FINAL;
        h.sendMessage(msg);
    }

    @Override
    public void search(String title, Handler h) {
        title = title.replace(" ","+");
        String url = BASE_URL_SEARCH+title;
        System.out.println(url);
        URL urlApi;
        try {
            urlApi = new URL(url);
//            System.out.println("URL: "+baseUrlSearch+title.replace(" ","-"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().header("Referer", "https://mangalivre.blog/").url(urlApi).build();
        try (Response response = httpClient.newCall(request).execute()) {
//            System.out.println(response.body().string());
            ArrayList<Manga> mangas = this.responseToValueObject(response.body().string());

            if(mangas.isEmpty()){
                Message msg = Message.obtain();
                msg.what = RESPONSE_EMPTY;
                h.sendMessage(msg);
                return;
            }
            loadMangaLogo(h, mangas);
            currentPage++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<Manga> responseToValueObject(String response) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Document document = Jsoup.parse(response);
        Elements cards = document.getElementsByClass("manga-card");

        for(Element card : cards){
            Manga manga = new Manga();
            manga.setId(card.getElementsByTag("a").first().attr("href"));
            manga.setId(manga.getId().split("//")[1]);
            manga.setId(manga.getId().replace("/", "@"));

            try{
                manga.setCoverName(card.getElementsByClass("attachment-manga-cover").first().attr("src"));
            }catch (NullPointerException e){
                try{
                    manga.setCoverName(card.getElementsByClass("manga-cover-img").first().attr("src"));
                }catch (NullPointerException ex){
                    manga.setCoverName("");
                }

            }
//            System.out.println(manga.getCoverName());
            manga.setTitulo(card.getElementsByClass("manga-card-title").text());
            mangas.add(manga);
        }

        return mangas;
    }


    private String loadMangaInfo(String idManga,Handler h) {

        idManga = idManga.replace("@","/");

        String url = idManga.contains(BASE_URL) ? idManga : (BASE_URL + idManga);
        url = url.toLowerCase();
        URL urlApi = null;
        String html = "";
        try {
            urlApi = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient.Builder().callTimeout(8,TimeUnit.SECONDS).build();
        Request request = new Request.Builder().header("Referer", "https://mangalivre.blog/").url(urlApi).build();
        try (Response response = client.newCall(request).execute()) {
            if(response.isSuccessful()){
                html = response.body().string();
            }
            else{
                return "";
            }
            Manga manga = responseToFullValueObjet(html);
            if(onMangaLoaded != null)onMangaLoaded.onMangaLoaded(manga);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return html;
    }
    private Manga responseToFullValueObjet(String html){
        Manga manga = new Manga();
        Document document = Jsoup.parse(html);
        String authors = document.getElementsByClass("meta-value").get(1).text();
        String[] authorsArray = authors.split(", ");

        for(String s:authorsArray){
            manga.getAutor().add(s);
        }
        Elements tagsElements = document.getElementsByClass("manga-tag");

        ArrayList<TagManga> tagMangas = new ArrayList<>();

        for(Element element:tagsElements){
            TagManga tagManga = new TagManga();
            tagManga.setNome(element.text());
            tagMangas.add(tagManga);
        }
        manga.setTags(tagMangas);
        Elements paragaphs = document.getElementsByClass("synopsis-content").first().getElementsByTag("p");
        manga.setDescricao("");
        for(Element p:paragaphs){
            manga.setDescricao(manga.getDescricao()+p.text());
        }
        return manga;
    }
    @Override
    public void setLanguage(String language) {

    }

    @Override
    public ArrayList<ChapterManga> viewChapters(String mangaId) {
        Document document = Jsoup.parse(loadMangaInfo(mangaId,null));
        Elements chapterElements = document.getElementsByClass("chapter-item");
        ArrayList<ChapterManga> chapterMangas = new ArrayList<>();
        for(Element element:chapterElements){
            ChapterManga chapterManga = new ChapterManga();
            chapterManga.setId(element.getElementsByTag("a").first().attr("href").split("//")[1]);

            String chapter = element.getElementsByClass("chapter-number").first().getElementsByTag("span").first().
                    text();

            chapterManga.setChapter(chapter.split(" ")[1]);

            chapterManga.setChapter(chapterManga.getChapter().replace(":",""));


            if(chapter.contains(":")){
                chapterManga.setTitle(chapter.split("[:]")[1]);
            }else chapterManga.setTitle("");

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

            String RFC3339 = dateTimeFormatter.format(getValidDate(element.getElementsByClass("chapter-date").first().text()).toInstant().atZone(ZoneId.systemDefault()));
            chapterManga.setDateRFC3339(RFC3339);
            chapterManga.setAlredyRead(false);
            chapterManga.setScan("");
            chapterMangas.add(chapterManga);

        }
        return chapterMangas;
    }

    @Override
    public void getChapterPages(Handler h, String idChapter) {
        String idApi = idChapter.contains(BASE_URL) ? idChapter : (BASE_URL + idChapter);
        URL url = null;
        System.out.println(url);
        try {
            url = new URL(idApi);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(url).header("Referer","https://mangalivre.blog/").build();
        try (Response response = client.newCall(request).execute()) {
//            System.out.println(response.body().string());
            Document document = Jsoup.parse(response.body().string());
            Elements imgs = document.getElementsByClass("wp-manga-chapter-img");
            if(imgs.isEmpty())imgs = document.getElementsByClass("chapter-image");
            Message msg = Message.obtain();
            msg.what = RESPONSE_ITEM;
            Bundle bundle = new Bundle();
            bundle.putInt("numPages", imgs.size());
            msg.setData(bundle);
            if(h != null)h.sendMessage(msg);
            loadChapterPages(h, imgs);
        } catch (IOException e) {
            e.printStackTrace();
            Message msg = Message.obtain();
            msg.what = RESPONSE_ERROR;
            if(h != null)h.sendMessage(msg);
        }
    }
    public void loadChapterPages(Handler h, Elements imgs){
        int index = 1;
        PageStorage pageStorage = h != null? PageCacheManager.getInstance(context): ChapterPageBuffer.getInstance(context);
        for (Element s : imgs) {
            URL urlImage = null;
            try {
                String sFinal = "";
                for (int i = 1; i < s.toString().length() - 1; i++) {
                    sFinal += s.toString().charAt(i);
                }
                urlImage = new URL(s.attr("src"));
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }

            OkHttpClient http = new OkHttpClient();

            Request req = new Request.Builder().url(urlImage).build();

            try (Response response = http.newCall(req).execute();) {
//                System.out.println("tipo do arquivo"+response.body().contentType().toString());
                assert response.body() != null;
                if (Objects.requireNonNull(response.body().contentType()).toString().contains("image/")) {
                    InputStream ImageInput = response.body().byteStream();

                    if (index == 1) {
                        pageStorage.clearFolder();
                    }

                    BitmapFactory.Options op = new BitmapFactory.Options();
                    op.inJustDecodeBounds = false;
                    Bitmap bit = BitmapFactory.decodeStream(ImageInput, null, op);


                    String url = pageStorage.insertBitmapInFolder(bit, Integer.toString(index) + ".jpeg");
                    if (bit != null) {
                        bit.recycle();
                    }


                    Message msg = Message.obtain();
                    msg.what = Extensions.RESPONSE_PAGE;
                    Bundle bundle = new Bundle();
                    bundle.putString("img", url);
                    bundle.putInt("index", index);
                    msg.setData(bundle);
                    if(h != null)h.sendMessage(msg);
                }
            } catch (ConnectException ex) {
                Message msg = Message.obtain();
                msg.what = Extensions.RESPONSE_PAGE;
                Bundle bundle = new Bundle();
                Bitmap bit = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.time_out);
                bundle.putParcelable("img", bit);
                bundle.putInt("index", index);
                msg.setData(bundle);
                if(h != null)h.sendMessage(msg);
            } catch (IOException ex) {
                ex.printStackTrace();
            }


            index++;
        }
    }
    @Override
    public void loadChapterPages(Handler h, JsonArray array, String hash, String urlBase) {

    }

    @Override
    public Bundle getChapterPages(String idChapter) {

        return null;
    }

    @Override
    public void loadUniquePage(String chapterIdApi, int chapterPage, Handler h) {

    }

    @Override
    public Bitmap[] loadChapterPages(String[] array, String hash, String urlBase) {
        return new Bitmap[0];
    }

    @Override
    public void addTags(ArrayList<String> tags) {

    }

    @Override
    public ArrayList<TagManga> getTags() {
        return null;
    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void setContext(Context context) {

    }

    @Override
    public double getMangaStatus(String idApiManga) {
        return 0;
    }

    @Override
    public ArrayList<ChapterManga> viewChapters(String mangaId, Handler h) {
        Document document = Jsoup.parse(loadMangaInfo(mangaId,h));
        Elements chapterElements = document.getElementsByClass("chapter-item");
        ArrayList<ChapterManga> chapterMangas = new ArrayList<>();
        for(Element element:chapterElements){
            ChapterManga chapterManga = new ChapterManga();
            chapterManga.setId(element.getElementsByTag("a").first().attr("href").split("//")[1]);

            String chapter = element.getElementsByClass("chapter-number").first().getElementsByTag("span").first().
                    text();

            chapterManga.setChapter(chapter.split(" ")[1]);

            chapterManga.setChapter(chapterManga.getChapter().replace(":",""));


            if(chapter.contains(":")){
                chapterManga.setTitle(chapter.split("[:]")[1]);
            }else chapterManga.setTitle("");

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

            String RFC3339 = dateTimeFormatter.format(getValidDate(element.getElementsByClass("chapter-date").first().text()).toInstant().atZone(ZoneId.systemDefault()));
            chapterManga.setDateRFC3339(RFC3339);
            chapterManga.setAlredyRead(false);
            chapterManga.setScan("");
            chapterMangas.add(chapterManga);

        }
        return chapterMangas;
    }
    private Calendar getValidDate(String s){
        s.toLowerCase();
        int toDecrement = Integer.parseInt(s.split(" ")[1])*-1;
        Calendar today = Calendar.getInstance();
        if(s.contains("ano")){
            today.add(Calendar.YEAR,toDecrement);
        } else if(s.contains("mes")){
            today.add(Calendar.MONTH,toDecrement);
        } else if (s.contains("sem")) {
            today.add(Calendar.DAY_OF_MONTH,toDecrement*7);
        } else if (s.contains("dia")) {
            today.add(Calendar.DAY_OF_MONTH,toDecrement);
        } else if (s.contains("hor")) {
            today.add(Calendar.HOUR_OF_DAY,toDecrement);
        } else if (s.contains("min")) {
            today.add(Calendar.MINUTE,toDecrement);
        } else if (s.contains("seg")) {
            today.add(Calendar.SECOND,toDecrement);
        }
        return today;
    }

    public void switchStatus(){
        this.switchStatus = !switchStatus;
        this.currentPage = 1;
    }
}
