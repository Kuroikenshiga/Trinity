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
    private ArrayList<OnMangaRemovedListener> observers = new ArrayList<>();
    private ArrayList<OnMangaAddedNotifier> notifiers = new ArrayList<>();
    private Model(@Nullable Context context) {
        super(context, dataBaseName, null, version);
        this.context = context;
        sqLiteDatabase = this.getWritableDatabase();

    }

    public static Model getInstance(Context context){
        if(instance == null){
            instance = new Model(context);
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

    private long returnTagId(String id) {
        long returnValue = -1;

        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT * FROM tags WHERE id_tag = ?", new String[]{id})) {

            if (row.getCount() < 1) {

                return returnValue;
            }

            row.moveToFirst();

            returnValue = row.getInt(0);
        }

        return returnValue;
    }

    private long insertTag(TagManga t) {
        long returnValue = 0;

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("INSERT INTO tags(id_tag,name_tag) VALUES(?,?)");) {
            stmt.bindString(1, t.getId());
            stmt.bindString(2, t.getNome());
            returnValue = stmt.executeInsert();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return -1;
        }

        return returnValue;

    }

    private long insertRelationTagManga(String idManga, String idTag) {
        long returnValue;

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("INSERT INTO tag_mangas(id_manga,id_tag) VALUES(?,?)")) {
            stmt.bindString(1, idManga);
            stmt.bindString(2, idTag);
            returnValue = stmt.executeInsert();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return -1;
        }

        return returnValue;
    }

    private long insertManga(Manga m) {
        long returnValue = -1;

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("INSERT INTO mangas(cover_name,id_manga,image,description,date_added,title,language,last_chapter)values(?,?,?,?,CURRENT_TIMESTAMP,?,?,?)");) {

            stmt.bindString(1, m.coverName);
            stmt.bindString(2, m.getId());
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            m.getImage().compress(Bitmap.CompressFormat.PNG, 100, out);
            stmt.bindBlob(3, new byte[]{});
            stmt.bindString(4, m.getDescricao());
            stmt.bindString(5, m.getTitulo());
            stmt.bindString(6, m.getLanguage());
            stmt.bindString(7,Double.toString(m.getLastChapter()));
            returnValue = stmt.executeInsert();
            stmt.close();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return -1;
        }

        return returnValue;
    }

    private long returnMangaId(String id, String language) {
        long returnValue = -1;

        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT * FROM mangas WHERE id_manga = ? AND language = ?", new String[]{id, language})) {

            if (row.getCount() < 1) {
                row.close();

                return returnValue;
            }
            row.moveToFirst();
            returnValue = row.getInt(0);
            row.close();
        }

        return returnValue;
    }

    private long insertAuthor(String nameAuthor, String idManga) {
        long returnValue;

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("INSERT INTO authors(name,manga_id) VALUES(?,?)");) {
            stmt.bindString(1, nameAuthor);
            stmt.bindString(2, idManga);
            returnValue = stmt.executeInsert();
            stmt.close();
        } catch (SQLiteException ex) {

            ex.printStackTrace();
            return -1;
        }

        return returnValue;

    }


    private long returnChapterId(String apiId) {
        long returnValue = -1;

        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT * FROM chapters WHERE id_chapter = ?", new String[]{apiId})) {

            if (row.getCount() < 1) {

                return -1;
            }
            row.moveToNext();
            returnValue = row.getLong(0);
        } catch (SQLiteException ex) {
            ex.printStackTrace();


            return -1;
        }

        return returnValue;
    }

    private long insertChapter(ChapterManga chapter, String idManga) {
        long returnValue = -1;

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("INSERT INTO chapters" +
                "(id_chapter,title,scan,date_RFC3339,manga_id,chapter,alredy_read) VALUES(?,?,?,?,?,?,?)")) {
            stmt.bindString(1, chapter.getId());
            stmt.bindString(2, chapter.getTitle());
            stmt.bindString(3, chapter.getScan());
            stmt.bindString(4, chapter.getDateRFC3339());
            stmt.bindString(5, idManga);
            stmt.bindString(6, chapter.getChapter());
            stmt.bindString(7, Boolean.toString(chapter.isAlredyRead()));
            returnValue = stmt.executeInsert();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return -1;
        }

        return returnValue;
    }

    public ArrayList<Manga> selectAllMangas(boolean loadChaptersToo) {
        ArrayList<Manga> mangaArrayList = new ArrayList<>();

        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT * FROM mangas ORDER BY id DESC", new String[]{});) {
//            System.out.println("quantidade de linhas: " + row.getCount());
            if (row.getCount() == 0) {

                return mangaArrayList;
            }

            int uuidIndex = row.getColumnIndex("id");
            while (row.moveToNext()) {

                String id = row.getString(2);
                String cover = row.getString(1);
//                int indexLogo = row.getColumnIndex("logo_path");
//                String imageByte = row.getString(indexLogo);
//                BitmapFactory.Options op = new BitmapFactory.Options();
//                op.inSampleSize = 2;
//                Bitmap image = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length,op);
                int indexLastChapter = row.getColumnIndex("last_chapter");
                String desc = row.getString(4);
                String title = row.getString(6);
                String language = row.getString(7);
                Manga m = new Manga(id, title, null, this.selectAllAuthorByIdManga(row.getString(0)), desc, selectAllTagMangasByIdManga(row.getString(0)), language, cover, (loadChaptersToo ? selectAllChaptersByIdManga(row.getString(0)) : null));
                m.isAdded = true;
                m.uuid = row.getLong(uuidIndex);
                m.setLastChapter(row.getDouble(indexLastChapter));
                mangaArrayList.add(m);

            }
        }

        return mangaArrayList;
    }

    private int amountChaptersToRead(long idManga) {
        int returnValue = 0;

        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT *FROM chapters WHERE alredy_read = ? AND manga_id = ?", new String[]{"false", Long.toString(idManga)})) {
            returnValue = row.getCount();

        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return -1;
        }

        return returnValue;
    }

    public int getAmountChaptersToRead(String idMangaApi, String language) {
        long id = this.returnMangaId(idMangaApi, language);
        if (id == -1) {
            return 0;
        }
        return this.amountChaptersToRead(id);
    }

    private ArrayList<Manga> selectAllMangas(String mangaTitle) {
        ArrayList<Manga> mangaArrayList = new ArrayList<>();

        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT * FROM mangas WHERE title LIKE ? ORDER BY id DESC", new String[]{"%" + mangaTitle + "%"});) {
//            System.out.println("quantidade de linhas: " + row.getCount());
            if (row.getCount() == 0) {

                return mangaArrayList;
            }
            while (row.moveToNext()) {

                String id = row.getString(2);
                String cover = row.getString(1);
//                String imageByte = row.getString(9);
//                Bitmap image = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                String desc = row.getString(4);
                String title = row.getString(6);
                String language = row.getString(7);
                mangaArrayList.add(new Manga(id, title, null, this.selectAllAuthorByIdManga(row.getString(0)), desc, selectAllTagMangasByIdManga(row.getString(0)), language, cover, selectAllChaptersByIdManga(row.getString(0))));

            }
        }

        return mangaArrayList;
    }

    private ArrayList<ChapterManga> selectAllChaptersByIdManga(String id) {
        ArrayList<ChapterManga> chapterMangaArrayList = new ArrayList<>();

        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT * FROM chapters WHERE manga_id = ?", new String[]{id})) {
            int mangaIdIndex = row.getColumnIndex("manga_id");
            while (row.moveToNext()) {
                String idChap = row.getString(1);
                String title = row.getString(2);
                String scan = row.getString(3);
                String RFC3339 = row.getString(4);
                String chapter = row.getString(6);

                boolean alredyRead = Boolean.parseBoolean(row.getString(7));
                int currentPage = row.getInt(8);

                int isDownloadedIndex = row.getColumnIndex("is_downloaded");
                boolean isDownloded = Boolean.parseBoolean(row.getString(isDownloadedIndex));
                ChapterManga chapterManga = new ChapterManga(idChap, title, chapter, scan, RFC3339, alredyRead, currentPage, isDownloded);
                chapterManga.mangaUUID = Long.parseLong(row.getString(mangaIdIndex));
                chapterMangaArrayList.add(chapterManga);
            }
        }

        return chapterMangaArrayList;
    }

    private ArrayList<String> selectAllAuthorByIdManga(String id) {
        ArrayList<String> Authors = new ArrayList<>();

        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT * FROM authors WHERE manga_id = ?", new String[]{id})) {
            while (row.moveToNext()) {
                Authors.add(row.getString(1));
            }
        }

        return Authors;
    }

    private ArrayList<TagManga> selectAllTagMangasByIdManga(String id) {
        ArrayList<TagManga> tagMangaArrayList = new ArrayList<>();

        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT tags.* FROM mangas " +
                "INNER JOIN tag_mangas ON mangas.id = tag_mangas.id_manga " +
                "INNER JOIN tags ON tag_mangas.id_tag = tags.id WHERE mangas.id= ?", new String[]{id});) {

            while (row.moveToNext()) {
                String idTag = row.getString(1);
                String name = row.getString(2);
                tagMangaArrayList.add(new TagManga(idTag, name));
            }
        }

        return tagMangaArrayList;
    }

    private Long insertUpdate(long idchap) {
        long returnValue = -1;

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("INSERT INTO updates(chapter) VALUES(?)")) {
            stmt.bindLong(1, idchap);
            returnValue = stmt.executeInsert();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return (long) -1;
        }

        return returnValue;
    }

    private boolean deleteTagFromManga(long id) {

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("DELETE FROM tag_mangas WHERE id_manga = ?")) {
            stmt.bindLong(1, id);
            stmt.executeUpdateDelete();

        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }

        return true;
    }

    private boolean deleteAuthorsFromManga(long id) {

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("DELETE FROM authors WHERE manga_id = ?")) {
            stmt.bindLong(1, id);
            stmt.executeUpdateDelete();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }

        return true;
    }

    private boolean deleteUpdates(long id) {

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("DELETE FROM updates WHERE chapter = ?")) {
            stmt.bindLong(1, id);
            stmt.executeUpdateDelete();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }

        return true;
    }

    private boolean deleteReadingHistoricFromManga(long id) {

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("DELETE FROM reading_historic WHERE manga_id = ?")) {
            stmt.bindLong(1, id);
            stmt.executeUpdateDelete();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }

        return true;
    }

    private boolean deleteChapterFromManga(long id) {

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("DELETE FROM chapters WHERE manga_id = ?")) {
            stmt.bindLong(1, id);
            stmt.executeUpdateDelete();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }

        return true;
    }

    private boolean deleteManga(long id) {

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("DELETE FROM mangas WHERE id = ?")) {
            stmt.bindLong(1, id);
            stmt.executeUpdateDelete();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }

        return true;
    }

    private boolean insertHistory(long idManga, long currentTime) {

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("INSERT INTO reading_historic(manga_id,last_acessed) values(?,?)")) {
            stmt.bindLong(1, idManga);
            stmt.bindLong(2, currentTime);
            long returnID = stmt.executeInsert();
            if (returnID == -1) {

                return false;
            }
        } catch (SQLiteException ex) {

            ex.printStackTrace();
            return false;
        }

        return true;
    }

    private long returnHistoryId(long idManga) {
        long returnValue = -1;

        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT*FROM reading_historic WHERE manga_id = ?", new String[]{Long.toString(idManga)})) {
            if (row.getCount() == 0) {

                return -1;
            }
            row.moveToNext();
            returnValue = row.getLong(0);
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return -1;
        }

        return returnValue;
    }

    private boolean updateHistory(long idHistory, long currentTime) {

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("UPDATE reading_historic SET last_acessed=? WHERE id=?")) {
            stmt.bindLong(1, currentTime);
            stmt.bindLong(2, idHistory);
            stmt.executeUpdateDelete();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }

        return true;
    }

    public boolean addOrUpdateReadingHitory(History history) {
        long idManga = this.returnMangaId(history.getManga().getId(), history.getManga().getLanguage());
        if (idManga == -1) return false;
        long idHistory = this.returnHistoryId(idManga);

        if (idHistory == -1) {
            return this.insertHistory(idManga, history.getLastAcess());
        } else {

            return this.updateHistory(idHistory, history.getLastAcess());
        }

    }

    public ArrayList<History> selectAllHistory() {
        ArrayList<History> histories = new ArrayList<>();
        /*
        * "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "cover_name VARCHAR(50) NOT NULL," +
                "id_manga VARCHAR(50) NOT NULL," +
                "image BLOB NOT NULL," +
                "description VARCHAR(500)," +
                "date_added TEXT NOT NULL," +
                "title VARCHAR(50) NOT NULL," +
                "language VARCHAR(5) NOT NULL,"+
                "last_chapter_read TEXT)");
        * */

        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT*FROM mangas " +
                "INNER JOIN reading_historic on manga_id = mangas.id ORDER BY last_acessed DESC LIMIT 30", new String[]{})) {
            while (row.moveToNext()) {
                int coverNameColumn = row.getColumnIndex("cover_name");
                int id_mangaColumn = row.getColumnIndex("id_manga");
                int imageColumn = row.getColumnIndex("logo_path");
                int descriptionColumn = row.getColumnIndex("description");
                int titleColumn = row.getColumnIndex("title");
                int languageColumn = row.getColumnIndex("language");
                int time = row.getColumnIndex("last_acessed");
                int indexLastChapter = row.getColumnIndex("last_chapter");

                String cover = row.getString(coverNameColumn);
                String idManga = row.getString(id_mangaColumn);
//                String imageByte = row.getString(imageColumn);
//                Bitmap image = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                String description = row.getString(descriptionColumn);
                String title = row.getString(titleColumn);
                String language = row.getString(languageColumn);

                long lastAcess = row.getLong(time);

                Manga m = new Manga(idManga, title, null, this.selectAllAuthorByIdManga(row.getString(0)), description, this.selectAllTagMangasByIdManga(row.getString(0)), language, cover, null);
                m.isAdded = true;
                m.setLastChapter(row.getDouble(indexLastChapter));
                History h = new History(m, lastAcess);

                histories.add(h);
            }

            return histories;
        }
    }

    private boolean setChapterAlredyRead(long idChap) {

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("UPDATE chapters SET alredy_read=? WHERE id = ?")) {
            stmt.bindString(1, "true");
            stmt.bindLong(2, idChap);
            stmt.executeUpdateDelete();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }

        return true;
    }

    public ArrayList<Manga> loadSearch(String mangaTitle, LibraryFragment libraryFragment) {
        ArrayList<Manga> mangaArrayList = new ArrayList<>();

        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT * FROM mangas WHERE title LIKE ? ORDER BY id DESC", new String[]{"%" + mangaTitle + "%"});) {
//            System.out.println("quantidade de linhas: " + row.getCount());
            if (row.getCount() == 0) {

                return mangaArrayList;
            }

            while (row.moveToNext()) {
                int indexLastChapter = row.getColumnIndex("last_chapter");

                String id = row.getString(2);
                String cover = row.getString(1);
//                String imageByte = row.getString(9);
//                Bitmap image = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                String desc = row.getString(4);
                String title = row.getString(6);
                String language = row.getString(7);
                Manga m = new Manga(id, title, null, this.selectAllAuthorByIdManga(row.getString(0)), desc, selectAllTagMangasByIdManga(row.getString(0)), language, cover, selectAllChaptersByIdManga(row.getString(0)));
                m.isAdded = true;
                m.setLastChapter(row.getDouble(indexLastChapter));
                mangaArrayList.add(m);

            }
        }
        MainActivity mainActivity = (MainActivity) context;

