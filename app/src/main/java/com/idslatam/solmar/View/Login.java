package com.idslatam.solmar.View;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

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
import com.idslatam.solmar.Alert.Services.ServicioAlerta;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Api.Parser.JsonParser;
import com.idslatam.solmar.Models.Crud.AsistenciaCrud;
import com.idslatam.solmar.Models.Crud.ConfigurationCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Asistencia;
import com.idslatam.solmar.Models.Entities.Configuration;
import com.idslatam.solmar.R;
import com.idslatam.solmar.Tracking.Broadcast.AlarmLocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Login extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks
        ,GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{

    private GoogleApiClient mGoogleApiClient;
    final static int REQUEST_LOCATION = 199;
    Button acceso, verConfiguracion;

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mComponentName;
    private static final int ADMIN_INTENT = 1;

    EditText password;
    Context context;
    String authorization;
    String pass;
    protected String URL_API;
    int _Asistencia_id = 0;
    String Numero, DispositivoId, FechaInicioCelular;

    ConfigurationCrud configurationCRUD = new ConfigurationCrud(this);
    DBHelper dataBaseHelper = new DBHelper(this);

    String cvalidacion, cNumero, cGuidDispositivo, cToken, cOutApp, cIntervaloTracking, cIntervaloAlert, cMargenAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_login);
        this.context = this;

        //**********************************************************************************************************************************

        if (mGoogleApiClient== null) {

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
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
                                status.startResolutionForResult(Login.this, REQUEST_LOCATION);
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

        // METODO QUE INICIA EL SERVICIO LOCATION ***************************************************
        Intent alarm = new Intent(this.context, AlarmLocation.class);
        boolean alarmRunning = (PendingIntent.getBroadcast(this.context, 0, alarm, PendingIntent.FLAG_NO_CREATE) != null);

        int vApi = Build.VERSION.SDK_INT;

        //Toast.makeText(getApplicationContext(), "Prender Alarma", Toast.LENGTH_SHORT).show();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.context, 0, alarm, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if(vApi <= 19){
            if(alarmRunning == false) {
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 1000*2, pendingIntent);
            }else{
            }
        }
        else{
            if(alarmRunning == false) {
                long timeInMillis = (SystemClock.elapsedRealtime() + 1000*2);
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeInMillis, pendingIntent);
            }
        }

        // FIN METODO QUE INICIA EL SERVICIO LOCATION ***********************************************

        // ------------------------------------------------------------------------------------------------------------------------

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        password = (EditText)findViewById(R.id.Ed_codEmpleado);
        password.setEnabled(false);

        acceso = (Button)findViewById(R.id.login_button);
//        verConfiguracion = (Button)findViewById(R.id.boton_configuracion);

        acceso.setOnClickListener(this);
        acceso.setEnabled(false);

