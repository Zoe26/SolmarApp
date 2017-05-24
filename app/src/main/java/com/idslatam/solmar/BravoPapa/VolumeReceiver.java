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
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class VolumeReceiver extends BroadcastReceiver {

    int vFirst, vLast, contador = 0, VecesPresionarVolumen;
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

        vFirst = (Integer)intent.getExtras().get("android.media.EXTRA_VOLUME_STREAM_VALUE");
        Log.e("--- VOLUME ACTUAL ", String.valueOf(vFirst));






        /*try{

            DBHelper dbHelperVolumen = new DBHelper(mContext);
            SQLiteDatabase sqlVolumen = dbHelperVolumen.getWritableDatabase();
            String selectQuery = "SELECT NivelVolumen FROM Configuration";
            Cursor c = sqlVolumen.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {
                vLast = c.getInt(c.getColumnIndex("NivelVolumen"));
            }

            c.close();
            sqlVolumen.close();

        }catch (Exception e){
            Log.e("-- |Exception| ", e.getMessage());
        }

        //IF DE PRIMER REGISTRO DE VOLUMEN
        if(vLast==0){

            try {
                DBHelper dbHelperAlarm = new DBHelper(mContext);
                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                dba.execSQL("UPDATE Configuration SET NivelVolumen = '"+vFirst+"'");
                dba.close();
            } catch (Exception e){}

            //return;
        }


        /*//*******************************************************************************



        try{

            DBHelper dbHelperVolumen = new DBHelper(mContext);
            SQLiteDatabase sqlVolumen = dbHelperVolumen.getWritableDatabase();
            String selectQuery = "SELECT ContadorPulsacion, VecesPresionarVolumen FROM Configuration";
            Cursor c = sqlVolumen.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {
                contador = c.getInt(c.getColumnIndex("ContadorPulsacion"));
                VecesPresionarVolumen = c.getInt(c.getColumnIndex("VecesPresionarVolumen"));
            }
            c.close();
            sqlVolumen.close();

        }catch (Exception e){
        }

        Log.e("-- |vFirst| ", String.valueOf(vFirst));
        Log.e("-- |vLast| ", String.valueOf(vLast));

        if(vFirst < vLast){
            contador ++;
            Log.e("-- |contador| ", String.valueOf(contador));

        } else if (vFirst == 0) {
            contador ++;
            Log.e("--- VOLUME = 0 ", String.valueOf(vFirst));
            Log.e("-- |contador| ", String.valueOf(contador));
        }

        try {
            DBHelper dbHelperAlarm = new DBHelper(mContext);
            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
            dba.execSQL("UPDATE Configuration SET ContadorPulsacion = '"+contador+"'");
            dba.close();
        } catch (Exception e){}


        if(contador>=2){

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    resetCuenta();

                }
            }, 1000*3);
        }

        if(contador==VecesPresionarVolumen){

            resetCuenta();

            sendBP();
            Log.e("********|SEND| ", "***|BP|********");
        }

        try {
            DBHelper dbHelperAlarm = new DBHelper(mContext);
            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
            dba.execSQL("UPDATE Configuration SET NivelVolumen = '"+vFirst+"'");
            dba.close();
        } catch (Exception e){}*/
    }

    public void resetCuenta(){

        try {
            DBHelper dbHelperAlarm = new DBHelper(mContext);
            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
            dba.execSQL("UPDATE Configuration SET ContadorPulsacion = '0'");
            dba.close();
        } catch (Exception e){}

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

        now = Calendar.getInstance();

        try {
            FechaDispositivo = formatoGuardar.format(now.getTime());
        } catch (Exception e){}


        if (Numero == null){
            Log.e("-- |Numero | ", " NULL ");
            return;
        }

        if (Latitud == null){
            Log.e("-- |Latitud | ", " NULL ");
            return;
        }

        if (Longitud == null){
            Log.e("-- |Longitud | ", " NULL ");
            return;
        }

        if (Velocidad == null){
            Log.e("-- |Velocidad | ", " NULL ");
            return;
        }

        if (FechaDispositivo == null){
            Log.e("-- |FechaDispositivo | ", " NULL ");
            return;
        }

        if (DispositivoId == null){
            Log.e("-- |DispositivoId | ", " NULL ");
            return;
        }

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
                            Log.e("-- |BP | ", e.getMessage());
                            return;
                        }

                        if(result.getHeaders().code()==200){

                            Log.e("JSON result BP ", result.getResult());

                            try {

                                Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                                vibrator.vibrate(1000 * 2);

                            } catch (Exception eewf) {
                                Log.e(" Exception ","vibrator");
                            }

                        } else {

                            Log.e("¡Pánico NO enviado! ", "");
                            //Toast.makeText(mContext, "¡Pánico no enviado! ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

}
