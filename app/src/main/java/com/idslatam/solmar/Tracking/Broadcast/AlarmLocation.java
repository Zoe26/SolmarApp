package com.idslatam.solmar.Tracking.Broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.idslatam.solmar.Tracking.Services.LocationFusedApi;

import java.text.SimpleDateFormat;

public class AlarmLocation extends BroadcastReceiver {

    Context mContext;
    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss"),
            formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;

        Intent background = new Intent(context, LocationFusedApi.class);
        context.startService(background);

        int vApi = Build.VERSION.SDK_INT;

        if (vApi > 19) {

            Log.e("Alarm Api > 19", "Execute");

            Intent alarm = new Intent(context, AlarmLocation.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarm, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            long timeInMillis = (SystemClock.elapsedRealtime() + 1000 * 59);
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeInMillis, pendingIntent);

        } else {

            Log.e("Alarm Api < 19", "Execute");

            Intent alarm = new Intent(context, AlarmLocation.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarm, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            long timeInMillis = (SystemClock.elapsedRealtime() + 1000 * 59);
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeInMillis, pendingIntent);
        }
    }

}