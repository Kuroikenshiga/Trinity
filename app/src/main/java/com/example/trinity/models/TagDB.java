package com.example.trinity.models;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;

import com.example.trinity.valueObject.TagManga;

import java.util.ArrayList;

public class TagDB extends DBAcess{


    public TagDB(SQLiteDatabase sqLiteDatabase) {
        super(sqLiteDatabase);
    }

    public long returnTagId(String id) {
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
    public long insertTag(TagManga t) {
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
    public long insertRelationTagManga(String idManga, String idTag) {
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
    public ArrayList<TagManga> selectAllTagMangasByIdManga(String id) {
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
    public ArrayList<TagManga> selectAllTags() {
        ArrayList<TagManga> tags = new ArrayList<>();


        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT*FROM tags  name_tag WHERE id_tag NOT LIKE \"%genre%\" ORDER BY name_tag", new String[]{})) {
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

    public boolean deleteTagFromManga(long id) {

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("DELETE FROM tag_mangas WHERE id_manga = ?")) {
            stmt.bindLong(1, id);
            stmt.executeUpdateDelete();

        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }

        return true;
    }
}