//        verConfiguracion.setOnClickListener(this);

        SQLiteDatabase dbToken = dataBaseHelper.getReadableDatabase();
        String selectQueryToken = "SELECT Token FROM Configuration WHERE Token is null or Token = ''";
        Cursor cbuscaToken = dbToken.rawQuery(selectQueryToken, new String[]{}, null);
        int buscaToken = cbuscaToken.getCount();
        cbuscaToken.close();
        dbToken.close();

        if(buscaToken==0){
            startActivity(new Intent(getBaseContext(), MenuPrincipal.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
        } else {
            password.setEnabled(true);
            acceso.setEnabled(true);
        }


    }


    // METODOS DE FUSED *****************************************************************************
    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("INGRESO  ", "onActivityResult");
//        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);

        switch (requestCode) {
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Toast.makeText(this, "¡Configuración éxitosa!", Toast.LENGTH_LONG).show();
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

        }
    }

    //**********************************************************************************************
    @Override
    public void onClick(View v) {

        switch(v.getId()) {

//            case R.id.boton_configuracion:
//
//                try {
////                    BD_backup();
////                    Intent intent = new Intent(LoginActivity.this, FragmentApps.class);
////                    startActivity(intent);
//
//                } catch (Exception e) {
//                    Log.e("Error en extraer bd", e.getMessage());
//                }
//
//                consultaConfiguracion();
//                verConfiguracion();
//
//                break;

            case R.id.login_button:

                String type = "password";
//                String user = "luis.calua@idslatam.com";
                pass = password.getText().toString();
                String user = pass.concat("@gruposolmar.com.pe");

                new PostAsync().execute(type, user, pass);

                break;
        }
    }

    class PostAsync extends AsyncTask<String, String, JSONObject> {

        JsonParser jsonParser = new JsonParser();

        private ProgressDialog pDialog;

        private final String URL = URL_API.concat("/token");//"https://solmar.azurewebsites.net/token";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(Login.this);
            pDialog.setMessage("Accediendo..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {

                HashMap<String, String> params = new HashMap<>();
                params.put("grant_type", args[0]);
                params.put("username", args[1]);
                params.put("password", args[2]);

                Log.d("request", "starting");

                JSONObject json = jsonParser.makeHttpRequest(
                        URL, "POST",  params);

                if (json != null) {

                    try {

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
                        FechaInicioCelular = sdf.format(new Date());

                        SQLiteDatabase dbA = dataBaseHelper.getReadableDatabase();
                        String selectQueryA = "SELECT NumeroCel, GuidDipositivo FROM Configuration";
                        Cursor cA = dbA.rawQuery(selectQueryA, new String[]{});

                        if (cA.moveToFirst()) {

                            Numero = cA.getString(cA.getColumnIndex("NumeroCel"));
                            DispositivoId = cA.getString(cA.getColumnIndex("GuidDipositivo"));

                        }
                        cA.close();
                        dbA.close();

                    }catch (Exception e){}

                    String id = DispositivoId; //"4349C419-3409-4D35-B379-AD907604F888";
//                    new GetAsync().execute(DispositivoId);

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
            String token=null;

            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (json != null) {
                new GetAsync().execute(DispositivoId);

                try {
                    token = json.getString("access_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Configuration configuration = new Configuration();
                configuration.Token= token;
                configuration.ConfigurationId = 1;
                configurationCRUD.updateToken(configuration);

                // GUARDAR DATOS EN LA CLASE ASISTENCIA
                guardarAsistencia();

                new PostAsyncAsistencia().execute(Numero, DispositivoId, FechaInicioCelular);


                try {
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                startActivity(new Intent(getBaseContext(), MenuPrincipal.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));

//                Intent intent = new Intent(Login.this, MenuPrincipal.class);
//                startActivity(intent);
            }

            if (success == 1) {
                Log.d("Hecho!", message);
            }else{
                Log.d("Falló", message);
            }

            // sacar token de la bd
//            authorization = token; //"Bearer OFLUyn09Ye4jqYVuc2cuFh7fXrAizKlfCbeX3pVbbr6wAuCPmN7_AfXjh6nyHdqmiKVDbUMr2vssyrV3yR4pPGNxAr2sQKGLYJnfyZLpbtxw72wyroNHT7Rlyegob5XLiV_ntGUq4KSzutOhBCGEvV-F7oy9f4v0b6ttb4tTTtaZFwSx0mIshUfx-f4gRkQZ9HC7nz2C-PfGHkKJSUs9IafmPOq8G02-IyjGxif7G6ZbqdDfM09vBXXL1H56nXGI21cJgsAa5INRxgiBHDAWgUU0MFfec_hkP8UKgqhZkybipH9Px-2P9cmS9wPgSG9s13l3qA2j890hhzMSmm_-X85Zn7DtuuHQC-B3DN0TjyO9Phur3xcpfp-nESb4rVmSo4QL2PQ-S4zvViY0UOkkQw0DvaJOROU2T6_B-No3zQtxsOKy065qtMuSY0cIYtMCZT9TZ92son--ER4YkhYGImiDTm1FkuNhkXS0NbEyiVItVliCM2xDB58EXdA_pFEIFwOTQMC-CCe56E4Exw06-wKNB7OVWb7oLaCQJTHpLmI";//token;
//            String id = "4349C419-3409-4D35-B379-AD907604F888";
//            new GetAsync().execute(id);
        }
    }

    public void guardarAsistencia (){

        try {

            AsistenciaCrud asistenciaCRUD = new AsistenciaCrud(this);
            Asistencia asistencia = new Asistencia();

            asistencia.NumeroAs = Numero;
            asistencia.DispositivoId = DispositivoId;
            asistencia.FechaInicio = FechaInicioCelular;
            asistencia.Fotocheck = pass;
            asistencia.AsistenciaId = _Asistencia_id;
            _Asistencia_id = asistenciaCRUD.insert(asistencia);

        }catch (Exception e){}

    }

    class PostAsyncAsistencia extends AsyncTask<String, String, JSONObject> {

        JsonParser jsonParser = new JsonParser();

        private ProgressDialog pDialog;

        private final String URL = URL_API.concat("/api/Attendance");//"http://solmar.azurewebsites.net/api/Attendance";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        @Override
        protected void onPreExecute() {}

        @Override
        protected JSONObject doInBackground(String... args) {

            try {

                HashMap<String, String> params = new HashMap<>();
                params.put("Numero", args[0]);
                params.put("DispositivoId", args[1]);
                params.put("FechaInicioCelular", args[2]);

                JSONObject json = jsonParser.makeHttpRequest(
                        URL, "POST",  params);

                if (json != null) {

                    Log.d("JSON result", json.toString());

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
            String AsistenciaId=null;

            if (json != null) {

                try {
                    AsistenciaId = json.getString("AsistenciaId");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Configuration configuration = new Configuration();
                configuration.AsistenciaId= AsistenciaId;
                configuration.CodigoEmpleado= pass;
                configuration.ConfigurationId = 1;
                configurationCRUD.updateAsistencia(configuration);

                Log.e("Asistencia ", json.toString());

                try {
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success == 1) {
                Log.d("Hecho!", message);
            }else{
                Log.d("Falló", message);
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    class GetAsync extends AsyncTask<String, String, JSONObject> {

        JsonParser jsonParser = new JsonParser();

        private ProgressDialog pDialog;

//        private static final String LOGIN_URL = "https://solmar.azurewebsites.net/Aplicacion/GetAppByUser";

        private final String LOGIN_URL = URL_API.concat("/Aplicacion/GetAppByUser");

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        @Override
        protected void onPreExecute() {}

        @Override
        protected JSONObject doInBackground(String... args) {

            try {

                HashMap<String, String> params = new HashMap<>();
                params.put("id", args[0]);

                JSONObject json = jsonParser.makeHttpRequest(
                        LOGIN_URL, "GET", params);

                if (json != null) {

                    datosMenu(json);

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
                    if (ServicioAlerta.serviceRunningAlerta == true) {
                        Log.e("--------", "Servicio Alert ya está ejecutándose!--------");
                    } else {
                        startService(new Intent(Login.this, ServicioAlerta.class));
                        Log.e("--------", "Servicio Alert Detenido! ...Reiniciando..");
                    }
                } catch (Exception e){
                    Log.e("-- Error! Serv Alerta", " A");
                }

                try {
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success == 1) {
                Log.d("Success!", message);
            }else{
                Log.d("Failure", message);
            }
        }

    }

    public Boolean datosMenu(JSONObject json){

        Log.e("JSON GET: ", json.toString());

        String Menu = null;
        try {
            Menu = json.getString("Menu");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray jsonA = null;
        try {
            jsonA = new JSONArray(Menu);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String []valores = new String[5];
        int []val = new int[5];

        int a = 0, b=0;

//                    int [][]m = new int [5][5];

        try {
            for (int i = 0; i < jsonA.length(); i++) {

                JSONObject c = jsonA.getJSONObject(i);
//                            valores[i] = c.getString("Nombre");
                val[i] = c.getInt("Id");
                valores[i] = c.getString("Configuracion");

                JSONArray jsonValores = new JSONArray(valores[i]);

                for (int j = 0; j < jsonValores.length(); j++) {
                    JSONObject v = jsonValores.getJSONObject(j);

                    if(c.getInt("Id")==1){
//
//                                    Log.e("-- "+c.getString("Nombre"), String.valueOf(v.getInt("ConfiguracionId")));
                        if(v.getInt("ConfiguracionId")==7){
                            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                            db.execSQL("UPDATE Configuration SET Precision = '" + v.getInt("Valor") + "'");
                            db.close();

                            Log.e("CONF login "+c.getString("Nombre"), String.valueOf(v.getInt("Valor")));
                        }

                        if(v.getInt("ConfiguracionId")==1){
                            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                            db.execSQL("UPDATE Configuration SET IntervaloTracking = '" + v.getInt("Valor") + "'");
                            db.close();

                            Log.e("CONF login " + c.getString("Nombre"), String.valueOf(v.getInt("Valor")));
                        }

                        if(v.getInt("ConfiguracionId")==2){
                            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                            db.execSQL("UPDATE Configuration SET IntervaloTrackingSinConex = '" + v.getInt("Valor") + "'");
                            db.close();

                            Log.e("CONF login "+c.getString("Nombre"), String.valueOf(v.getInt("Valor")));
                        }

                    }

                    if(c.getInt("Id")==2){
//
//                                    Log.e("-- "+c.getString("Nombre"), String.valueOf(v.getInt("ConfiguracionId")));
                        if(v.getInt("ConfiguracionId")==3){
                            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                            db.execSQL("UPDATE Configuration SET IntervaloMarcacion = '" + v.getInt("Valor") + "'");
                            db.close();

//                                        Log.e("--"+c.getString("Nombre")+" M[" + i + "," + j + "]= ", String.valueOf(v.getInt("Valor")));
                        }

                        if(v.getInt("ConfiguracionId")==4){
                            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                            db.execSQL("UPDATE Configuration SET IntervaloMarcacionTolerancia = '" + v.getInt("Valor")  + "'");
                            db.close();
//                                        Log.e("--"+c.getString("Nombre")+" M[" + i + "," + j + "]= ", String.valueOf(v.getInt("Valor")));
                        }
                    }

                    if(c.getInt("Id")==3){

                        if(v.getInt("ConfiguracionId")==5){
                            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                            db.execSQL("UPDATE Configuration SET VecesPresionarVolumen = '"+v.getInt("Valor")+"'");
                            db.close();

                            a = v.getInt("Valor");

                        }
                        if(v.getInt("ConfiguracionId")==6){
                            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                            db.execSQL("UPDATE Configuration SET IntervaloTrackingEmergencia = '"+v.getInt("Valor")+"'");
                            db.close();

                            a = v.getInt("Valor");

                        }
                    }
                }
            }
        } catch (Exception e){
            Log.e("-- Error! ", " al obtener Menu");
        }

        return true;
    }

    public void configuracionRechazada(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("¡Advertencia!");
        builder.setMessage("Al no aceptar las configuraciones previas Solgis no iniciara de manera correcta. Por favor intente nuevamente");
        builder.setPositiveButton("Ok", null);
        builder.show();
    }

}
