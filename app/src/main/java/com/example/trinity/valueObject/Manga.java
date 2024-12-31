package com.example.trinity.valueObject;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;


import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class Manga implements Parcelable, Serializable {

    public String coverName;
    public long uuid;
    private String id;
    private String titulo;
    private Bitmap image;
    private String language;
    private ArrayList<String> autores;
    private String descricao;
    private ArrayList<TagManga> tags;
    private ArrayList<ChapterManga>chapters;
    private String idOfLastChapterOpen;
    private int amountChaptersToRead;
    public boolean isAdded = false;
    private double lastChapter;
    public static final double COMPLETED = -1;
    public boolean isVisible = true;
    public Manga(String id, String titulo, Bitmap image, ArrayList<String> autor, String descricao, ArrayList<TagManga> tags) {
        this.id = id;
        this.titulo = titulo;
        this.image = image;
        this.autores = autor;
        this.descricao = descricao;
        this.tags = tags;
        this.idOfLastChapterOpen = "";
    }

    public Manga(String id, String titulo, Bitmap image, ArrayList<String> autor, String descricao, ArrayList<TagManga> tags, String language) {

        this.id = id;
        this.titulo = titulo;
        this.image = image;
        this.autores = autor;
        this.descricao = descricao;
        this.tags = tags;
        this.language = language;
        this.idOfLastChapterOpen = "";
    }

    public Manga(String id, String titulo, Bitmap image, ArrayList<String> autor, String descricao, ArrayList<TagManga> tags, String language,String cover,ArrayList<ChapterManga> chapters) {

        this.id = id;
        this.titulo = titulo;
        this.image = image;
        this.autores = autor;
        this.descricao = descricao;
        this.tags = tags;
        this.language = language;
        this.coverName = cover;
        this.chapters = chapters;
        this.idOfLastChapterOpen = "";

    }
//    public Manga(String id, String titulo, Bitmap image, ArrayList<String> autor, String descricao, ArrayList<TagManga> tags, String language,String cover,ArrayList<ChapterManga> chapters,String idOfLastChapterOpen) {
//        this.id = id;
//        this.titulo = titulo;
//        this.image = image;
//        this.autores = autor;
//        this.descricao = descricao;
//        this.tags = tags;
//        this.language = language;
//        this.coverName = cover;
//        this.chapters = chapters;
//        this.idOfLastChapterOpen = idOfLastChapterOpen;
//    }

    public Manga() {
        this.chapters = new ArrayList<>();
        this.autores = new ArrayList<>();
        this.tags = new ArrayList<>();
    }


    protected Manga(Parcel in) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            coverName = in.readString();
            id = in.readString();
            titulo = in.readString();
//            image = in.readParcelable(Bitmap.class.getClassLoader());
            language = in.readString();
            autores = in.createStringArrayList();
            descricao = in.readString();
            tags = in.createTypedArrayList(TagManga.CREATOR);
            chapters = in.createTypedArrayList(ChapterManga.CREATOR);
            idOfLastChapterOpen = in.readString();
            isAdded = in.readBoolean();
            lastChapter = in.readDouble();
            uuid = in.readLong();
        }
    }

    public static final Creator<Manga> CREATOR = new Creator<Manga>() {
        @Override
        public Manga createFromParcel(Parcel in) {
            return new Manga(in);
        }

        @Override
        public Manga[] newArray(int size) {
            return new Manga[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public ArrayList<String> getAutor() {
        return autores;
    }

    public void setAutor(ArrayList<String> autor) {
        this.autores = autor;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public ArrayList<TagManga> getTags() {
        return tags;
    }

    public void setTags(ArrayList<TagManga> tags) {
        this.tags = tags;
    }

    public ArrayList<ChapterManga> getChapters() {
        return chapters;
    }

    public void setChapters(ArrayList<ChapterManga> chapters) {
        this.chapters = chapters;
    }

    public int getAmountChaptersToRead() {
        return amountChaptersToRead;
    }

    public Manga setAmountChaptersToRead(int amountChaptersToRead) {
        if(amountChaptersToRead < this.getAmountChaptersToRead() || this.getAmountChaptersToRead() == 0){
            this.amountChaptersToRead = amountChaptersToRead;
        }

        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            dest.writeString(coverName);
            dest.writeString(id);
            dest.writeString(titulo);
//            dest.writeParcelable(image, flags);
            dest.writeString(language);
            dest.writeStringList(autores);
            dest.writeString(descricao);
            dest.writeTypedList(tags);
            dest.writeTypedList(chapters);
            dest.writeString(idOfLastChapterOpen);
            dest.writeBoolean(isAdded);
            dest.writeDouble(lastChapter);
            dest.writeLong(uuid);
        }
    }

    public String getIdOfLastChapterOpen() {
        return idOfLastChapterOpen;
    }
    public void setIdOfLastChapterOpen(String idOfLastChapterOpen) {
        this.idOfLastChapterOpen = idOfLastChapterOpen;
    }

    public boolean isChapterAlredySaved(String apiChapterID){
        if(this.chapters == null){
            throw new RuntimeException("Propriedade chapters é nula");
        }
        for(ChapterManga c:this.chapters){
            if(c.getId().equals(apiChapterID)){
                return true;
            }
        }
        return false;
    }

    public double getLastChapter() {
        return lastChapter;
    }

    public Manga setLastChapter(double lastChapter) {
        this.lastChapter = lastChapter;
        return this;
    }
    public boolean isOngoing(){
        for(ChapterManga c:this.chapters){
            if(Double.parseDouble(c.getChapter()) == this.lastChapter){
                return true;
            }
        }
        return false;
    }
    public boolean isOngoing(ArrayList<ChapterManga> chapters){
        if(chapters == null){
            return true;
        }
        if(COMPLETED == lastChapter){
            return false;
        }
        for(ChapterManga c:chapters){
            if(Double.parseDouble(c.getChapter()) == this.lastChapter){
                return false;
            }
        }
        return true;
    }

    public String getCoverName() {
        return coverName;
    }

    public void setCoverName(String coverName) {
        this.coverName = coverName;
    }
}
