package com.idslatam.solmar.Alert.Broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.Toast;

import com.idslatam.solmar.Tracking.Broadcast.AlarmLocation;

public class AlarmAlert extends BroadcastReceiver {

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        this.mContext = context;


        try {

            Intent alarm = new Intent(context, AlarmAlert.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarm, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            long timeInMillis = (SystemClock.elapsedRealtime() + 1000 * 60);
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeInMillis, pendingIntent);

        } catch (Exception e){
            Toast.makeText(mContext, "Excepcion Start Service", Toast.LENGTH_LONG).show();
        }

    }
}
