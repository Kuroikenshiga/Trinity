package com.example.trinity.models;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;

import com.example.trinity.valueObject.ChapterManga;

import java.util.ArrayList;

public class ChaptersDB extends DBAcess{
    public ChaptersDB(SQLiteDatabase sqLiteDatabase) {
        super(sqLiteDatabase);
    }
    public long insertChapter(ChapterManga chapter, String idManga) {
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

    public long returnChapterId(String apiId) {
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

    public ArrayList<ChapterManga> selectAllChaptersByIdManga(String id) {
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
    public boolean deleteChapterFromManga(long id) {

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("DELETE FROM chapters WHERE manga_id = ?")) {
            stmt.bindLong(1, id);
            stmt.executeUpdateDelete();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }

        return true;
    }

    public boolean setChapterAlredyRead(long idChap) {

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
    boolean setIsdownloadedAsFalseForAll() {

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("UPDATE chapters set is_downloaded = false")) {
            stmt.executeUpdateDelete();
        } catch (SQLiteException ex) {

            ex.printStackTrace();
            return false;
        }

        return true;
    }
}
