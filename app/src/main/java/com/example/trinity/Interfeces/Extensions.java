package com.example.trinity.Interfeces;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;

import androidx.fragment.app.Fragment;

import com.example.trinity.valueObject.ChapterManga;
import com.example.trinity.valueObject.Manga;
import com.example.trinity.valueObject.TagManga;
import com.google.gson.JsonArray;


import java.util.ArrayList;

public interface Extensions {

    //Lista de extensões suportadas
    public final String MANGADEX = "MangaDex";
    public final String MANGAKAKALOT = "MangakaKalot";

    public final int RESPONSE_ITEM = 1;
    public final int RESPONSE_PAGE = 2;
    public final int RESPONSE_ERROR = 3;
    public final int RESPONSE_FINAL = 4;
    public final int RESPONSE_EMPTY = 5;

    void updates( Handler h);
    void loadMangaLogo(Handler h, ArrayList<Manga> mangaArrayList);

    void search(String title, Handler h);

    ArrayList<Manga> responseToValueObject(String response);
    void setLanguage(String language);
    ArrayList<ChapterManga> viewChapters(String mangaId);

    void getChapterPages(Handler h, String idChapter);

    void loadChapterPages(Handler h, JsonArray array, String hash, String urlBase);

    Bundle getChapterPages(String idChapter);

    void loadUniquePage(String chapterIdApi, int chapterPage, Handler h);

    Bitmap[] loadChapterPages(String[] array, String hash, String urlBase);

    void addTags(ArrayList<String> tags);

    ArrayList<TagManga> getTags();

    Context getContext();

    void setContext(Context context);

    double getMangaStatus(String idApiManga);

}