package com.idslatam.solmar.SettingsDevice.Configurations;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.SettingsPermissions;
import com.idslatam.solmar.View.Settings.AccessSettings;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ServiceAccessSettings extends Service {


    final Handler handler = new Handler();
    int _SettingsPermissions_Id=0;
    String estadoPermiso;

    boolean flagLock= false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

            runnable.run();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // METODOS PARA ACCESO A CONFIGURACIONES ********************************************************************************
    Runnable runnable = new Runnable() {
        public void run() {
//            Log.e("Ingreso run ", " Runnable");
            try {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
//                    activePackages = getActivePackages();
//                    getRunningSuperioKITKAT();
//                    getLollipopFGAppPackageName();
                    printForegroundTask();
                } else {
                    getRunningKITKAT();
//                    activePackages = getActivePackagesCompat();
                }


            }catch (Exception e){}
            handler.postDelayed(runnable, 400);
        }
    };

    public void getRunningKITKAT(){


        try {
            ActivityManager am = (ActivityManager) this
                    .getSystemService(Context.ACTIVITY_SERVICE);

            List<ActivityManager.RunningTaskInfo> alltasks = am.getRunningTasks(1);

            for (ActivityManager.RunningTaskInfo aTask : alltasks) {

//                Log.e("aTask ", String.valueOf(aTask.topActivity.getClassName()));
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
                    // When user on call screen show a alert message
//                    Log.e("Ingreso if ", " Settings");

                    if (estadoPermiso.equals("false")) {

                        Log.e("---! estadoPermiso IF ", estadoPermiso);

                        Intent dialogIntent = new Intent(this, AccessSettings.class);
                        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(dialogIntent);
//                        flagLock=true;

                    }
//                    else {
//                        flagLock=false;
//                    }
                }
            }

        } catch (Throwable t) {
            Log.w("TAG", "Throwable caught: "
                    + t.getMessage(), t);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void printForegroundTask() {

//        Log.e("_printForegroundTask ", " ingreso");

        String foregroundApp = "";
        final long timeEnd = System.currentTimeMillis();
        final long timeBegin = timeEnd - 1000;

        UsageStatsManager mUsageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();

        List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,timeBegin,timeEnd);

        Log.e("_stats ", String.valueOf(stats));

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
                Log.e("Ingreso if ", " Settings");
//                Toast.makeText(this, "Settings.", Toast.LENGTH_LONG).show();

                Intent dialogIntent = new Intent(this, AccessSettings.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(dialogIntent);
            }

            Log.e("_app last if ", foregroundApp);
        }

        // CONDICION PARA FUNCIONAMIENTO EN API 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UsageEvents usageEvents = mUsageStatsManager.queryEvents(time - 100 * 1000, time);
            UsageEvents.Event event = new UsageEvents.Event();
            // get last event
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);
            }
            if (foregroundApp.equals(event.getPackageName()) && event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                Log.e("_mUsageStatsManager", String.valueOf(foregroundApp));
            }

        }

    }

    // FIN DE METODOS PARA ACCESO A CONFIGURACIONES **************************************************************************
}
