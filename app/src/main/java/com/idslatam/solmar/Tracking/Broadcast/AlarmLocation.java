package com.idslatam.solmar.Tracking.Broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Pruebas.Crud.AlarmTrackCrud;
import com.idslatam.solmar.Pruebas.Entities.AlarmTrack;
import com.idslatam.solmar.Tracking.Services.LocationFusedApi;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AlarmLocation extends BroadcastReceiver {

    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss"),
            formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    String scurrentInicio;
    Calendar ccurrentInicio;

    int intervalo;

    //+++++++++++++++++++++
    private Context mContext;
    int _AlarmTrack_Id = 0;

    //--------------------------------------------------------
    boolean gps;
    //---------------------------------------------------------

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;

        try {
            setMobileDataEnabled(mContext, true);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }



           /* IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            BroadcastReceiver mReceiver = new ScreenReceiver();

            mContext.getApplicationContext().registerReceiver(mReceiver, filter);*/



        gps = isGPSAvailable();

        //NO TOCAR *****************************************************************************************************************************
        int vApi = Build.VERSION.SDK_INT;

        if (vApi > 19) {

            //Log.e("Alarm Api > 19", "Execute");

            updateFechaAlarmLocation();

            try {

                Intent alarm = new Intent(context, AlarmLocation.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarm, 0);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                long timeInMillis = (SystemClock.elapsedRealtime() + 1000 * 60);
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeInMillis, pendingIntent);

            } catch (Exception e){
                Toast.makeText(mContext, "Excepcion Start Service", Toast.LENGTH_LONG).show();
            }

        } else {

            Log.e("Alarm Api < 19", "Execute");

            updateFechaAlarmLocation();

            try {

                Intent alarm = new Intent(context, AlarmLocation.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarm, 0);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                long timeInMillis = (SystemClock.elapsedRealtime() + 1000 * 60);
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeInMillis, pendingIntent);

            } catch (Exception e){
                Log.e(" pendingIntent ", " ALARM EXEPTION ");
            }


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

        //Log.e("-- PRUEBA INT ALARM ", String.valueOf(intervalo));


        if(intervalo <= 1){

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

        //Log.e("-- PRUEBA ALARM ", formatoIso.format(currentDate.getTime()));

        AlarmTrackCrud alarmTrackCrud = new AlarmTrackCrud(mContext);
        AlarmTrack alarmTrack = new AlarmTrack();

        alarmTrack.FechaAlarm = formatoIso.format(currentDate.getTime());
        alarmTrack.Estado = "true";

        try {
            _AlarmTrack_Id = alarmTrackCrud.insert(alarmTrack);
        }catch (Exception e){}

        return true;
    }

    private boolean isGPSAvailable() {

        //return true;
        LocationManager locationManagerx = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManagerx.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        } else {
            return true;
        }
    }

    private void setMobileDataEnabled(Context context, boolean enabled) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final ConnectivityManager conman = (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final Class conmanClass = Class.forName(conman.getClass().getName());
        final Field connectivityManagerField = conmanClass.getDeclaredField("mService");
        connectivityManagerField.setAccessible(true);
        final Object connectivityManager = connectivityManagerField.get(conman);
        final Class connectivityManagerClass =  Class.forName(connectivityManager.getClass().getName());
        final Method setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
        setMobileDataEnabledMethod.setAccessible(true);

        setMobileDataEnabledMethod.invoke(connectivityManager, enabled);

        //Log.e("--| ACTIVANDO ","DATOS.....");
        //Toast.makeText(this, "Activando Datos..", Toast.LENGTH_SHORT).show();
    }

}