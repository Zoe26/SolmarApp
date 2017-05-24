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

    private GoogleApiClient mGoogleApiClient;
    final static int REQUEST_LOCATION = 199;
    Button acceso;

    EditText password;
    Context context;
    String pass;
    String user;
    protected String URL_API;


    ConfigurationCrud configurationCRUD = new ConfigurationCrud(this);
    DBHelper dataBaseHelper = new DBHelper(this);

    String Fotoch;

    BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_login);
        this.context = this;

        /*//------------------------------------------------------------------------------------------

        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            mReceiver = new ScreenReceiver();

            this.getApplicationContext().registerReceiver(mReceiver, filter);

        } catch (IllegalArgumentException e) {
            Log.e("EXCEPTION REGISTER ", e.getMessage());
        }*/
        //------------------------------------------------------------------------------------------

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

            //Log.e("builder Last ",String.valueOf(builder));
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
                            //Log.e("Request SUCCESS ",String.valueOf(status));
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be
                            // fixed by showing the user

                            //Log.e("RESOLUTION_REQUIRED ",String.valueOf(status));
                            try {

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

                            //Log.e("Request ", "SETTINGS_CHANGE_UNAVAILABLE "+String.valueOf(status));
                            break;
                    }
                }
            });
        }

        // METODO QUE INICIA EL SERVICIO LOCATION ***************************************************
        Intent alarm = new Intent(this.context, AlarmLocation.class);
        boolean alarmRunning = (PendingIntent.getBroadcast(this, 0, alarm, PendingIntent.FLAG_NO_CREATE) != null);

        int vApi = Build.VERSION.SDK_INT;

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
                startActivity(new Intent(getBaseContext(), Perfil.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra("State", false));
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
        //Log.e("INGRESO  ", "onActivityResult");
//        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);

        switch (requestCode) {
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Toast.makeText(context, "¡Configuración éxitosa!", Toast.LENGTH_LONG).show();
                        //Log.e("VerificarAcceso ","RESULT_OK");
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
                    getCredentials();
                }


                break;
        }
    }

    String accessToken;
    private void getCredentials() {

        final ProgressDialog pDialog;

        pDialog = new ProgressDialog(Login.this);
        pDialog.setMessage("Cargando Configuraci\u00f3n...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

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

                        if(result == null){

                            try {

                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }

                            } catch (Exception es){}

                            Toast.makeText(context, "¡Error de red!. Por favor revise su conexión a internet.", Toast.LENGTH_LONG).show();
                            Log.e("Error de red ", " -- API TOKEN --");
                            return;
                        }

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

                            Intent intent = new Intent(Login.this, Perfil.class);
                            intent.putExtra("State", true);

                            try {

                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }

                            } catch (Exception es){}

                            startActivity(intent);

                        }

                        try {

                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }

                        } catch (Exception es){}

                    }
                });
    }

    //----------------------------------------------------------------------------------------------
    public void configuracionRechazada(){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle("¡Advertencia!");
        builder.setMessage("Al no aceptar las configuraciones previas Solgis no iniciara de manera correcta. Por favor intente nuevamente");
        builder.setPositiveButton("Ok", null);
        builder.show();
    }

}
