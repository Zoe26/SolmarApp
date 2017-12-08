package com.idslatam.solmar.BravoPapa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.content.Context.AUDIO_SERVICE;

public class VolumeReceiver extends BroadcastReceiver {

    int volumenInicial, volumenFinal, volumenActual, contador = 0, auxM = 0, auxCero = 0, VecesPresionarVolumen;
    Context mContext;

    String Estado = null, DispositivoId, isScreen;
    String Latitud = null, Longitud = null, Numero = null, Velocidad, FechaDispositivo, fechaBP;

    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss")
            , formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat formatoIsoParse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    protected String URL_API;

    private static final float BEEP_VOLUME = 0.90f;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.mContext = context;
        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        volumenActual = (Integer)intent.getExtras().get("android.media.EXTRA_VOLUME_STREAM_VALUE");

        try{

            DBHelper dbHelperVolumen = new DBHelper(mContext);
            SQLiteDatabase sqlVolumen = dbHelperVolumen.getWritableDatabase();
            String selectQuery = "SELECT NivelVolumen, ContadorPulsacion, VecesPresionarVolumen," +
                    "ContadorAux, isScreen FROM Configuration";
            Cursor c = sqlVolumen.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {
                volumenFinal = c.getInt(c.getColumnIndex("NivelVolumen"));
                contador = c.getInt(c.getColumnIndex("ContadorPulsacion"));
                VecesPresionarVolumen = c.getInt(c.getColumnIndex("VecesPresionarVolumen"));
                auxCero = c.getInt(c.getColumnIndex("ContadorAux"));
                isScreen = c.getString(c.getColumnIndex("isScreen"));
            }

            c.close();
            sqlVolumen.close();

        }catch (Exception e){
            Log.e("-- |Exception| ", e.getMessage());
        }

        //IF DE PRIMER REGISTRO DE VOLUMEN
        if(volumenFinal == -1){

            contador ++;
            auxCero ++;

            try {
                DBHelper dbHelperAlarm = new DBHelper(mContext);
                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                dba.execSQL("UPDATE Configuration SET NivelVolumen = '"+volumenActual+"'");
                dba.execSQL("UPDATE Configuration SET ContadorPulsacion = '"+contador+"'");
                dba.execSQL("UPDATE Configuration SET ContadorAux = '"+auxCero+"'");
                dba.close();
            } catch (Exception e){}

            Log.e("-- |NIVEL DE VOLUMEN | ", " -1 TRUEEEEEEE");
            Log.e("-- |CONTADOR FINAL | ", String.valueOf(contador));

            return;
        }

        //******************************************************************************************
        if(contador == 0 && volumenActual == volumenFinal){

            contador ++;
            auxCero ++;

            try {
                DBHelper dbHelperAlarm = new DBHelper(mContext);
                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                dba.execSQL("UPDATE Configuration SET ContadorPulsacion = '"+contador+"'");
                dba.execSQL("UPDATE Configuration SET ContadorAux = '"+auxCero+"'");
                dba.close();
            } catch (Exception e){}

        } else if(volumenActual < volumenFinal){

            contador ++;
            auxCero ++;

            try {
                DBHelper dbHelperAlarm = new DBHelper(mContext);
                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                dba.execSQL("UPDATE Configuration SET ContadorPulsacion = '"+contador+"'");
                dba.execSQL("UPDATE Configuration SET ContadorAux = '"+auxCero+"'");
                dba.close();
            } catch (Exception e){}

            resetCuenta();

        }  else if(auxCero > 0 && auxCero < 3 && volumenActual == 1) {

            Log.e("-------------------- "," ---------------------");
            contador --;

            try {
                DBHelper dbHelperAlarm = new DBHelper(mContext);
                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                dba.execSQL("UPDATE Configuration SET ContadorPulsacion = '"+contador+"'");
                dba.close();
            } catch (Exception e){}

            playBeepSound();
            Log.e("-- |RETURN| ", " TRUEEEEEE " + String.valueOf(contador));

        } else if(volumenActual > volumenFinal) {

            try {
                DBHelper dbHelperAlarm = new DBHelper(mContext);
                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                dba.execSQL("UPDATE Configuration SET ContadorPulsacion = '0'");
                dba.execSQL("UPDATE Configuration SET ContadorAux = '0'");
                dba.close();
            } catch (Exception e){}

        } else if(volumenActual == 0){

            Log.e("---", "Screen off");
            intent = new Intent(context, SoundService.class);
            intent.putExtra("action", 0);
            context.startService(intent);

            playBeepSound();
            Log.e("----------------------P","----------------------");
            Log.e("-- |volumenFinal| ", String.valueOf(volumenFinal));
            Log.e("-- |volumenActual| ", String.valueOf(volumenActual));
            Log.e("-- |volumenAuxiliar| ", String.valueOf(auxCero));
            Log.e("-- |CONTADOR FINAL | ", String.valueOf(contador));
            Log.e("----------------------F","----------------------");


            if (isScreen.equalsIgnoreCase("true")){

                auxCero ++;
                if (auxCero%2==0){
                    contador ++;
                }

            } else {
                auxCero ++;
                contador ++;

            }

            try {
                DBHelper dbHelperAlarm = new DBHelper(mContext);
                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                dba.execSQL("UPDATE Configuration SET ContadorPulsacion = '"+contador+"'");
                dba.execSQL("UPDATE Configuration SET ContadorAux = '"+auxCero+"'");
                dba.close();
            } catch (Exception e){}


        }

