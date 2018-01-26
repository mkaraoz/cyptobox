package com.mk.cryptobox.sneaky;

import android.content.Intent;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import org.joda.time.DateTime;

/**
 * Created by mk on 26.01.2018.
 */

public class SneakyPhotoScanJobService extends JobService
{
    @Override
    public boolean onStartJob(JobParameters job)
    {
        Log.d("_MK", "JOB Started.");
        Intent i = new Intent(SneakyPhotoScanJobService.this, PhotoScanService.class);
        startService(i);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job)
    {
        Log.d("_MK", "JOB COMPLETED @ " + TimeOps.print(DateTime.now()));
        return false;
    }
}
