package com.idslatam.solmar.Tracking.Broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.SettingsDevice.Configurations.ServiceAccessSettings;
import com.idslatam.solmar.Tracking.Services.LocationFusedApi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AlarmLocation extends BroadcastReceiver {

    Context mContext;
    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss"),
            formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    String scurrentInicio;
    Calendar ccurrentInicio;

    int intervalo;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;

        // INTENTI AL SERVICIO ----------------------------------------------------------------------
        Intent backgroundS = new Intent(context, ServiceAccessSettings.class);
        mContext.startService(backgroundS);
        // FIN INTENTI AL SERVICIO ------------------------------------------------------------------

        //NO TOCAR *****************************************************************************************************************************
        int vApi = Build.VERSION.SDK_INT;

        if (vApi > 19) {

            Log.e("Alarm Api > 19", "Execute");

            updateFechaAlarmLocation();

            Intent alarm = new Intent(context, AlarmLocation.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarm, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            long timeInMillis = (SystemClock.elapsedRealtime() + 1000 * 60);
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeInMillis, pendingIntent);

        } else {

            Log.e("Alarm Api < 19", "Execute");

            updateFechaAlarmLocation();

            Intent alarm = new Intent(context, AlarmLocation.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarm, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            long timeInMillis = (SystemClock.elapsedRealtime() + 1000 * 60);
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeInMillis, pendingIntent);
        }
        //*************************************************************************************************************************************


        try {

            DBHelper dbHelperIntervalo = new DBHelper(mContext);
            SQLiteDatabase dba = dbHelperIntervalo.getWritableDatabase();
            String selectQuery = "SELECT IntervaloTracking, FechaInicioIso FROM Configuration";
            Cursor ca = dba.rawQuery(selectQuery, new String[]{});

            if (ca.moveToFirst()) {
                intervalo = ca.getInt(ca.getColumnIndex("IntervaloTracking"));
                scurrentInicio = ca.getString(ca.getColumnIndex("FechaInicioIso"));
            }
            ca.close();
            dba.close();

        } catch (Exception e) {}

        if(intervalo == 0){ intervalo = 1;}

        Log.e("-- INTERVALO ALARM ", String.valueOf(intervalo));

        if(intervalo < 2){

            try {
                DBHelper dataBaseHelperA = new DBHelper(mContext);
                SQLiteDatabase dbA = dataBaseHelperA.getWritableDatabase();
                dbA.execSQL("UPDATE Configuration SET FechaInicioIso = NULL");
                dbA.close();

            } catch (Exception e) {}

            // INTENTI AL SERVICIO ----------------------------------------------------------------------
            Intent background = new Intent(context, LocationFusedApi.class);
            context.startService(background);
            // FIN INTENTI AL SERVICIO ------------------------------------------------------------------


        } else {

            if(scurrentInicio==null){

                intervalo = intervalo-1;
                Calendar currentNow = Calendar.getInstance();
                currentNow.add(Calendar.MINUTE, intervalo);

                try {
                    DBHelper dataBaseHelperA = new DBHelper(mContext);
                    SQLiteDatabase dbA = dataBaseHelperA.getWritableDatabase();
                    dbA.execSQL("UPDATE Configuration SET FechaInicioIso = '"+ formatoIso.format(currentNow.getTime()) +"'");
                    dbA.close();

                } catch (Exception e) {}

                Log.e("-- ALARM NULL ", String.valueOf(currentNow.getTime()));

            } else {

                Log.e("-- ALARM FechInicioIso ", String.valueOf(scurrentInicio));
                Calendar currentNow = Calendar.getInstance();

                ccurrentInicio = Calendar.getInstance();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    ccurrentInicio.setTime(sdf.parse(scurrentInicio));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Log.e("-- INTERVALO ALARM NOW ", String.valueOf(currentNow.getTime()));
                Log.e("-- INTERVALO ALARM ", String.valueOf(ccurrentInicio.getTime()));

                if(currentNow.after(ccurrentInicio)){

                    currentNow.add(Calendar.MINUTE, intervalo);

                    try {
                        DBHelper dataBaseHelperA = new DBHelper(mContext);
                        SQLiteDatabase dbA = dataBaseHelperA.getWritableDatabase();
                        dbA.execSQL("UPDATE Configuration SET FechaInicioIso = '"+ formatoIso.format(currentNow.getTime()) +"'");
                        dbA.close();

                    } catch (Exception e) {}

                    // INTENTI AL SERVICIO ----------------------------------------------------------------------
                    Intent background = new Intent(context, LocationFusedApi.class);
                    context.startService(background);
                    // FIN INTENTI AL SERVICIO ------------------------------------------------------------------
                }
            }
        }

    }

    public Boolean updateFechaAlarmLocation() {

        Calendar currentDate = Calendar.getInstance();

        try {
            DBHelper dataBaseHelperA = new DBHelper(mContext);
            SQLiteDatabase dbA = dataBaseHelperA.getWritableDatabase();
            dbA.execSQL("UPDATE Configuration SET FechaEjecucionAlarm = '"+ formatoGuardar.format(currentDate.getTime()) +"'");
            dbA.execSQL("UPDATE Configuration SET FechaAlarmaIso = '"+ formatoIso.format(currentDate.getTime()) +"'");
            dbA.close();

        } catch (Exception e) {}

        return true;
    }

}