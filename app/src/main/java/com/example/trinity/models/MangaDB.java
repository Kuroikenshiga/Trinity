package com.example.trinity.models;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;

import com.example.trinity.valueObject.Manga;

import java.util.ArrayList;

public class MangaDB extends DBAcess{

    public MangaDB(SQLiteDatabase sqLiteDatabase) {
        super(sqLiteDatabase);
    }

    public long insertManga(Manga m) {
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

    public long returnMangaId(String id, String language) {
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

    private ArrayList<Manga> selectAllMangas(String sql,String[] arguments,boolean loadChaptersToo) {
        ArrayList<Manga> mangaArrayList = new ArrayList<>();
        ChaptersDB chaptersDB = new ChaptersDB(this.sqLiteDatabase);
        AuthorDB authorDB = new AuthorDB(this.sqLiteDatabase);
        TagDB tagDB = new TagDB(this.sqLiteDatabase);
        try (Cursor row = this.sqLiteDatabase.rawQuery(sql, arguments)) {

            if (row.getCount() == 0) {

                return mangaArrayList;
            }

            int uuidIndex = row.getColumnIndex("id");
            while (row.moveToNext()) {

                String id = row.getString(2);
                String cover = row.getString(1);
                int indexLastChapter = row.getColumnIndex("last_chapter");
                String desc = row.getString(4);
                String title = row.getString(6);
                String language = row.getString(7);
                Manga m = new Manga(id, title, null, authorDB.selectAllAuthorByIdManga(row.getString(0)), desc, tagDB.selectAllTagMangasByIdManga(row.getString(0)), language, cover, (loadChaptersToo ? chaptersDB.selectAllChaptersByIdManga(row.getString(0)) : null));
                m.isAdded = true;
                m.uuid = row.getLong(uuidIndex);
                m.setLastChapter(row.getDouble(indexLastChapter));
                mangaArrayList.add(m);

            }
        }

        return mangaArrayList;
    }
    public int amountChaptersToRead(long idManga) {
        int returnValue = 0;

        try (Cursor row = this.sqLiteDatabase.rawQuery("SELECT *FROM chapters WHERE alredy_read = ? AND manga_id = ?", new String[]{"false", Long.toString(idManga)})) {
            returnValue = row.getCount();

        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return -1;
        }

        return returnValue;
    }
    public boolean deleteManga(long id) {

        try (SQLiteStatement stmt = this.sqLiteDatabase.compileStatement("DELETE FROM mangas WHERE id = ?")) {
            stmt.bindLong(1, id);
            stmt.executeUpdateDelete();
        } catch (SQLiteException ex) {
            ex.printStackTrace();

            return false;
        }

        return true;
    }

    boolean setLastChapterRead(String idApiChapter, String idApiManga, String language) {
        long idManga = this.returnMangaId(idApiManga, language);
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
        } catch (SQLiteException ex) {

            return "";
        }

        return lasChapterRead;
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
    public int getAmountMangasSalved(){
        try(Cursor row = this.sqLiteDatabase.rawQuery("SELECT COUNT(*) FROM mangas ",new String[]{})){
            row.moveToNext();
            return  row.getInt(0);
        }
    }

    public final class SelectClause{
        public ArrayList<Manga> AllMangas(boolean loadChaptersToo, int limit, int offSet){
            return selectAllMangas("SELECT * FROM mangas ORDER BY id DESC LIMIT ? OFFSET ?",
                    new String[]{Integer.toString(limit),Integer.toString(offSet)},loadChaptersToo);
        }
        public ArrayList<Manga> MangaByTitle(String mangaTitle, int limit){
            return selectAllMangas("SELECT * FROM mangas WHERE title LIKE ?  LIMIT ?",
                    new String[]{mangaTitle + "%",Integer.toString(limit)},true);
        }


    }
    public SelectClause select(){
        return new SelectClause();
    }
}
