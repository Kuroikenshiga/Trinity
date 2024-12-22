package com.example.trinity.extensions;

import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.fragment.app.Fragment;

import com.example.trinity.Interfeces.Extensions;
import com.example.trinity.R;
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MangakakalotExtension implements Extensions {
    private Context context;
    private Fragment fragment;
    private int currentPage = 1;
    private String language = "en";
    private static String BASE_URL = "https://";
    private OnMangaLoaded onMangaLoaded;
    public static String CHAPMANGANATO = "chapmanganato";
    public static String MANGAKAKALOT = "mangakakalot";

    public MangakakalotExtension(OnMangaLoaded onMangaLoaded) {
        this.onMangaLoaded = onMangaLoaded;
    }

    @Override
    public void updates(Handler h) {
        String url = "https://mangakakalot.com/manga_list?type=latest&category=all&state=all&page=" + currentPage;
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
        ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 4, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), Executors.defaultThreadFactory());
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

                    Request request = new Request.Builder().url(urlApiImage).build();
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
    }

    @Override
    public void search(String title, Handler h) {
        String baseUrlSearch = "https://mangakakalot.com/search/story/";
        System.out.println(baseUrlSearch+title.replace(" ","_"));
        URL urlApi;
        try {
            urlApi = new URL(baseUrlSearch+title.replace(" ","_"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        OkHttpClient httpClient = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(urlApi).build();
        try (Response response = httpClient.newCall(request).execute()) {
//            System.out.println(response.body().string());
            ArrayList<Manga> mangas = this.responseSearchToValueObject(response.body().string());
            if(mangas.isEmpty()){
                Message msg = Message.obtain();
                msg.what = RESPONSE_EMPTY;
                h.sendMessage(msg);
            }
            loadMangaLogo(h, mangas);
            currentPage++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String loadMangaInfo(String idManga) {
        String url = idManga.contains(BASE_URL) ? idManga : (BASE_URL + idManga);
        URL urlApi = null;
        String html = "";
        try {
            urlApi = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(urlApi).build();
        try (Response response = client.newCall(request).execute()) {
            html = response.body().string();
            Manga manga = idManga.contains(CHAPMANGANATO) ? responseToFullValueObjectChapManganato(html) : responseToFullValueObjectMangakakalot(html);

            if (this.onMangaLoaded != null) {
                this.onMangaLoaded.onMangaLoaded(manga);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return html;
    }

    @Override
    public ArrayList<Manga> responseToValueObject(String response) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Document document = Jsoup.parse(response);
        Elements mangaElements = document.getElementsByClass("list-truyen-item-wrap");
        for (Element element : mangaElements) {
            Manga manga = new Manga();
            manga.setCoverName(element.getElementsByTag("img").first().attr("src"));
            manga.setTitulo(element.getElementsByTag("h3").first().getElementsByTag("a").first().attr("title"));
            manga.setId(element.getElementsByTag("a").first().attr("href"));

            manga.setId(manga.getId().split("//")[1]);
            manga.setId(manga.getId().replace("/", "@"));
            manga.setLanguage(language);
            mangas.add(manga);


        }
        return mangas;
    }
    public ArrayList<Manga> responseSearchToValueObject(String response) {
        ArrayList<Manga> mangas = new ArrayList<>();
        Document document = Jsoup.parse(response);
        Elements mangaElements = document.getElementsByClass("story_item");
        if(mangaElements == null) return new ArrayList<>();
        for (Element element : mangaElements) {
            Manga manga = new Manga();
            manga.setCoverName(element.getElementsByTag("img").first().attr("src"));
            manga.setTitulo(element.getElementsByTag("h3").first().getElementsByTag("a").first().text());
            manga.setId(element.getElementsByTag("a").first().attr("href"));

            manga.setId(manga.getId().split("//")[1]);
            manga.setId(manga.getId().replace("/", "@"));
            manga.setLanguage(language);
            mangas.add(manga);

        }
        return mangas;
    }
    private Manga responseToFullValueObjectChapManganato(String html) {
        Document document = Jsoup.parse(html);
        Element table = document.getElementsByClass("variations-tableInfo").first();
        Elements values = table.getElementsByClass("table-value");
        ArrayList<String> authors = new ArrayList<>();
        String status = "";
        String description = "";
        ArrayList<TagManga> tagMangas = new ArrayList<>();
        int startIndex = table.getElementsByClass("table-label").first().getElementsByTag("i").isEmpty() ? 0 : 1;
        for (int i = startIndex; i < values.size(); i++) {
            if (i == startIndex) {
                Elements links = values.get(i).getElementsByTag("a");
                for (Element element : links) {
                    authors.add(element.text());
                }
            } else if (i == startIndex + 1) {
                status = values.get(i).text();
            } else if (i == startIndex + 2) {
                Elements tags = values.get(i).getElementsByTag("a");
                for (Element element : tags) {
                    tagMangas.add(new TagManga(element.attr("href").split("//")[1], element.text()));
                }
                break;
            }
        }
        description = document.getElementById("panel-story-info-description").text();
        Manga manga = new Manga();
        manga.setAutor(authors);
        if (!status.equals("Ongoing")) {
            manga.setLastChapter(Manga.COMPLETED);
        }
        manga.setDescricao(description);
        manga.setTags(tagMangas);
        return manga;
    }

    private Manga responseToFullValueObjectMangakakalot(String html) {
        Document document = Jsoup.parse(html);
        Element ul = document.getElementsByClass("manga-info-text").first();
        Elements values = ul.getElementsByTag("li");
        Manga manga = new Manga();
        ArrayList<String> authors = new ArrayList<>();
        ArrayList<TagManga> tagMangas = new ArrayList<>();
        String status = "";
        String description = "";
        for (int i = 1; i < values.size(); i++) {
            switch (i) {
                case 1:
                    Elements links = values.get(i).getElementsByTag("a");
                    for (Element element : links) {
                        authors.add(element.text());
                    }
                    break;
                case 2:
                    status = values.get(i).text().split(" : ")[1];
                    break;
                case 6:
                    Elements tags = values.get(i).getElementsByTag("a");
                    for (Element element : tags) {
                        tagMangas.add(new TagManga(element.attr("href").split("//")[1], element.text()));
                    }
                    break;
            }
        }

        description = document.getElementById("noidungm").text();

        manga.setAutor(authors);
        if (!status.equals("Ongoing")) {
            manga.setLastChapter(Manga.COMPLETED);
        }
        manga.setDescricao(description);
        manga.setTags(tagMangas);
        return manga;
    }

    public ArrayList<ChapterManga> viewChaptersChapmanganato(String html) {
        if (html == null) {
            return new ArrayList<>();
        }
        Document document = Jsoup.parse(html);
        if(document.getElementsByClass("panel-story-chapter-list").isEmpty())return new ArrayList<>();
        if(document.getElementsByClass("panel-story-chapter-list").first().getElementsByClass("row-content-chapter").isEmpty())return new ArrayList<>();
        Element ul = document.getElementsByClass("row-content-chapter").first();
        Elements li = ul.getElementsByTag("li");
        ArrayList<ChapterManga> chapterMangas = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        for (Element item : li) {
            String id = item.getElementsByTag("a").attr("href").split("//")[1];
            String title = item.getElementsByTag("a").attr("title");
            double chapter = Double.parseDouble(item.getElementsByTag("a").attr("href").split("chapter-")[1]);
//            String data = item.getElementsByTag("span").last().attr("data-fn-time");
            String RFC3339 = simpleDateFormat.format(dateFormaterChapmanganato(item.getElementsByTag("span").last().attr("data-fn-time")).getTime());
            chapterMangas.add(new ChapterManga(id, title, String.valueOf(chapter), "", RFC3339, false));
        }
        return chapterMangas;
    }

    public ArrayList<ChapterManga> viewChaptersMangakakalot(String html) {
        Document document = Jsoup.parse(html);
        if(document.getElementsByClass("chapter-list").isEmpty())return new ArrayList<>();
        if(document.getElementsByClass("chapter-list").first().getElementsByClass("row").isEmpty())return new ArrayList<>();
        Element div = document.getElementsByClass("chapter-list").first();
        Elements divsChap = div.getElementsByTag("div");
        ArrayList<ChapterManga> chapterMangas = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        for (Element item : divsChap) {
            String id = item.getElementsByTag("span").first().getElementsByTag("a").first().attr("href").split("//")[1];
            String title = item.getElementsByTag("span").first().getElementsByTag("a").first().attr("title");

            double chapter = Double.parseDouble(item.getElementsByTag("a").attr("href").split("chapter_")[1]);
//            String RFC3339 = simpleDateFormat.format(dateFormaterMangakakalot(item.getElementsByTag("span").last().text()).getTime());
//            System.out.println(dateFormaterMangakakalot(item.getElementsByTag("span").last().text()).getTime());
            String RFC3339 = dateTimeFormatter.format(dateFormaterMangakakalot(item.getElementsByTag("span").last().attr("title")).getTime().toInstant().atZone(ZoneId.systemDefault()));
            chapterMangas.add(new ChapterManga(id, title, String.valueOf(chapter), "", RFC3339, false));
        }
        return chapterMangas;
    }

    @Override
    public ArrayList<ChapterManga> viewChapters(String mangaId) {
        mangaId = mangaId.replace("@", "/");
        String urlApi = mangaId.contains(BASE_URL) ? mangaId : (BASE_URL + mangaId);
        String html = loadMangaInfo(mangaId);

        return mangaId.contains(MANGAKAKALOT) ? viewChaptersMangakakalot(html) : viewChaptersChapmanganato(html);
    }

    @Override
    public void getChapterPages(Handler h, String idChapter) {
        String idApi = idChapter.contains(BASE_URL) ? idChapter : (BASE_URL + idChapter);
        URL url = null;
        try {
            url = new URL(idApi);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            Document document = Jsoup.parse(response.body().string());
            Elements imgs = document.select(".container-chapter-reader > img");

            Message msg = Message.obtain();
            msg.what = RESPONSE_ITEM;
            Bundle bundle = new Bundle();
            bundle.putInt("numPages", imgs.size());
            msg.setData(bundle);
            h.sendMessage(msg);
            loadChapterPages(h, imgs);
        } catch (IOException e) {
            e.printStackTrace();
            Message msg = Message.obtain();
            msg.what = RESPONSE_ERROR;
            h.sendMessage(msg);
        }


    }

    private void loadChapterPages(Handler h, Elements imgs) {
        int index = 1;
        for (Element img : imgs) {
            URL url = null;
            try {
                url = new URL(img.attr("src"));

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            OkHttpClient client = new OkHttpClient.Builder().build();
            Request request = new Request.Builder().url(url).header("Referer", img.attr("src").contains("manganato") ? "https://chapmanganato.to" : "https://mangakakalot.com").build();
            try (Response response = client.newCall(request).execute()) {

                if (response.body().contentType().toString().contains("image/")) {
                    InputStream imageInput = response.body().byteStream();
                    if (index == 1) {
                        PageCacheManager.getInstance(context).clearCache();
                    }
                    BitmapFactory.Options op = new BitmapFactory.Options();
                    op.inJustDecodeBounds = false;
                    Bitmap bit = BitmapFactory.decodeStream(imageInput, null, op);

                    String urlImage = PageCacheManager.getInstance(context).insertBitmapInCache(bit, String.valueOf(index) + ".jpeg");

                    if (bit != null) {
                        bit.recycle();
                    }
                    Message msg = Message.obtain();
                    msg.what = RESPONSE_PAGE;
                    Bundle bundle = new Bundle();
                    bundle.putString("img", urlImage);
                    bundle.putInt("index", index);
                    msg.setData(bundle);
                    h.sendMessage(msg);
                }
            } catch (ConnectException ex) {
                Message msg = Message.obtain();
                msg.what = RESPONSE_PAGE;
                Bundle bundle = new Bundle();
                Bitmap bit = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.time_out);
                bundle.putParcelable("img", bit);
                bundle.putInt("index", index);
                msg.setData(bundle);
                h.sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
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
        return context;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public double getMangaStatus(String idApiManga) {
        return 0;
    }

    public static interface OnMangaLoaded {
        void onMangaLoaded(Manga manga);
    }

    public void setOnMangaLoaded(OnMangaLoaded onMangaLoaded) {
        this.onMangaLoaded = onMangaLoaded;
    }

    private static double chapFormater(String s) {
        String[] words = s.split(" ");
        Pattern pattern = Pattern.compile("[a-zA-Z]");
        boolean subStringFound = true;
        String subStringChapter = "";
        for (String string : words) {
            subStringFound = true;
            for (int i = 0; i < string.length(); i++) {
                if (pattern.matcher("" + string.charAt(i)).matches()) {
                    subStringFound = false;
                    break;
                }

            }
            if (subStringFound) {
                subStringChapter = string;
                break;
            }
        }
        StringBuilder chapter = new StringBuilder();
        pattern = Pattern.compile("[\\d.]");
        for (int i = 0; i < subStringChapter.length(); i++) {
            if (subStringChapter.charAt(i) == '.' && i == subStringChapter.length() - 1) {
                break;
            }
            if (pattern.matcher("" + subStringChapter.charAt(i)).matches()) {
                chapter.append("" + subStringChapter.charAt(i));
            } else if (subStringChapter.charAt(i) == '-') {
                chapter.append(".");
            }
        }
        return Double.parseDouble(chapter.toString().isEmpty() ? "0" : chapter.toString());
    }

    private static Calendar dateFormaterChapmanganato(String s) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Date.from(Instant.ofEpochSecond(Long.parseLong(s))));
        calendar.add(HOUR_OF_DAY,-10);
        return calendar;
//        Calendar calendar = Calendar.getInstance();
//        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
//        String[] words = s.split(",");
//        int index = 0;
//        for (String string : words) {
//            if (index == 0) {
//                String m = string.split(" ")[0];
//                for (int i = 0; i < months.length; i++) {
//                    if (m.equals(months[i])) {
//                        calendar.set(MONTH, i);
//                        break;
//                    }
//                }
//                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(string.split(" ")[1]));
//            } else {
//                String x = string.split(" ")[0];
//                calendar.set(YEAR, Integer.parseInt(string.split(" ")[0]));
//                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(string.split(" ")[1].split(":")[0]));
//                calendar.set(Calendar.MINUTE, Integer.parseInt(string.split(" ")[1].split(":")[1]));
//            }
//            index++;
//        }
//        System.out.println(calendar.get(Calendar.DAY_OF_MONTH)+"/"+calendar.get(MONTH)+"/"+calendar.get(YEAR));
//        return calendar;
    }

    private static Calendar dateFormaterMangakakalot(String s) {
        Calendar calendar = Calendar.getInstance();

        if (s.contains("day") || s.contains("hour") || s.contains("minute") || s.contains("second")) {
            String[] time = s.split(" ");
            calendar.add((s.contains("day") ? Calendar.DAY_OF_MONTH : s.contains("hour") ? Calendar.HOUR_OF_DAY : s.contains("minute") ? Calendar.MINUTE : Calendar.SECOND), Integer.parseInt(time[0]) * -1);

            return calendar;
        }

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] words = s.split(" ");
        for (int i = 0; i < words.length; i++) {
            if (i == 0) {
                String[] dayMonthYear = words[i].split("-");
                for (int k = 0; k < dayMonthYear.length; k++) {
                    if (k == 0) {
                        for (int j = 0; j < months.length; j++) {
                            if (dayMonthYear[0].equals(months[j])) {
                                calendar.set(MONTH, j);
                                break;
                            }
                        }
                    } else if (k == 1) {
                        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayMonthYear[1]));
                    } else {
                        calendar.set(YEAR, Integer.parseInt(dayMonthYear[2]));
                    }
                }
            } else {
                String[] time = words[i].split(":");
                for (int j = 0; j < time.length; j++) {
                    if (j == 0) {
                        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
                    } else {
                        calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
                    }
                }
            }
        }
        return calendar;
    }

    private static Calendar formaterDate(String mangaId, String date) {
        if (mangaId.contains(CHAPMANGANATO)) {
            return dateFormaterChapmanganato(date);
        }
        return dateFormaterMangakakalot(date);
    }
    public void setLanguage(String language){

    }
}
