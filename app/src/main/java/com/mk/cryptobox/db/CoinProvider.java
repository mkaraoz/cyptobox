package com.mk.cryptobox.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.mk.cryptobox.db.DatabaseContract.COIN_PROJECTION;
import static com.mk.cryptobox.db.DatabaseContract.COLUMN_SYMBOL;
import static com.mk.cryptobox.db.DatabaseContract.CONTENT_AUTHORITY;

/**
 * Created by mk on 23.01.2018.
 */

public class CoinProvider extends ContentProvider
{
    private CoinDbHelper mDbHelper;

    private static final int URI_ALL_COINS = 100;
    private static final int URI_SINGLE_COIN = 101;
    private static final int URI_COIN_COUNT = 102;

    private static final UriMatcher uriMatcher;

    static
    {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(CONTENT_AUTHORITY, DatabaseContract.CoinTable.TABLE_NAME + "/" + "coins", URI_ALL_COINS);
        uriMatcher.addURI(CONTENT_AUTHORITY, DatabaseContract.CoinTable.TABLE_NAME + "/" + "count", URI_COIN_COUNT);
        uriMatcher.addURI(CONTENT_AUTHORITY, DatabaseContract.CoinTable.TABLE_NAME + "/" + "*", URI_SINGLE_COIN);
    }

    @Override
    public boolean onCreate()
    {
        mDbHelper = new CoinDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder)
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int uriCode = uriMatcher.match(uri);
        switch (uriCode)
        {
            case URI_SINGLE_COIN:
            {
                SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
                projection = COIN_PROJECTION;
                selection = COLUMN_SYMBOL + " = ?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                sortOrder = null;
                queryBuilder.setTables(DatabaseContract.CoinTable.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case URI_ALL_COINS:
            {
                SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
                projection = COIN_PROJECTION;
                selection = null;
                selectionArgs = null;
                sortOrder = null;
                queryBuilder.setTables(DatabaseContract.CoinTable.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case URI_COIN_COUNT:
            {
                SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
                projection = new String[]{"count(*) AS count"};
                selection = null;
                selectionArgs = null;
                sortOrder = null;
                queryBuilder.setTables(DatabaseContract.CoinTable.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // Tell the cursor what uri to watch, so it knows when its source data changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri)
    {
        int uriCode = uriMatcher.match(uri);
        switch (uriCode)
        {
            case URI_SINGLE_COIN:
            {
                return DatabaseContract.CoinTable.SINGLE_COIN;
            }
            case URI_ALL_COINS:
            {
                return DatabaseContract.CoinTable.COIN_LIST;
            }
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues)
    {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String tableName = uri.getLastPathSegment();
        long rowId = db.insertOrThrow(tableName, null, contentValues);
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(DatabaseContract.BASE_CONTENT_URI.buildUpon().appendPath(tableName).build(), rowId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings)
    {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings)
    {
        return 0;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values)
    {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        int rowsInserted = 0;
        try
        {
            for (ContentValues value : values)
            {
                String tableName = uri.getLastPathSegment();
                long _id = db.insert(tableName, null, value);
                if (_id != -1)
                {
                    rowsInserted++;
                }
            }
            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
        }

        if (rowsInserted > 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsInserted;
    }
}