//        if (libraryFragment != null) {
//            mainActivity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    libraryFragment.searchResult(mangaArrayList);
//                }
//            });
//        }

        return mangaArrayList;


    }

    public boolean addInFavorites(Manga m) {

        long mangaID = -1;


        for (TagManga tg : m.getTags()) {
            if (this.returnTagId(tg.getId()) == -1) {
                if (this.insertTag(tg) == -1) {
                    if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                    return false;
                }
            }
        }
        if (this.returnMangaId(m.getId(), m.getLanguage()) != -1) {
            if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.setTransactionSuccessful();

            return false;
        }

        mangaID = this.insertManga(m);
        String mangaIDStringFormat = Long.toString(mangaID);
        if (mangaID == -1) {
            if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

            return false;
        }
        for (String s : m.getAutor()) {
            if (this.insertAuthor(s, mangaIDStringFormat) == -1) {
                if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                return false;
            }
        }
        for (TagManga t : m.getTags()) {
            if (insertRelationTagManga(mangaIDStringFormat, Long.toString(this.returnTagId(t.getId()))) == -1) {
                if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                return false;
            }
        }
        for (ChapterManga chap : m.getChapters()) {
            if (returnChapterId(chap.getId()) == -1) {
                if (insertChapter(chap, mangaIDStringFormat) == -1) {
                    if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                    return false;
                }
            }
        }
        if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.setTransactionSuccessful();
        ((Activity)context).runOnUiThread(()->{
            for(OnMangaAddedNotifier notifier:notifiers){
                notifier.someMangaAdded();
            }
        });

        return true;
    }

    public boolean removeFromFavorites(Manga manga) {
        long id = -1;

        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT * FROM mangas WHERE id_manga = ? AND language = ?", new String[]{manga.getId(), manga.getLanguage()})) {
            if (row.getCount() < 1) {

                return false;
            }
            row.moveToNext();
            id = row.getLong(0);
            this.sqLiteDatabase.beginTransaction();

            for (ChapterManga chap : manga.getChapters()) {
                if (!deleteUpdates(returnChapterId(chap.getId()))) {
                    if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                    return false;
                }
            }
            if (!deleteReadingHistoricFromManga(id)) {
                if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                return false;
            }
            if (!deleteAuthorsFromManga(id)) {
                if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                return false;
            }
            if (!deleteTagFromManga(id)) {
                if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                return false;
            }
            if (!deleteChapterFromManga(id)) {
                if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                return false;
            }
            if (!deleteManga(id)) {
                if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.endTransaction();

                return false;
            }
            if (this.sqLiteDatabase.inTransaction()) this.sqLiteDatabase.setTransactionSuccessful();
            this.sqLiteDatabase.endTransaction();

        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }
        ((Activity)context).runOnUiThread(()->{
            for(OnMangaRemovedListener listener:observers){
                listener.onMangaRemoved(manga.getLanguage(),manga.getId());
            }
        });
        return true;
    }

    public boolean mangaAlredFavorited(Manga m) {

        if (this.returnMangaId(m.getId(), m.getLanguage()) != -1) {
            return true;
        }
        return false;
    }

    public boolean addNewChapter(ChapterUpdated chapterUpdated) {

        long mangaID = this.returnMangaId(chapterUpdated.getManga().getId(), chapterUpdated.getManga().getLanguage());
        if (mangaID == -1) {

            return false;
        }


        sqLiteDatabase.beginTransaction();

        long chapId = this.insertChapter(chapterUpdated.getChapterManga(), Long.toString(mangaID));
        if (chapId == -1) {
            sqLiteDatabase.endTransaction();

            return false;
        }
        if (this.insertUpdate(chapId) == -1) {
            sqLiteDatabase.endTransaction();

            return false;
        }
        sqLiteDatabase.setTransactionSuccessful();
        sqLiteDatabase.endTransaction();

        return true;
    }

    public ArrayList<ChapterManga> loadUpdates() {
        ArrayList<ChapterManga> updateds = new ArrayList<>();


        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT * FROM updates " +
                "INNER JOIN chapters ON updates.chapter = chapters.id ORDER BY STRFTIME('%s',date_RFC3339) DESC LIMIT 100", new String[]{})) {

            int mangaUuidIndex = row.getColumnIndex("manga_id");
            while (row.moveToNext()) {
                String idChap = row.getString(3);
                String title = row.getString(4);
                String scan = row.getString(5);
                String RFC3339 = row.getString(6);
                String chapter = row.getString(8);
                boolean alredyRead = Boolean.parseBoolean(row.getString(9));
                int currentPage = row.getInt(10);

                int isDownloadedIndex = row.getColumnIndex("is_downloaded");
                boolean isDownloded = Boolean.parseBoolean(row.getString(isDownloadedIndex));
                ChapterManga c = new ChapterManga(idChap, title, chapter, scan, RFC3339, alredyRead, currentPage, isDownloded);
                c.mangaUUID = row.getLong(mangaUuidIndex);
                updateds.add(c);
            }
        }
        return updateds;
    }

    public boolean setChapterLastPage(String idApiChapter, int currentPage) {
        long chapId = this.returnChapterId(idApiChapter);

        if (chapId == -1) {

            return false;
        }
        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("UPDATE chapters SET currentPage=? WHERE id = ?")) {
            stmt.bindString(1, Integer.toString(currentPage));
            stmt.bindLong(2, chapId);
            stmt.executeUpdateDelete();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }

        return true;
    }

    public boolean chapterRead(ChapterManga ch) {
        long chapId = this.returnChapterId(ch.getId());
        if (chapId == -1) {
            return false;
        }
        if (!this.setChapterAlredyRead(chapId)) {
            return false;
        }
        return true;
    }

    public boolean isChapterAlredyRead(String chap) {

        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT*FROM chapters WHERE id_chapter = ?", new String[]{chap})) {
            if (row.getCount() < 1) {

                throw new RuntimeException("Capítulo não encontrado");
            }
            row.moveToNext();
            if (Boolean.parseBoolean(row.getString(7))) {

                return true;
            }
        }

        return false;
    }

    public ArrayList<ChapterManga> getAllChapterByMangaID(String apiID, String mangaLanguage) {
        long id = this.returnMangaId(apiID, mangaLanguage);
        if (id == -1) {
            return new ArrayList<ChapterManga>();
        }
        return this.selectAllChaptersByIdManga(Long.toString(id));
    }

    public boolean setLastChapterRead(String idApiChapter, String idApiManga, String language) {
        long idManga = this.returnMangaId(idApiManga, language);
//        System.out.println(idApiManga);
        if (idManga == -1) {
            return false;
        }

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("UPDATE mangas SET last_chapter_read = ? WHERE id = ?")) {
            stmt.bindString(1, idApiChapter);
            stmt.bindLong(2, idManga);
            stmt.executeUpdateDelete();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }

        return true;
    }

    public String getIdApiOfLastChapterRead(String idMangaApi, String language) {
        long idManga = this.returnMangaId(idMangaApi, language);

        String lasChapterRead = "";
        if (idManga == -1) {
            return "";
        }
        try (Cursor row = sqLiteDatabase.rawQuery("SELECT * FROM mangas WHERE id = ?", new String[]{Long.toString(idManga)})) {

            if (row.getCount() < 1) {

                return "";
            }
            row.moveToNext();
            int rowColumn = row.getColumnIndex("last_chapter_read");
            if (rowColumn == -1) {

                return "";
            }
            lasChapterRead = row.getString(rowColumn) == null ? "" : row.getString(rowColumn);
//            System.out.println("row column: " + lasChapterRead);
        } catch (SQLiteException ex) {

            return "";
        }

        return lasChapterRead;
    }

    public long getMangaCount() {

        long retunsValue = 0;
        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("SELECT COUNT(*) FROM mangas")) {
            retunsValue = stmt.simpleQueryForLong();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return 0;
        }

        return retunsValue;
    }

    private long insertChapterPage(long idChapter, String absolutePath, int numPage) {
        long returnvalue = -1;

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("INSERT INTO chapterPage(file_name,chapter,page_number) values(?,?,?)")) {
            stmt.bindString(1, absolutePath);
            stmt.bindLong(2, idChapter);
            stmt.bindString(3, Integer.toString(numPage));
            returnvalue = stmt.executeInsert();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return -1;
        }

        return returnvalue;
    }

    public boolean setChapterDownloaded(long id, boolean isDownloaded) {
        boolean returnValue = false;

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("UPDATE chapters set is_downloaded = ? WHERE id = ?")) {
            stmt.bindString(1, Boolean.toString(isDownloaded));
            stmt.bindLong(2, id);
            returnValue = stmt.executeUpdateDelete() > 0;
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }

        return returnValue;
    }

    public boolean savePage(String idChapterApi, String path, int pageNumber) {
        long chapter = returnChapterId(idChapterApi);

        if (chapter == -1) {

            return false;
        }
        this.sqLiteDatabase.beginTransaction();

        if (insertChapterPage(chapter, path, pageNumber) == -1) {
            this.sqLiteDatabase.endTransaction();


            return false;
        }
        if (!setChapterDownloaded(chapter, true)) {
            this.sqLiteDatabase.endTransaction();

            return false;
        }
        this.sqLiteDatabase.setTransactionSuccessful();
        this.sqLiteDatabase.endTransaction();

        return true;
    }

    public ContentValues[] getPagesDownloaded(String chapApiID) {
        long id = this.returnChapterId(chapApiID);

        ContentValues[] values = null;
        if (id == -1) {

            return null;
        }
        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT * FROM chapterPage WHERE chapter = ?", new String[]{Long.toString(id)})) {
            if (row.getCount() < 1) return null;
            values = new ContentValues[row.getCount()];
            int index = 0;
            while (row.moveToNext()) {
                int fileNameIndex = row.getColumnIndex("file_name");
                int pageIndex = row.getColumnIndex("page_number");

                ContentValues value = new ContentValues();
                value.put("path", row.getString(fileNameIndex));
                value.put("page", row.getInt(pageIndex));

                values[index] = value;
                index++;
            }

        }

        return values;
    }

    public int getNumberPagesDownloaded(String chapApiID) {
        long id = this.returnChapterId(chapApiID);
        int returnValue = 0;

        if (id == -1) return 0;
        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT * FROM chapterPage WHERE chapter = ?", new String[]{Long.toString(id)})) {
            if (row.getCount() < 1) {

                return 0;
            }
            returnValue = row.getCount();
        }

        return returnValue;
    }

    private boolean deleteChapterPages() {

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("DELETE FROM chapterPage")) {
            stmt.executeUpdateDelete();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }

        return true;
    }

    private boolean setIsdownloadedAsFalse() {

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("UPDATE chapters set is_downloaded = false")) {
            stmt.executeUpdateDelete();
        } catch (SQLiteException ex) {

            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean deleteAllpagesDownloaded() {

        this.sqLiteDatabase.beginTransaction();
        if (!setIsdownloadedAsFalse()) {
            this.sqLiteDatabase.endTransaction();

            return false;
        }
        if (!deleteChapterPages()) {
            this.sqLiteDatabase.endTransaction();

            return false;
        }
        this.sqLiteDatabase.setTransactionSuccessful();
        this.sqLiteDatabase.endTransaction();

        return true;
    }

    //    public boolean deleteAllcap() {
//        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("DELETE FROM chapters ")) {
//
//            stmt.executeUpdateDelete();
//        } catch (SQLiteException ex) {
//            ex.printStackTrace();
//            return false;
//        }
//        return true;
//    }
    public void saveTag(ArrayList<TagManga> tags) {
        if (tags.isEmpty()) {
            return;
        }
        for (TagManga tag : tags) {
            if (returnTagId(tag.getId()) == -1) {
                insertTag(tag);
            }
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(ConfigClass.TAG_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(ConfigClass.ConfigContent.ALREDY_LOADED_TAGS, true);
        editor.apply();
    }

    public ArrayList<TagManga> selectAllTags() {
        ArrayList<TagManga> tags = new ArrayList<>();


        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT*FROM tags ORDER BY name_tag ASC", new String[]{})) {
            if (row.getCount() == 0) {

                return tags;
            }
            while (row.moveToNext()) {
                int idIndex = row.getColumnIndex("id_tag");
                int nameIndex = row.getColumnIndex("name_tag");
                tags.add(new TagManga(row.getString(idIndex), row.getString(nameIndex)));
            }
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return tags;
        }

        return tags;
    }

//    private boolean insertLocalStorageOfLogos(long id, String logoPath) {
//        if (logoPath.isEmpty()) return false;
//        if (id == -1) return false;
//        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("UPDATE mangas set logo_path = ? WHERE id = ?")) {
//            stmt.bindString(1, logoPath);
//            stmt.bindLong(2, id);
//            return stmt.executeUpdateDelete() > 0;
//        } catch (SQLiteException ex) {
//            ex.printStackTrace();
//            return false;
//        }
//    }
    public void removeImagesFromDataBase(){
        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("UPDATE mangas set image = ?")) {
            stmt.bindBlob(1,new byte[]{});
            long numRows = stmt.executeUpdateDelete();
            if(context instanceof MainActivity){
                ((MainActivity)(context)).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,numRows+" imagens migradas com sucesso",Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (SQLiteException ex) {
            ex.printStackTrace();

        }
    }
    public void doUpdateLogos() {
        try (Cursor row = sqLiteDatabase.rawQuery("SELECT * FROM mangas", new String[]{})) {
            if (row.getCount() == 0) return;
            int id = row.getColumnIndex("id");
            int image = row.getColumnIndex("image");
            int idApi = row.getColumnIndex("id_manga");
            int language = row.getColumnIndex("language");
            LogoMangaStorage storage = new LogoMangaStorage(context);
            storage.createIfNotExistsFolderForLogos();
            while (row.moveToNext()) {
                byte[] imageByte = row.getBlob(image);
                Bitmap imageLogo = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                if(imageLogo != null && imageByte.length != 0){
                    storage.insertLogoManga(imageLogo, row.getString(idApi));
                }

            }
            this.removeImagesFromDataBase();
            SharedPreferences sharedPreferences = context.getSharedPreferences(ConfigClass.TAG_PREFERENCE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(ConfigClass.ConfigLogoMigration.ALREDY_MIGRATED, true);
            editor.apply();
        }
    }
    public void setLastChapterManga(String idApiManga,String language,double chapter){
        long id = this.returnMangaId(idApiManga,language);
        if(id == -1)return;
        try(SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("UPDATE mangas set last_chapter = ? WHERE id = ?")){
            stmt.bindString(1,Double.toString(chapter));
            stmt.bindLong(2,id);
            stmt.executeUpdateDelete();
        }catch (SQLiteException ex){
            ex.printStackTrace();
        }
    }
    public ArrayList<Boolean> verifyReadStatus(String idApiManga,String language){
        long id = returnMangaId(idApiManga,language);
        if(id == -1){
            return new ArrayList<Boolean>();
        }
        ArrayList <Boolean>response = new ArrayList<>();

        try(Cursor row = this.sqLiteDatabase.rawQuery("SELECT * FROM chapters Where id = ?",new String[]{Long.toString(id)})){
            int index = 0;
            while(row.moveToNext()){
                int column = row.getColumnIndex("alredy_read");
//                int ex = row.getColumnIndex("chapter");
//                System.out.println(row.getString(0));
                response.add(Boolean.parseBoolean(row.getString(column)));
                index++;
            }
        }
        return response;
    }

    public static interface OnMangaRemovedListener{
        public static final int LIBRARY_OWNER = 0;
        int getOwner();
        void onMangaRemoved(String language,String idAPI);
    }
    public static interface OnMangaAddedNotifier{
        public static final int LIBRARY_OWNER = 0;
        int getOwner();
        void someMangaAdded();
    }
    public void addNotifier(OnMangaAddedNotifier notifier){
        if(notifiers.stream().anyMatch(notifier1 -> notifier1.getOwner() == OnMangaAddedNotifier.LIBRARY_OWNER))return;
        this.notifiers.add(notifier);
    }
    public void addOnMangaRemovedListener(OnMangaRemovedListener listener){
        if(observers.stream().anyMatch(observer -> observer.getOwner() == OnMangaRemovedListener.LIBRARY_OWNER))return;
        this.observers.add(listener);
    }
    public int getObserversSize(){return this.observers.size();}
    public void removeOnMangaRemovedListener(OnMangaRemovedListener listener){
        this.observers.remove(listener);
    }

//    public void removeLastChapter(){
//
//        try(SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("UPDATE mangas set last_chapter = ?")){
//            stmt.bindString(1,"0");
//            stmt.executeUpdateDelete();
//        }catch (SQLiteException ex){
//            ex.printStackTrace();
//        }
//    }

}
