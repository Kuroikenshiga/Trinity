package com.example.trinity.models;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;

import com.example.trinity.valueObject.ChapterManga;

import java.util.ArrayList;

public class UpdatesDB extends DBAcess{
    public UpdatesDB(SQLiteDatabase sqLiteDatabase) {
        super(sqLiteDatabase);
    }

    public Long insertUpdate(long idchap) {
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

    public boolean deleteUpdates(long id) {

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("DELETE FROM updates WHERE chapter = ?")) {
            stmt.bindLong(1, id);
            stmt.executeUpdateDelete();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }

        return true;
    }

    public ArrayList<ChapterManga> selectFirst100Updates() {
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
}
