package com.example.trinity.valueObject;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class TagManga implements Parcelable {

    private String id;
    private String nome;

    public TagManga(String id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public TagManga() {

    }

    protected TagManga(Parcel in) {
        id = in.readString();
        nome = in.readString();
    }

    public static final Creator<TagManga> CREATOR = new Creator<TagManga>() {
        @Override
        public TagManga createFromParcel(Parcel in) {
            return new TagManga(in);
        }

        @Override
        public TagManga[] newArray(int size) {
            return new TagManga[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return "TagManga{" +
                "id='" + id + '\'' +
                ", nome='" + nome + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nome);
    }
}
