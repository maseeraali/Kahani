package com.example.maseera.kahani;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by maseera on 22/1/17.
 */

public class MovieDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " (" +
                MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL, " +
                MovieContract.MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieContract.MovieEntry.COLUMN_IMAGE + " TEXT, " +
                MovieContract.MovieEntry.COLUMN_IMAGE2 + " TEXT, " +
                MovieContract.MovieEntry.COLUMN_OVERVIEW + " TEXT, " +
                MovieContract.MovieEntry.COLUMN_RATING + " INTEGER, " +
                MovieContract.MovieEntry.COLUMN_DATE + " TEXT);";

        final String SQL_CREATE_POPULAR_TABLE = "CREATE TABLE " + MovieContract.PopularEntry.TABLE_NAME + " (" +
                MovieContract.PopularEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieContract.PopularEntry.COLUMN_DATE + " TEXT, " +
                MovieContract.PopularEntry.COLUMN_IMAGE2 + " TEXT, " +
                MovieContract.PopularEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL, " +
                MovieContract.PopularEntry.COLUMN_RATING + " INTEGER, " +
                MovieContract.PopularEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieContract.PopularEntry.COLUMN_OVERVIEW + " TEXT, " +
                MovieContract.PopularEntry.COLUMN_IMAGE + " TEXT);";

        final String SQL_CREATE_RATED_TABLE = "CREATE TABLE " + MovieContract.RatedEntry.TABLE_NAME + " (" +
                MovieContract.RatedEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieContract.RatedEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL, " +
                MovieContract.RatedEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieContract.RatedEntry.COLUMN_IMAGE + " TEXT, " +
                MovieContract.RatedEntry.COLUMN_IMAGE2 + " TEXT, " +
                MovieContract.RatedEntry.COLUMN_OVERVIEW + " TEXT, " +
                MovieContract.RatedEntry.COLUMN_RATING + " INTEGER, " +
                MovieContract.RatedEntry.COLUMN_DATE + " TEXT);";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_POPULAR_TABLE);
        db.execSQL(SQL_CREATE_RATED_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.PopularEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.RatedEntry.TABLE_NAME);
        onCreate(db);
    }
}
