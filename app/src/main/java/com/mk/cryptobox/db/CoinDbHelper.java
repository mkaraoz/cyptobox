package com.mk.cryptobox.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by mk on 23.01.2018.
 */

public class CoinDbHelper extends SQLiteOpenHelper
{
    private final static String LOG_TAG = CoinDbHelper.class.getSimpleName();
    private final static String CREATE_ENTRY_TABLE_SQL = "CREATE TABLE "
            + DatabaseContract.CoinTable.TABLE_NAME + " ("
            + DatabaseContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            + DatabaseContract.COLUMN_UID + " TEXT, "
            + DatabaseContract.COLUMN_NAME + " TEXT, "
            + DatabaseContract.COLUMN_SYMBOL + " TEXT, "
            + DatabaseContract.COLUMN_IMAGE + " TEXT"
            + ");";
    private final static String DB_NAME = "coindb";
    private final static int DB_VERSION = 1;

    CoinDbHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE_ENTRY_TABLE_SQL);
        Log.i(LOG_TAG, "Creating table with query: " + CREATE_ENTRY_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {
    }
}
