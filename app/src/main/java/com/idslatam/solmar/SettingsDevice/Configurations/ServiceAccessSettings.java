package com.idslatam.solmar.SettingsDevice.Configurations;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.SettingsPermissions;
import com.idslatam.solmar.R;
import com.idslatam.solmar.View.Settings.AccessSettings;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ServiceAccessSettings extends Service {

    final Handler handler = new Handler();
    String estadoPermiso;

    ActivityManager am;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        runnable.run();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // METODOS PARA ACCESO A CONFIGURACIONES ********************************************************************************
    Runnable runnable = new Runnable() {
        public void run() {
            try {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                    //printForegroundTask();
                } else {
                    getRunningKITKAT();
                }
            }catch (Exception e){}
            handler.postDelayed(runnable, 1000);
        }
    };

    public void getRunningKITKAT(){

        try {

            List<ActivityManager.RunningTaskInfo> alltasks = am.getRunningTasks(1);
            for (ActivityManager.RunningTaskInfo aTask : alltasks) {
                Log.e("aTask ", String.valueOf(aTask.topActivity.getClassName()));
                String g = aTask.topActivity.getClassName();


                try {

                    DBHelper dataBaseHelper = new DBHelper(this);
                    SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
                    String selectQuery = "SELECT Nombre, Estado FROM SettingsPermissions " +
                            "WHERE Nombre = '"+g+"'";
                    Cursor c = db.rawQuery(selectQuery, new String[]{});
                    if (c.moveToFirst()) {
                        estadoPermiso = c.getString(c.getColumnIndex("Estado"));
                    }
                    c.close();
                    db.close();

                } catch (Exception e){}


                if (aTask.topActivity.getClassName().equals("com.android.settings.Settings")
                        || aTask.topActivity.getClassName().equals("com.android.settings.Settings$DateTimeSettingsActivity"))
                {
                    if (estadoPermiso.equals("false")) {
                        Intent dialogIntent = new Intent(this, AccessSettings.class);
                        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(dialogIntent);
                    }
                }
            }

        } catch (Throwable t) {
            Log.w("TAG", "Throwable caught: "
                    + t.getMessage(), t);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void printForegroundTask() {

        try {

            String foregroundApp = "";
            final long timeEnd = System.currentTimeMillis();
            final long timeBegin = timeEnd - 1000;

            UsageStatsManager mUsageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,timeBegin,timeEnd);

            if (stats!= null) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : stats) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    foregroundApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }

                Log.e("_app first if ", foregroundApp);

                if (foregroundApp.equalsIgnoreCase("com.android.settings")) {

                    Intent dialogIntent = new Intent(this, AccessSettings.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(dialogIntent);
                }
            }

        } catch (Exception e){}
    }


    // FIN DE METODOS PARA ACCESO A CONFIGURACIONES **************************************************************************
}
