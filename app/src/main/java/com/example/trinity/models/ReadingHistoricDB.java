package com.example.trinity.models;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;

import com.example.trinity.valueObject.History;
import com.example.trinity.valueObject.Manga;

import java.util.ArrayList;

public class ReadingHistoricDB extends TagDB{
    public ReadingHistoricDB(SQLiteDatabase sqLiteDatabase) {
        super(sqLiteDatabase);
    }

    public boolean deleteReadingHistoricFromManga(long id) {

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("DELETE FROM reading_historic WHERE manga_id = ?")) {
            stmt.bindLong(1, id);
            stmt.executeUpdateDelete();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }

        return true;
    }

    public boolean insertHistory(long idManga, long currentTime) {

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
    public long returnHistoryId(long idManga) {
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
    public boolean updateHistory(long idHistory, long currentTime) {

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
    public ArrayList<History> selectAllHistory() {
        ArrayList<History> histories = new ArrayList<>();
        AuthorDB authorDB = new AuthorDB(this.sqLiteDatabase);
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

                Manga m = new Manga(idManga, title, null, authorDB.selectAllAuthorByIdManga(row.getString(0)), description, this.selectAllTagMangasByIdManga(row.getString(0)), language, cover, null);
                m.isAdded = true;
                m.setLastChapter(row.getDouble(indexLastChapter));
                History h = new History(m, lastAcess);

                histories.add(h);
            }

            return histories;
        }
    }
}
