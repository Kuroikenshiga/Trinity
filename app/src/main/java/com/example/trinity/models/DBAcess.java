package com.example.trinity.models;

import android.database.sqlite.SQLiteDatabase;

public abstract class DBAcess {
    protected SQLiteDatabase sqLiteDatabase;

    public DBAcess(SQLiteDatabase sqLiteDatabase) {
        this.sqLiteDatabase = sqLiteDatabase;
    }
}
