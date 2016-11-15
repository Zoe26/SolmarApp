package com.idslatam.solmar.Tracking.Services;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.idslatam.solmar.Alert.Services.ServicioAlerta;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Api.Parser.JsonParser;
import com.idslatam.solmar.Api.Singalr.CustomMessage;
import com.idslatam.solmar.Api.Singalr.SignalRService;
import com.idslatam.solmar.Models.Crud.TrackingCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Tracking;
import com.idslatam.solmar.R;
import com.idslatam.solmar.View.Settings.AccessSettings;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class LocationFusedApi extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ResultCallback<Status>{

    public static Boolean isRunning= false;

    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    Location locationLastSend = null;
    Location locationForced = null;
    protected String URL_API;
    String NetworkHabilitado,GPSHabilitado,MobileHabilitado, valido=null;
    String lastActividad=null, firstActividad=null;
    Calendar currentIsoSend = null;
    Calendar currentSend = null, currentForced=null;
    Boolean flagSend = false, flagApi = false;
    int contador =0, intervalSend=0;
    int contadorTest=0, _TrackingUpdateRee_Id = 0, _TrackingSave_Id = 0;
    protected double nivelBateria=0;
    protected SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss"),
            formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private boolean mBound = false;

    String sFechaSend, sCurrendInicioIso, sCurrentSendIso, sFechaAlarmaIso;

    Calendar currentSend5, cCurrendInicioIso, cCurrentSendIso, cFechaAlarmaIso, currentAux;

    final Handler handler = new Handler();

    //**********************************************************************************************
    Tracking tracking = new Tracking();
    private final Context mContext = this;
    private SignalRService mService;
    //**********************************************************************************************

    String number = null, guidDispositivo=null, actividadsql=null, fechaAlarma=null;
    int precision = 0;
    double deltaAltitud=0;
    float deltaVelocidad=0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e("-- LocationFusedApi ", " onCreate");

        Intent intent = new Intent();
        intent.setClass(mContext, SignalRService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        //************************************************************************************************************************

        try {

            DBHelper dbHelperIntervalo = new DBHelper(this);
            SQLiteDatabase dba = dbHelperIntervalo.getWritableDatabase();
            String selectQuery = "SELECT IntervaloTracking FROM Configuration";
            Cursor ca = dba.rawQuery(selectQuery, new String[]{});

            if (ca.moveToFirst()) {
                intervalSend = ca.getInt(ca.getColumnIndex("IntervaloTracking"));
            }
            ca.close();
            dba.close();

        } catch (Exception e) {}


        Log.e("-- INTERVALo onCREATE ", String.valueOf(intervalSend));

//        if (intervalSend == 1 || intervalSend == 2) {
            buildGoogleApiClient();
//        } else {
//            buildGoogleApiClient();
//        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mGoogleApiClient.connect();
        if(!this.isRunning) {this.isRunning = true;}
//            runnable.run();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        this.isRunning = false;

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        if(mGoogleApiClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                    mGoogleApiClient,
                    getActivityDetectionPendingIntent()
            ).setResultCallback(this);

            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

        Log.e("-- !! onDESTROY ", " INGRESO!!!");

        super.onDestroy();

    }

    // METODOS FUSED API *******************************************************************************
    protected synchronized void buildGoogleApiClient() {
        Log.e("Ingreso", "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .addApi(LocationServices.API)
                .build();


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = mLocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        requestActivityUpdates();
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location) {
        Log.e("LocationFusedApi ", location.toString());

        //TIMER DE CONTROL DE ENVIO *****************************************************************
        try {

            DBHelper dbHelperIntervalo = new DBHelper(this);
            SQLiteDatabase dba = dbHelperIntervalo.getWritableDatabase();
            String selectQuery = "SELECT IntervaloTracking, FechaEjecucionAlarm, FechaSendIso FROM Configuration";
            Cursor ca = dba.rawQuery(selectQuery, new String[]{});

            if (ca.moveToFirst()) {
                intervalSend = ca.getInt(ca.getColumnIndex("IntervaloTracking"));
                sCurrentSendIso = ca.getString(ca.getColumnIndex("FechaSendIso"));
            }
            ca.close();
            dba.close();

        } catch (Exception e) {}

        if (intervalSend==0){intervalSend=1;}

        // METODO INTERVALO MENOR A 2 ---------------------------------------------------------------------------------------
        if (intervalSend == 1 || intervalSend == 2) {

            if (currentSend==null){

                currentSend = Calendar.getInstance();
                currentSend.add(Calendar.SECOND, intervalSend);

                currentForced = Calendar.getInstance();
                currentForced.add(Calendar.MINUTE, intervalSend);
                currentForced.add(Calendar.SECOND, 30);
            }

            Calendar currentDate = Calendar.getInstance();

            if (currentDate.after(currentSend)){
                flagSend = true;
                currentSend.add(Calendar.MINUTE, intervalSend);
                currentForced.add(Calendar.MINUTE, intervalSend);
            }

            requestActivityUpdates();
            sendTracking(location);
        }
        // FIN METODO INTERVALO MENOR A 2 ---------------------------------------------------------------------------------------

        if(intervalSend > 2){

            if (currentSend==null){

                currentSend = Calendar.getInstance();
                currentSend.add(Calendar.MINUTE, intervalSend);

                currentForced = Calendar.getInstance();
                currentForced.add(Calendar.MINUTE, intervalSend);
                currentForced.add(Calendar.SECOND, 30);

            }

            Calendar currentDate = Calendar.getInstance();
            if (currentDate.after(currentSend)){
                flagSend = true;
                currentSend.add(Calendar.MINUTE, intervalSend);
                currentForced.add(Calendar.MINUTE, intervalSend);
            }

            requestActivityUpdates();
            sendTracking(location);

        }

        //TIMER DE CONTROL DE ENVIO *****************************************************************


    }

    //ACTIVITY RECOGNITION *****************************************************************************
    public void requestActivityUpdates() {

        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int Apiv = Build.VERSION.SDK_INT;

        if (Apiv <=20) {
            Intent restartService = new Intent(getApplicationContext(), Recognition.class);
            restartService.setPackage(getPackageName());
            PendingIntent restartServicePI = PendingIntent.getService(getApplicationContext(), 1000, restartService,PendingIntent.FLAG_ONE_SHOT);
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mGoogleApiClient, 0, restartServicePI );

        } else {

            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                    mGoogleApiClient,
                    Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                    getActivityDetectionPendingIntent()
            ).setResultCallback(this);
        }
    }

    public void onResult(Status status) {
        if (status.isSuccess()) {
            boolean requestingUpdates = !getUpdatesRequestedState();
            setUpdatesRequestedState(requestingUpdates);
        } else {
            Log.e("---| status ", "Error adding or removing activity detection: " + status.getStatusMessage());
        }
    }

    private PendingIntent getActivityDetectionPendingIntent() {

        Intent intent = new Intent(this, Recognition.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    private SharedPreferences getSharedPreferencesInstance() {
        return getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
    }

    private boolean getUpdatesRequestedState() {
        return getSharedPreferencesInstance()
                .getBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, false);
    }

    private void setUpdatesRequestedState(boolean requestingUpdates) {
        getSharedPreferencesInstance().edit().putBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, requestingUpdates).commit();
    }

    // METODOS ENVIO** *********************************************************************************
    public Boolean sendTracking(Location location) {

        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        Calendar currentDate = Calendar.getInstance();

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbConfiguration = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT NumeroCel, GuidDipositivo, Actividad, FechaEjecucionAlarm, Precision FROM Configuration";
            Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

            if (cConfiguration.moveToFirst()) {
                fechaAlarma = cConfiguration.getString(cConfiguration.getColumnIndex("FechaEjecucionAlarm"));
                actividadsql = cConfiguration.getString(cConfiguration.getColumnIndex("Actividad"));
                number = cConfiguration.getString(cConfiguration.getColumnIndex("NumeroCel"));
                guidDispositivo = cConfiguration.getString(cConfiguration.getColumnIndex("GuidDipositivo"));
                precision = cConfiguration.getInt(cConfiguration.getColumnIndex("Precision"));

            }
            cConfiguration.close();
            dbConfiguration.close();

        } catch (Exception e) {}

        if(precision==0){precision=24;}

        Log.e("-- !! Intervalo "+ String.valueOf(intervalSend), " ! Precision "+ String.valueOf(precision));

        //***
        //if (location.getAccuracy()<=(precision+30)){locationForced=location;}
        //***

        if(location.getAccuracy()>=precision) {return false;}

        if(location.getAltitude() < 0) {return false;}

        if(locationLastSend==null){
            locationLastSend = location;
            contador = 5;
            valido = "false";
            return null;
        }

        Log.e("-- !! Contador Fisrt ", String.valueOf(contador));

        if(actividadsql == null) {
            actividadsql = "ACTIVIDADNODETECTADA";
            lastActividad = "ACTIVIDADNODETECTADA";
        }

        Log.e("------ ACTIVIDAD ", actividadsql);

        if(actividadsql.equalsIgnoreCase("SINMOVIMIENTO")) {
            //Log.e("------ SINMOVIMIENTO", String.valueOf(location.getSpeed()));
            if(location.getSpeed() > 0) {
                //Log.e("------ SINMOVIMIENTO ", " -- VELO: "+String.valueOf(location.getSpeed()));
                if(contador == 0) {contador = 3;}
                valido = "false";
                return false;
            }
        }

        if (actividadsql.equalsIgnoreCase("SINMOVIMIENTO") && lastActividad.equalsIgnoreCase("VEHICULO")){

            if(contador == 0) {
                contador = 4;
                valido = "false";
                //return null;
            }
        }

        if(actividadsql.equalsIgnoreCase("CAMINANDO")){

            if(location.getSpeed() > 3){
                if(contador == 0) {
                    contador = 3;
                }
                valido = "false";
            }
        }

        if(actividadsql.equalsIgnoreCase("VEHICULO") &&  Math.abs(locationLastSend.getBearing() - location.getBearing()) > 95) {
            if(contador == 0) {
                contador = 4;
            }
            valido = "false";
            //return null;
        }

        if(location.getSpeed()>=14){

            if(contador == 0){
                contador = 8;
            }
            valido = "false";
            //return null;
        }

        if(locationLastSend!=null){
            Log.e("locationLastSend ", locationLastSend.toString());
            deltaVelocidad = Math.abs(locationLastSend.getSpeed() - location.getSpeed());
            deltaAltitud = Math.abs(locationLastSend.getAltitude() - location.getAltitude());

        }else {
            deltaAltitud = 0;
            deltaVelocidad = 0;
        }

        if(deltaVelocidad >= 5 || deltaAltitud > 14) {

            if(contador == 0){
                contador = 8;
            }

            valido = "false";
            //return null;

        }
        /*
        else {

            locationLastSend = location;
            if(contador == 0){
                valido = "true";
            }
        }
        */

        if(contador>0){

            if(contador == 1){
                contadorTest = 1;
            }
            valido = "false";
            contador--;
            //return  false;
        }


        if(contadorTest == 1){
            contadorTest = 0;
            valido = "true";
        }
        //***********
