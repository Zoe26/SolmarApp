package com.idslatam.solmar.Tracking.Broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Cargo.CargoActivity;
import com.idslatam.solmar.Models.Crud.CargoFotoCrud;
import com.idslatam.solmar.Models.Crud.PatrolFotoCrud;
import com.idslatam.solmar.Models.Crud.PeopleFotoCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.CargoFoto;
import com.idslatam.solmar.Models.Entities.PatrolFoto;
import com.idslatam.solmar.Models.Entities.PeopleFoto;
import com.idslatam.solmar.Pruebas.Crud.AlarmTrackCrud;
import com.idslatam.solmar.Pruebas.Entities.AlarmTrack;
import com.idslatam.solmar.Tracking.Services.LocationFusedApi;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.Response;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class AlarmLocation extends BroadcastReceiver {

    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss"),
            formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    String scurrentInicio;
    Calendar ccurrentInicio;

    int intervalo;

    //+++++++++++++++++++++
    private Context mContext;
    int _AlarmTrack_Id = 0;

    //--------------------------------------------------------
    boolean gps;
    //---------------------------------------------------------

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;

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



           /* IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            BroadcastReceiver mReceiver = new ScreenReceiver();

            mContext.getApplicationContext().registerReceiver(mReceiver, filter);*/



        gps = isGPSAvailable();

        //NO TOCAR *****************************************************************************************************************************
        int vApi = Build.VERSION.SDK_INT;

        if (vApi > 19) {

            //Log.e("Alarm Api > 19", "Execute");

            updateFechaAlarmLocation();

            try {

                Intent alarm = new Intent(context, AlarmLocation.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarm, 0);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                long timeInMillis = (SystemClock.elapsedRealtime() + 1000 * 60);
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeInMillis, pendingIntent);

            } catch (Exception e){
                Toast.makeText(mContext, "Excepcion Start Service", Toast.LENGTH_LONG).show();
            }

        } else {

            Log.e("Alarm Api < 19", "Execute");

            updateFechaAlarmLocation();

            try {

                Intent alarm = new Intent(context, AlarmLocation.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarm, 0);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                long timeInMillis = (SystemClock.elapsedRealtime() + 1000 * 60);
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeInMillis, pendingIntent);

            } catch (Exception e){
                Log.e(" pendingIntent ", " ALARM EXEPTION ");
            }


        }
        //*************************************************************************************************************************************

        try {

            DBHelper dbHelperIntervalo = new DBHelper(mContext);
            SQLiteDatabase dba = dbHelperIntervalo.getWritableDatabase();
            String selectQuery = "SELECT IntervaloTracking, FechaInicioIso FROM Configuration";
            Cursor ca = dba.rawQuery(selectQuery, new String[]{});

            if (ca.moveToFirst()) {
                intervalo = ca.getInt(ca.getColumnIndex("IntervaloTracking"));
                scurrentInicio = ca.getString(ca.getColumnIndex("FechaInicioIso"));
            }
            ca.close();
            dba.close();

        } catch (Exception e) {}

        if(intervalo == 0){ intervalo = 1;}

        //Log.e("-- PRUEBA INT ALARM ", String.valueOf(intervalo));


        if(intervalo <= 1){

            try {
                DBHelper dataBaseHelperA = new DBHelper(mContext);
                SQLiteDatabase dbA = dataBaseHelperA.getWritableDatabase();
                dbA.execSQL("UPDATE Configuration SET FechaInicioIso = NULL");
                dbA.close();

            } catch (Exception e) {}

            // INTENTI AL SERVICIO ----------------------------------------------------------------------
            Intent background = new Intent(context, LocationFusedApi.class);
            context.startService(background);
            // FIN INTENTI AL SERVICIO ------------------------------------------------------------------


        } else {

            if(scurrentInicio==null){

                intervalo = intervalo-1;
                Calendar currentNow = Calendar.getInstance();
                currentNow.add(Calendar.MINUTE, intervalo);

                try {
                    DBHelper dataBaseHelperA = new DBHelper(mContext);
                    SQLiteDatabase dbA = dataBaseHelperA.getWritableDatabase();
                    dbA.execSQL("UPDATE Configuration SET FechaInicioIso = '"+ formatoIso.format(currentNow.getTime()) +"'");
                    dbA.close();

                } catch (Exception e) {}

                Log.e("-- ALARM NULL ", String.valueOf(currentNow.getTime()));

            } else {

                Log.e("-- ALARM FechInicioIso ", String.valueOf(scurrentInicio));
                Calendar currentNow = Calendar.getInstance();

                ccurrentInicio = Calendar.getInstance();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    ccurrentInicio.setTime(sdf.parse(scurrentInicio));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Log.e("-- INTERVALO ALARM NOW ", String.valueOf(currentNow.getTime()));
                Log.e("-- INTERVALO ALARM ", String.valueOf(ccurrentInicio.getTime()));

                if(currentNow.after(ccurrentInicio)){

                    currentNow.add(Calendar.MINUTE, intervalo);

                    try {
                        DBHelper dataBaseHelperA = new DBHelper(mContext);
                        SQLiteDatabase dbA = dataBaseHelperA.getWritableDatabase();
                        dbA.execSQL("UPDATE Configuration SET FechaInicioIso = '"+ formatoIso.format(currentNow.getTime()) +"'");
                        dbA.close();

                    } catch (Exception e) {}

                    // INTENTI AL SERVICIO ----------------------------------------------------------------------
                    Intent background = new Intent(context, LocationFusedApi.class);
                    context.startService(background);
                    // FIN INTENTI AL SERVICIO ------------------------------------------------------------------

                }
            }
        }

        try{
            Log.e("alarmSendPhtAsync","Inicio de envío async 0");
            //Envio de imagenes:
            CargoFotoCrud _imagenCargo = new CargoFotoCrud(mContext);
            List<CargoFoto> cargoFotos = _imagenCargo.listFotosForSync();

            if(cargoFotos.size() >0){
                new AlarmLocation.sendPhotoAsync().execute(cargoFotos);
                Log.e("alarmSendPhtAsync","Inicio de envío async 1");
            }
        }
        catch (Exception e){

        }

        //Reenvío Patrol
        try{
            Log.e("alarmSendPhtAsync","Inicio de envío async 0");
            //Envio de imagenes:
            PatrolFotoCrud _imagenPatrol = new PatrolFotoCrud(mContext);
            List<PatrolFoto> patrolFotos = _imagenPatrol.listFotosForSync();

            if(patrolFotos.size() >0){
                new AlarmLocation.sendPhotoPatrolAsync().execute(patrolFotos);
                Log.e("alarmSendPhtAsync","Inicio de envío async 1");
            }
        }
        catch (Exception e){

        }

        //Reenvío People
        try{
            Log.e("alarmSendPhtAsync","Inicio de envío async 0");
            //Envio de imagenes:
            PeopleFotoCrud _imagenPeople = new PeopleFotoCrud(mContext);
            List<PeopleFoto> peopleFotos = _imagenPeople.listFotosForSync();

            if(peopleFotos.size() >0){
                new AlarmLocation.sendPhotoPeopleAsync().execute(peopleFotos);
                Log.e("alarmSendPhtAsync","Inicio de envío async 2");
            }
        }
        catch (Exception e){

        }

    }

    private class sendPhotoPeopleAsync extends AsyncTask<List<PeopleFoto>, Void, List<PeopleFoto>> {

        @Override
        protected List<PeopleFoto> doInBackground(List<PeopleFoto>... params) {

            String DispositivoId = "";
            String URL_API = "";
            int TIME_OUT = 5*60 * 1000;

            Constants globalClass = new Constants();
            URL_API = globalClass.getURL();

            List<PeopleFoto> objFotoWork = params[0];
            PeopleFotoCrud peopleFotoCrud = new PeopleFotoCrud(mContext);

            try {
                DBHelper dataBaseHelper = new DBHelper(mContext);
                SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
                String selectQuery = "SELECT GuidDipositivo, NumeroCel FROM Configuration";
                Cursor c = dbst.rawQuery(selectQuery, new String[]{});
                if (c.moveToFirst()) {
                    //Numero = c.getString(c.getColumnIndex("NumeroCel"));
                    DispositivoId = c.getString(c.getColumnIndex("GuidDipositivo"));
                }
                c.close();
                dbst.close();

            } catch (Exception e) {}


            for (PeopleFoto peopleFoto : objFotoWork) {

                File archivoFoto = new File(peopleFoto.filePath);

                if(archivoFoto.isFile()){

                    //
                    String URL = URL_API.concat("api/People/SincronizacionFoto");

                    //Log.e("Numero", Numero);
                    Log.e("DispositivoId", DispositivoId);
                    Log.e("CodigoSincro", peopleFoto.codigoSincronizacion);
                    //Log.e("Tipo Foto", String.valueOf(cargoFoto.tipoFoto));
                    Log.e("File Path", peopleFoto.filePath);
                    Log.e("Indice", peopleFoto.indice);

                    Ion.with(mContext)
                            .load(URL)
                            .uploadProgressHandler(new ProgressCallback() {
                                @Override
                                public void onProgress(long uploaded, long total) {
                                    Log.e("total = " + String.valueOf((int) total), "--- uploaded = " + String.valueOf(uploaded));
                                }
                            })
                            .setTimeout(TIME_OUT)
                            .setMultipartParameter("DispositivoId", DispositivoId)
                            .setMultipartParameter("CodigoSincronizacion", peopleFoto.codigoSincronizacion)
                            .setMultipartParameter("TipoFoto", String.valueOf(peopleFoto.tipoFoto))
                            .setMultipartParameter("Id", String.valueOf(peopleFoto.peopleFotoId))
                            .setMultipartParameter("Indice", peopleFoto.indice)
                            .setMultipartFile("file", new File(peopleFoto.filePath))
                            //.setMultipartFile("Panoramica", new File(Panoramica))
                            .asString()
                            .withResponse()
                            .setCallback(new FutureCallback<Response<String>>() {
                                @Override
                                public void onCompleted(Exception e, Response<String> response) {

                                    if(response.getHeaders().code()==200){

                                        Gson gson = new Gson();
                                        JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                                        Log.e("JsonObject ", result.toString());

                                        if (result.get("Estado").getAsBoolean()){

                                            peopleFotoCrud.removePeopleFoto(peopleFoto);

                                            File file = new File(peopleFoto.filePath);
                                            file.delete();

                                        }

                                    }
                                }
                            });
                }
                else{
                    peopleFotoCrud.removePeopleFoto(peopleFoto);
                }
            }

            return objFotoWork;
        }

        @Override
        protected void onPostExecute(List<PeopleFoto> result) {

        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    private class sendPhotoPatrolAsync extends AsyncTask<List<PatrolFoto>, Void, List<PatrolFoto>> {

        @Override
        protected List<PatrolFoto> doInBackground(List<PatrolFoto>... params) {

            String DispositivoId = "";
            String URL_API = "";
            int TIME_OUT = 5*60 * 1000;

            Constants globalClass = new Constants();
            URL_API = globalClass.getURL();

            List<PatrolFoto> objFotoWork = params[0];
            PatrolFotoCrud patrolFotoCrud = new PatrolFotoCrud(mContext);

            try {
                DBHelper dataBaseHelper = new DBHelper(mContext);
                SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
                String selectQuery = "SELECT GuidDipositivo, NumeroCel FROM Configuration";
                Cursor c = dbst.rawQuery(selectQuery, new String[]{});
                if (c.moveToFirst()) {
                    //Numero = c.getString(c.getColumnIndex("NumeroCel"));
                    DispositivoId = c.getString(c.getColumnIndex("GuidDipositivo"));
                }
                c.close();
                dbst.close();

            } catch (Exception e) {}


            for (PatrolFoto patrolFoto : objFotoWork) {

                File archivoFoto = new File(patrolFoto.filePath);

                if(archivoFoto.isFile()){

                    //
                    String URL = URL_API.concat("api/Patrol/SincronizacionFoto");

                    //Log.e("Numero", Numero);
                    Log.e("DispositivoId", DispositivoId);
                    Log.e("CodigoSincro", patrolFoto.codigoSincronizacion);
                    //Log.e("Tipo Foto", String.valueOf(cargoFoto.tipoFoto));
                    Log.e("File Path", patrolFoto.filePath);
                    Log.e("Indice", patrolFoto.indice);

                    Ion.with(mContext)
                            .load(URL)
                            .uploadProgressHandler(new ProgressCallback() {
                                @Override
                                public void onProgress(long uploaded, long total) {
                                    Log.e("total = " + String.valueOf((int) total), "--- uploaded = " + String.valueOf(uploaded));
                                }
                            })
                            .setTimeout(TIME_OUT)
                            .setMultipartParameter("DispositivoId", DispositivoId)
                            .setMultipartParameter("CodigoSincronizacion", patrolFoto.codigoSincronizacion)
                            .setMultipartParameter("Id", String.valueOf(patrolFoto.patrolFotoId))
                            .setMultipartParameter("Indice", patrolFoto.indice)
                            .setMultipartFile("file", new File(patrolFoto.filePath))
                            //.setMultipartFile("Panoramica", new File(Panoramica))
                            .asString()
                            .withResponse()
                            .setCallback(new FutureCallback<Response<String>>() {
                                @Override
                                public void onCompleted(Exception e, Response<String> response) {

                                    if(response.getHeaders().code()==200){

                                        Gson gson = new Gson();
                                        JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                                        Log.e("JsonObject ", result.toString());

                                        if (result.get("Estado").getAsBoolean()){

                                            patrolFotoCrud.removePatrolFoto(patrolFoto);

                                            File file = new File(patrolFoto.filePath);
                                            file.delete();

                                        }

                                    }
                                }
                            });
                }
                else{
                    patrolFotoCrud.removePatrolFoto(patrolFoto);
                }
            }

            return objFotoWork;
        }

        @Override
        protected void onPostExecute(List<PatrolFoto> result) {

        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    private class sendPhotoAsync extends AsyncTask<List<CargoFoto>, Void, List<CargoFoto>> {


        @Override
        protected List<CargoFoto> doInBackground(List<CargoFoto>... params) {

            Log.e("alarmSendPhtAsync","Inicio de envío async 2");

            String Numero = "";
            String DispositivoId = "";
            String URL_API = "";
            int TIME_OUT = 5*60 * 1000;

            Constants globalClass = new Constants();
            URL_API = globalClass.getURL();

            List<CargoFoto> objFotoWork = params[0];
            CargoFotoCrud cargoFotoCrud = new CargoFotoCrud(mContext);

            try {
                DBHelper dataBaseHelper = new DBHelper(mContext);
                SQLiteDatabase dbst = dataBaseHelper.getWritableDatabase();
                String selectQuery = "SELECT GuidDipositivo, NumeroCel FROM Configuration";
                Cursor c = dbst.rawQuery(selectQuery, new String[]{});
                if (c.moveToFirst()) {
                    Numero = c.getString(c.getColumnIndex("NumeroCel"));
                    DispositivoId = c.getString(c.getColumnIndex("GuidDipositivo"));
                }
                c.close();
                dbst.close();

            } catch (Exception e) {}


            for (CargoFoto cargoFoto : objFotoWork) {

                File archivoFoto = new File(cargoFoto.filePath);

                if(archivoFoto.isFile()){

                    //
                    String URL = URL_API.concat("api/Cargo/SincronizacionFoto");

                    Log.e("Numero", Numero);
                    Log.e("DispositivoId", DispositivoId);
                    Log.e("CodSincro", cargoFoto.codigoSincronizacion);
                    Log.e("Tipo Foto", String.valueOf(cargoFoto.tipoFoto));
                    Log.e("File Path", cargoFoto.filePath);
                    Log.e("Indice", cargoFoto.indice);

                    Ion.with(mContext)
                            .load(URL)
                            .uploadProgressHandler(new ProgressCallback() {
                                @Override
                                public void onProgress(long uploaded, long total) {
                                    Log.e("total = " + String.valueOf((int) total), "--- uploaded = " + String.valueOf(uploaded));
                                }
                            })
                            .setTimeout(TIME_OUT)
                            .setMultipartParameter("DispositivoId", DispositivoId)
                            .setMultipartParameter("CodigoSincronizacion", cargoFoto.codigoSincronizacion)
                            .setMultipartParameter("TipoFoto", String.valueOf(cargoFoto.tipoFoto))
                            .setMultipartParameter("Id", String.valueOf(cargoFoto.cargoFotoId))
                            .setMultipartParameter("Indice", cargoFoto.indice)
                            .setMultipartFile("file", new File(cargoFoto.filePath))
                            //.setMultipartFile("Panoramica", new File(Panoramica))
                            .asString()
                            .withResponse()
                            .setCallback(new FutureCallback<Response<String>>() {
                                @Override
                                public void onCompleted(Exception e, Response<String> response) {

                                    if(response.getHeaders().code()==200){

                                        Gson gson = new Gson();
                                        JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                                        Log.e("JsonObject ", result.toString());

                                        if (result.get("Estado").getAsBoolean()){

                                            cargoFotoCrud.removeCargoFoto(cargoFoto);

                                            File file = new File(cargoFoto.filePath);
                                            file.delete();

                                        }

                                    }
                                }
                            });
                }
                else{
                    cargoFotoCrud.removeCargoFoto(cargoFoto);
                }
            }

            return objFotoWork;
        }

        @Override
        protected void onPostExecute(List<CargoFoto> result) {

        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    public Boolean updateFechaAlarmLocation() {

        Calendar currentDate = Calendar.getInstance();

        try {
            DBHelper dataBaseHelperA = new DBHelper(mContext);
            SQLiteDatabase dbA = dataBaseHelperA.getWritableDatabase();
            dbA.execSQL("UPDATE Configuration SET FechaEjecucionAlarm = '"+ formatoGuardar.format(currentDate.getTime()) +"'");
            dbA.execSQL("UPDATE Configuration SET FechaAlarmaIso = '"+ formatoIso.format(currentDate.getTime()) +"'");
            dbA.close();

        } catch (Exception e) {}

        //Log.e("-- PRUEBA ALARM ", formatoIso.format(currentDate.getTime()));

        AlarmTrackCrud alarmTrackCrud = new AlarmTrackCrud(mContext);
        AlarmTrack alarmTrack = new AlarmTrack();

        alarmTrack.FechaAlarm = formatoIso.format(currentDate.getTime());
        alarmTrack.Estado = "true";

        try {
            _AlarmTrack_Id = alarmTrackCrud.insert(alarmTrack);
        }catch (Exception e){}

        return true;
    }

    private boolean isGPSAvailable() {

        //return true;
        LocationManager locationManagerx = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManagerx.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        } else {
            return true;
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

        //Log.e("--| ACTIVANDO ","DATOS.....");
        //Toast.makeText(this, "Activando Datos..", Toast.LENGTH_SHORT).show();
    }

}