package com.example.trinity.valueObject;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ChapterUpdated implements Parcelable {

    private Manga manga;
    private ChapterManga chapterManga;

    public ChapterUpdated(Manga manga, ChapterManga chapterManga) {
        this.manga = manga;
        this.chapterManga = chapterManga;
    }

    protected ChapterUpdated(Parcel in) {
        manga = in.readParcelable(Manga.class.getClassLoader());
        chapterManga = in.readParcelable(ChapterManga.class.getClassLoader());
    }

    public static final Creator<ChapterUpdated> CREATOR = new Creator<ChapterUpdated>() {
        @Override
        public ChapterUpdated createFromParcel(Parcel in) {
            return new ChapterUpdated(in);
        }

        @Override
        public ChapterUpdated[] newArray(int size) {
            return new ChapterUpdated[size];
        }
    };

    public Manga getManga() {
        return manga;
    }

    public void setManga(Manga manga) {
        this.manga = manga;
    }

    public ChapterManga getChapterManga() {
        return chapterManga;
    }

    public void setChapterManga(ChapterManga chapterManga) {
        this.chapterManga = chapterManga;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeParcelable(manga, flags);
        dest.writeParcelable(chapterManga, flags);
    }
}
