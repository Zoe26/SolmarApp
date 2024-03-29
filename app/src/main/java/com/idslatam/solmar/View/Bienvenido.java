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
import android.net.Uri;
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
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Models.Crud.CargoCrud;
import com.idslatam.solmar.Models.Crud.ConfigurationCrud;
import com.idslatam.solmar.Models.Crud.ContactosCrud;
import com.idslatam.solmar.Models.Crud.PeopleCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Cargo;
import com.idslatam.solmar.Models.Entities.Configuration;
import com.idslatam.solmar.Models.Entities.Contactos;
import com.idslatam.solmar.Models.Entities.People;
import com.idslatam.solmar.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
public class Bienvenido extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks
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
    private int _Configuration_Id = 0, _Contactos_Id = 0, _Cargo_Id = 0, _People_Id = 0;
    String numero;

    boolean flagIsFused = false, flagIsPlaySevice = true, flagIsUpdate = false;

    int buscaN;
    int busca, buscaCont;

    ConfigurationCrud configurationCRUD = new ConfigurationCrud(this);
    CargoCrud cargoCrud = new CargoCrud(this);
    PeopleCrud peopleCrud = new PeopleCrud(this);

    DBHelper dataBaseHelper = new DBHelper(this);

    String SimOtorgaNumero="false";
    String validacion;
    String NumeroReinstlado, ClienteId;
    protected String URL_API;
    String estado, RequiereNumero, Id;
    TextView txtApro, txtNumeroCelular;

    boolean flagPermisos;

    Context mContext;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int MY_READ_PHONE_STATE = 1 ;
    private static final int MY_ACCESS_FINE_LOCATION = 2 ;
    private static final int MY_INTERNET = 3 ;
    private static final int MY_ACCESS_NETWORK_STATE = 4;
    private static final int MY_VIBRATE = 5;
    private static final int MY_CAMERA = 6;
    private static final int MY_WRITE_EXTERNAL_STORAGE = 8;
    private static final int MY_READ_EXTERNAL_STORAGE = 11;
    private static final int MY_CALL_PHONE_STATE = 10;

    String serieSIM;
    String imei;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenido);

        mContext= this;
        txtApro = (TextView)findViewById(R.id.text_aprobacion);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        //-------------------------------------------------------------------------------------------------------------------------

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

        //*********************************************************************************************************************

        // PERMISO DE MEDIO EXTERNO
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_READ_EXTERNAL_STORAGE);
            } else {
                flagPermisos = false;
                //  Log.e("WRITE_EXTERNAL_STORAGE"," false");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_READ_EXTERNAL_STORAGE);
            }
        } else {
            //Log.e("WRITE_EXTERNAL_STORAGE"," true");
            flagPermisos = true;
        }

        // PERMISO DE LLAMADA
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
            } else {

                //Log.e("CALL_PHONE"," false");
                flagPermisos = false;
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, MY_CALL_PHONE_STATE);

            }
        } else {
            //Log.e("CALL_PHONE"," true");
            flagPermisos = true;
        }

        // PERMISO DE LEER TELEFONO
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
            } else {
                flagPermisos = false;
                //Log.e("READ_PHONE_STATE"," false");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_READ_PHONE_STATE);

            }
        } else {
            //Log.e("READ_PHONE_STATE"," true");
            flagPermisos = true;
        }

        // PERMISO DE GPS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                flagPermisos = false;
              //  Log.e("ACCESS_FINE_LOCATION"," false");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_ACCESS_FINE_LOCATION);
            }
        } else {
            //Log.e("ACCESS_FINE_LOCATION"," true");
            flagPermisos = true;
        }

        // PERMISO DE INTERNET
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)) {
            } else {
                flagPermisos = false;
              //  Log.e("INTERNET"," false");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, MY_INTERNET);
            }
        } else {
            //Log.e("INTERNET"," true");
            flagPermisos = true;
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
                flagPermisos = false;
              //  Log.e("VIBRATE"," false");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.VIBRATE}, MY_VIBRATE);
            }
        } else {
            //Log.e("VIBRATE"," true");
            flagPermisos = true;
        }

        // PERMISO DE LA CAMARA
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            } else {
                flagPermisos = false;
              //  Log.e("CAMERA"," false");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA);
            }
        } else {
            //Log.e("CAMERA"," true");
            flagPermisos = true;
        }

        // PERMISO DE MEDIO EXTERNO
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                flagPermisos = false;
              //  Log.e("WRITE_EXTERNAL_STORAGE"," false");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            //Log.e("WRITE_EXTERNAL_STORAGE"," true");
            flagPermisos = true;
        }



        if (flagPermisos == false){
            //Log.e("flagPermisos"," return");
            return;
        }

        //**********************************************************************************************************************
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            try {
                setMobileNetworkfromLollipop(this);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

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
                            //Log.e("Request SUCCESS ",String.valueOf(status));
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be
                            // fixed by showing the user
                            // a dialog.
                            //Log.e("RESOLUTION_REQUIRED ",String.valueOf(status));
                            try {
                                // Show the dialog by calling
                                // startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(Bienvenido.this, REQUEST_LOCATION);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                               // Log.e("ERROR RESOLUTION ", e.getMessage());
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have
                            // no way to fix the
                            // settings so we won't show the dialog.
                            //Log.e("Request ", "SETTINGS_CHANGE_UNAVAILABLE "+String.valueOf(status));
                            break;
                    }
                }
            });
        }

        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);

        int networkType = tm.getNetworkType();
        switch (networkType) {
            case (TelephonyManager.NETWORK_TYPE_1xRTT) :
                //Log.e("--NETWORK_TYPE_1xRTT ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_CDMA) :
                //Log.e("--PHONE_TYPE_CDMA ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_EDGE) :
                //Log.e("--NETWORK_TYPE_EDGE ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_EHRPD) :
                //Log.e("--NETWORK_TYPE_EHRPD ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_EVDO_0) :
                //Log.e("--NETWORK_TYPE_EVDO_0 ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_EVDO_A) :
                //Log.e("--NETWORK_TYPE_EVDO_A ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_EVDO_B) :
                //Log.e("--NETWORK_TYPE_EVDO_B ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_GPRS) :
                //Log.e("--NETWORK_TYPE_GPRS ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_HSDPA) :
                //Log.e("--NETWORK_TYPE_HSDPA ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_HSPA) :
                //Log.e("--NETWORK_TYPE_HSPA ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_LTE) :
                //Log.e("--NETWORK_TYPE_LTE ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_UMTS) :
                //Log.e("--NETWORK_TYPE_UMTS ", String.valueOf(networkType));
                break;
            case (TelephonyManager.NETWORK_TYPE_UNKNOWN) :
                //Log.e("--NETWORK_TYPE_UNKNOWN ", String.valueOf(networkType));
                break;

        }

        int phoneType=tm.getPhoneType();

        switch (phoneType)
        {
            case (TelephonyManager.PHONE_TYPE_CDMA):
                // your code
                //Log.e("--PHONE_TYPE_CDMA ", String.valueOf(phoneType));
                break;
            case (TelephonyManager.PHONE_TYPE_GSM):
                // your code
                //Log.e("--PHONE_TYPE_GSM ", String.valueOf(phoneType));
                break;
            case (TelephonyManager.PHONE_TYPE_NONE):
                // your code
                //Log.e("--PHONE_TYPE_NONE ", String.valueOf(phoneType));
                break;
        }

        int SIMState=tm.getSimState();
        switch(SIMState) {
            case TelephonyManager.SIM_STATE_ABSENT :
                // your code
                break;

            case TelephonyManager.SIM_STATE_READY :
                // your code

                serieSIM = tm.getSimSerialNumber();
                imei = tm.getDeviceId();
                numero = tm.getLine1Number();

                DBHelper dataBaseHelper = new DBHelper(this);
                SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                db.execSQL("UPDATE Configuration SET SimSerie = '"+serieSIM+"'");
                db.close();

                SimOtorgaNumero = "true";
                if (numero.equals("")) {SimOtorgaNumero = "false";}
                break;

            case TelephonyManager.SIM_STATE_UNKNOWN :
                // your code
                //Log.e("--STATE_UNKNOWN ", String.valueOf(SIMState));
                break;

        }

        isWIFIAvailable();

        //**********************DESPUES DE VALIDAR SIN SE OBTIENE DATOS DEL TELEFONO************************************
        String modelo = Build.MODEL;
        String fabricante = Build.MANUFACTURER;
        int versionOS = Build.VERSION.SDK_INT;
        Integer dObjsdk = new Integer(versionOS);
        String versionO = dObjsdk.toString();
        String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        numero = tm.getLine1Number();

        String numeroAux = null;

        txtNumeroCelular = (TextView)findViewById(R.id.info);

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbc = dataBaseHelper.getReadableDatabase();
            String selectQueryBusca = "SELECT NumeroCel FROM Configuration WHERE ConfigurationId = 1";
            Cursor cbusca = dbc.rawQuery(selectQueryBusca, new String[]{});
            busca = cbusca.getCount();

            if(cbusca.moveToLast()){
                numeroAux = cbusca.getString(cbusca.getColumnIndex("NumeroCel"));
            }

            cbusca.close();
            dbc.close();

        } catch (Exception e) {}


        if (numeroAux!=null){
            Log.e("numeroAux ", numeroAux);

            txtNumeroCelular.setText(numeroAux);
        }

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

            Cargo cargo = new Cargo();
            cargo.Initial = "true";
            cargo.TipoCarga = "1";
            cargo.isLicencia = "true";
            cargo.isCarga = "false";
            cargo.EppCasco = "false";
            cargo.EppChaleco = "false";
            cargo.EppBotas = "false";
            cargo.tamanoContenedor = "20";
            cargo.tipoDocumento = "1";

            cargo.CargoId = _Cargo_Id;
            _Cargo_Id = cargoCrud.insert(cargo);


            People people = new People();
            people.Initial = "true";
            people.PeopleId = _People_Id;
            _People_Id = peopleCrud.insert(people);

            /*if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            }*/
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

        if(validacion=="true"){txtApro.setVisibility(View.INVISIBLE);}

        if(imei == null){imei = androidId;}

        Log.e("--NUMERO ", String.valueOf(numero));
        Log.e("--HWID ", String.valueOf(androidId));
        Log.e("--HWIMEI ", String.valueOf(imei));
        Log.e("--MODELO ", String.valueOf(modelo));
        Log.e("--OTORGA ", String.valueOf(SimOtorgaNumero));
        Log.e("--SERIE SIM ", String.valueOf(serieSIM));
        Log.e("--FABRCANTE ", String.valueOf(fabricante));
        Log.e("--VERSION ", String.valueOf(versionO));

        if (flagIsPlaySevice == true && flagIsUpdate == true){

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
            pDialog.setCancelable(true);
            pDialog.show();

            Ion.with(this)
                    .load("POST", URL)
                    .setJsonObjectBody(json)
                    .asJsonObject()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<JsonObject>>() {
                        @Override
                        public void onCompleted(Exception e, Response<JsonObject> response) {

                            if(response == null){

                                Toast.makeText(mContext, "¡Error de red!. Por favor revise su conexión a internet.", Toast.LENGTH_LONG).show();

                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                                return;

                            }

                            if (response.getHeaders().code() == 200) {

                                Gson gson = new Gson();
                                JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                                Log.e("JsonObject BIENVENIDO ", result.toString());

                                estado = result.get("Estado").getAsString();
                                RequiereNumero = result.get("RequiereNumero").getAsString();
                                Id = result.get("Id").getAsString();


                                /*if(!result.get("Configuracion").isJsonNull()){
                                    JsonArray jarray = result.getAsJsonArray("Configuracion");
                                    for (JsonElement pa : jarray) {
                                        JsonObject paymentObj = pa.getAsJsonObject();
                                    }
                                }*/


                                if(result.get("ClienteId").isJsonNull()){
                                    ClienteId = null;
                                    Log.e("ClienteId ", "INGRESÓ NULL");
                                } else {
                                    ClienteId = result.get("ClienteId").getAsString();

                                    try {

                                        DBHelper dataBaseHelper = new DBHelper(mContext);
                                        SQLiteDatabase dbc = dataBaseHelper.getReadableDatabase();
                                        String selectQueryBusca = "SELECT Nombre FROM Contactos WHERE ContactosId = 1";
                                        Cursor cbusca = dbc.rawQuery(selectQueryBusca, new String[]{});
                                        buscaCont = cbusca.getCount();
                                        cbusca.close();
                                        dbc.close();

                                    } catch (Exception efgre) {}

                                    if(buscaCont==0){

                                        JsonArray jarray = result.getAsJsonArray("ClienteContactos");

                                        for (JsonElement pa : jarray) {

                                            JsonObject paymentObj = pa.getAsJsonObject();

                                            ContactosCrud contactosCrud = new ContactosCrud(mContext);

                                            Contactos contactos = new Contactos();
                                            contactos.Nombre = paymentObj.get("Nombre").getAsString();
                                            contactos.PrimerNumero = paymentObj.get("Numero0").getAsInt();
                                            if (!paymentObj.get("Numero1").isJsonNull()){
                                                contactos.SegundoNumero = paymentObj.get("Numero1").getAsInt();
                                            }

                                            contactos.ContactosId = _Contactos_Id;
                                            _Contactos_Id = contactosCrud.insert(contactos);

                                        }

                                            Log.e("jarray CLI ", jarray.toString());

                                        }

                                    }

                                    if(result.get("Numero").isJsonNull()){
                                        NumeroReinstlado = null;
                                        RequiereNumero = "true";
                                        Log.e("NumeroReinstlado ", "INGRESÓ");
                                    } else {
                                        Log.e("NumeroReinstlado ", result.get("Numero").getAsString());
                                        NumeroReinstlado = result.get("Numero").getAsString();
                                        try {

                                            DBHelper dbHelperNumero = new DBHelper(mContext);
                                            SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
                                            dbNro.execSQL("UPDATE Configuration SET NumeroCel = '"+result.get("Numero").getAsString()+"'");
                                            dbNro.close();

                                        } catch (Exception esv){}

                                        txtNumeroCelular.setText(result.get("Numero").getAsString());
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

                                } else {
                                    Toast.makeText(mContext, "¡Error de servidor!. Por favor comuníquese con su administrador.", Toast.LENGTH_LONG).show();
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
                    Log.e("CALL_PHONE"," if");
                } else {
                    Log.e("CALL_PHONE"," else");
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
        }
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
            dba.execSQL("UPDATE Configuration SET ClienteId = '"+ClienteId+"'");
            dba.execSQL("UPDATE Configuration SET FlagUpdate = 'true'");
            dba.execSQL("UPDATE Configuration SET NivelVolumen = '-1'");
            dba.execSQL("UPDATE Configuration SET isScreen = 'true'");
            dba.execSQL("UPDATE Configuration SET SimSerie = '"+serieSIM+"'");
            dba.close();

        } catch (Exception e){}

        return true;
    }

    public  Boolean actualizarNumero(){

        String numeroAux = null;

        try {

            DBHelper dbHelperNumero = new DBHelper(this);
            SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
            dbNro.execSQL("UPDATE Configuration SET NumeroCel = '"+NumeroReinstlado+"'");
            dbNro.close();

        } catch (Exception e){}

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbc = dataBaseHelper.getReadableDatabase();
            String selectQueryBusca = "SELECT NumeroCel FROM Configuration";
            Cursor cbusca = dbc.rawQuery(selectQueryBusca, new String[]{});

            if(cbusca.moveToLast()){
                numeroAux = cbusca.getString(cbusca.getColumnIndex("NumeroCel"));
            }

            cbusca.close();
            dbc.close();

        } catch (Exception e) {}
        Log.e("NumeroReinstlado ", NumeroReinstlado);

        if (numeroAux!=null){
            Log.e("numeroAux ", numeroAux);

            txtNumeroCelular.setText(numeroAux);
        }

        return true;
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

    public static void setMobileNetworkfromLollipop(Context context) throws Exception {

        Log.e("setMobileLollipop ", "INGRESÓ");

        String command = null;
        int state = 0;
        try {
            // Get the current state of the mobile network.
            state = isMobileDataEnabledFromLollipop(context) ? 0 : 1;
            // Get the value of the "TRANSACTION_setDataEnabled" field.
            String transactionCode = getTransactionCode(context);
            // Android 5.1+ (API 22) and later.
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {

                Log.e("Build ", ">");

                SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                // Loop through the subscription list i.e. SIM list.
                for (int i = 0; i < mSubscriptionManager.getActiveSubscriptionInfoCountMax(); i++) {
                    if (transactionCode != null && transactionCode.length() > 0) {
                        // Get the active subscription ID for a given SIM card.
                        int subscriptionId = mSubscriptionManager.getActiveSubscriptionInfoList().get(i).getSubscriptionId();
                        // Execute the command via `su` to turn off
                        // mobile network for a subscription service.
                        command = "service call phone " + transactionCode + " i32 " + subscriptionId + " i32 " + state;
                        executeCommandViaSu(context, "-c", command);
                    }
                }
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                Log.e("Build ", "==");

                // Android 5.0 (API 21) only.
                if (transactionCode != null && transactionCode.length() > 0) {
                    // Execute the command via `su` to turn off mobile network.
                    command = "service call phone " + transactionCode + " i32 " + state;
                    executeCommandViaSu(context, "-c", command);
                }
            }
        } catch(Exception e) {
            // Oops! Something went wrong, so we throw the exception here.
            throw e;
        }
    }

    private static boolean isMobileDataEnabledFromLollipop(Context context) {
        boolean state = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            state = Settings.Global.getInt(context.getContentResolver(), "mobile_data", 0) == 1;
        }
        return state;
    }

    private static String getTransactionCode(Context context) throws Exception {
        try {
            final TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final Class<?> mTelephonyClass = Class.forName(mTelephonyManager.getClass().getName());
            final Method mTelephonyMethod = mTelephonyClass.getDeclaredMethod("getITelephony");
            mTelephonyMethod.setAccessible(true);
            final Object mTelephonyStub = mTelephonyMethod.invoke(mTelephonyManager);
            final Class<?> mTelephonyStubClass = Class.forName(mTelephonyStub.getClass().getName());
            final Class<?> mClass = mTelephonyStubClass.getDeclaringClass();
            final Field field = mClass.getDeclaredField("TRANSACTION_setDataEnabled");
            field.setAccessible(true);
            return String.valueOf(field.getInt(null));
        } catch (Exception e) {
            // The "TRANSACTION_setDataEnabled" field is not available,
            // or named differently in the current API level, so we throw
            // an exception and inform users that the method is not available.
            throw e;
        }
    }

    private static void executeCommandViaSu(Context context, String option, String command) {
        boolean success = false;
        String su = "su";
        for (int i=0; i < 3; i++) {
            // Default "su" command executed successfully, then quit.
            if (success) {
                break;
            }
            // Else, execute other "su" commands.
            if (i == 1) {
                su = "/system/xbin/su";
            } else if (i == 2) {
                su = "/system/bin/su";
            }
            try {
                // Execute command as "su".
                Runtime.getRuntime().exec(new String[]{su, option, command});
            } catch (IOException e) {
                success = false;
                // Oops! Cannot execute `su` for some reason.
                // Log error here.
            } finally {
                success = true;
            }
        }
    }

    public void contacts() {
        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbc = dataBaseHelper.getReadableDatabase();
            String selectQueryBusca = "SELECT Nombre FROM Contactos WHERE ContactosId = 1";
            Cursor cbusca = dbc.rawQuery(selectQueryBusca, new String[]{});
            buscaCont = cbusca.getCount();
            cbusca.close();
            dbc.close();

        } catch (Exception e) {}

        if(buscaCont==0){

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.execSQL("INSERT INTO Contactos (ContactosId, Nombre, PrimerNumero, SegundoNumero) " +
                    "VALUES (1,'Luis Calua','942269173','987654321')");

            db.execSQL("INSERT INTO Contactos (ContactosId, Nombre, PrimerNumero, SegundoNumero) " +
                    "VALUES (2,'Luis Calua','988977701','942269173')");

            db.execSQL("INSERT INTO Contactos (ContactosId, Nombre, PrimerNumero, SegundoNumero) " +
                    "VALUES (3,'Elvis Calua','987213999','988977701')");

            db.close();
        }

    }

}
