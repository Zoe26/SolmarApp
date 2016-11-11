package com.idslatam.solmar.Tracking.Broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.idslatam.solmar.Api.Singalr.SignalRService;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Tracking;
import com.idslatam.solmar.SettingsDevice.Configurations.ServiceAccessSettings;
import com.idslatam.solmar.Tracking.Services.LocationFusedApi;

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
    private SignalRService mService;
    private boolean mBound = false;
    private Context mContext;
    int _TrackingUpdateRee_Id = 0;


    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;

        // INTENTI AL SERVICIO ----------------------------------------------------------------------
        Intent backgroundS = new Intent(context, ServiceAccessSettings.class);
        mContext.startService(backgroundS);
        // FIN INTENTI AL SERVICIO ------------------------------------------------------------------

        consultaSinConexion();

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

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to SignalRService, cast the IBinder and get SignalRService instance
            SignalRService.LocalBinder binder = (SignalRService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e("Close SignalR", "Closed Yeah");
            mBound = false;
        }
    };

    public Boolean consultaSinConexion(){

        try {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbN = dataBaseHelper.getWritableDatabase();
            String selectQueryBuscaN = "SELECT Numero FROM Tracking WHERE EstadoEnvio = 'false'";
            Cursor cbuscaN = dbN.rawQuery(selectQueryBuscaN, new String[]{}, null);
            int contador = cbuscaN.getCount();
            cbuscaN.close();
            dbN.close();

            if (contador>0) {
                sendSave();
            }

        }catch (Exception e){
            Log.e("-- Error Reenvio Track", e.getMessage());
        }

        return true;
    }

    public void sendSave() {

        int i =0;
        Log.e("--! Reenvio ", "sendSave");

        try {

            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            String selectQuery = "SELECT TrackingId, Numero, DispositivoId, FechaCelular, Latitud, Longitud, EstadoCoordenada, " +
                    "OrigenCoordenada, Velocidad, Bateria, Precision, SenialCelular, GpsHabilitado, WifiHabilitado, " +
                    "DatosHabilitado, ModeloEquipo, Imei, VersionApp, FechaAlarma, Time, ElapsedRealtimeNanos, " +
                    "Altitude, Bearing, Extras, Classx, Actividad, Valido, Intervalo, EstadoEnvio FROM Tracking WHERE EstadoEnvio = 'false'";
            Cursor c = db.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                do {
                    Tracking trackingPos = new Tracking();

                    _TrackingUpdateRee_Id = c.getInt(c.getColumnIndex("TrackingId"));

                    trackingPos.Numero = c.getString(c.getColumnIndex("Numero"));
                    trackingPos.DispositivoId = c.getString(c.getColumnIndex("DispositivoId"));
                    trackingPos.FechaCelular = c.getString(c.getColumnIndex("FechaCelular"));
                    trackingPos.Latitud = c.getString(c.getColumnIndex("Latitud"));
                    trackingPos.Longitud = c.getString(c.getColumnIndex("Longitud"));
                    trackingPos.EstadoCoordenada = "OK";
                    trackingPos.OrigenCoordenada = "fused";
                    trackingPos.Velocidad = c.getString(c.getColumnIndex("Velocidad"));
                    trackingPos.Bateria = c.getString(c.getColumnIndex("Bateria"));
                    trackingPos.Precision = c.getString(c.getColumnIndex("Precision"));
                    trackingPos.SenialCelular = c.getString(c.getColumnIndex("SenialCelular"));
                    trackingPos.GpsHabilitado = c.getString(c.getColumnIndex("GpsHabilitado"));
                    trackingPos.WifiHabilitado = c.getString(c.getColumnIndex("WifiHabilitado"));
                    trackingPos.DatosHabilitado = c.getString(c.getColumnIndex("DatosHabilitado"));
                    trackingPos.ModeloEquipo = c.getString(c.getColumnIndex("ModeloEquipo"));
                    trackingPos.Imei = c.getString(c.getColumnIndex("Imei"));
                    trackingPos.VersionApp = c.getString(c.getColumnIndex("VersionApp"));
                    trackingPos.FechaAlarma = c.getString(c.getColumnIndex("FechaAlarma"));
                    trackingPos.Time = c.getString(c.getColumnIndex("Time"));
                    trackingPos.ElapsedRealtimeNanos = c.getString(c.getColumnIndex("ElapsedRealtimeNanos"));
                    trackingPos.Altitude = c.getString(c.getColumnIndex("Altitude"));
                    trackingPos.Bearing = c.getString(c.getColumnIndex("Bearing"));
                    trackingPos.Extras = "Tracking@5246.Solmar";
                    trackingPos.Classx = "Location";
                    trackingPos.Actividad = c.getString(c.getColumnIndex("Actividad"));
                    trackingPos.Valido = c.getString(c.getColumnIndex("Valido"));
                    trackingPos.Intervalo = c.getString(c.getColumnIndex("Intervalo"));

                    deleteTracking(_TrackingUpdateRee_Id);

                    mService.sendMessage(trackingPos);

                    i++;

                } while(c.moveToNext() && i<10);

            }
            c.close();
            db.close();

        } catch (Exception e){}

    }

    public  Boolean deleteTracking(int id) {
        DBHelper dbgelperDeete = new DBHelper(mContext);
        SQLiteDatabase sqldbDelete = dbgelperDeete.getWritableDatabase();
        sqldbDelete.execSQL("DELETE FROM  Tracking WHERE TrackingId = "+id);
        sqldbDelete.close();
        return true;
    }
}