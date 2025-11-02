package com.example.trinity.models;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.trinity.MainActivity;
import com.example.trinity.MangaShowContentActivity;
import com.example.trinity.fragments.LibraryFragment;
import com.example.trinity.preferecesConfig.ConfigClass;
import com.example.trinity.storageAcess.LogoMangaStorage;
import com.example.trinity.storageAcess.LogoMangaStorageTemp;
import com.example.trinity.valueObject.ChapterManga;
import com.example.trinity.valueObject.ChapterUpdated;
import com.example.trinity.valueObject.History;
import com.example.trinity.valueObject.Manga;
import com.example.trinity.valueObject.TagManga;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Predicate;

public class Model extends SQLiteOpenHelper {

    private static final String dataBaseName = "TrinityDB";
    private static final int version = 10;
    private SQLiteDatabase sqLiteDatabase;
    private static Model instance;
    private Context context;
    private boolean MANGA_TABLE_HAS_CHANGES = false;
    private boolean MANGA_UPDATES_TABLE_HAS_CHANGES = false;
    private Model(@Nullable Context context) {
        super(context, dataBaseName, null, version);
        this.context = context;
        sqLiteDatabase = this.getWritableDatabase();

    }

    public static Model getInstance(Context context){
        if(instance == null){
            instance = new Model(context.getApplicationContext());
        }
        return instance;
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS tags(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_tag VARCHAR(50) UNIQUE NOT NULL," +
                "name_tag VARCHAR(45) NOT NULL)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS mangas(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "cover_name VARCHAR(50) NOT NULL," +
                "id_manga VARCHAR(50) NOT NULL," +
                "image BLOB NOT NULL," +
                "description VARCHAR(500)," +
                "date_added TEXT NOT NULL," +
                "title VARCHAR(50) NOT NULL," +
                "language VARCHAR(5) NOT NULL," +
                "last_chapter_read TEXT,"+
                "last_chapter TEXT)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS authors(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name VARCHAR(45) NOT NULL," +
                "manga_id INTEGER NOT NULL," +
                "FOREIGN KEY(manga_id)REFERENCES mangas(id))");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS tag_mangas(" +
                "id_manga INTEGER," +
                "id_tag INTEGER," +
                "FOREIGN KEY(id_manga)REFERENCES mangas(id)," +
                "FOREIGN KEY(id_tag)REFERENCES tags(id)," +
                "PRIMARY KEY(id_manga,id_tag))");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS chapters(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_chapter VARCHAR(50) UNIQUE NOT NULL," +
                "title VARCHAR(45) NOT NULL," +
                "scan VARCHAR(45) NOT NULL," +
                "date_RFC3339 VARCHAR(45) NOT NULL," +
                "manga_id INTEGER NOT NULL," +
                "chapter VARCHAR(15)," +
                "alredy_read text," +
                "currentPage INTEGER," +
                "is_downloaded TEXT," +
                "FOREIGN KEY(manga_id)REFERENCES mangas(id))");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS reading_historic(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "manga_id INTEGER NOT NULL," +
                "last_acessed TEXT NOT NULL," +
                "FOREIGN KEY(manga_id)REFERENCES mangas(id))");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS updates(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "chapter INTEGER NOT NULL," +
                "FOREIGN KEY(chapter)REFERENCES chapters(id))");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS chapterPage(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "file_name TEXT NOT NULL," +
                "chapter INTEGER NOT NULL," +
                "page_number INTEGER NOT NULL," +
                "FOREIGN KEY(chapter)REFERENCES chapters(id))");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        if (oldVersion == 1) {
//            db.beginTransaction();
//            try {
//                db.execSQL("ALTER TABLE chapters ADD COLUMN currentPage INTEGER");
//            } catch (SQLiteException ex) {
//                db.endTransaction();
//            }
//            db.setTransactionSuccessful();
//            db.endTransaction();
//        }
//        if (oldVersion == 2) {
//            db.beginTransaction();
//            try {
//                db.execSQL("ALTER TABLE mangas ADD COLUMN last_chapter_read TEXT");
//            } catch (SQLiteException ex) {
//                db.endTransaction();
//            }
//            db.setTransactionSuccessful();
//            db.endTransaction();
//        }
//        if(oldVersion == 3){
//            db.beginTransaction();
//            try {
//                db.execSQL("CREATE TABLE IF NOT EXISTS chapterPage(" +
//                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
//                        "file_name TEXT NOT NULL," +
//                        "chapter INTEGER NOT NULL," +
//                        "page_number INTEGER NOT NULL," +
//                        "FOREIGN KEY(chapter)REFERENCES chapters(id))");
//            } catch (SQLiteException ex) {
//                db.endTransaction();
//            }
//            db.setTransactionSuccessful();
//            db.endTransaction();
//        }
//        if (oldVersion == 4) {
//            db.beginTransaction();
//            try {
//                db.execSQL("ALTER TABLE chapters ADD COLUMN is_downloaded TEXT");
//            } catch (SQLiteException ex) {
//                db.endTransaction();
//            }
//            db.setTransactionSuccessful();
//            db.endTransaction();
//        }
//        if (oldVersion == 5) {
//            db.beginTransaction();
//            try {
//                db.execSQL("ALTER TABLE chapters ADD COLUMN page_number INTEGER NOT NULL");
//            } catch (SQLiteException ex) {
//                db.endTransaction();
//            }
//            db.setTransactionSuccessful();
//            db.endTransaction();
//        }
//        if(oldVersion == 6){
//            db.beginTransaction();
//            try {
//                db.execSQL("ALTER TABLE chapterPage ADD COLUMN page_number INTEGER NOT NULL");
//            } catch (SQLiteException ex) {
//                db.endTransaction();
//            }
//            db.setTransactionSuccessful();
//            db.endTransaction();
//        }
//        if (oldVersion < version) {
//            db.execSQL("ALTER TABLE mangas ADD COLUMN logo_path TEXT");
//
//        }
        if (oldVersion == 9) {
            db.beginTransaction();
            try {
                db.execSQL("ALTER TABLE mangas ADD COLUMN last_chapter TEXT");
            } catch (SQLiteException ex) {
                db.endTransaction();
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    public ArrayList<Manga> selectAllMangas(boolean loadChaptersToo, int limit,int offSet) {

        return new MangaDB(this.sqLiteDatabase).select().AllMangas(loadChaptersToo,limit,offSet);
    }

    public boolean addOrUpdateReadingHitory(History history) {
        MangaDB mangaDB = new MangaDB(this.sqLiteDatabase);
        ReadingHistoricDB readingHistoricDB = new ReadingHistoricDB(this.sqLiteDatabase);
        long idManga = mangaDB.returnMangaId(history.getManga().getId(), history.getManga().getLanguage());
        if (idManga == -1) return false;
        long idHistory = readingHistoricDB.returnHistoryId(idManga);

        if (idHistory == -1) {
            return readingHistoricDB.insertHistory(idManga, history.getLastAcess());
        } else {

            return readingHistoricDB.updateHistory(idHistory, history.getLastAcess());
        }

    }

    public ArrayList<History> selectAllHistory() {

        return new ReadingHistoricDB(this.sqLiteDatabase).selectAllHistory();
    }

    public ArrayList<Manga> loadSearch(String mangaTitle, int limit) {
        MangaDB mangaDB = new MangaDB(this.sqLiteDatabase);
        return mangaDB.select().MangaByTitle(mangaTitle,limit);

    }

    public boolean addInFavorites(Manga m) {

        long mangaID = -1;
        MangaDB mangaDB = new MangaDB(this.sqLiteDatabase);
        TagDB tagDB = new TagDB(this.sqLiteDatabase);
        AuthorDB authorDB = new AuthorDB(this.sqLiteDatabase);
        ChaptersDB chaptersDB = new ChaptersDB(this.sqLiteDatabase);

        for (TagManga tg : m.getTags()) {
            if (tagDB.returnTagId(tg.getId()) == -1) {
                if (tagDB.insertTag(tg) == -1) {
                    if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                    return false;
                }
            }
        }
        if (mangaDB.returnMangaId(m.getId(), m.getLanguage()) != -1) {
            if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.setTransactionSuccessful();

            return false;
        }

        mangaID = mangaDB.insertManga(m);
        String mangaIDStringFormat = Long.toString(mangaID);
        if (mangaID == -1) {
            if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

            return false;
        }
        for (String s : m.getAutor()) {
            if (authorDB.insertAuthor(s, mangaIDStringFormat) == -1) {
                if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                return false;
            }
        }
        for (TagManga t : m.getTags()) {
            if(t.getId() == null || t.getId().isEmpty())continue;
            if (tagDB.insertRelationTagManga(mangaIDStringFormat, Long.toString(tagDB.returnTagId(t.getId()))) == -1) {
                if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                return false;
            }
        }

        for (int i = 0; i < m.getChapters().size();i++) {
            if (chaptersDB.returnChapterId(m.getChapters().get(i).getId()) == -1) {
                if (chaptersDB.insertChapter(m.getChapters().get(i), mangaIDStringFormat) == -1) {
                    if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                    return false;
                }
            }
        }
        if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.setTransactionSuccessful();

        MANGA_TABLE_HAS_CHANGES = true;
        MANGA_UPDATES_TABLE_HAS_CHANGES = true;
        return true;
    }

    public boolean removeFromFavorites(Manga manga) {
        long id = -1;

        MangaDB mangaDB = new MangaDB(this.sqLiteDatabase);
        TagDB tagDB = new TagDB(this.sqLiteDatabase);
        AuthorDB authorDB = new AuthorDB(this.sqLiteDatabase);
        ChaptersDB chaptersDB = new ChaptersDB(this.sqLiteDatabase);
        UpdatesDB updatesDB = new UpdatesDB(this.sqLiteDatabase);
        ReadingHistoricDB readingHistoricDB = new ReadingHistoricDB(this.sqLiteDatabase);

        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT * FROM mangas WHERE id_manga = ? AND language = ?", new String[]{manga.getId(), manga.getLanguage()})) {
            if (row.getCount() < 1) {

                return false;
            }
            LogoMangaStorageTemp storageTemp = new LogoMangaStorageTemp(context);
            row.moveToNext();
            id = row.getLong(0);
            this.sqLiteDatabase.beginTransaction();

            for (ChapterManga chap : manga.getChapters()) {
                if (!updatesDB.deleteUpdates(chaptersDB.returnChapterId(chap.getId()))) {
                    if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                    return false;
                }
            }
            if (!readingHistoricDB.deleteReadingHistoricFromManga(id)) {
                if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                return false;
            }
            if (!authorDB.deleteAuthorsFromManga(id)) {
                if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                return false;
            }
            if (!tagDB.deleteTagFromManga(id)) {
                if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                return false;
            }
            if (!chaptersDB.deleteChapterFromManga(id)) {
                if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                return false;
            }
            if (!mangaDB.deleteManga(id)) {
                if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                return false;
            }
            if(!storageTemp.receiveFile(manga.getId()))this.sqLiteDatabase.endTransaction();

            if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.setTransactionSuccessful();
            this.sqLiteDatabase.endTransaction();

        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }
        MANGA_TABLE_HAS_CHANGES = true;
        MANGA_UPDATES_TABLE_HAS_CHANGES = true;
        return true;
    }

    public boolean mangaAlredFavorited(Manga m) {

        if (new MangaDB(this.sqLiteDatabase).returnMangaId(m.getId(), m.getLanguage()) != -1) {
            return true;
        }
        return false;
    }

    public boolean addNewChapter(ChapterUpdated chapterUpdated) {
        MangaDB mangaDB = new MangaDB(this.sqLiteDatabase);
        ChaptersDB chaptersDB = new ChaptersDB(this.sqLiteDatabase);
        UpdatesDB updatesDB = new UpdatesDB(this.sqLiteDatabase);

        long mangaID = mangaDB.returnMangaId(chapterUpdated.getManga().getId(), chapterUpdated.getManga().getLanguage());
        if (mangaID == -1) {

            return false;
        }


        sqLiteDatabase.beginTransaction();

        long chapId = chaptersDB.insertChapter(chapterUpdated.getChapterManga(), Long.toString(mangaID));
        if (chapId == -1) {
            sqLiteDatabase.endTransaction();

            return false;
        }
        if (updatesDB.insertUpdate(chapId) == -1) {
            sqLiteDatabase.endTransaction();

            return false;
        }
        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();

        return true;
    }
    @Deprecated
    public ArrayList<ChapterManga> loadUpdates() {
        return new UpdatesDB(this.sqLiteDatabase).selectFirst100Updates();
    }

    public boolean setChapterLastPage(String idApiChapter, int currentPage) {
        return new ChaptersDB(this.sqLiteDatabase).setChapterLastPage(idApiChapter,currentPage);
    }

    public boolean chapterRead(ChapterManga ch) {
        long chapId = new ChaptersDB(this.sqLiteDatabase).returnChapterId(ch.getId());
        if (chapId == -1) {
            return false;
        }
        if (!new ChaptersDB(this.sqLiteDatabase).setChapterAlredyRead(chapId)) {
            return false;
        }
        return true;
    }

    public ArrayList<ChapterManga> getAllChapterByMangaID(String apiID, String mangaLanguage) {
        long id = new MangaDB(this.sqLiteDatabase).returnMangaId(apiID, mangaLanguage);
        if (id == -1) {
            return new ArrayList<ChapterManga>();
        }
        return new ChaptersDB(this.sqLiteDatabase).selectAllChaptersByIdManga(Long.toString(id));
    }

    public boolean setLastChapterRead(String idApiChapter, String idApiManga, String language) {
        return new MangaDB(this.sqLiteDatabase).setLastChapterRead(idApiChapter, idApiManga, language);
    }

    public String getIdApiOfLastChapterRead(String idMangaApi, String language) {

        return new MangaDB(this.sqLiteDatabase).getIdApiOfLastChapterRead(idMangaApi, language);
    }
    @Deprecated
    private long insertChapterPage(long idChapter, String absolutePath, int numPage) {
        return new ChapterPages(this.sqLiteDatabase).insertChapterPage(idChapter, absolutePath, numPage);
    }
    @Deprecated
    public boolean setChapterDownloaded(long id, boolean isDownloaded) {
        return new ChaptersDB(this.sqLiteDatabase).setChapterDownloaded(id, isDownloaded);
    }

    public boolean savePage(String idChapterApi, String path, int pageNumber) {
        long chapter = new ChaptersDB(this.sqLiteDatabase).returnChapterId(idChapterApi);

        if (chapter == -1) {

            return false;
        }
        this.sqLiteDatabase.beginTransaction();

        if (new ChapterPages(this.sqLiteDatabase).insertChapterPage(chapter, path, pageNumber) == -1) {
            this.sqLiteDatabase.endTransaction();


            return false;
        }
        if (!new ChaptersDB(this.sqLiteDatabase).setChapterDownloaded(chapter, true)) {
            this.sqLiteDatabase.endTransaction();

            return false;
        }
        this.sqLiteDatabase.setTransactionSuccessful();
        this.sqLiteDatabase.endTransaction();

        return true;
    }

    public ContentValues[] getPagesDownloaded(String chapApiID) {
        return new ChapterPages(this.sqLiteDatabase).getPagesDownloaded(chapApiID);
    }

    public int getNumberPagesDownloaded(String chapApiID) {
        return new ChapterPages(this.sqLiteDatabase).getNumberPagesDownloaded(chapApiID);
    }
    @Deprecated
    private boolean deleteChapterPages() {
        return new ChapterPages(this.sqLiteDatabase).deleteChapterPages();
    }
    @Deprecated
    private boolean setIsdownloadedAsFalse() {
        return new ChaptersDB(this.sqLiteDatabase).setIsdownloadedAsFalseForAll();
    }

    public boolean deleteAllpagesDownloaded() {

        this.sqLiteDatabase.beginTransaction();
        if (!new ChaptersDB(this.sqLiteDatabase).setIsdownloadedAsFalseForAll()) {
            this.sqLiteDatabase.endTransaction();

            return false;
        }
        if (!new ChapterPages(this.sqLiteDatabase).deleteChapterPages()) {
            this.sqLiteDatabase.endTransaction();

            return false;
        }
        this.sqLiteDatabase.setTransactionSuccessful();
        this.sqLiteDatabase.endTransaction();

        return true;
    }
    public void saveTag(ArrayList<TagManga> tags) {
        if (tags.isEmpty()) {
            return;
        }
        for (TagManga tag : tags) {
            if (new TagDB(this.sqLiteDatabase).returnTagId(tag.getId()) == -1) {
                new TagDB(this.sqLiteDatabase).insertTag(tag);
            }
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(ConfigClass.TAG_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(ConfigClass.ConfigContent.ALREDY_LOADED_TAGS, true);
        editor.apply();
    }

    public ArrayList<TagManga> selectAllTags() {
        return new TagDB(this.sqLiteDatabase).selectAllTags();
    }

    public void setLastChapterManga(String idApiManga,String language,double chapter){
        new MangaDB(this.sqLiteDatabase).setLastChapterManga(idApiManga,language,chapter);
    }

    public int getAmountMangasSalved(){
        return new MangaDB(this.sqLiteDatabase).getAmountMangasSalved();
    }

    public boolean mangaTableHasChanges(){
        if(MANGA_TABLE_HAS_CHANGES){
            MANGA_TABLE_HAS_CHANGES = false;
            return true;
        }
        return false;

    }
    public boolean mangaUpdateTableHasChanges(){
        if(MANGA_UPDATES_TABLE_HAS_CHANGES){
            MANGA_UPDATES_TABLE_HAS_CHANGES = false;
            return true;
        }
        return false;

    }

}
