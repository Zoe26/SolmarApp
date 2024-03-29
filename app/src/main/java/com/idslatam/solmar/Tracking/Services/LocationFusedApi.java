package com.idslatam.solmar.Tracking.Services;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
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
import android.net.Uri;
import android.net.wifi.WifiManager;
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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Api.Singalr.SignalRService;
import com.idslatam.solmar.BravoPapa.ScreenReceiver;
import com.idslatam.solmar.Models.Crud.AlertCrud;
import com.idslatam.solmar.Models.Crud.TrackingCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Alert;
import com.idslatam.solmar.Models.Entities.Tracking;
import com.idslatam.solmar.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LocationFusedApi extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ResultCallback<Status> {

    public static Boolean isRunning = false;

    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    Location locationLastSend = null;
    protected String URL_API;
    String NetworkHabilitado, GPSHabilitado, MobileHabilitado, valido = null;
    String lastActividad = null;
    String versionAp = "2.0";
    Calendar currentSend = null;
    Boolean flagSend = false, flagDelay = false;
    int contador = 0, intervalSend = 0;
    int contadorTest = 0, _TrackingUpdateRee_Id = 0, _TrackingSave_Id = 0, _Tracking_Id_Pos = 0;
    protected double nivelBateria = 0;
    protected SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss"),
            formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private boolean mBound = false;

    String sFlagUpdate, sFlagIsGuardar;

    Calendar currentPrecision;

    //**********************************************************************************************
    Tracking tracking = new Tracking();
    private final Context mContext = this;
    private SignalRService mService;
    //**********************************************************************************************

    String number = null, guidDispositivo = null, actividadsql = null, fechaAlarma = null;
    int precision = 0;
    double deltaAltitud = 0;
    float deltaVelocidad = 0;

    private Handler handler = new Handler();

    BroadcastReceiver mReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
/*
            WifiManager wifi = (WifiManager) getSystemService(mContext.WIFI_SERVICE);
            wifi.setWifiEnabled(true);
*/
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

        Log.e("-- LocationFusedApi ", " onCreate");

        //Intent intent = new Intent();
        //intent.setClass(mContext, SignalRService.class);
        //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        //------------------------------------------------------------------------------------------
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

        } catch (Exception e) {
        }

        //Log.e("-- INTERVALo onCREATE ", String.valueOf(intervalSend));

        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            mReceiver = new ScreenReceiver();
            this.getApplicationContext().registerReceiver(mReceiver, filter);

        } catch (IllegalArgumentException e) {
            Log.e("EXCEPTION REGISTER ", e.getMessage());
        }

        buildGoogleApiClient();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mGoogleApiClient.connect();

        if (!this.isRunning) {
            this.isRunning = true;
        }
