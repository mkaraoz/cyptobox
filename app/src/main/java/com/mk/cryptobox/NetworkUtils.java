package com.mk.cryptobox;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by mk on 23.01.2018.
 */

class NetworkUtils
{
    //https://api.coinmarketcap.com/v1/ticker/bitcoin/
    final static String CMC_BASE_API_URL = "https://api.coinmarketcap.com/v1";
    final static String CMC_BASE_WEB_URL = "https://www.coinmarketcap.com";
    final static String TICKER_PATH = "ticker";

    public static URL buildUrl(Coin theCoin)
    {
        Uri builtUri = Uri.parse(CMC_BASE_API_URL).buildUpon().appendPath(TICKER_PATH).appendPath(theCoin.getId()).build();

        URL url = null;
        try
        {
            url = new URL(builtUri.toString());
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return url;
    }

    public static URL buildMarketUrl(Coin theCoin)
    {
        final String CURRENCY = "currencies";
        final String MARKETS = "#markets";

        Uri builtUri = Uri.parse(CMC_BASE_WEB_URL).buildUpon().appendPath(CURRENCY).appendPath(theCoin.getId()).appendEncodedPath(MARKETS).build();

        URL url = null;
        try
        {
            url = new URL(builtUri.toString());
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return url;
    }

    public static Map<String, String> getResponseFromHttpUrl(URL cmcUrl)
    {
        InputStreamReader reader = null;
        try
        {
            reader = new InputStreamReader(cmcUrl.openStream());
            JsonArray jArray = new Gson().fromJson(reader, JsonArray.class);
            JsonObject jObj = jArray.get(0).getAsJsonObject();
            JsonElement price_usd = jObj.get(Constants.PRICE_USD);
            JsonElement percent_change_24h = jObj.get(Constants.PERCENT_CHANGE_24);
            JsonElement last_updated = jObj.get(Constants.LAST_UPDATED);
            JsonElement market_cap_usd = jObj.get(Constants.MARKET_CAP);
            JsonElement total_supply = jObj.get(Constants.TOTAL_SUPPLY);
            Map<String, String> map = new HashMap<>();

            String priceUsd = price_usd.toString();
            if (priceUsd != null && !priceUsd.equals("null"))
            {
                priceUsd = priceUsd.replace("\"", "");
                map.put(Constants.PRICE_USD, "$" + priceUsd.replace("\"", "") + " USD");
            }
            else
            {
                map.put(Constants.PRICE_USD, null);
            }

            String percentChange24h = percent_change_24h.toString();
            if (percentChange24h != null && !percentChange24h.equals("null"))
            {
                percentChange24h = percentChange24h.replace("\"", "");
                double change = Double.parseDouble(percentChange24h);
                map.put(Constants.PERCENT_CHANGE_24, "(" + percentChange24h + "%)");
                if (change > 0)
                {
                    map.put(Constants.WAY, Constants.UP);
                }
                else
                {
                    map.put(Constants.WAY, Constants.DOWN);
                }
            }
            else
            {
                map.put(Constants.PERCENT_CHANGE_24, null);
            }

            String marketCapUsd = market_cap_usd.toString();
            if (marketCapUsd != null && !marketCapUsd.equals("null"))
            {
                marketCapUsd = marketCapUsd.replace("\"", "");
                marketCapUsd = NumberFormat.getNumberInstance(Locale.US).format(Double.parseDouble(marketCapUsd));
                map.put(Constants.MARKET_CAP, "Market Cap: " + marketCapUsd);
            }
            else
            {
                map.put(Constants.MARKET_CAP, "Market Cap is not available");
            }

            String totalSupply = total_supply.toString();
            if (totalSupply != null && !totalSupply.equals("null"))
            {
                totalSupply = totalSupply.replace("\"", "");
                totalSupply = NumberFormat.getNumberInstance(Locale.US).format(Double.parseDouble(totalSupply));
                map.put(Constants.TOTAL_SUPPLY, "Total Supply: " + totalSupply.replace("\"", ""));
            }
            else
            {
                map.put(Constants.TOTAL_SUPPLY, "Total supply is not available");
            }

            long linuxTimeStamp = Long.parseLong(last_updated.toString().replace("\"", ""));
            Date date = new Date(linuxTimeStamp * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = sdf.format(date);
            map.put(Constants.LAST_UPDATED, "Updated @ " + formattedDate);

            return map;
        }
        catch (IOException e)
        {
            Log.e("_MK", "API Parse Error", e);
            return null;
        }
    }
}
