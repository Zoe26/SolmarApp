package com.idslatam.solmar.Tracking.Services;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    protected String URL_API;
    String NetworkHabilitado,GPSHabilitado,MobileHabilitado;
    protected int _Tracking_Id=0;
    protected double nivelBateria=0;
    protected SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss"),
            formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //**********************************************************************************************
    private HubConnection mHubConnection;
    private HubProxy mHubProxy;
    private Handler mHandler; // to display Toast message
    private final IBinder mBinder = new LocalBinder(); // Binder given to client
    Tracking tracking = new Tracking();
    //**********************************************************************************************

    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service.
//        startSignalR();
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();
        mHandler = new Handler(Looper.getMainLooper());
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
        mHubConnection.stop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    // METODOS SIGNALR *********************************************************************************
    public class LocalBinder extends Binder {
        public LocationFusedApi getService() {
            // Return this instance of SignalRService so clients can call public methods
            return LocationFusedApi.this;
        }
    }

    public void sendMessage(String message) {
        String SERVER_METHOD_SEND = "Send";
        mHubProxy.invoke(SERVER_METHOD_SEND, message);
    }

    private void startSignalR() {
        Platform.loadPlatformComponent(new AndroidPlatformComponent());

        /*Credentials credentials = new Credentials() {
            @Override
            public void prepareRequest(Request request) {
                request.addHeader("User-Name", "BNK");
            }
        };*/

        String serverUrl = "http://solmar.azurewebsites.net/";
        mHubConnection = new HubConnection(serverUrl);
        //mHubConnection.setCredentials(credentials);
        String SERVER_HUB_CHAT = "trackingHub";
        mHubProxy = mHubConnection.createHubProxy(SERVER_HUB_CHAT);
        ClientTransport clientTransport = new ServerSentEventsTransport(mHubConnection.getLogger());
        SignalRFuture<Void> signalRFuture = mHubConnection.start(clientTransport);
        Log.e("Signal R", signalRFuture.toString());
        try {
            signalRFuture.get();
            Log.e("Try", "startSignalR");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }



        mHubProxy.invoke(String.class, "addMarker", tracking).done(new Action<String>() {
            @Override
            public void run(String s) throws Exception {
                Log.e("Signal R", "Ejecución Ok");
                Log.w("SimpleSignalR", s);
            }
        }).onError(new ErrorCallback() {
            @Override
            public void onError(Throwable throwable) {
                Log.e("SimpleSignalR", throwable.toString());
            }
        });

        String HELLO_MSG = "Hello from Android!";
        sendMessage(HELLO_MSG);

        Log.e("Signal R ENvio", "Envio de mensaje");

        String CLIENT_METHOD_BROADAST_MESSAGE = "addNewMessageToPage";

        mHubProxy.on(CLIENT_METHOD_BROADAST_MESSAGE,
                new SubscriptionHandler1<CustomMessage>() {
                    @Override
                    public void run(final CustomMessage msg) {
                        final String finalMsg = msg.UserName + " says " + msg.Message;
                        // display Toast message
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), finalMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                , CustomMessage.class);
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
//        Log.e("LocationFusedApi ", location.toString());
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

        String numberDevice, number = null, FechaCelular, Latitud, Longitud, EstadoCoordenada ,
                OrigenCoordenada, Velocidad, Bateria, Precision, SenialCelular, GpsHabilitado,
                WifiHabilitado, DatosHabilitado, ModeloEquipo, Imei, VersionApp, FechaEjecucionAlarm,
                Time, ElapsedRealtimeNanos, Altitude, Bearing, Extras, Class, guidDispositivo=null;


        isGPSAvailable();
        isMOBILEAvailable();
        isWIFIAvailable();

        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        Calendar currentDate = Calendar.getInstance();

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbConfiguration = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT NumeroCel, GuidDipositivo FROM Configuration";
            Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

            if (cConfiguration.moveToFirst()) {
                number = cConfiguration.getString(cConfiguration.getColumnIndex("NumeroCel"));
                guidDispositivo = cConfiguration.getString(cConfiguration.getColumnIndex("GuidDipositivo"));
            }
            cConfiguration.close();
            dbConfiguration.close();
        } catch (Exception e) {
        }
        try {
            IntentFilter batIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = this.registerReceiver(null, batIntentFilter);
            nivelBateria = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        } catch (Exception e) {}


        numberDevice = number;//"945783335";//"931732035";
        FechaCelular = formatoGuardar.format(currentDate.getTime());
        Latitud = Double.toString(location.getLatitude());
        Longitud = Double.toString(location.getLongitude());
        EstadoCoordenada = "OK";
        OrigenCoordenada = "fused";
        Velocidad = Double.toString(location.getSpeed());
        Bateria = Double.toString(nivelBateria);
        Precision = Double.toString(location.getAccuracy());
        SenialCelular = "5";
        GpsHabilitado = GPSHabilitado;
        WifiHabilitado = NetworkHabilitado;
        DatosHabilitado = MobileHabilitado;
        ModeloEquipo = Build.MODEL;
        Imei = telephonyManager.getDeviceId();
        VersionApp = Integer.toString(Build.VERSION.SDK_INT);
        FechaEjecucionAlarm = formatoGuardar.format(currentDate.getTime());
        Time = formatoGuardar.format(location.getTime());
        ElapsedRealtimeNanos = Long.toString(location.getElapsedRealtimeNanos());
        Altitude = Double.toString(location.getAltitude());;
        Bearing = Double.toString(location.getBearing());;
        Extras = "Tracking@5246.Solmar";
        Class = "Location";

        tracking.Numero = number;//"945783335";//"931732035";
        tracking.FechaCelular = formatoGuardar.format(currentDate.getTime());
        tracking.Latitud = Double.toString(location.getLatitude());
        tracking.Longitud = Double.toString(location.getLongitude());
        tracking.EstadoCoordenada = "OK";
        tracking.OrigenCoordenada = "fused";
        tracking.Velocidad = Double.toString(location.getSpeed());
        tracking.Bateria = Double.toString(nivelBateria);
        tracking.Presicion = Double.toString(location.getAccuracy());
        tracking.SenialCelular = "5";
        tracking.GpsHabilitado = GPSHabilitado;
        tracking.WifiHabilitado = NetworkHabilitado;
        tracking.DatosHabilitado = MobileHabilitado;
        tracking.ModeloEquipo = Build.MODEL;
        tracking.Imei = telephonyManager.getDeviceId();
        tracking.VersionApp = Integer.toString(Build.VERSION.SDK_INT);
        tracking.FechaEjecucionAlarm = formatoGuardar.format(currentDate.getTime());
        tracking.Time = formatoGuardar.format(location.getTime());
        tracking.ElapsedRealtimeNanos = Long.toString(location.getElapsedRealtimeNanos());
        tracking.Altitude = Double.toString(location.getAltitude());;
        tracking.Bearing = Double.toString(location.getBearing());;
        tracking.Extras = "Tracking@5246.Solmar";
        tracking.Class = "Location";
        tracking.Actividad = "NO";

        SignalRService signalRService = new SignalRService();
        signalRService.sendMessage(tracking);


//        new PostAsync().execute(numberDevice, FechaCelular, Latitud, Longitud, EstadoCoordenada, OrigenCoordenada, Velocidad,
//                Bateria, Precision, SenialCelular, GpsHabilitado, WifiHabilitado, DatosHabilitado, ModeloEquipo, Imei,
//                VersionApp, FechaEjecucionAlarm, Time, ElapsedRealtimeNanos, Altitude, Bearing, Extras, Class, guidDispositivo);

        return  true;
    }

    // HILO DE ENVIO ***********************************************************************************
    class PostAsync extends AsyncTask<String, String, JSONObject> {

        JsonParser jsonParser = new JsonParser();

        private ProgressDialog pDialog;

        private final String URL = URL_API.concat("api/Tracking");//"http://solmar.azurewebsites.net/api/Tracking";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {

                HashMap<String, String> params = new HashMap<>();

                params.put("Numero", args[0]);
                params.put("FechaCelular", args[1]);
                params.put("Latitud", args[2]);
                params.put("Longitud", args[3]);
                params.put("EstadoCoordenada", args[4]);
                params.put("OrigenCoordenada", args[5]);
                params.put("Velocidad", args[6]);
                params.put("Bateria", args[7]);
                params.put("Precision", args[8]);
                params.put("SenialCelular", args[9]);
                params.put("GpsHabilitado", args[10]);
                params.put("WifiHabilitado", args[11]);
                params.put("DatosHabilitado", args[12]);
                params.put("ModeloEquipo", args[13]);
                params.put("Imei", args[14]);
                params.put("VersionApp", args[15]);
                params.put("FechaAlarma", args[16]);
                params.put("Time", args[17]);
                params.put("ElapsedRealtimeNanos", args[18]);
                params.put("Altitude", args[19]);
                params.put("Bearing", args[20]);
                params.put("Extras", args[21]);
                params.put("Classx", args[22]);
                params.put("DispositivoId", args[23]);

                JSONObject json = jsonParser.makeHttpRequest(URL, "POST", params);

                if (json != null) {

                    Log.e("JSON", json.toString());
                    return json;

                } else {
                    Log.e("HTTP", "Error Json Request");
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(JSONObject json) {

            int success = 0;
            String message = "";


            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }

        }
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

}
