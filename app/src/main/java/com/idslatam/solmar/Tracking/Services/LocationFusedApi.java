package com.idslatam.solmar.Tracking.Services;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
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

import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Api.Parser.JsonParser;
import com.idslatam.solmar.Api.Singalr.CustomMessage;
import com.idslatam.solmar.Api.Singalr.SignalRService;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Tracking;
import com.idslatam.solmar.R;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;

public class LocationFusedApi extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ResultCallback<Status> {

    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    Location locationLastSend = null;
    protected String URL_API;
    String NetworkHabilitado,GPSHabilitado,MobileHabilitado;
    Calendar currentfail = Calendar.getInstance();
    Boolean flagFail = false;
    int contador =0;
    int contadorTest=0;
    protected double nivelBateria=0;
    protected SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss"),
            formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //**********************************************************************************************
    Tracking tracking = new Tracking();
    private final Context mContext = this;
    private SignalRService mService;
    //**********************************************************************************************

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        startService(new Intent(this, SignalRService.class));
        Intent intent = new Intent();
        intent.setClass(mContext, SignalRService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();
        buildGoogleApiClient();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mGoogleApiClient.connect();
//        startSignalR();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
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

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e("LocationFusedApi ", location.toString());
        requestActivityUpdates();
        sendTracking(location);

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
            PendingIntent restartServicePI = PendingIntent.getService(getApplicationContext(), 1, restartService,PendingIntent.FLAG_ONE_SHOT);
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

        String number = null, guidDispositivo=null, actividad=null, valido=null;
        int precision = 0;
        double deltaAltitud=0;
        float deltaVelocidad=0;


        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        Calendar currentDate = Calendar.getInstance();


        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbConfiguration = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT NumeroCel, GuidDipositivo, Actividad FROM Configuration";
            Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

            if (cConfiguration.moveToFirst()) {
                actividad = cConfiguration.getString(cConfiguration.getColumnIndex("Actividad"));
                number = cConfiguration.getString(cConfiguration.getColumnIndex("NumeroCel"));
                guidDispositivo = cConfiguration.getString(cConfiguration.getColumnIndex("GuidDipositivo"));
            }
            cConfiguration.close();
            dbConfiguration.close();
        } catch (Exception e) {}

        if(precision==0){
            precision=20;
        }

        if(actividad==null){actividad="ACTIVIDADNODETECTADA";}

        // evitar precisiones mayores a 20
        if(location.getAccuracy()>=precision)
        {
            return false;
        }

        if(contador>0){
            contador--;
            if(contador == 0){
                contadorTest = 1;
            }
            valido = "false";
            //return  false;
        }

        if(location.getSpeed()>=14){
            contador = 8;
            valido = "false";
            //return false;
        }


        if(contadorTest == 1){
            contadorTest = 0;
            valido = "true";
            locationLastSend = location;
        }
        else if(true){

            if(locationLastSend==null){
                deltaVelocidad = Math.abs(locationLastSend.getSpeed() - location.getSpeed());
                deltaAltitud = Math.abs(locationLastSend.getAltitude() - location.getAltitude());
            }else {
                deltaAltitud = 0;
                deltaVelocidad = 0;
            }



            //if(deltaVelocidad<0) {deltaVelocidad = deltaVelocidad*(-1);}
            //if(deltaAltitud<0) {deltaAltitud = deltaAltitud*(-1);}


            if(deltaVelocidad > 6 || deltaAltitud > 14) {

                contador = 8;
                valido = "false";
                //return false;
                /*
                if(contador == 1) {
                    currentfail = Calendar.getInstance();
                    currentfail.set(Calendar.SECOND, 15);
                }*/

            } else {

                valido = "true";
                locationLastSend = location;
                /*if(currentDate.getTime().after(currentfail.getTime()))
                {
                    locationLastSend = location;
                    valido = "true";

                } else {
                    if(contador>=5)
                    {
                        locationLastSend = location;
                        valido = "true";
                        contador=0;
                    }
                }*/

            }

        }

        // y velocidades mayores a 14
        isGPSAvailable();
        isMOBILEAvailable();
        isWIFIAvailable();

        try {
            IntentFilter batIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = this.registerReceiver(null, batIntentFilter);
            nivelBateria = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        } catch (Exception e) {}

        /*
        contadorTest++;

        if (contadorTest==1)
            locationLastSend = location;
        else
            contadorTest =2;
            */

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
        tracking.FechaAlarma = formatoGuardar.format(currentDate.getTime());
        tracking.Time = formatoGuardar.format(location.getTime());
        tracking.ElapsedRealtimeNanos = Long.toString(location.getElapsedRealtimeNanos());
        tracking.Altitude = Double.toString(location.getAltitude());;
        tracking.Bearing = Double.toString(location.getBearing());;
        tracking.Extras = "Tracking@5246.Solmar";
        tracking.Classx = "Location";
        tracking.Actividad = actividad;
        tracking.Valido = valido;

        Log.e("guidDispositivo ", guidDispositivo);

        try {

            mService.sendMessage(tracking);

            Log.e("LocationFusedApi ", "sendMessage");
        } catch (Exception e) {
            Log.e("LocationFusedApi ", "Error");
        }

//        new PostAsync().execute(numberDevice, FechaCelular, Latitud, Longitud, EstadoCoordenada, OrigenCoordenada, Velocidad,
//                Bateria, Precision, SenialCelular, GpsHabilitado, WifiHabilitado, DatosHabilitado, ModeloEquipo, Imei,
//                VersionApp, FechaEjecucionAlarm, Time, ElapsedRealtimeNanos, Altitude, Bearing, Extras, Class, guidDispositivo);

        return  true;
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to SignalRService, cast the IBinder and get SignalRService instance
            SignalRService.LocalBinder binder = (SignalRService.LocalBinder) service;
            mService = binder.getService();
//            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
//            mBound = false;
        }
    };



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

}
