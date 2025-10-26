package com.example.trinity.models;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;

public class AuthorDB extends DBAcess{
    public AuthorDB(SQLiteDatabase sqLiteDatabase) {
        super(sqLiteDatabase);
    }
    public long insertAuthor(String nameAuthor, String idManga) {
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

    public ArrayList<String> selectAllAuthorByIdManga(String id) {
        ArrayList<String> Authors = new ArrayList<>();

        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT * FROM authors WHERE manga_id = ?", new String[]{id})) {
            while (row.moveToNext()) {
                Authors.add(row.getString(1));
            }
        }

        return Authors;
    }

    public boolean deleteAuthorsFromManga(long id) {

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("DELETE FROM authors WHERE manga_id = ?")) {
            stmt.bindLong(1, id);
            stmt.executeUpdateDelete();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }

        return true;
    }

}
