package com.idslatam.solmar.View;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.JsonObject;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Api.Parser.JsonParser;
import com.idslatam.solmar.BravoPapa.ScreenReceiver;
import com.idslatam.solmar.Models.Crud.ConfigurationCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Configuration;
import com.idslatam.solmar.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Bienvenido extends AppCompatActivity implements View.OnClickListener
        ,GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener{

    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private static final int ADMIN_INTENT = 1;
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mComponentName;

    //--------------------------------------------------------
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    final static int REQUEST_LOCATION = 199;

    //---------------------------------------------------------
    private EditText value;
    private Button btn, btnC;
    private int _Configuration_Id = 0;
    TelephonyManager tm;
    String numero;

    String cNumero, cGuidDispositivo, cToken, cOutApp, cIntervaloTracking, cIntervaloAlert, cMargenAlert;
    boolean flagIsFused = false, flagIsPlaySevice = true, flagIsUpdate = false;

    int buscaN;
    int busca;

    ConfigurationCrud configurationCRUD = new ConfigurationCrud(this);

    DBHelper dataBaseHelper = new DBHelper(this);

    String SimOtorgaNumero="false";
    String validacion;
    String NumeroReinstlado;
    protected String URL_API;
    String estado, RequiereNumero, Id;
    TextView txtApro;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int MY_READ_PHONE_STATE = 1 ;
    private static final int MY_ACCESS_FINE_LOCATION = 2 ;
    private static final int MY_ACCESS_COARSE_LOCATION = 3 ;
    private static final int MY_INTERNET = 3 ;
    private static final int MY_ACCESS_NETWORK_STATE = 4;
    private static final int MY_VIBRATE = 5;
    private static final int MY_CAMERA = 6;
    private static final int MY_WRITE_SETTINGS = 7;
    private static final int MY_WRITE_EXTERNAL_STORAGE = 8;
    private static final int MY_PACKAGE_USAGE_STATS = 9;

    String serieSIM;
    String imei;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenido);

        dataAccessSettings();

        txtApro = (TextView)findViewById(R.id.text_aprobacion);

        //**********************************************************************************************************************

        try {
            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifi.setWifiEnabled(true);

            setMobileDataEnabled(this, true);
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

        //*********************************************************************************************************************



        // PERMISO DE LEER TELEFONO
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_READ_PHONE_STATE);

            }
        }

        // PERMISO DE GPS

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_ACCESS_FINE_LOCATION);
            }
        }

        // PERMISO DE INTERNET

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, MY_INTERNET);
            }
        }

        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_NETWORK_STATE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, MY_ACCESS_NETWORK_STATE);
            }
        }*/

        // PERMISO DEL SENSOR DE VIBRACION

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.VIBRATE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.VIBRATE}, MY_VIBRATE);
            }
        }

        // PERMISO DE LA CAMARA

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA);
            }
        }

        // PERMISO DE MEDIO EXTERNO

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_WRITE_EXTERNAL_STORAGE);
            }
        }

        // PERMISO DE LA CONFIGURACION

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_SETTINGS)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_SETTINGS}, MY_WRITE_SETTINGS);
            }
        }

        // PERMISO DE LA APP

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.PACKAGE_USAGE_STATS)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.PACKAGE_USAGE_STATS)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.PACKAGE_USAGE_STATS}, MY_PACKAGE_USAGE_STATS);
            }
        }

        //**********************************************************************************************************************************

        if (mGoogleApiClient== null) {

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(Bienvenido.this).build();
            mGoogleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            // **************************
            builder.setAlwaysShow(true);
            // **************************

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                    .checkLocationSettings(mGoogleApiClient, builder.build());

            Log.e("builder Last ",String.valueOf(builder));
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result
                            .getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can
                            // initialize location
                            // requests here.
                            flagIsFused = true;
                            Log.e("Request SUCCESS ",String.valueOf(status));
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be
                            // fixed by showing the user
                            // a dialog.
                            Log.e("RESOLUTION_REQUIRED ",String.valueOf(status));
                            try {
                                // Show the dialog by calling
                                // startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(Bienvenido.this, REQUEST_LOCATION);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                                Log.e("ERROR RESOLUTION ", e.getMessage());
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have
                            // no way to fix the
                            // settings so we won't show the dialog.
                            Log.e("Request ", "SETTINGS_CHANGE_UNAVAILABLE "+String.valueOf(status));
                            break;
                    }
                }
            });
        }

        //-------------------------------------------------------------------------------------------------------------------------
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                toggle();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        //--------------------------------------------------------------------------------------------------------------------------
        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();


        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);

        int networkType = tm.getNetworkType();
        switch (networkType) {
            case (TelephonyManager.NETWORK_TYPE_1xRTT) :
                Log.e("--NETWORK_TYPE_1xRTT ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_CDMA) :
                Log.e("--PHONE_TYPE_CDMA ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_EDGE) :
                Log.e("--NETWORK_TYPE_EDGE ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_EHRPD) :
                Log.e("--NETWORK_TYPE_EHRPD ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_EVDO_0) :
                Log.e("--NETWORK_TYPE_EVDO_0 ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_EVDO_A) :
                Log.e("--NETWORK_TYPE_EVDO_A ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_EVDO_B) :
                Log.e("--NETWORK_TYPE_EVDO_B ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_GPRS) :
                Log.e("--NETWORK_TYPE_GPRS ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_HSDPA) :
                Log.e("--NETWORK_TYPE_HSDPA ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_HSPA) :
                Log.e("--NETWORK_TYPE_HSPA ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_LTE) :
                Log.e("--NETWORK_TYPE_LTE ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_UMTS) :
                Log.e("--NETWORK_TYPE_UMTS ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_UNKNOWN) :
                Log.e("--NETWORK_TYPE_UNKNOWN ", String.valueOf(networkType));
                break;

        }

        int phoneType=tm.getPhoneType();

        switch (phoneType)
        {
            case (TelephonyManager.PHONE_TYPE_CDMA):
                // your code
                Log.e("--PHONE_TYPE_CDMA ", String.valueOf(phoneType));
                break;
            case (TelephonyManager.PHONE_TYPE_GSM):
                // your code
                Log.e("--PHONE_TYPE_GSM ", String.valueOf(phoneType));
                break;
            case (TelephonyManager.PHONE_TYPE_NONE):
                // your code
                Log.e("--PHONE_TYPE_NONE ", String.valueOf(phoneType));
                break;
        }

        int SIMState=tm.getSimState();
        switch(SIMState)
        {
            case TelephonyManager.SIM_STATE_ABSENT :
                // your code
                Log.e("--STATE_ABSENT ", String.valueOf(SIMState));

                //serieSIM = "8951061121515203889f";
                //imei = "014578003254447";
                //numero = "931732035";

                //simDialogo();
                break;

            case TelephonyManager.SIM_STATE_READY :
                // your code

                serieSIM = tm.getSimSerialNumber();
                imei = tm.getDeviceId();
                numero = tm.getLine1Number();

                SimOtorgaNumero = "true";
                if (numero.equals("")) {SimOtorgaNumero = "false";}

                Log.e("--STATE_READY ", String.valueOf(SIMState));
                Log.e("--Country ", String.valueOf(tm.getSimCountryIso()));
                Log.e("--OperatorCode ", String.valueOf(tm.getSimOperator()));
                Log.e("--OperatorName ", String.valueOf(tm.getSimOperatorName()));
                Log.e("--simSerial ", String.valueOf(tm.getSimSerialNumber()));

                break;

            case TelephonyManager.SIM_STATE_UNKNOWN :
                // your code
                Log.e("--STATE_UNKNOWN ", String.valueOf(SIMState));
                break;

        }

        isWIFIAvailable();

        //**********************DESPUES DE VALIDAR SIN SE OBTIENE DATOS DEL TELEFONO************************************
        String modelo = Build.MODEL;
        String fabricante = Build.MANUFACTURER;
        int versionOS = Build.VERSION.SDK_INT;
        Integer dObjsdk = new Integer(versionOS);
        String versionO = dObjsdk.toString();
        String imei = tm.getDeviceId();
        String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        numero = tm.getLine1Number();

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbc = dataBaseHelper.getReadableDatabase();
            String selectQueryBusca = "SELECT NumeroCel FROM Configuration WHERE ConfigurationId = 1";
            Cursor cbusca = dbc.rawQuery(selectQueryBusca, new String[]{});
            busca = cbusca.getCount();
            cbusca.close();
            dbc.close();

        } catch (Exception e) {}


        if(busca==0){

            Configuration configuration = new Configuration();
            configuration.NumeroCel = numero;
            configuration.EstaAcceso = "False";
            configuration.ConfigurationId = _Configuration_Id;
            _Configuration_Id = configurationCRUD.insert(configuration);

//            alertFechas();

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.execSQL("UPDATE Configuration SET VecesPresionarVolumen = '5'");
            db.close();

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            }
        }

        // BUSCA SI EL NUMERO TIENE PERMISOS DE ACCESO
        try {

            SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT EstaActivado FROM Configuration";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                validacion = c.getString(c.getColumnIndex("EstaActivado"));
            }
            c.close();
            dbst.close();

        } catch (Exception e) {}


        if (!checkPlayServices()) {
            flagIsPlaySevice = false;
            //Toast.makeText(this, "Instalar PlayStore", Toast.LENGTH_SHORT).show();
        }

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(status != ConnectionResult.SUCCESS) {
            if(status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED){
                DialgActualizarPlaySerice();
                //Toast.makeText(this,"Por favor actualizar su google play service",Toast.LENGTH_LONG).show();
            }
            else {
                DialgDescargarPlaySerice();
                //Toast.makeText(this, "Por favor descargar google play service", Toast.LENGTH_SHORT).show();
            }
        } else {
            flagIsUpdate = true;
        }

        //validacion = "true";
        // ************************************* VALIDACIONES **************************************
        Log.e("--! busca " + String.valueOf(busca), "! validacion " + validacion);

        if (flagIsPlaySevice == true && flagIsUpdate == true){
            if(busca!=0 && validacion.equals("true")){
                startActivity(new Intent(getBaseContext(), Login.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();

                return;
            }
        }

        if(validacion=="true"){
            txtApro.setVisibility(View.INVISIBLE);
        }

        Log.e("--NUMERO ", String.valueOf(numero));
        Log.e("--ID ", String.valueOf(androidId));
        Log.e("--IMEI ", String.valueOf(imei));
        Log.e("--MODELO ", String.valueOf(modelo));
        Log.e("--OTORGA ", String.valueOf(SimOtorgaNumero));
        Log.e("--SERIE SIM ", String.valueOf(serieSIM));
        Log.e("--FABRCANTE ", String.valueOf(fabricante));
        Log.e("--VERSION ", String.valueOf(versionO));

        Log.e("-- IF | ", String.valueOf(flagIsFused) +"-"+String.valueOf(flagIsPlaySevice)+"-"+String.valueOf(flagIsUpdate));

        if (flagIsPlaySevice == true && flagIsUpdate == true){
            //new PostAsync().execute(numero, androidId, imei, modelo, SimOtorgaNumero, serieSIM, fabricante, versionO);

            String URL = URL_API.concat("api/dispositivo");

            JsonObject json = new JsonObject();
            json.addProperty("Numero", numero);
            json.addProperty("HWID", androidId);
            json.addProperty("HWIMEI", imei);
            json.addProperty("Modelo", modelo);
            json.addProperty("SIMOtorgaNumero", SimOtorgaNumero);
            json.addProperty("SIMSerie", serieSIM);
            json.addProperty("Fabricante", fabricante);
            json.addProperty("VersionOS", versionO);

            final ProgressDialog pDialog;

            pDialog = new ProgressDialog(Bienvenido.this);
            pDialog.setMessage("Obteniendo C\u00f3digo...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

            Ion.with(this)
                    .load("POST", URL)
                    .setJsonObjectBody(json)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {

                            if(result!=null){
                                Log.e("JsonObject ", result.toString());

                                //    estado = result.("Estado");
                                estado = result.get("Estado").getAsString();
                                RequiereNumero = result.get("RequiereNumero").getAsString();
                                Id = result.get("Id").getAsString();

                                if(result.get("Numero").isJsonNull()){
                                    NumeroReinstlado = null;
                                    Log.e("NumeroReinstlado ", "INGRESÓ");
                                } else {
                                    NumeroReinstlado = result.get("Numero").getAsString();
                                }

                                //*********************************
                                actualizarConfiguracion();
                                //*********************************

                                if (estado.equals("true")){
                                    actualizarNumero();
                                }

                                if (estado.equals("true") && RequiereNumero =="false"){

                                    startActivity(new Intent(getBaseContext(), Login.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));

                                    if (pDialog != null && pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                    return;


                                }else {

                                    if(RequiereNumero =="true"){

                                        Intent intent = new Intent(Bienvenido.this, RegisterNumber.class);
                                        intent.putExtra("Id", Id);
                                        startActivity(intent);

                                    }
                                }


                            } else  {
                                Log.e("Exception ", "Finaliza" );
                            }

                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }

                        }
                    });
        }

    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                //Toast.makeText(this, "Play store NO Soportado", Toast.LENGTH_LONG).show();
                //finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    //**************************************************************************************************************

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {

        switch (requestCode) {
            case MY_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
            }
            case MY_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
            }
            case MY_INTERNET: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
            }
            case MY_ACCESS_NETWORK_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
            }
            case MY_VIBRATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
            }
            case MY_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
            }

            case MY_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
            }

            case MY_WRITE_SETTINGS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
            }
        }
    }

    @Override
    public void onClick(View v) {

    }

    public void simDialogo(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Inserte tarjeta SIM Card");
        builder.setMessage("Debe insertar tarjeta SIM Card para poder iniciar la aplicaci\u003fn");
        builder.setPositiveButton("Aceptar", null);
        builder.show();
    }

    //************************************************************************************************************************

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("INGRESO  ", "onActivityResult");
//        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);

        switch (requestCode) {
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Toast.makeText(Bienvenido.this, "Configuraci\u00f3n \u00e9xitosa", Toast.LENGTH_LONG).show();
                        Log.e("VerificarAcceso ","RESULT_OK");
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        configuracionRechazada();
                        break;
                    default:
                        break;
                }
                break;

            case ADMIN_INTENT:

                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Toast.makeText(getApplicationContext(), "Aplicaci\u00f3n Registrada", Toast.LENGTH_SHORT).show();

                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        configuracionRechazada();
                        break;
                    default:
                        break;
                }

                break;
        }
    }

    public void configuracionRechazada(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("¡Advertencia!");
        builder.setMessage("Al no aceptar las configuraciones previas Solgis no iniciara de manera correcta. Por favor intente nuevamente");
        builder.setPositiveButton("Ok", null);
        builder.show();
    }

    private boolean isWIFIAvailable() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) { }
    @Override
    public void onConnectionSuspended(int i) { }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    public void DialgDescargarPlaySerice(){

        try {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle("Intalar Play Services");
            builder.setMessage("Por favor instale Play Services para un optimo funcionamiento del aplicativo");
            builder.setPositiveButton("Aceptar", null);
            builder.show();
        } catch (Exception e){}
    }

    public void DialgActualizarPlaySerice(){

        try {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle("Actualizar Play Services");
            builder.setMessage("Por favor actualice su Play Services para un optimo funcionamiento del aplicativo");
            builder.setPositiveButton("Aceptar", null);
            builder.show();
        } catch (Exception e){}
    }

    //----------------------------------------------------------------------------------------------
    public  Boolean actualizarConfiguracion(){

        try {

            DBHelper dbHelperAlarm = new DBHelper(this);
            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
            dba.execSQL("UPDATE Configuration SET EstaActivado = '"+estado+"'");
            dba.execSQL("UPDATE Configuration SET IntervaloTracking = '1'");
            dba.execSQL("UPDATE Configuration SET GuidDipositivo = '"+Id+"'");
            dba.execSQL("UPDATE Configuration SET FlagUpdate = 'true'");
            dba.close();

        } catch (Exception e){}

        return true;
    }

    public  Boolean actualizarNumero(){

        try {

            DBHelper dbHelperNumero = new DBHelper(this);
            SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
            dbNro.execSQL("UPDATE Configuration SET NumeroCel = '"+NumeroReinstlado+"' WHERE ConfigurationId = 1");
            dbNro.close();

        } catch (Exception e){}

        return true;
    }

    public void dataAccessSettings() {
        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbc = dataBaseHelper.getReadableDatabase();
            String selectQueryBusca = "SELECT Nombre FROM SettingsPermissions WHERE SettingsPermissionsId = 1";
            Cursor cbusca = dbc.rawQuery(selectQueryBusca, new String[]{});
            busca = cbusca.getCount();
            cbusca.close();
            dbc.close();

        } catch (Exception e) {}

        if(busca==0){

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.execSQL("INSERT INTO SettingsPermissions (SettingsPermissionsId, Nombre, Estado) " +
                    "VALUES (1,'com.android.settings.Settings','false')");

            db.execSQL("INSERT INTO SettingsPermissions (SettingsPermissionsId, Nombre, Estado) " +
                    "VALUES (2,'com.android.settings.Settings$DateTimeSettingsActivity','false')");

            db.execSQL("INSERT INTO SettingsPermissions (SettingsPermissionsId, Nombre, Estado) " +
                    "VALUES (3,'com.android.settings','false')");

            db.close();
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

        //Toast.makeText(this, "Activando Datos..", Toast.LENGTH_SHORT).show();
    }
}
