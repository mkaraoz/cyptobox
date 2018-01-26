package com.mk.cryptobox.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by mk on 23.01.2018.
 */

public class DatabaseContract
{
    // Android-internal name of the Content Provider
    static final String CONTENT_AUTHORITY = "com.mk.coin.db.provider";

    // Base Content Uri
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Define table columns
    public static final String ID = BaseColumns._ID;
    public static final String COLUMN_UID = "uid";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SYMBOL = "symbol";
    public static final String COLUMN_IMAGE = "image";

    // projection that defines the columns that will be returned for each row
    static final String[] COIN_PROJECTION = new String[]{
                /*0*/ ID,
                /*1*/ COLUMN_UID,
                /*2*/ COLUMN_NAME,
                /*3*/ COLUMN_SYMBOL,
                /*4*/ COLUMN_IMAGE };

    //
    // Coin Table
    //
    public static class CoinTable implements BaseColumns
    {
        // Table name
        public static final String TABLE_NAME = "coin";

        // Content Uri
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        // Use MIME type
        public static final String COIN_LIST = "vnd.android.cursor.dir/vnd.com.mk.coin.db.provider.coins";
        public static final String SINGLE_COIN = "vnd.android.cursor.item/vnd.com.mk.coin.db.provider.coin";

    }
}
