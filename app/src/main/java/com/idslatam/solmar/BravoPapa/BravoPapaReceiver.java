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

import com.google.gson.JsonObject;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Api.Parser.JsonParser;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

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

        String URL = URL_API.concat("api/BravoPapa");

        Ion.with(mContext)
                .load("POST", URL)
                .setBodyParameter("Numero", Numero)
                .setBodyParameter("Latitud", Latitud)
                .setBodyParameter("Longitud", Longitud)
                .setBodyParameter("Velocidad", Velocidad)
                .setBodyParameter("FechaDispositivo", FechaDispositivo)
                .setBodyParameter("DispositivoId", DispositivoId)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {

                        if (e!=null){
                            return;
                        }

                        if(result.getHeaders().code()==200){

                            String stringBP = result.getResult();
                            Log.e("JSON result BP ", stringBP);

                            try {

                                Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                                vibrator.vibrate(1000 * 2);

                            } catch (Exception eewf) {
                                eewf.printStackTrace();
                            }

                        } else {

                            Log.e("¡Pánico NO enviado! ", "");
                            //Toast.makeText(mContext, "¡Pánico no enviado! ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}
