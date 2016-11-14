package com.idslatam.solmar.View;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Api.Parser.JsonParser;
import com.idslatam.solmar.Models.Crud.ConfigurationCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Configuration;
import com.idslatam.solmar.R;

import org.json.JSONObject;
import org.w3c.dom.Text;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bienvenido);

        dataAccessSettings();

        txtApro = (TextView)findViewById(R.id.text_aprobacion);
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_NETWORK_STATE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, MY_ACCESS_NETWORK_STATE);
            }
        }

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

//        btn = (Button) findViewById(R.id.btn_login);
//        btnC = (Button) findViewById(R.id.btn_configuracion);

//        btn.setOnClickListener(this);
//        btnC.setOnClickListener(this);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        String serieSIM = null;

        try {
            serieSIM = tm.getSimSerialNumber();
        }catch (Exception e){}

        isWIFIAvailable();

        if (serieSIM == null) {
            simDialogo();
            return;
        }

        //**********************DESPUES DE VALIDAR SIN SE OBTIENE DATOS DEL TELEFONO************************************
        String modelo = Build.MODEL;
        String fabricante = Build.MANUFACTURER;
        int versionOS = Build.VERSION.SDK_INT;
        Integer dObjsdk = new Integer(versionOS);
        String versionO = dObjsdk.toString();
        String imei = tm.getDeviceId();
        String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        numero = tm.getLine1Number();

        SimOtorgaNumero = "true";
        if (numero.equals("")) {SimOtorgaNumero = "false";}

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


        // ************************************* VALIDACIONES **************************************
        Log.e("--! busca " + String.valueOf(busca), "! validacion " + validacion);
        if(busca!=0 && validacion.equals("true")){
            startActivity(new Intent(getBaseContext(), Login.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();

            return;
        }

        if(validacion=="true"){
            txtApro.setVisibility(View.INVISIBLE);
        }

        new PostAsync().execute(numero, androidId, imei, modelo, SimOtorgaNumero, serieSIM, fabricante, versionO);


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
                        Toast.makeText(Bienvenido.this, "¡Configuración éxitosa!", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(getApplicationContext(), "Aplicación Registrada", Toast.LENGTH_SHORT).show();

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

    //----------------------------------------------------------------------------------------------
    class PostAsync extends AsyncTask<String, String, JSONObject> {
        JsonParser jsonParser = new JsonParser();

        private ProgressDialog pDialog;

        private final String URL = URL_API.concat("api/dispositivo");
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        @Override
        protected void onPreExecute() {

            pDialog = new ProgressDialog(Bienvenido.this);
            pDialog.setMessage("Obteniendo C\u00f3digo...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {

                HashMap<String, String> params = new HashMap<>();

                params.put("Numero", args[0]);
                params.put("HWID", args[1]);
                params.put("HWIMEI", args[2]);
                params.put("Modelo", args[3]);
                params.put("SIMOtorgaNumero", args[4]);
                params.put("SIMSerie", args[5]);
                params.put("Fabricante", args[6]);
                params.put("VersionOS", args[7]);
                Log.d("request", "starting");

                JSONObject json = jsonParser.makeHttpRequest(
                        URL, "POST", params);

                if (json != null) {

                    estado = json.getString("Estado");
                    RequiereNumero = json.getString("RequiereNumero");
                    Id = json.getString("Id");
                    NumeroReinstlado = json.getString("Numero");
                    //*********************************
                    actualizarConfiguracion();
                    //*********************************

                    if (estado.equals("true")){
                        actualizarNumero();
                    }
                    Log.e("VerificarAcceso", json.toString());

                    return json;
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

            if (json != null) {

                try {

                    if (estado.equals("true") && RequiereNumero =="false"){

                        startActivity(new Intent(getBaseContext(), Login.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                        finish();


                    }else {

                        if(RequiereNumero =="true" && NumeroReinstlado !=null){

                            Intent intent = new Intent(Bienvenido.this, RegisterNumber.class);
                            intent.putExtra("Id", Id);
                            startActivity(intent);

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (success == 1) {
                Log.d("Hecho!", message);
            }else {
                Log.d("Falló", message);
            }
        }

    }

    public  Boolean actualizarConfiguracion(){

        DBHelper dbHelperAlarm = new DBHelper(this);
        SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
        dba.execSQL("UPDATE Configuration SET EstaActivado = '"+estado+"'");
        dba.execSQL("UPDATE Configuration SET IntervaloTracking = '1'");
        dba.execSQL("UPDATE Configuration SET GuidDipositivo = '"+Id+"'");
        dba.close();

        return true;
    }

    public  Boolean actualizarNumero(){

        DBHelper dbHelperNumero = new DBHelper(this);
        SQLiteDatabase dbNro = dbHelperNumero.getWritableDatabase();
        dbNro.execSQL("UPDATE Configuration SET NumeroCel = '"+NumeroReinstlado+"' WHERE ConfigurationId = 1");
        dbNro.close();

        return true;
    }

    public static boolean usageAccessGranted(Context context) {

        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    //******************************************************************
    public void alertFechas(){
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        db.execSQL("INSERT INTO AlertFechas (AlertFechasId, FechaEjecucion, FechaComprobacion) " +
                "VALUES (1,'1900,01,01,00,00,00','1900,01,01,00,00,00')");
        db.close();
    }
    //******************************************************************

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
}
