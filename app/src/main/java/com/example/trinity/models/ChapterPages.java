package com.example.trinity.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;

public class ChapterPages extends DBAcess{
    public ChapterPages(SQLiteDatabase sqLiteDatabase) {
        super(sqLiteDatabase);
    }
    public long insertChapterPage(long idChapter, String absolutePath, int numPage) {
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
    public ContentValues[] getPagesDownloaded(String chapApiID) {
        long id = new ChaptersDB(this.sqLiteDatabase).returnChapterId(chapApiID);

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
        long id = new ChaptersDB(this.sqLiteDatabase).returnChapterId(chapApiID);
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
    public boolean deleteChapterPages() {

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("DELETE FROM chapterPage")) {
            stmt.executeUpdateDelete();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }

        return true;
    }
}
