package com.idslatam.solmar.View;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Api.Parser.JsonParser;
import com.idslatam.solmar.BravoPapa.ScreenReceiver;
import com.idslatam.solmar.Models.Crud.AsistenciaCrud;
import com.idslatam.solmar.Models.Crud.ConfigurationCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Asistencia;
import com.idslatam.solmar.Models.Entities.Configuration;
import com.idslatam.solmar.R;
import com.idslatam.solmar.Tracking.Broadcast.AlarmLocation;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Login extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks
        ,GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{

    private int currentApiVersion = android.os.Build.VERSION.SDK_INT;


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
    String user;
    protected String URL_API;
    int _Asistencia_id = 0;
    String Numero, DispositivoId, FechaInicioCelular;

    ConfigurationCrud configurationCRUD = new ConfigurationCrud(this);
    DBHelper dataBaseHelper = new DBHelper(this);

    String cvalidacion, cNumero, cGuidDispositivo, cToken
            , cOutApp, cIntervaloTracking, cIntervaloAlert
            , cMargenAlert, Fotoch;

    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_login);
        this.context = this;

        //REGISTO DE BROACAST PARA BP_---
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
        // FIN BP

        //**********************************************************************************************************************************

        if (mGoogleApiClient== null) {

            try {

                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API).addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this).build();
                mGoogleApiClient.connect();

            } catch (Exception e){}

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
        boolean alarmRunning = (PendingIntent.getBroadcast(this, 0, alarm, PendingIntent.FLAG_NO_CREATE) != null);

        int vApi = Build.VERSION.SDK_INT;

        //Toast.makeText(getApplicationContext(), "Prender Alarma", Toast.LENGTH_SHORT).show();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarm, 0);
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

        try {

            DBHelper dbhToken = new DBHelper(context);
            SQLiteDatabase sqlToken = dbhToken.getWritableDatabase();
            String selectQueryToken = "SELECT Token FROM Configuration WHERE Token is null or Token = ''";
            Cursor cbuscaToken = sqlToken.rawQuery(selectQueryToken, new String[]{}, null);
            int buscaToken = cbuscaToken.getCount();
            cbuscaToken.close();
            sqlToken.close();

            if(buscaToken==0){
                startActivity(new Intent(getBaseContext(), MenuPrincipal.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();
            } else {
                password.setEnabled(true);
                acceso.setEnabled(true);
            }

        } catch (Exception e){}

    }


    // METODOS DE FUSED *****************************************************************************
    @Override
    public void onConnected(@Nullable Bundle bundle) {}

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("INGRESO  ", "onActivityResult");
//        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);

        switch (requestCode) {
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Toast.makeText(context, "¡Configuración éxitosa!", Toast.LENGTH_LONG).show();
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

            case R.id.login_button:

                String type = "password";
                pass = password.getText().toString();
                Fotoch = pass;
                user = pass.concat("@gruposolmar.com.pe");

                if(pass.matches("")){
                    Toast.makeText(this, "Ingrese Codigo", Toast.LENGTH_SHORT).show();
                } else {
                    //new PostAsync().execute(type, user, pass);
                    getCredentials();
                }


                break;
        }
    }

    String accessToken;
    private void getCredentials() {

        String URL = URL_API.concat("token");

        Ion.with(this)
                .load("POST", URL)
                .setBodyParameter("grant_type", "password")
                .setBodyParameter("username", user)
                .setBodyParameter("password", pass)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {

                        if(result.getHeaders().code()==200){

                            try {

                                JSONObject j = new JSONObject(result.getResult().toString());
                                accessToken = j.getString("access_token");
                                Log.e("j ", j.getString("access_token"));

                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }

                            Configuration configuration = new Configuration();
                            configuration.Token= accessToken;
                            configuration.CodigoEmpleado = pass;
                            configuration.ConfigurationId = 1;
                            configurationCRUD.updateToken(configuration);
                            Intent intent = new Intent(Login.this, MenuPrincipal.class);
                            intent.putExtra("Fotoch", Fotoch);
                            startActivity(intent);

                        }
                    }
                });
    }

    public Boolean getMenu(){
        new GetAsync().execute(DispositivoId);
        Log.e("request ", "Boolean getMenu");
        return true;
    }

    public void guardarAsistencia (){

        try {

            AsistenciaCrud asistenciaCRUD = new AsistenciaCrud(context);
            Asistencia asistencia = new Asistencia();

            asistencia.NumeroAs = Numero;
            asistencia.DispositivoId = DispositivoId;
            asistencia.FechaInicio = FechaInicioCelular;
            asistencia.Fotocheck = pass;
            asistencia.AsistenciaId = _Asistencia_id;
            _Asistencia_id = asistenciaCRUD.insert(asistencia);

        }catch (Exception e){}

    }

    public void sendAsistencia(){

        String Num = null, DisId = null;
        String FechaInicioCelular = formatoGuardar.format(new Date());

        try {

            FechaInicioCelular = formatoGuardar.format(new Date());
            DBHelper dbhGUID = new DBHelper(this);
            SQLiteDatabase dbA = dbhGUID.getReadableDatabase();
            String selectQueryA = "SELECT NumeroCel, GuidDipositivo FROM Configuration";
            Cursor cA = dbA.rawQuery(selectQueryA, new String[]{});

            if (cA.moveToFirst()) {

                Num = cA.getString(cA.getColumnIndex("NumeroCel"));
                DisId = cA.getString(cA.getColumnIndex("GuidDipositivo"));

            }
            cA.close();
            dbA.close();

        }catch (Exception e){}

        String URL = URL_API.concat("api/alert");

        JsonObject json = new JsonObject();
        json.addProperty("Numero", Num);
        json.addProperty("DispositivoId", DisId);
        json.addProperty("FechaInicioCelular", FechaInicioCelular);

        Ion.with(this)
                .load("POST", URL)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject response) {

                        if(response!=null){
                            Log.e("JsonObject ", response.toString());
                        } else  {
                            Log.e("Exception ", "Finaliza" );
                        }
                    }
                });

    }

    class PostAsyncAsistencia extends AsyncTask<String, String, JSONObject> {

        JsonParser jsonParser = new JsonParser();

        private ProgressDialog pDialog;

        private final String URL = URL_API.concat("api/Attendance");//"http://solmar.azurewebsites.net/api/Attendance";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE0 = "message";

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

                try {

                    Configuration configuration = new Configuration();
                    configuration.AsistenciaId= AsistenciaId;
                    configuration.CodigoEmpleado= pass;
                    configuration.ConfigurationId = 1;
                    configurationCRUD.updateAsistencia(configuration);

                } catch (Exception e) {}



                Log.e("Asistencia ", json.toString());

                try {
                    success = json.getInt(TAG_SUCCESS);

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

        private final String LOGIN_URL = URL_API.concat("Aplicacion/GetAppByUser");

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
                    Log.e("--PostAsyncAsistencia ", "datosMenu(json)");

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

            if (json != null) {


                try{
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (success == 1) {
                Log.d("Success!", message);
            }else{
                Log.d("Failure", message);
            }
        }

    }

    public Boolean datosMenu(JSONObject json){

        Log.e("JSON GET: Boolean", json.toString());

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

        try {
            for (int i = 0; i < jsonA.length(); i++) {

                JSONObject c = jsonA.getJSONObject(i);
//                            valores[i] = c.getString("Nombre");
                val[i] = c.getInt("Id");
                valores[i] = c.getString("Configuracion");

                JSONArray jsonValores = new JSONArray(valores[i]);

                for (int j = 0; j < jsonValores.length(); j++) {
                    JSONObject v = jsonValores.getJSONObject(j);


                    if(c.getInt("Id")==2){
//
//                                    Log.e("-- "+c.getString("Nombre"), String.valueOf(v.getInt("ConfiguracionId")));
                        if(v.getInt("ConfiguracionId")==3){

                            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                            db.execSQL("UPDATE Configuration SET IntervaloMarcacion = '" + v.getInt("Valor") + "'");
                            db.close();

                                        Log.e("--"+c.getString("Nombre")+" M[" + i + "," + j + "]= ", String.valueOf(v.getInt("Valor")));
                        }

                        if(v.getInt("ConfiguracionId")==4){

                            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                            db.execSQL("UPDATE Configuration SET IntervaloMarcacionTolerancia = '" + v.getInt("Valor")  + "'");
                            db.close();
                                        Log.e("--"+c.getString("Nombre")+" M[" + i + "," + j + "]= ", String.valueOf(v.getInt("Valor")));
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

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle("¡Advertencia!");
        builder.setMessage("Al no aceptar las configuraciones previas Solgis no iniciara de manera correcta. Por favor intente nuevamente");
        builder.setPositiveButton("Ok", null);
        builder.show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        try
        {
            if(!hasFocus)
            {
                Object service  = getSystemService("statusbar");
                Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                Method collapse;

                //Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                if (currentApiVersion <= 16) {
                    collapse = statusbarManager.getMethod("collapse");
                    collapse.invoke(service);
                    collapse .setAccessible(true);
                    collapse .invoke(service);

                } else {
                    collapse = statusbarManager.getMethod("collapsePanels");
                    collapse.invoke(service);
                    collapse.setAccessible(true);
                    collapse.invoke(service);

                }


                //Method collapse = statusbarManager.getMethod("collapse");

            }
        }
        catch(Exception ex)
        {
            if(!hasFocus)
            {
                try {

                    Object service  = getSystemService("statusbar");
                    Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                    Method collapse;

                    //Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                    if (currentApiVersion <= 16) {
                        collapse = statusbarManager.getMethod("collapse");
                        collapse.invoke(service);
                        collapse.setAccessible(true);
                        collapse.invoke(service);

                    } else {
                        collapse = statusbarManager.getMethod("collapsePanels");
                        collapse.invoke(service);
                        collapse.setAccessible(true);
                        collapse.invoke(service);

                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                ex.printStackTrace();
            }
        }
    }

}
