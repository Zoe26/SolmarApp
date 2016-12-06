package com.idslatam.solmar.BravoPapa;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;

import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Api.Parser.JsonParser;
import com.idslatam.solmar.Models.Database.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class BravoPapaReceiver extends BroadcastReceiver {

    Context mContext;

    int vFirst, vLast;

    int i, prueA, prueB;

    String sCurrentLast;

    Calendar currentLast, currentFirst;

    Calendar currentComparacion = Calendar.getInstance();

    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss")
            , formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    protected String URL_API;

    String Estado = null, DispositivoId;
    String Latitud = null, Longitud = null, Numero = null, Velocidad, FechaDispositivo;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext= context;

        vFirst = (Integer)intent.getExtras().get("android.media.EXTRA_VOLUME_STREAM_VALUE");

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        try{

            DBHelper dbHelperVolumen = new DBHelper(mContext);
            SQLiteDatabase sqlVolumen = dbHelperVolumen.getWritableDatabase();
            String selectQuery = "SELECT NivelVolumen, BPFechaInicio FROM Configuration";
            Cursor c = sqlVolumen.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {
                vLast = c.getInt(c.getColumnIndex("NivelVolumen"));
                sCurrentLast = c.getString(c.getColumnIndex("BPFechaInicio"));
            }
            c.close();
            sqlVolumen.close();

        }catch (Exception e){
            Log.e("-- |Exception| ", e.getMessage());
        }

        if(vFirst == 0){
            Log.e("-- |VOL| ", "");
        }


        //IF DE PRIMER REGISTRO DE VOLUMEN
        if(vLast==0){

            currentFirst = Calendar.getInstance();

            try {
                DBHelper dbHelperAlarm = new DBHelper(mContext);
                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                dba.execSQL("UPDATE Configuration SET NivelVolumen = '"+vFirst+"'");
                dba.execSQL("UPDATE Configuration SET BPFechaInicio = '"+formatoIso.format(currentFirst.getTime())+"'");
                dba.close();
            } catch (Exception e){}

            return;
        }

        if(vFirst < vLast){

            currentLast = Calendar.getInstance();
            try {
                currentLast.setTime(formatoIso.parse(sCurrentLast));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Log.e("-- |cLast| ", formatoIso.format(currentLast.getTime()));
            Log.e("-- |cComp| ", formatoIso.format(currentComparacion.getTime()));

            long milis1, milis2, diff;
            milis1 = currentLast.getTimeInMillis();
            milis2 = currentComparacion.getTimeInMillis();
            diff = milis2-milis1;

            long diffMinutos =  Math.abs (diff/(1000));
            i = (int) diffMinutos;

            Log.e("-- |DIFERENCIA| ", String.valueOf(i));
            if(i>=6){
                i=0;
                //currentLast = currentComparacion;
                sendBP();
                Log.e("********|SEND| ", "***|BP|********");
            }
        }

        //ACTUALIZAR VOLUMEN
        try {
            DBHelper dbHelperAlarm = new DBHelper(mContext);
            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
            dba.execSQL("UPDATE Configuration SET NivelVolumen = '"+vFirst+"'");
            dba.close();
        } catch (Exception e){}

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    DBHelper dbHelperAlarm = new DBHelper(mContext);
                    SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                    dba.execSQL("UPDATE Configuration SET NivelVolumen = '0'");
                    dba.close();
                } catch (Exception e){}
            }
        }, 1000*10);

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
                params.put("FechaDispositivo", args[1]);
                params.put("Latitud", args[2]);
                params.put("Longitud", args[3]);
                params.put("Velocidad", args[4]);
                params.put("DispositivoId", args[5]);

                Log.d("request", "starting");
                Log.e("-- |POST | ", "ASINC");
                JSONObject json = jsonParser.makeHttpRequest(URL, "POST", params);

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
            if (json != null) {

                Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(1000 * 3);

                try {
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success == 0) {
                Log.d("Hecho!", message);

            }
        }
    }
}