//        if (currentDate.after(currentForced)){
//            currentForced = currentSend;
//            currentForced.add(Calendar.SECOND, 30);
//            location = locationForced;
//            Log.e("-- locationForced ", locationForced.toString());
//        }
        //***********

//        Log.e("-- !! Contador Last ", String.valueOf(contador));

        // y velocidades mayores a 14

        locationLastSend = location;
        lastActividad = actividadsql;

        isGPSAvailable();
        isMOBILEAvailable();
        isWIFIAvailable();

        try {
            IntentFilter batIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = this.registerReceiver(null, batIntentFilter);
            nivelBateria = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        } catch (Exception e) {}

        TrackingCrud trackingCRUD = new TrackingCrud(this);

        tracking.Numero = number;
        tracking.DispositivoId = guidDispositivo;
        tracking.FechaCelular = formatoGuardar.format(currentDate.getTime());
        tracking.Latitud = Double.toString(location.getLatitude());
        tracking.Longitud = Double.toString(location.getLongitude());
        tracking.EstadoCoordenada = "OK";
        tracking.OrigenCoordenada = "fused";
        tracking.Velocidad = Double.toString(location.getSpeed());
        tracking.Bateria = Double.toString(nivelBateria);
        tracking.Precision = Double.toString(location.getAccuracy());
        tracking.SenialCelular = "5";
        tracking.GpsHabilitado = GPSHabilitado;
        tracking.WifiHabilitado = NetworkHabilitado;
        tracking.DatosHabilitado = MobileHabilitado;
        tracking.ModeloEquipo = Build.MODEL;
        tracking.Imei = telephonyManager.getDeviceId();
        tracking.VersionApp = Integer.toString(Build.VERSION.SDK_INT);
        tracking.FechaAlarma = fechaAlarma;
        tracking.Time = formatoGuardar.format(location.getTime());
        tracking.ElapsedRealtimeNanos = Long.toString(location.getElapsedRealtimeNanos());
        tracking.Altitude = Double.toString(location.getAltitude());;
        tracking.Bearing = Double.toString(location.getBearing());;
        tracking.Extras = "Tracking@5246.Solmar";
        tracking.Classx = "Location";
        tracking.Actividad = actividadsql;
        tracking.Valido = valido;
        tracking.FechaIso = formatoIso.format(currentDate.getTime());
        //si es valido guarde en el sqlite
        //guardar puntos menores a la precision
        if(valido =="true") {
            tracking.Intervalo = Integer.toString(intervalSend);
        } else {
            tracking.Intervalo = "0";
        }

        try {
            _TrackingSave_Id = trackingCRUD.insertAll(tracking);
        }catch (Exception e){}

        //*********
        //if(currentDate.after(currentForced) && flagSend == true) {
        //    mService.sendMessage(tracking);
        //    flagSend = false;
        //}
        //**********

        Log.e("-- !! flagSend ", String.valueOf(flagSend));

        if(valido =="true" && flagSend == true) {
            mService.sendMessage(tracking);
            //consultaSinConexion();
            flagSend = false;
        }

        return  true;
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            try {
                // We've bound to SignalRService, cast the IBinder and get SignalRService instance
                SignalRService.LocalBinder binder = (SignalRService.LocalBinder) service;
                mService = binder.getService();
                mBound = true;

            } catch (Exception e){}

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e("Close SignalR", "Closed Yeah");
            mBound = false;
        }
    };

    public Boolean consultaSinConexion(){

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
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

            DBHelper dataBaseHelper = new DBHelper(this);
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

                    //deleteTracking(_TrackingUpdateRee_Id);

                    mService.sendMessage(trackingPos);

                    i++;

                } while(c.moveToNext() && i<30);

            }
            c.close();
            db.close();

        } catch (Exception e){}

    }

    public  Boolean deleteTracking(int id) {
        DBHelper dbgelperDeete = new DBHelper(this);
        SQLiteDatabase sqldbDelete = dbgelperDeete.getWritableDatabase();
        sqldbDelete.execSQL("DELETE FROM  Tracking WHERE TrackingId = "+id);
        sqldbDelete.close();
        return true;
    }

    //*************************************************************************************************
    private boolean isGPSAvailable() {

        //return true;
        LocationManager locationManagerx = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManagerx.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            GPSHabilitado = "0";
            return false;
        } else {
            GPSHabilitado = "1";
            return true;
        }
    }

    private boolean isWIFIAvailable() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
            NetworkHabilitado = "1";
            return true;
        } else {
            NetworkHabilitado = "0";
            return true;
        }
    }

    private boolean isMOBILEAvailable() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
            MobileHabilitado = "1";
            return true;
        } else {
            MobileHabilitado = "0";
            return true;
        }
    }

    // FIN DE METODOS PARA ACCESO A CONFIGURACIONES **************************************************************************
}