//            runnable.run();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        unregisterReceiver(this.mReceiver);

        this.isRunning = false;

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        if (mGoogleApiClient.isConnected()) {
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
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        //Log.e("LocationFusedApi ", location.toString());

        //TIMER DE CONTROL DE ENVIO *****************************************************************
        try {

            DBHelper dbHelperIntervalo = new DBHelper(this);
            SQLiteDatabase dba = dbHelperIntervalo.getWritableDatabase();
            String selectQuery = "SELECT IntervaloTracking, FlagUpdate FROM Configuration";
            Cursor ca = dba.rawQuery(selectQuery, new String[]{});

            if (ca.moveToFirst()) {
                intervalSend = ca.getInt(ca.getColumnIndex("IntervaloTracking"));
                sFlagUpdate = ca.getString(ca.getColumnIndex("FlagUpdate"));
            }
            ca.close();
            dba.close();

        } catch (Exception e) {
        }

        if (intervalSend == 0) {
            intervalSend = 1;
        }

        // METODO INTERVALO MENOR A 2 ---------------------------------------------------------------------------------------
        if (true) {
            //if (intervalSend == 1 || intervalSend == 2) {

            if (currentSend == null) {

                currentSend = Calendar.getInstance();
                currentSend.add(Calendar.SECOND, intervalSend);

                currentPrecision = Calendar.getInstance();
                int cS = currentSend.get(Calendar.SECOND) + 15;
                currentPrecision.set(Calendar.SECOND, cS);
            }

            Calendar currentDate = Calendar.getInstance();

            /*Log.e("---------- INFO ", "-----------");
            Log.e("-- C SEND ", formatoIso.format(currentSend.getTime()));
            Log.e("-- C  NOW ", formatoIso.format(currentDate.getTime()));
            Log.e("-- C PREC ", formatoIso.format(currentPrecision.getTime()));
            Log.e("-- F SEND ", String.valueOf(flagSend));
            Log.e("-- F UPDTE ", sFlagUpdate);*/

            if (currentDate.after(currentSend)) {

                flagSend = true;
            }

            flagDelay = false;
            if (currentDate.after(currentPrecision)) {
                flagDelay = true;
                //Log.e("-- DELAY TRUE ", String.valueOf(precision)+ " ----------- ");
            }

            requestActivityUpdates();
            sendTracking(location);
        }
        // FIN METODO INTERVALO MENOR A 2 ---------------------------------------------------------------------------------------

    }

    //ACTIVITY RECOGNITION *****************************************************************************
    public void requestActivityUpdates() {

        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int Apiv = Build.VERSION.SDK_INT;

        if (Apiv <= 20) {
            Intent restartService = new Intent(getApplicationContext(), Recognition.class);
            restartService.setPackage(getPackageName());
            PendingIntent restartServicePI = PendingIntent.getService(getApplicationContext(), 1000, restartService, PendingIntent.FLAG_ONE_SHOT);
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 0, restartServicePI);

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
            Log.e("---| status ", "Error adding or removing activity detection: " + "onResult");
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
            String selectQueryconfiguration = "SELECT NumeroCel, GuidDipositivo, Actividad, FechaEjecucionAlarm, Precision, FlagSave FROM Configuration";
            Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

            if (cConfiguration.moveToFirst()) {
                fechaAlarma = cConfiguration.getString(cConfiguration.getColumnIndex("FechaEjecucionAlarm"));
                actividadsql = cConfiguration.getString(cConfiguration.getColumnIndex("Actividad"));
                number = cConfiguration.getString(cConfiguration.getColumnIndex("NumeroCel"));
                guidDispositivo = cConfiguration.getString(cConfiguration.getColumnIndex("GuidDipositivo"));
                sFlagIsGuardar = cConfiguration.getString(cConfiguration.getColumnIndex("FlagSave"));

                if (sFlagUpdate.equalsIgnoreCase("true")) {
                    precision = cConfiguration.getInt(cConfiguration.getColumnIndex("Precision"));
                }

            }
            cConfiguration.close();
            dbConfiguration.close();

        } catch (Exception e) {
        }

        if (sFlagIsGuardar == null) {
            sFlagIsGuardar = "false";
        }

        if (precision == 0) {
            precision = 24;
        }

        if (precision > 100) {
            precision = 95;
        }

        if (currentDate.after(currentPrecision)) {
            precision = precision + 10;
            //Log.e("-- DELAY PRECISION ", "-- ");
        }

        //Log.e("-- !! Intervalo "+ String.valueOf(intervalSend), " ! Precision "+ String.valueOf(precision));

        if (location.getAccuracy() >= precision) {
            return false;
        }

        if (location.getAltitude() < 0) {
            return false;
        }

        if (locationLastSend == null) {
            locationLastSend = location;
            contador = 3;
            valido = "false";
            //return null;
        }

        if (actividadsql == null) {
            actividadsql = "ACTIVIDADNODETECTADA";
        }
        if (lastActividad == null) {
            lastActividad = "ACTIVIDADNODETECTADA";
        }

        //Log.e("-- !! Contador Fisrt ", String.valueOf(contador));

        //Log.e("------ ACTIVIDAD ", actividadsql);

        if (actividadsql.equalsIgnoreCase("SINMOVIMIENTO")) {
            //Log.e("------ SINMOVIMIENTO", String.valueOf(location.getSpeed()));
            if (location.getSpeed() > 0) {
                //Log.e("------ SINMOVIMIENTO ", " -- VELO: "+String.valueOf(location.getSpeed()));
                if (contador == 0) {
                    contador = 3;
                }
                valido = "false";
                return false;
            }
        }

        if (actividadsql.equalsIgnoreCase("SINMOVIMIENTO") && lastActividad.equalsIgnoreCase("VEHICULO")) {

            if (contador == 0) {
                contador = 4;
                valido = "false";
                //return null;
            }
        }

        if (actividadsql.equalsIgnoreCase("CAMINANDO")) {

            if (location.getSpeed() > 3) {
                if (contador == 0) {
                    contador = 3;
                }
                valido = "false";
            }
        }

        if (actividadsql.equalsIgnoreCase("VEHICULO") && Math.abs(locationLastSend.getBearing() - location.getBearing()) > 95) {
            if (contador == 0) {
                contador = 4;
            }
            valido = "false";
            //return null;
        }

        if (location.getSpeed() >= 14) {

            if (contador == 0) {
                contador = 8;
            }
            valido = "false";
            //return null;
        }

        if (locationLastSend != null) {
            //Log.e("locationLastSend ", locationLastSend.toString());
            deltaVelocidad = Math.abs(locationLastSend.getSpeed() - location.getSpeed());
            deltaAltitud = Math.abs(locationLastSend.getAltitude() - location.getAltitude());

        } else {
            deltaAltitud = 0;
            deltaVelocidad = 0;
        }

        if (deltaVelocidad >= 5 || deltaAltitud > 14) {

            if (contador == 0) {
                contador = 8;
            }

            valido = "false";
            //return null;

        }

        if (contador > 0) {

            if (contador == 1) {
                contadorTest = 1;
            }
            valido = "false";
            contador--;
            //return  false;
        }


        if (contadorTest == 1) {
            contadorTest = 0;
            valido = "true";
        }

        locationLastSend = location;
        lastActividad = actividadsql;

        isGPSAvailable();
        isMOBILEAvailable();
        isWIFIAvailable();

        try {
            IntentFilter batIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = this.registerReceiver(null, batIntentFilter);
            nivelBateria = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        } catch (Exception e) {
        }

        TrackingCrud trackingCRUD = new TrackingCrud(this);

        tracking.Numero = number;
        tracking.DispositivoId = guidDispositivo;
        tracking.FechaCelular = formatoGuardar.format(currentDate.getTime());
        //Log.e("-- FechaCelular Service", formatoGuardar.format(currentDate.getTime()));
        tracking.Latitud = Double.toString(location.getLatitude());
        tracking.Longitud = Double.toString(location.getLongitude());
        if (flagDelay == true) {
            tracking.EstadoCoordenada = "DELAY";
        } else {
            tracking.EstadoCoordenada = "OK";
        }
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
        tracking.VersionApp = versionAp;
        tracking.FechaAlarma = fechaAlarma;
        tracking.Time = formatoGuardar.format(location.getTime());
        tracking.ElapsedRealtimeNanos = Long.toString(location.getElapsedRealtimeNanos());
        tracking.Altitude = Double.toString(location.getAltitude());
        tracking.Bearing = Double.toString(location.getBearing());
        tracking.Extras = "Tracking@5246.Solmar";
        tracking.Classx = "Location";
        tracking.Actividad = actividadsql;
        tracking.Valido = valido;
        tracking.FechaIso = formatoIso.format(currentDate.getTime());
        //si es valido guarde en el sqlite
        //guardar puntos menores a la precision
        if (valido == "true") {
            tracking.Intervalo = Integer.toString(intervalSend);
        } else {
            tracking.Intervalo = "0";
        }

        if (valido == "true") {

            try {
                DBHelper dataBaseHelper = new DBHelper(this);
                SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                db.execSQL("UPDATE Configuration SET Longitud = '" + Double.toString(location.getLongitude()) + "'");
                db.execSQL("UPDATE Configuration SET Latitud = '" + Double.toString(location.getLatitude()) + "'");
                db.close();
            } catch (Exception e) {
            }

        }

        if (valido == "true" && flagSend == true) {

            try {

               // _TrackingSave_Id = trackingCRUD.insertAll(tracking);
                eliminarRegistro();

            } catch (Exception e) {}

            Log.e("-- |*** UPDATE ***", "| -- ");

            currentSend.add(Calendar.MINUTE, intervalSend);
            currentPrecision.add(Calendar.MINUTE, intervalSend);
            flagSend = false;

            Calendar update = Calendar.getInstance();

            if (update.after(currentSend)) {

                long milis1, milis2, diff;
                milis1 = currentSend.getTimeInMillis();
                milis2 = update.getTimeInMillis();
                diff = milis2 - milis1;

                long diffMinutos = Math.abs(diff / (60 * 1000));
                int i = (int) diffMinutos;

                currentSend.add(Calendar.MINUTE, i);
                currentPrecision.add(Calendar.MINUTE, i);

            }

            consultaSinConexion();

            consultarAlertasNoEnviadas();


            String URL = URL_API.concat("api/Tracking");

            JsonObject json = new JsonObject();
            json.addProperty("Numero", number);
            json.addProperty("DispositivoId", guidDispositivo);
            json.addProperty("FechaCelular", formatoGuardar.format(currentDate.getTime()));
            json.addProperty("Latitud", Double.toString(location.getLatitude()));
            json.addProperty("Longitud", Double.toString(location.getLongitude()));
            if (flagDelay == true) {
                json.addProperty("EstadoCoordenada", "DELAY");
            } else {
                json.addProperty("EstadoCoordenada", "OK");
            }
            json.addProperty("OrigenCoordenada", "fused");
            json.addProperty("Velocidad", Double.toString(location.getSpeed()));
            json.addProperty("Bateria", Double.toString(nivelBateria));
            json.addProperty("Precision", Double.toString(location.getAccuracy()));
            json.addProperty("SenialCelular", "5");
            json.addProperty("GpsHabilitado", GPSHabilitado);
            json.addProperty("WifiHabilitado", NetworkHabilitado);
            json.addProperty("DatosHabilitado", MobileHabilitado);
            json.addProperty("ModeloEquipo", Build.MODEL);
            json.addProperty("Imei", telephonyManager.getDeviceId());
            json.addProperty("VersionApp", versionAp);
            json.addProperty("FechaAlarma", fechaAlarma);
            json.addProperty("Time", formatoGuardar.format(location.getTime()));
            json.addProperty("ElapsedRealtimeNanos", Long.toString(location.getElapsedRealtimeNanos()));
            json.addProperty("Altitude", Double.toString(location.getAltitude()));
            json.addProperty("Bearing", Double.toString(location.getBearing()));
            //json.addProperty("Extras", "Tracking@5246.Solmar");
            //json.addProperty("Classx", "Location");
            json.addProperty("Actividad", actividadsql);
            json.addProperty("Valido", valido);
            if (valido == "true") {
                json.addProperty("Intervalo", Integer.toString(intervalSend));
            } else {
                json.addProperty("Intervalo", "0");
            }

            Ion.with(this)
                    .load("POST", URL)
                    .setJsonObjectBody(json)
                    .asJsonObject()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<JsonObject>>() {
                        @Override
                        public void onCompleted(Exception e, Response<JsonObject> response) {

                            if (response == null) {

                                saveError(tracking);
                                Log.e("¡ERROR DE RED! ", " -- Tracking saveError --");

                                return;
                            }

                            if (response.getHeaders().code() == 200) {

                                Gson gson = new Gson();
                                JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                                Log.e("JsonObject Track ", result.toString());

                                if (!result.get("Configuracion").isJsonNull()) {

                                    /*Intent call = new Intent(Intent.ACTION_CALL);
                                    call.setData(Uri.parse("tel://" + "123" + ""));
                                    call.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    call.addFlags(Intent.FLAG_FROM_BACKGROUND);

                                    handler.postDelayed(r, 1000*10);

                                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                        // TODO: Consider calling
                                        //    ActivityCompat#requestPermissions
                                        // here to request the missing permissions, and then overriding
                                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                        //                                          int[] grantResults)
                                        // to handle the case where the user grants the permission. See the documentation
                                        // for ActivityCompat#requestPermissions for more details.
                                        return;
                                    }
                                    startActivity(call);*/

                                    JsonArray jarray = result.getAsJsonArray("Configuracion");

                                    for (JsonElement pa : jarray) {

                                        JsonObject paymentObj = pa.getAsJsonObject();

                                        if(paymentObj.get("ConfiguracionId").getAsString().equalsIgnoreCase("7")){
                                            DBHelper dataBaseHelper = new DBHelper(mContext);
                                            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                                            db.execSQL("UPDATE Configuration SET Precision = '" + paymentObj.get("Valor").getAsString() + "'");
                                            db.close();
                                        }

                                        if(paymentObj.get("ConfiguracionId").getAsString().equalsIgnoreCase("1")){
                                            DBHelper dataBaseHelper = new DBHelper(mContext);
                                            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                                            db.execSQL("UPDATE Configuration SET IntervaloTracking = '" + paymentObj.get("Valor").getAsString() + "'");
                                            db.close();
                                        }

                                        Log.e(" ConfiguracionId", paymentObj.get("ConfiguracionId").getAsString());
                                        Log.e(" Valor", paymentObj.get("Valor").getAsString());

                                    }

                                } else {
                                    Log.e("¡Configuration NULL ! ", " -- Tracking saveError --");
                                }

                            } else  {

                                saveError(tracking);
                                Log.e("¡ERROR DE SERVER! ", " -- Tracking saveError --");

                            }

                        }
                    });

            try {

                DBHelper dbHelperAlarm = new DBHelper(this);
                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                dba.execSQL("UPDATE Configuration SET FlagUpdate = 'true'");
                dba.close();

            } catch (Exception e){}

        } else {

            try {

                DBHelper dbHelperAlarm = new DBHelper(this);
                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                dba.execSQL("UPDATE Configuration SET FlagUpdate = 'false'");
                dba.close();

            } catch (Exception e){}

        }

        return  true;
    }


    final Runnable r = new Runnable() {
        public void run() {

            Log.e("Hello World", " Runnable I ");
            endCall(mContext);
            Log.e("Hello World", " Runnable ");
            //handler.postDelayed(this, 1000*10);
        }
    };


    public void endCall(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Class c = Class.forName(tm.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            Object telephonyService = m.invoke(tm);

            c = Class.forName(telephonyService.getClass().getName());
            m = c.getDeclaredMethod("endCall");
            m.setAccessible(true);
            m.invoke(telephonyService);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            try {
                // We've bound to SignalRService, cast the IBinder and get SignalRService instance
                //SignalRService.LocalBinder binder = (SignalRService.LocalBinder) service;
                //mService = binder.getService();
                //mBound = true;

            } catch (Exception e){}

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e("Close SignalR", "Closed Yeah");
            //mBound = false;
        }
    };

    public Boolean consultaSinConexion(){

        Log.e("-- COSULTA SIN ", "--|CONEXION|--");

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbN = dataBaseHelper.getWritableDatabase();
            String selectQueryBuscaN = "SELECT NumeroCel FROM Tracking WHERE EstadoEnvio = 'false'";
            Cursor cbuscaN = dbN.rawQuery(selectQueryBuscaN, new String[]{}, null);
            int contador = cbuscaN.getCount();
            cbuscaN.close();
            dbN.close();

            //Log.e("-- POR SEND ", String.valueOf(contador));

            if (contador>0) {
                sendSave();
                Log.e("-- if ", "--||--");
            }

        }catch (Exception e){
            Log.e("-- Error Reenvio Track", "EstadoEnvio");
        }

        return true;
    }

    public Boolean consultarAlertasNoEnviadas(){


        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbN = dataBaseHelper.getWritableDatabase();
            String selectQueryBuscaN = "SELECT FechaMarcacion FROM Alert WHERE Estado = 'false' AND EstadoBoton = 'true'";
            Cursor cbuscaN = dbN.rawQuery(selectQueryBuscaN, new String[]{}, null);
            int contadorAlertas = cbuscaN.getCount();
            cbuscaN.close();
            dbN.close();

            if (contadorAlertas>0) {
                enviarAlertasGuardadas();
                Log.e("-- EXISTEN ALERTAS ", "--|POR ENVIAR|-- "+String.valueOf(contadorAlertas) );
            } else {
                Log.e("-- NO HAY ALERTAS ", "--|POR ENVIAR|--");
            }

        }catch (Exception e){}

        return true;
    }

    public void enviarAlertasGuardadas(){

        int _Alert_id_Consultado = 0;

        String fechaEspera = null, numeroCel = null, fechaMarcacion = null,
                fechaProxima = null, dispositivoId = null, codigoEmpleado = null,
                finTurno = null, flagTiempo = null, latitud = null, longitud = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase existeDatos = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT AlertId, NumeroCel, FechaEsperada, FechaMarcacion, FechaProxima, " +
                    "DispositivoId, CodigoEmpleado, FinTurno, FlagTiempo, Latitud, Longitud FROM Alert WHERE Estado = 'false' AND EstadoBoton = 'true'";
            Cursor cA = existeDatos.rawQuery(selectQueryconfiguration, new String[]{});

            if (cA.moveToFirst()) {
                do {
                    _Alert_id_Consultado = cA.getInt(cA.getColumnIndex("AlertId"));
                    numeroCel = cA.getString(cA.getColumnIndex("NumeroCel"));
                    fechaEspera = cA.getString(cA.getColumnIndex("FechaEsperada"));
                    fechaMarcacion = cA.getString(cA.getColumnIndex("FechaMarcacion"));
                    fechaProxima = cA.getString(cA.getColumnIndex("FechaProxima"));
                    dispositivoId = cA.getString(cA.getColumnIndex("DispositivoId"));
                    codigoEmpleado = cA.getString(cA.getColumnIndex("CodigoEmpleado"));
                    finTurno = cA.getString(cA.getColumnIndex("FinTurno"));
                    flagTiempo = cA.getString(cA.getColumnIndex("FlagTiempo"));
                    latitud = cA.getString(cA.getColumnIndex("Latitud"));
                    longitud = cA.getString(cA.getColumnIndex("Longitud"));

                    Log.e("FECHA GUARDADA ",fechaEspera);

                    registrarAlertaApi(_Alert_id_Consultado, numeroCel, fechaEspera, fechaMarcacion, fechaProxima, dispositivoId, codigoEmpleado, finTurno, flagTiempo, latitud, longitud);

                } while(cA.moveToNext());

            }
            cA.close();
            existeDatos.close();

        } catch (Exception e) {Log.e("Exception ", e.getMessage());}

    }

    public void registrarAlertaApi(int _Alert_id_Consultado, String numeroCel, String fechaEspera, String fechaMarcacion, String fechaProxima, String dispositivoId, String codigoEmpleado, String finTurno, String flagTiempo, String latitud, String longitud){

        Log.e("INGRESO ENVIO ", "ALERTA API "+ _Alert_id_Consultado);

        Calendar currentMarcacion = Calendar.getInstance();
        Calendar currentEsperada = Calendar.getInstance();
        Calendar currentProxima = Calendar.getInstance();

        try {currentMarcacion.setTime(formatoIso.parse(fechaMarcacion));}
        catch (ParseException e) {Log.e(" EXCEPTION ", "Error al Parsear FECHAAA ");e.printStackTrace();}

        try {currentEsperada.setTime(formatoIso.parse(fechaEspera));}
        catch (ParseException e) {Log.e(" EXCEPTION ", "Error al Parsear FECHAAA ");e.printStackTrace();}

        try {currentProxima.setTime(formatoIso.parse(fechaProxima));}
        catch (ParseException e) {Log.e(" EXCEPTION ", "Error al Parsear FECHAAA ");e.printStackTrace();}

        /*Log.e("-----------SEND  ","ALERT-----------");
        Log.e("--- Numero ", numeroCel);
        Log.e("--- FechaMarcacion ", formatoGuardar.format(currentMarcacion.getTime()));
        Log.e("--- FechaEsperada ", formatoGuardar.format(currentEsperada.getTime()));
        Log.e("--- FechaProxima ", formatoGuardar.format(currentProxima.getTime()));
        Log.e("--- FlagTiempo ", flagTiempo);
        Log.e("--- MargenAceptado ", "1");
        Log.e("--- Latitud ", latitud);
        Log.e("--- Longitud ", longitud);
        Log.e("--- DispositivoId ", dispositivoId);
        Log.e("--- CodigoEmpleado ", codigoEmpleado);
        Log.e("--------- FIN SEND  ","ALERT---------");*/


        String URL = URL_API.concat("api/alert");

        JsonObject json = new JsonObject();
        json.addProperty("Numero", numeroCel);
        json.addProperty("FechaMarcacion", formatoGuardar.format(currentMarcacion.getTime()));
        json.addProperty("FechaEsperada", formatoGuardar.format(currentEsperada.getTime()));
        json.addProperty("FechaProxima", formatoGuardar.format(currentProxima.getTime()));
        json.addProperty("FlagTiempo", flagTiempo);
        json.addProperty("MargenAceptado", "1");
        json.addProperty("Latitud", latitud);
        json.addProperty("Longitud", longitud);
        json.addProperty("DispositivoId", dispositivoId);
        json.addProperty("CodigoEmpleado", codigoEmpleado);

        Ion.with(this)
                .load("POST", URL)
                .setJsonObjectBody(json)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> response) {

                        if(response == null){
                            Log.e(" responde Alert ", " NULL ");
                            return;
                        }

                        if (response.getHeaders().code() == 200) {

                            AlertCrud alertCrud = new AlertCrud(mContext);
                            Alert alert = new Alert();
                            alert.EstadoA = "true";
                            alert.AlertId = _Alert_id_Consultado;
                            alertCrud.updateEstado(alert);

                            Log.e("Alerta GUARDADA ", response.getResult().toString());


                        }
                    }
                });

    }


    public void sendSave() {

        int i =0;
        Log.e("--|| Reenvio ", "sendSave ||");

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            String selectQuery = "SELECT TrackingId, NumeroCel, DispositivoId, FechaCelular, Latitud, Longitud, EstadoCoordenada, " +
                    "OrigenCoordenada, Velocidad, Bateria, Precision, SenialCelular, GpsHabilitado, WifiHabilitado, " +
                    "DatosHabilitado, ModeloEquipo, Imei, VersionApp, FechaAlarma, Time, ElapsedRealtimeNanos, " +
                    "Altitude, Bearing, Extras, Classx, Actividad, Valido, Intervalo, EstadoEnvio FROM Tracking WHERE EstadoEnvio = 'false'";
            Cursor c = db.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                do {
                    Tracking trackingPos = new Tracking();

                    _TrackingUpdateRee_Id = c.getInt(c.getColumnIndex("TrackingId"));

                    trackingPos.Numero = c.getString(c.getColumnIndex("NumeroCel"));
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

                    //mService.sendMessage(trackingPos);
                    String URL = URL_API.concat("api/Tracking");

                    JsonObject json = new JsonObject();
                    json.addProperty("Numero", c.getString(c.getColumnIndex("NumeroCel")));
                    json.addProperty("DispositivoId", c.getString(c.getColumnIndex("DispositivoId")));
                    json.addProperty("FechaCelular", c.getString(c.getColumnIndex("FechaCelular")));
                    json.addProperty("Latitud", c.getString(c.getColumnIndex("Latitud")));
                    json.addProperty("Longitud", c.getString(c.getColumnIndex("Longitud")));
                    json.addProperty("EstadoCoordenada", c.getString(c.getColumnIndex("EstadoCoordenada")));
                    json.addProperty("OrigenCoordenada", "fused");
                    json.addProperty("Velocidad", c.getString(c.getColumnIndex("Velocidad")));
                    json.addProperty("Bateria", c.getString(c.getColumnIndex("Bateria")));
                    json.addProperty("Precision", c.getString(c.getColumnIndex("Precision")));
                    json.addProperty("SenialCelular", "5");
                    json.addProperty("GpsHabilitado", c.getString(c.getColumnIndex("GpsHabilitado")));
                    json.addProperty("WifiHabilitado", c.getString(c.getColumnIndex("WifiHabilitado")));
                    json.addProperty("DatosHabilitado", c.getString(c.getColumnIndex("DatosHabilitado")));
                    json.addProperty("ModeloEquipo", c.getString(c.getColumnIndex("ModeloEquipo")));
                    json.addProperty("Imei", c.getString(c.getColumnIndex("Imei")));
                    json.addProperty("VersionApp", c.getString(c.getColumnIndex("VersionApp")));
                    json.addProperty("FechaAlarma", c.getString(c.getColumnIndex("FechaAlarma")));
                    json.addProperty("Time", c.getString(c.getColumnIndex("Time")));
                    json.addProperty("ElapsedRealtimeNanos", c.getString(c.getColumnIndex("ElapsedRealtimeNanos")));
                    json.addProperty("Altitude", c.getString(c.getColumnIndex("Altitude")));
                    json.addProperty("Bearing", c.getString(c.getColumnIndex("Bearing")));
                    //json.addProperty("Extras", "Tracking@5246.Solmar");
                    //json.addProperty("Classx", "Location");
                    json.addProperty("Actividad", c.getString(c.getColumnIndex("Actividad")));
                    json.addProperty("Valido", c.getString(c.getColumnIndex("Valido")));
                    json.addProperty("Intervalo", c.getString(c.getColumnIndex("Intervalo")));

                    Ion.with(this)
                            .load("POST", URL)
                            .setJsonObjectBody(json)
                            .asJsonObject()
                            .withResponse()
                            .setCallback(new FutureCallback<Response<JsonObject>>() {
                                @Override
                                public void onCompleted(Exception e, Response<JsonObject> response) {

                                    if(response == null){
                                        saveError(trackingPos);
                                        Log.e("¡ERROR DE RED! ", " -- Tracking saveError --");

                                        return;
                                    }

                                    if (response.getHeaders().code() != 200) {
                                        saveError(trackingPos);
                                    }

                                }
                            });

                    i++;

                } while(c.moveToNext() || i<30);

            }
            c.close();
            db.close();

        } catch (Exception e){}

    }

    public void saveError(Tracking marker){

        Log.e("-- INGRESÓ ", "saveError");

        try {

            TrackingCrud trackingCRUD = new TrackingCrud(this);
            marker.EstadoEnvio = "false";
            marker.TrackingId = _Tracking_Id_Pos;
            _Tracking_Id_Pos = trackingCRUD.insert(marker);

        }catch (Exception e){}

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

    public void eliminarRegistro(){

        try {
            SimpleDateFormat fchActual = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fechaActual = fchActual.format(new Date());
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.execSQL("DELETE FROM Tracking WHERE FechaIso < datetime('"+fechaActual+"','-180 minutes')");
            db.close();

            Log.e(" EliminarRegistro ", " -- ");

        }catch (Exception e){}

    }

    /*public void eliminarRegistro(){

        try {

            SimpleDateFormat fchActual = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fechaActual = fchActual.format(new Date());
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.execSQL("DELETE FROM Tracking WHERE EstadoEnvio IS NULL AND FechaIso < datetime('"+fechaActual+"','-10 minutes')");
            db.close();

        }catch (Exception e){}

    }*/

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

        Toast.makeText(this, "Activando Datos..", Toast.LENGTH_SHORT).show();
    }

    // FIN DE METODOS PARA ACCESO A CONFIGURACIONES **************************************************************************
}
