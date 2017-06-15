package com.idslatam.solmar.BravoPapa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.idslatam.solmar.Models.Database.DBHelper;

public class ScreenReceiver extends BroadcastReceiver {
    public ScreenReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        //Log.e("--- INGRESÓ ", "Screen");

        if ("android.intent.action.SCREEN_OFF".equals(intent.getAction()))
        {

            try {
                DBHelper dbHelperAlarm = new DBHelper(context);
                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                dba.execSQL("UPDATE Configuration SET isScreen = 'false'");
                dba.close();
            } catch (Exception e){}

            Log.e("---", "Screen off");
            intent = new Intent(context, SoundService.class);
            intent.putExtra("action", 0);
            context.startService(intent);

        } else if ("android.intent.action.SCREEN_ON".equals(intent.getAction()))
        {

            try {
                DBHelper dbHelperAlarm = new DBHelper(context);
                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                dba.execSQL("UPDATE Configuration SET isScreen = 'true'");
                dba.close();
            } catch (Exception e){}

            Log.e("---", "Screen on");
            intent = new Intent(context, SoundService.class);
            intent.putExtra("action", 1);
            context.startService(intent);
        }
    }
}
