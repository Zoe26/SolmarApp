package com.idslatam.solmar.SettingsDevice.Broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.idslatam.solmar.Tracking.Broadcast.AlarmLocation;
import com.idslatam.solmar.View.Bienvenido;

public class BootSettings extends BroadcastReceiver {
    public BootSettings() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // METODO QUE INICIA EL SERVICIO LOCATION ***************************************************
        Intent alarm = new Intent(context, AlarmLocation.class);
        boolean alarmRunning = (PendingIntent.getBroadcast(context, 0, alarm, PendingIntent.FLAG_NO_CREATE) != null);

        int vApi = Build.VERSION.SDK_INT;

        //Toast.makeText(getApplicationContext(), "Prender Alarma", Toast.LENGTH_SHORT).show();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarm, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(vApi <= 19){
            if(alarmRunning == false) {
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 1000*2, pendingIntent);
            }else{
            }
        }
        else{
            if(alarmRunning == false) {
                long timeInMillis = (SystemClock.elapsedRealtime() + 1000*2);
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeInMillis, pendingIntent);
            }
        }

        // FIN METODO QUE INICIA EL SERVICIO LOCATION ***********************************************

        try {

            Log.e("Auto Arranque ", " App!");
            Intent i = new Intent(context, Bienvenido.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

        } catch (Exception e) {
            Log.e("Auto Arranque ", " Falló!");
        }
    }
}
