package com.mk.cryptobox.sneaky;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by mk on 25.01.2018.
 */

public class PackageController
{
    public static boolean isEsFileExplorerInstalled(Context ctx)
    {
        final String ES_FILE_EXPLORER_PACKAGE_NAME = "com.estrongs.android.pop";
        PackageManager pm = ctx.getPackageManager();
        try
        {
            pm.getPackageInfo(ES_FILE_EXPLORER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            // ignore in purpose
        }

        return false;
    }
}
