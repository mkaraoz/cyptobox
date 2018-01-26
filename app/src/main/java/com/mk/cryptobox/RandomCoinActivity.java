package com.mk.cryptobox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mk.cryptobox.db.DatabaseContract;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class RandomCoinActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Map<String, String>>
{
    private ImageView mCoinIcon;
    private TextView mCoinName, mLabelPrice, mLabelPercentageChange, mLabelMarketCap, mLabelTotalSupply, mLabelLastUpdated;
    private static final int MARKET_CAP_API_CALL_LOADER = 14;
    private static final String CMC_API_CALL_URL_EXTRA = "query";
    private Coin mCurrentCoin = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_container);

        setTitle("Crypto");

        mCoinIcon = findViewById(R.id.image_logo);
        mCoinName = findViewById(R.id.label_coin_name);
        mLabelPrice = findViewById(R.id.label_prize);
        mLabelPercentageChange = findViewById(R.id.label_percent);
        mLabelMarketCap = findViewById(R.id.label_market_cap);
        mLabelTotalSupply = findViewById(R.id.label_total_supply);
        mLabelLastUpdated = findViewById(R.id.label_last_updated);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View view)
            {
                mCurrentCoin = null;
                final ImageChangeTask ict = new ImageChangeTask();
                ict.execute();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<Map<String, String>> onCreateLoader(int id, final Bundle args)
    {
        return new AsyncTaskLoader<Map<String, String>>(this)
        {
            @Override
            protected void onStartLoading()
            {
                if (args == null)
                {
                    return;
                }

                forceLoad();
            }

            @Override
            public Map<String, String> loadInBackground()
            {
                String cmcApiUrlString = args.getString(CMC_API_CALL_URL_EXTRA);
                if (cmcApiUrlString == null || TextUtils.isEmpty(cmcApiUrlString))
                {
                    return null;
                }

                try
                {
                    URL cmcUrl = new URL(cmcApiUrlString);
                    Map<String, String> cmcApiResults = NetworkUtils.getResponseFromHttpUrl(cmcUrl);
                    return cmcApiResults;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Map<String, String>> loader, Map<String, String> map)
    {
        if (map == null)
        {
            mLabelPrice.setVisibility(View.INVISIBLE);
            mLabelPercentageChange.setVisibility(View.INVISIBLE);
            mLabelLastUpdated.setVisibility(View.INVISIBLE);
            mLabelMarketCap.setVisibility(View.INVISIBLE);
            mLabelTotalSupply.setVisibility(View.INVISIBLE);
        }
        else
        {
            SharedPreferences tempPrefs = getSharedPreferences("temp", MODE_PRIVATE);
            SharedPreferences.Editor editor = tempPrefs.edit();
            editor.putString(Constants.PRICE_USD, map.get(Constants.PRICE_USD));
            editor.putString(Constants.PERCENT_CHANGE_24, map.get(Constants.PERCENT_CHANGE_24));
            editor.putString(Constants.LAST_UPDATED, map.get(Constants.LAST_UPDATED));
            editor.putString(Constants.MARKET_CAP, map.get(Constants.MARKET_CAP));
            editor.putString(Constants.TOTAL_SUPPLY, map.get(Constants.TOTAL_SUPPLY));
            editor.putString(Constants.WAY, map.get(Constants.WAY));
            editor.commit();
        }
    }

    @Override
    public void onLoaderReset(Loader<Map<String, String>> loader)
    {
    }

    private void startLoader(Coin theCoin)
    {
        Bundle queryBundle = new Bundle();
        URL coinMarketCapApiUrl = NetworkUtils.buildUrl(theCoin);
        queryBundle.putString(CMC_API_CALL_URL_EXTRA, coinMarketCapApiUrl.toString());
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> cmcApiLoader = loaderManager.getLoader(MARKET_CAP_API_CALL_LOADER);
        if (cmcApiLoader == null)
        {
            loaderManager.initLoader(MARKET_CAP_API_CALL_LOADER, queryBundle, this);
        }
        else
        {
            loaderManager.restartLoader(MARKET_CAP_API_CALL_LOADER, queryBundle, this);
        }
    }

    class ImageChangeTask extends AsyncTask<Void, Coin, Void>
    {
        int mCoinCount = countCoins();
        private List<Coin> randomCoins;

        ImageChangeTask()
        {
        }

        public int countCoins()
        {
            Uri uri = Uri.withAppendedPath(DatabaseContract.CoinTable.CONTENT_URI, "count");
            Cursor countCursor = getContentResolver().query(uri, new String[]{"count(*) AS count"}, null, null, null);
            countCursor.moveToFirst();
            int count = countCursor.getInt(0);
            return count;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            SharedPreferences tempPrefs = getSharedPreferences("temp", MODE_PRIVATE);
            tempPrefs.edit().clear().commit();

            mLabelPrice.setVisibility(View.INVISIBLE);
            mLabelPercentageChange.setVisibility(View.INVISIBLE);
            mLabelLastUpdated.setVisibility(View.INVISIBLE);
            mLabelMarketCap.setVisibility(View.INVISIBLE);
            mLabelTotalSupply.setVisibility(View.INVISIBLE);

            int[] randomNumbers = new int[100];
            getRandomNumbers(randomNumbers);
            randomCoins = selectCoins(randomNumbers);

            Coin theCoin = randomCoins.get(randomCoins.size() - 1);
            mCurrentCoin = theCoin;

            startLoader(theCoin);
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            for (int i = 0; i < 100; i++)
            {
                Coin c = randomCoins.get(i);
                publishProgress(c);
                if (i == 98)
                {
                    sleep(450);
                }
                if (i == 97)
                {
                    sleep(300);
                }
                if (i == 96)
                {
                    sleep(160);
                }
                if (i == 95)
                {
                    sleep(80);
                }
                if (i == 94)
                {
                    sleep(80);
                }
                if (i == 93)
                {
                    sleep(80);
                }
                else if (i > 70 && i < 93)
                {
                    sleep(50);
                }
                else
                {
                    sleep(30);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);

            SharedPreferences tempPrefs = getSharedPreferences("temp", MODE_PRIVATE);
            String data = tempPrefs.getString(Constants.WAY, Constants.UP);
            if (data.equals(Constants.UP))
            {
                mLabelPercentageChange.setTextColor(getResources().getColor(R.color.md_green_500));
            }
            else
            {
                mLabelPercentageChange.setTextColor(getResources().getColor(R.color.md_red_500));
            }
            updateDetails(tempPrefs, Constants.PRICE_USD, mLabelPrice);
            updateDetails(tempPrefs, Constants.PERCENT_CHANGE_24, mLabelPercentageChange);
            updateDetails(tempPrefs, Constants.LAST_UPDATED, mLabelLastUpdated);
            updateDetails(tempPrefs, Constants.MARKET_CAP, mLabelMarketCap);
            updateDetails(tempPrefs, Constants.TOTAL_SUPPLY, mLabelTotalSupply);
        }

        private List<Coin> selectCoins(int[] randomNumbers)
        {
            List<Coin> allCoins = new ArrayList<>();
            Uri uri = Uri.withAppendedPath(DatabaseContract.CoinTable.CONTENT_URI, "coins");
            Cursor c = getContentResolver().query(uri, null, null, null, DatabaseContract.COLUMN_UID + "ASC");
            if (c.moveToFirst())
            {
                do
                {
                    String uid = c.getString(c.getColumnIndexOrThrow(DatabaseContract.COLUMN_UID));
                    String name = c.getString(c.getColumnIndexOrThrow(DatabaseContract.COLUMN_NAME));
                    String symbol = c.getString(c.getColumnIndexOrThrow(DatabaseContract.COLUMN_SYMBOL));
                    String image = c.getString(c.getColumnIndexOrThrow(DatabaseContract.COLUMN_IMAGE));

                    Coin coin = new Coin();
                    coin.setId(uid);
                    coin.setName(name);
                    coin.setSymbol(symbol);
                    coin.setImage(image);
                    allCoins.add(coin);

                } while (c.moveToNext());
            }

            List<Coin> randomCoins = new ArrayList<>();
            for (int i : randomNumbers)
                randomCoins.add(allCoins.get(i));

            return randomCoins;
        }

        private void getRandomNumbers(int[] randomNumbers)
        {
            int min = 0;
            int max = mCoinCount - 1;

            Set<Integer> set = new LinkedHashSet<>();
            while (set.size() < 100)
            {
                int number = ThreadLocalRandom.current().nextInt(min, max + 1);
                set.add(number);
            }

            int i = 0;
            for (int random : set)
            {
                randomNumbers[i] = random;
                i++;
            }
        }

        private void sleep(final int milis)
        {
            try
            {
                Thread.sleep(milis);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Coin... coins)
        {
            super.onProgressUpdate(coins);
            Coin c = coins[0];
            int resourceImage = RandomCoinActivity.this.getResources().getIdentifier(c.getImage(), "drawable", RandomCoinActivity.this.getPackageName());
            mCoinIcon.setImageResource(resourceImage);
            mCoinName.setText(c.getName());
            setTitle(c.getSymbol());
        }

        private void updateDetails(SharedPreferences tempPrefs, String spKey, TextView view)
        {
            String data = tempPrefs.getString(spKey, null);
            if (data != null)
            {
                view.setText(data);
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemThatWasClickedId = item.getItemId();
        if (itemThatWasClickedId == R.id.action_buy)
        {

            if (mCurrentCoin == null)
            {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://coinmarketcap.com"));
                startActivity(browserIntent);
            }
            else
            {
                URL marketURL = NetworkUtils.buildMarketUrl(mCurrentCoin);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(marketURL.toString()));
                startActivity(browserIntent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
