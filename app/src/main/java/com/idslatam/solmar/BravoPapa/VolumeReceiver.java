package com.idslatam.solmar.BravoPapa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;

import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Api.Parser.JsonParser;
import com.idslatam.solmar.Models.Database.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class VolumeReceiver extends BroadcastReceiver {

    int vFirst, contador = 0;
    Context mContext;

    String Estado = null, DispositivoId;
    String Latitud = null, Longitud = null, Numero = null, Velocidad, FechaDispositivo;

    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss")
            , formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    protected String URL_API;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.mContext = context;

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();


        try{

            DBHelper dbHelperVolumen = new DBHelper(mContext);
            SQLiteDatabase sqlVolumen = dbHelperVolumen.getWritableDatabase();
            String selectQuery = "SELECT ContadorPulsacion FROM Configuration";
            Cursor c = sqlVolumen.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {
                contador = c.getInt(c.getColumnIndex("ContadorPulsacion"));
            }
            c.close();
            sqlVolumen.close();

        }catch (Exception e){
            Log.e("-- |Exception| ", e.getMessage());
        }

        contador ++;

        Log.e("---", "onReceive " + String.valueOf(contador));
        vFirst = (Integer)intent.getExtras().get("android.media.EXTRA_VOLUME_STREAM_VALUE");
        Log.e("--- VOLUME", String.valueOf(vFirst));

        try {
            DBHelper dbHelperAlarm = new DBHelper(mContext);
            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
            dba.execSQL("UPDATE Configuration SET ContadorPulsacion = '"+contador+"'");
            dba.close();
        } catch (Exception e){}


        if(contador>=3){

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    try {
                        DBHelper dbHelperAlarm = new DBHelper(mContext);
                        SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                        dba.execSQL("UPDATE Configuration SET ContadorPulsacion = '0'");
                        dba.close();
                    } catch (Exception e){}
                }
            }, 1000*3);
        }

        if(contador==5){

            try {
                DBHelper dbHelperAlarm = new DBHelper(mContext);
                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                dba.execSQL("UPDATE Configuration SET ContadorPulsacion = '0'");
                dba.close();
            } catch (Exception e){}

            //currentLast = currentComparacion;
            sendBP();
            Log.e("********|SEND| ", "***|BP|********");
        }
    }

    public void sendBP (){

        try{

            DBHelper dbHelperVolumen = new DBHelper(mContext);
            SQLiteDatabase sqlVolumen = dbHelperVolumen.getWritableDatabase();
            String selectQuery = "SELECT NumeroCel, Latitud, Longitud, GuidDipositivo FROM Configuration";
            Cursor c = sqlVolumen.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {
                Numero = c.getString(c.getColumnIndex("NumeroCel"));
                Latitud = c.getString(c.getColumnIndex("Latitud"));
                Longitud = c.getString(c.getColumnIndex("Longitud"));
                Velocidad = "0";
                DispositivoId = c.getString(c.getColumnIndex("GuidDipositivo"));
            }

            c.close();
            sqlVolumen.close();

        }catch (Exception e){
            Log.e("-- |EXCEPTION | ", e.getMessage());
        }

        Calendar now = Calendar.getInstance();

        try {
            FechaDispositivo = formatoGuardar.format(now.getTime());
        } catch (Exception e){};

        Log.e("-- |Numero | ", Numero);
        Log.e("-- |Latitud | ", Latitud);
        Log.e("-- |Longitud | ", Longitud);
        Log.e("-- |Velocidad | ", Velocidad);
        Log.e("-- |FechaDispositivo | ", FechaDispositivo);
        Log.e("-- |DispositivoId | ", DispositivoId);

        new PostAsync().execute(Numero, Latitud, Longitud, Velocidad, FechaDispositivo, DispositivoId);
    }

    class PostAsync extends AsyncTask<String, String, JSONObject> {

        JsonParser jsonParser = new JsonParser();

        private final String URL = URL_API.concat("api/BravoPapa");

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            Log.e("-- |URL | ", URL);

            try {

                HashMap<String, String> params = new HashMap<>();

                params.put("Numero", args[0]);
                params.put("Latitud", args[1]);
                params.put("Longitud", args[2]);
                params.put("Velocidad", args[3]);
                params.put("FechaDispositivo", args[4]);
                params.put("DispositivoId", args[5]);

                Log.e("request", "starting");

                JSONObject json = jsonParser.makeHttpRequest(URL, "POST", params);
                Log.e("-- |POST | ", "ASINC");
                if (json != null) {
                    Log.e("JSON result", json.toString());
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

                Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(1000 * 3);
            }

            if (success == 0) {
                Log.d("Hecho!", message);

            }
        }
    }

}