        Log.e("-- |volumenFinal| ", String.valueOf(volumenFinal));
        Log.e("-- |volumenActual| ", String.valueOf(volumenActual));
        Log.e("-- |volumenAuxiliar| ", String.valueOf(auxCero));
        Log.e("-- |CONTADOR FINAL | ", String.valueOf(contador));

        if(contador==VecesPresionarVolumen){
            try {
                DBHelper dbHelperAlarm = new DBHelper(mContext);
                SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                dba.execSQL("UPDATE Configuration SET ContadorPulsacion = '0'");
                dba.execSQL("UPDATE Configuration SET ContadorAux = '0'");
                dba.close();
            } catch (Exception e){}

            try{

                DBHelper dbHelperVolumen = new DBHelper(mContext);
                SQLiteDatabase sqlVolumen = dbHelperVolumen.getWritableDatabase();
                String selectQuery = "SELECT FechaBP FROM Configuration";
                Cursor c = sqlVolumen.rawQuery(selectQuery, new String[]{});
                if (c.moveToFirst()) {fechaBP = c.getString(c.getColumnIndex("FechaBP"));}
                c.close();
                sqlVolumen.close();

            }catch (Exception e){}

            Calendar current = Calendar.getInstance();
            current.add(Calendar.SECOND, -5);

            Calendar calendar = Calendar.getInstance();


            if (fechaBP!=null){

                try {calendar.setTime(formatoIsoParse.parse(fechaBP));}
                catch (ParseException e) {Log.e(" Exception ", "Error al Parsear ");e.printStackTrace();}

                Log.e("********|current| ", formatoIsoParse.format(current.getTime()));
                Log.e("********|calendar| ", formatoIsoParse.format(calendar.getTime()));

                if (calendar.after(current)){
                    Log.e("********|RETURN DE |", "***|BP|********");
                    try {
                        DBHelper dbHelperAlarm = new DBHelper(mContext);
                        SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                        dba.execSQL("UPDATE Configuration SET ContadorPulsacion = '0'");
                        dba.close();
                    } catch (Exception e){}
                    return;
                }
                sendBP();
                Log.e("********|INICIO DE |", "***|PASO|********");

            } else {sendBP();}








        }

        try {
            DBHelper dbHelperAlarm = new DBHelper(mContext);
            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
            dba.execSQL("UPDATE Configuration SET NivelVolumen = '"+volumenActual+"'");
            dba.close();
        } catch (Exception e){}


    }

    public void resetCuenta(){

        if(contador>=3){

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    try {
                        DBHelper dbHelperAlarm = new DBHelper(mContext);
                        SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                        dba.execSQL("UPDATE Configuration SET ContadorPulsacion = '0'");
                        dba.execSQL("UPDATE Configuration SET ContadorAux = '0'");
                        dba.close();
                    } catch (Exception e){}

                }
            }, 1000*3);
        } else {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    try {
                        DBHelper dbHelperAlarm = new DBHelper(mContext);
                        SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
                        dba.execSQL("UPDATE Configuration SET ContadorPulsacion = '0'");
                        dba.execSQL("UPDATE Configuration SET ContadorAux = '0'");
                        dba.close();
                    } catch (Exception e){}

                }
            }, 1000*VecesPresionarVolumen);
        }

        auxCero = 0;

    }

    public void sendBP(){

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

        if (Numero == null){
            Log.e("-- |Numero | ", " NULL ");
            return;
        }

        if (Latitud == null){
            Toast.makeText(mContext, "BP está actualizando ubicación. Inténtelo en unos minutos.", Toast.LENGTH_LONG).show();
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
        try {
            Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(1000 * 1);
        } catch (Exception eewf) {
            Log.e(" Exception ","vibrator");
        }

        playBeepSound();

        Calendar current = Calendar.getInstance();

        try {
            DBHelper dbHelperAlarm = new DBHelper(mContext);
            SQLiteDatabase dba = dbHelperAlarm.getWritableDatabase();
            dba.execSQL("UPDATE Configuration SET FechaBP = '"+formatoIso.format(current.getTime())+"'");
            dba.close();
        } catch (Exception e){}

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
                            Log.e("-- |BP | ", "e.getMessage()");
                            return;
                        }

                        if(result.getHeaders().code()==200){

                            Log.e("********|SEND|", "***|BP|********");

                            Log.e("JSON result BP ", result.getResult());

                            try {

                                Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                                vibrator.vibrate(1000 * 3);

                            } catch (Exception eewf) {
                                Log.e(" Exception ","vibrator");
                            }

                        } else {Log.e("¡Pánico NO enviado! ", "");}

                    }
                });
    }



    public MediaPlayer playBeepSound() {

        final AudioManager mAudioManager = (AudioManager) mContext.getSystemService(AUDIO_SERVICE);
        final int originalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
                mp.stop();
                mp.release();
            }
        });

        /*MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();
            }
        });*/
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                //Log.w(TAG, "Failed to beep " + what + ", " + extra);
                // possibly media player error, so release and recreate
                mp.stop();
                mp.release();
                return true;
            }
        });
        try {
            AssetFileDescriptor file = mContext.getResources().openRawResourceFd(R.raw.empty);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            } finally {
                file.close();
            }
            mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
            mediaPlayer.prepare();
            mediaPlayer.start();
            return mediaPlayer;
        } catch (IOException ioe) {
            //Log.w(TAG, ioe);
            mediaPlayer.release();
            return null;
        }
    }


}
