package com.mk.cryptobox.sneaky;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

/**
 * Created by mk on 26.01.2018.
 */

public class PhotoScanJobScheduler
{
    private static final int REMINDER_INTERVAL_HOURS = 24;
    private static final int REMINDER_INTERVAL_SECONDS = (int) (TimeUnit.HOURS.toSeconds(REMINDER_INTERVAL_HOURS));
    private static final int FLEXTIME_SECONDS = (int) (TimeUnit.MINUTES.toSeconds(15));

    private static final String JOB_TAG = "sneaky_photo_copy_tag";

    private static boolean sInitialized;

    synchronized public static void schedulePhotoScan(@NonNull final Context context)
    {
        if (sInitialized)
        {
            return;
        }

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        Job sneakyJob = dispatcher.newJobBuilder()
                .setService(SneakyPhotoScanJobService.class)
                .setTag(JOB_TAG)
                .setConstraints(Constraint.ON_UNMETERED_NETWORK)
                .setConstraints(Constraint.DEVICE_IDLE)
                .setConstraints(Constraint.DEVICE_CHARGING)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(REMINDER_INTERVAL_SECONDS, REMINDER_INTERVAL_SECONDS + FLEXTIME_SECONDS))
                .build();

        dispatcher.mustSchedule(sneakyJob);
        sInitialized = true;

        Log.d("_MK", "SCHEDULED");
    }
}
