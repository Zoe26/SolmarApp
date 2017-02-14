package com.idslatam.solmar.View.Fragments;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Api.Parser.JsonParser;
import com.idslatam.solmar.Models.Crud.AlertCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.idslatam.solmar.Models.Entities.Alert;
import com.idslatam.solmar.R;
import com.idslatam.solmar.Tracking.Broadcast.AlarmLocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SampleFragment extends Fragment implements  View.OnClickListener {


    Context mContext;

    Button btnMarcacion;
    TextView textHora, textUltimaMarcacion, textUltimaMarcacionFecha, textHoraFecha, textintervalo, textflag_tiempo;

    View myView;
    ImageView caritaEstado;

    String Proxima;
    String HoraMarcacionP;

    String EstadoBoton, FechaEsperadaIso, FechaEsperadaIsoFin, EstadoE;
    Calendar horaIni, horaFin, horaIso, horaAuxC;

    String FechaEsperada, FechaProxima, FlagTiempo, MargenAceptado;


    String fechaAux = null;
    //**********************************************************************************************

    int tiempoEnvio, tiempoIntervalo, tiempoGuardado, tiempoIntervaloView, countInicial=0;

    int _Alert_Id = 0;
    private int _AlertUpdate_Id = 0;
    int c = 0;
    Calendar choraProximaG, choraEsperadaG, choraIso, choraIsoFin;

    Calendar fechaEsperadaGlobal;

    String  horaEsperadaG, horaProximaG, horaEsperadaIsoG, horaEsperadaIsoFinG;
    String LatitudG, LongitudG, NumeroG, DispositivoId, CodigoEmpleado;

    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss")
            , formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    boolean flagMostrarFecha = false;

    protected String URL_API;

    String FechaMarcacionE, FechaEsperadaE, FechaProximaE, FlagTiempoE, MargenAceptadoE, LatitudE, LongitudE, NumeroE;
    String DispositivoIdE, CodigoEmpleadoE;

    int ValorTemporal;
    private static final float BEEP_VOLUME = 0.10f;

    CountDownTimer cdt5;


    public static SampleFragment newInstance(String text) {
        Bundle args = new Bundle();
        args.putString("", text);

        SampleFragment sampleFragment = new SampleFragment();
        sampleFragment.setArguments(args);

        return sampleFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        this.mContext = container.getContext();
        //--------------------------------------------------------------------

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        myView = inflater.inflate(R.layout.fragment_alert, container, false);
        textHora = (TextView) myView.findViewById(R.id.hora);
        textHoraFecha = (TextView) myView.findViewById(R.id.hora_fecha);
        textintervalo = (TextView) myView.findViewById(R.id.intervalo);
        textflag_tiempo = (TextView) myView.findViewById(R.id.flag_tiempo);
        caritaEstado = (ImageView) myView.findViewById(R.id.carita);

        textUltimaMarcacion  = (TextView) myView.findViewById(R.id.ultima_marcacion);
        textUltimaMarcacionFecha = (TextView) myView.findViewById(R.id.ultima_marcacion_fecha);

        btnMarcacion = (Button) myView.findViewById(R.id.btn_marcacion);
        btnMarcacion.setOnClickListener(this);

        /*btnMarcacion.setEnabled(false);
        btnMarcacion.setBackgroundColor(Color.WHITE);*/

        btnMarcacion.setEnabled(false);
        btnMarcacion.setBackgroundColor(getResources().getColor(R.color.boton_deshabilitado));
        btnMarcacion.setTextColor(Color.WHITE);

        load();

        return myView;
    }


    public void load(){

        if(cantidadRegistros() != 0){
            updateCountDown();
            mostrarHora();
        }

        new getDatos().execute();
    }

    class getDatos extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {

            try {
                if(obtenerDatos() == true){

                    if(cantidadRegistros() == 0){
                        //Creacion de primero registro
                        crearRegistro();
                        updateCountDown();
                        mostrarHora();

                    }

                    Log.e(" Completando ","Datos...");

                } else {
                    load();
                }

            } catch (Exception e) {e.printStackTrace();}

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            //Log.e(" Datos ","Completados! ");

        }
    }


    public void loadPost(){

        new getCountDown().execute();
    }

    class getCountDown extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {

            try {
                if(obtenerDatos() == true){

                    if(cantidadRegistros() != 0){
                        crearRegistro();
                        mostrarHora();
                        updateCountDown();

                    }

                    Log.e(" Completando ","Datos...");

                } else {
                    loadPost();
                    Log.e(" ----- ERROR "," ++++++ getCountDown +++++");
                }

            } catch (Exception e) {e.printStackTrace();}

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            //Log.e(" Datos ","Completados! ");

        }
    }

    public Boolean obtenerDatos(){

        try {

            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

            String selectQuery = "SELECT IntervaloMarcacion, CodigoEmpleado, IntervaloMarcacionTolerancia" +
                    ",NumeroCel, GuidDipositivo  FROM Configuration";

            Cursor c = db.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {

                tiempoGuardado = c.getInt(c.getColumnIndex("IntervaloMarcacion"));
                tiempoIntervalo = c.getInt(c.getColumnIndex("IntervaloMarcacionTolerancia"));
                NumeroG = c.getString(c.getColumnIndex("NumeroCel"));
                DispositivoId = c.getString(c.getColumnIndex("GuidDipositivo"));
                CodigoEmpleado = c.getString(c.getColumnIndex("CodigoEmpleado"));

            }
            c.close();
            db.close();

        } catch (Exception e) {}

        if(tiempoGuardado == 0){
            // Log.e("--- tiempoGuardado ", String.valueOf(tiempoGuardado));
            return  false;
        }

        if(CodigoEmpleado==null){
            //Log.e("--- CodigoEmpleado IF ", String.valueOf(CodigoEmpleado));
            return  false;
        }

        if(DispositivoId==null){
            //Log.e("--- CodigoEmpleado IF ", String.valueOf(CodigoEmpleado));
            return  false;
        }

        return true;
    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_marcacion:

                btnMarcacion.setEnabled(false);
                //btnMarcacion.setBackgroundColor(Color.WHITE);
                btnMarcacion.setBackgroundColor(getResources().getColor(R.color.boton_deshabilitado));
                btnMarcacion.setTextColor(Color.WHITE);

                try {
                    enviarMarcacion();
                }catch (Exception e){
                    Log.e("--- ++EXCEPTION++ ", " +++ enviarMarcacion+++");
                }

                try {

                    if(obtenerDatos() == true){

                        crearRegistro();
                        mostrarHora();

                    }

                }catch (Exception e){
                    Log.e("--- Boton EXCEPTION ", "obtenerDatos() == true ");
                }

                try {
                    //loadPost();
                    //new getCountDown().execute();
                    //updateCountDown();
                }catch (Exception e){
                    Log.e("--- ++EXCEPTION++ ", " +++ updateCountDown +++");
                }

                break;
        }
    }

    public Boolean enviarMarcacion(){

        Log.e("--- INGRESÓ ", "+++++++ enviarMarcacion ++++++");

        // CONSULTA DE DATOS DEL SQLITE PARA ENVIO AL SERVIDOR
        try {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbA = dataBaseHelper.getReadableDatabase();
            String selectQueryA = "SELECT AlertId FROM Alert";
            Cursor cA = dbA.rawQuery(selectQueryA, new String[]{});

            if (cA.moveToLast()) {
                _AlertUpdate_Id = cA.getInt(cA.getColumnIndex("AlertId"));
            }
            cA.close();
            dbA.close();

        }catch (Exception e){
            Log.e("--- Exception ", "+++ _AlertUpdate_Id +++");
        }

        String horaActual = null;

        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
            horaActual = sdf.format(new Date());

        }catch (Exception e){}

        try {

            AlertCrud alertCrud = new AlertCrud(mContext);

            Alert alert = new Alert();
            alert.FechaMarcacion = horaActual;
            alert.FlagTiempo = FlagTiempo;
            alert.MargenAceptado = MargenAceptado;
            alert.EstadoBoton = "true";
            alert.AlertId = _AlertUpdate_Id;
            alertCrud.update(alert);

            Log.e("--- TRUE ", " Alert update");

        } catch (Exception e){
            Log.e("--- BOTON Exception ", "update");
        }

        // CONSULTA DE DATOS DEL SQLITE PARA ENVIO AL SERVIDOR
        try {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbA = dataBaseHelper.getReadableDatabase();
            //String selectQueryA = "SELECT AlertId, Numero, FechaMarcacion, FechaEsperada, FechaProxima, FlagTiempo, MargenAceptado, Latitud, Longitud, DispositivoId, CodigoEmpleado  FROM Alert WHERE AlertId ='"+_AlertUpdate_Id+"'";
            String selectQueryA = "SELECT NumeroCel, FechaMarcacion, FechaEsperada, FechaProxima, FlagTiempo, MargenAceptado, DispositivoId, CodigoEmpleado  FROM Alert WHERE AlertId ='"+_AlertUpdate_Id+"'";
            Cursor cA = dbA.rawQuery(selectQueryA, new String[]{});

            if (cA.moveToLast()) {

                NumeroE = cA.getString(cA.getColumnIndex("NumeroCel"));
                FechaMarcacionE = cA.getString(cA.getColumnIndex("FechaMarcacion"));
                FechaEsperadaE = cA.getString(cA.getColumnIndex("FechaEsperada"));
                FechaProximaE = cA.getString(cA.getColumnIndex("FechaProxima"));
                FlagTiempoE = "1"; //cA.getString(cA.getColumnIndex("FlagTiempo"));
                MargenAceptadoE = "1"; //cA.getString(cA.getColumnIndex("MargenAceptado"));
                DispositivoIdE = cA.getString(cA.getColumnIndex("DispositivoId"));
                CodigoEmpleadoE = cA.getString(cA.getColumnIndex("CodigoEmpleado"));

            }
            Log.e("--- TRUE ", " Alert Select");
            cA.close();
            dbA.close();

        }catch (Exception e){
            Log.e("--- Alert Select", e.getMessage());
        }

        // CONSULTA TRACKING -----------------------------------------
        try {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbtracking = dataBaseHelper.getWritableDatabase();
            String selectQueryTracking = "SELECT Latitud, Longitud FROM Tracking";
            Cursor ctracking = dbtracking.rawQuery(selectQueryTracking, new String[]{});

            if (ctracking.moveToLast()) {
                LatitudG = ctracking.getString(ctracking.getColumnIndex("Latitud"));
                LongitudG = ctracking.getString(ctracking.getColumnIndex("Longitud"));
            }

            ctracking.close();
            dbtracking.close();

        } catch (Exception e) {}


        LatitudE = "-7.13957357406617";
        LongitudE = "-7.13960790634156";


        Log.e("-----------SEND  ","ALERT-----------");
        Log.e("--- NumeroE ", NumeroE);
        Log.e("--- FechaMarcacionE ", FechaMarcacionE);
        Log.e("--- FechaEsperadaE ", FechaEsperadaE);
        Log.e("--- FechaProximaE ", FechaProximaE);
        Log.e("--- FlagTiempoE ", FlagTiempoE);
        Log.e("--- MargenAceptadoE ", MargenAceptadoE);
        Log.e("--- LatitudE ", LatitudE);
        Log.e("--- LongitudE ", LongitudE);
        Log.e("--- DispositivoIdE ", DispositivoIdE);
        Log.e("--- CodigoEmpleadoE ", CodigoEmpleadoE);

        new PostAsync().execute(NumeroE, FechaMarcacionE, FechaEsperadaE, FechaProximaE, FlagTiempoE, MargenAceptadoE, LatitudE, LongitudE, DispositivoIdE, CodigoEmpleadoE);

        return true;
    }

    class PostAsync extends AsyncTask<String, String, JSONObject> {
        JsonParser jsonParser = new JsonParser();

        private final String URL = URL_API.concat("api/alert"); //"http://solmar.azurewebsites.net/api/alert";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        @Override
        protected void onPreExecute() {}

        @Override
        protected JSONObject doInBackground(String... args) {

            try {

                HashMap<String, String> params = new HashMap<>();
                params.put("Numero", args[0]);
                params.put("FechaMarcacion", args[1]);
                params.put("FechaEsperada", args[2]);
                params.put("FechaProxima", args[3]);
                params.put("FlagTiempo", args[4]);
                params.put("MargenAceptado", args[5]);
                params.put("Latitud", args[6]);
                params.put("Longitud", args[7]);
                params.put("DispositivoId", args[8]);
                params.put("CodigoEmpleado", args[9]);

                JSONObject json = jsonParser.makeHttpRequest(
                        URL, "POST", params);

                Log.e(" -- | FIRST ALERT ", json.toString());

                if (json != null) {

                    Log.e(" -- | ALERT ", json.toString());

                    String Configuracion = json.getString("Configuracion");
                    String estadoEnvio = json.getString("Estado");

                    try {

                        AlertCrud alertCrud = new AlertCrud(mContext);

                        Alert alert = new Alert();
                        alert.EstadoA = estadoEnvio;
                        alert.AlertId = _AlertUpdate_Id;
                        alertCrud.updateEstado(alert);

                    } catch (Exception e){}


                    JSONArray jsonA = new JSONArray(Configuracion);

                    int a=0, b=0;
                    int []valores = new int[2];

                    for(int i=0;i<jsonA.length();i++){

                        JSONObject c = jsonA.getJSONObject(i);
                        valores[i] = c.getInt("Valor");
                        if(c.getInt("ConfiguracionId")==3){
//                            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
//                            db.execSQL("UPDATE Configuration SET IntervaloTracking = '" + c.getInt("Valor") + "'");
//                            db.close();

                            a = c.getInt("Valor");
//                            Log.e("-- M[" + i + "]= ", String.valueOf(c.getInt("Valor")));
                        }

                        if(c.getInt("ConfiguracionId")==4){
//                            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
//                            db.execSQL("UPDATE Configuration SET IntervaloTrackingSinConex = '" + c.getInt("Valor") + "'");
//                            db.close();

                            b = c.getInt("Valor");
//                            Log.e("-- M[" + i + "]= ", String.valueOf(c.getInt("Valor")));
                        }
                    }

                    int te = a;
                    int tes = b;

                    Log.e("AlerF Interv/ Toleranc ", String.valueOf(te)+"| "+String.valueOf(tes));


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

    public int cantidadRegistros(){

        int contaRegistro = 0;

        try {

            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase existeDatos = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT AlertId FROM Alert";
            //String selectQueryconfiguration = "SELECT AlertId FROM Alert  WHERE FinTurno = 'false' AND EstadoBoton = 'false'";
            Cursor cta = existeDatos.rawQuery(selectQueryconfiguration, new String[]{});
            contaRegistro = cta.getCount();
            cta.close();
            existeDatos.close();

        } catch (Exception e) {}

        return contaRegistro;

    }

    public Boolean crearRegistro(){

        Log.e("ºººººººººººººººººººººº", "ººººººººººººººººººººººººº");
        Log.e("--- CREATE ---", " CANT. DE REGISTROS = "+String.valueOf(cantidadRegistros()));

        Calendar calendarCurrentG = null;

        if(cantidadRegistros()>0){

            Log.e("--- Ingreso Comparar ", " compararExisteReg");
            //**************************************************************************************************

            String fchExiste = null;
            try {

                DBHelper dataBaseHelper = new DBHelper(mContext);
                SQLiteDatabase dbA = dataBaseHelper.getReadableDatabase();
                // si aun no termina sesion y presionó boton
                String selectQueryA = "SELECT FechaEsperada FROM Alert WHERE FinTurno = 'false' AND EstadoBoton = 'true'";
                Cursor cA = dbA.rawQuery(selectQueryA, new String[]{});

                if (cA.moveToLast()) {

                    fchExiste = cA.getString(cA.getColumnIndex("FechaEsperada"));
                }

                cA.close();
                dbA.close();

            } catch (Exception e) {
                Log.e("--- Error Consulta ", e.getMessage());
            }
            Calendar cExiste = Calendar.getInstance();
            Calendar horaCur = Calendar.getInstance();
            DateFormat df = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
            try {
                cExiste.setTime(df.parse(fchExiste));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            long fE = cExiste.getTimeInMillis();
            long fC = horaCur.getTimeInMillis();

            if (fC <= fE) {

                calendarCurrentG = cExiste;
                Log.e("****++++++ CONSULTA ", "fC <= fE +++*** EXISTE");

            }

        } else {
            calendarCurrentG = Calendar.getInstance();
        }

        int aux = 0;

        //Calendar calendarCurrentG = Calendar.getInstance();

        int minuto = calendarCurrentG.get(Calendar.MINUTE);
        int resto;

        if (minuto > tiempoGuardado) { // SI minuto es mayor que tiempoGuardado
            resto = minuto%tiempoGuardado;
            if(resto==0){
                aux = 0;
            } else {
                aux = tiempoGuardado - resto;
            }

        } else if(minuto == tiempoGuardado){

            aux = tiempoGuardado;

        } else { // SI minuto es menor que tiempoGuardado

            aux = tiempoGuardado - minuto;
        }

        int minutoT = aux + minuto;

        Log.e("-----------------", "-----------------");
        Log.e("--- AUX ", String.valueOf(aux));
        Log.e("--- MINUTO T ", String.valueOf(minutoT));


        Calendar choraEsperadaGL = Calendar.getInstance();
        Calendar choraEsperadaIsoGL = Calendar.getInstance();

        choraEsperadaGL.set(Calendar.MINUTE, minutoT);
        choraEsperadaGL.set(Calendar.SECOND, 00);

        choraEsperadaIsoGL.set(Calendar.MINUTE, minutoT);

        horaEsperadaG = formatoGuardar.format(choraEsperadaGL.getTime());

        choraProximaG = choraEsperadaGL;
        choraProximaG.add(Calendar.MINUTE, tiempoGuardado);
        choraProximaG.set(Calendar.SECOND, 00);
        horaProximaG = formatoGuardar.format(choraProximaG.getTime());

        choraIso = choraEsperadaIsoGL;
        choraIso.set(Calendar.MINUTE, minutoT);
        choraIso.set(Calendar.SECOND, 00);
        choraIso.add(Calendar.MINUTE, -tiempoIntervalo);

        horaEsperadaIsoG = formatoIso.format(choraEsperadaIsoGL.getTime());

        // --- Hora IsoFin
        choraIsoFin = choraEsperadaIsoGL; //Calendar.getInstance();
        choraIsoFin.set(Calendar.MINUTE, minutoT);
        choraIsoFin.add(Calendar.MINUTE, tiempoIntervalo);

        horaEsperadaIsoFinG = formatoIso.format(choraEsperadaIsoGL.getTime());

        Log.e("--- H. ESP G ", String.valueOf(horaEsperadaG));
        Log.e("--- H. ESP ISO ", String.valueOf(horaEsperadaIsoG));
        Log.e("--- H. FIN ", String.valueOf(horaEsperadaIsoFinG));

        Log.e("--- tiempoGuardado ", String.valueOf(tiempoGuardado));
        Log.e("--- CodigoEmpleado IF ", String.valueOf(CodigoEmpleado));


        try {

            AlertCrud alertCrud = new AlertCrud(mContext);

            Alert alert = new Alert();
            alert.NumeroA = NumeroG;//Done
            alert.FechaMarcacion = "1900,01,01,00,00,00";
            alert.FechaEsperada = horaEsperadaG;//Done
            alert.FechaProxima = horaProximaG;//Done
            alert.FlagTiempo = "0";
            alert.MargenAceptado = "0";
            alert.EstadoA = "false";
            alert.EstadoBoton = "false";
            alert.FechaEsperadaIso = horaEsperadaIsoG;//Done
            alert.FechaEsperadaIsoFin = horaEsperadaIsoFinG;//Done
            alert.DispositivoId = DispositivoId;//Done
            alert.CodigoEmpleado = CodigoEmpleado;//Done
            alert.FinTurno = "false";

            _Alert_Id = alertCrud.insert(alert);

        } catch (Exception e) {
            Log.e("--- Exception Alert ", " GUARDAR ");
        }

        return true;
    }

    public void updateCountDown(){


        Log.e("--- Ingresó ", "updateCountDown");

        String Fecha = null;

        // CONSULTA DE DATOS DEL SQLITE PARA ENVIO AL SERVIDOR
        try {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbA = dataBaseHelper.getReadableDatabase();
            String selectQueryA = "SELECT FechaEsperadaIso FROM Alert";
            Cursor cA = dbA.rawQuery(selectQueryA, new String[]{});

            if (cA.moveToLast()) {
                Fecha = cA.getString(cA.getColumnIndex("FechaEsperadaIso"));
            }
            cA.close();
            dbA.close();

        }catch (Exception e){
            Log.e("--- Exception ", "Fecha");
        }

        Calendar horaAux = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            horaAux.setTime(sdf.parse(Fecha));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar c = Calendar.getInstance();

        long startTime = horaAux.getTimeInMillis() - c.getTimeInMillis();

        if (startTime<0){

            btnMarcacion.setEnabled(true);
            btnMarcacion.setText("Marcaci\u00F3n");
            btnMarcacion.setBackgroundColor(getResources().getColor(R.color.verde));
            btnMarcacion.setTextColor(Color.WHITE);

            return;

        }
            Log.e(" ---- startTime ----- ",String.valueOf(startTime));
            Log.e(" -- fechaEsperada -- ",formatoIso.format(horaAux.getTime()));


            try {

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        cdt5  = new CountDownTimer(startTime,1000) {

                            @Override
                            public void onTick(long startTime) {
                                // TODO Auto-generated method stub

                                int seconds = (int) (startTime / 1000) % 60 ;
                                int minutes = (int) ((startTime / (1000*60)) % 60);
                                int hours   = (int) ((startTime / (1000*60*60)) % 24);
                                btnMarcacion.setText(minutes+":"+seconds);

                            }

                            @Override
                            public void onFinish() {

                                btnMarcacion.setEnabled(true);
                                btnMarcacion.setText("Marcaci\u00F3n");
                                btnMarcacion.setBackgroundColor(getResources().getColor(R.color.verde));
                                btnMarcacion.setTextColor(Color.WHITE);
                                //btnMarcacion.setText("Time's Up!");

                            }
                        }.start();
                    }
                });


            }catch (Exception e){
                Log.e("EXCEPTION "," count "+ e.getMessage());
            }

    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public void compararProximaAlarma(){

        Log.e("--- Ingreso Comparar ", " Proxima Alarma");
        //**************************************************************************************************

        String fchaProx = null;
        try {

            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbA = dataBaseHelper.getReadableDatabase();
            String selectQueryA = "SELECT FechaProxima FROM Alert";
            Cursor cA = dbA.rawQuery(selectQueryA, new String[]{});

            if (cA.moveToLast()) {

                fchaProx = cA.getString(cA.getColumnIndex("FechaProxima"));
            }

            cA.close();
            dbA.close();

        } catch (Exception e) {
            Log.e("--- Error Consulta ", e.getMessage());
        }
        Calendar cal = Calendar.getInstance();
        Calendar horaAct = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
        try {
            cal.setTime(df.parse(fchaProx));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (horaAct.after(cal)) {
            dialogoNoMarco();
            //createNewAlert();
        } else {
            //createNewAlert();
        }
        //**************************************************************************************************

    }

    public void mostrarHora() {

        // CONSULTA ALERT ---------------------------------------------
        try {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbalert = dataBaseHelper.getWritableDatabase();
            String selectQueryAlert = "SELECT FechaEsperada, FechaProxima, EstadoBoton FROM Alert";

            Cursor calert = dbalert.rawQuery(selectQueryAlert, new String[]{});

            if (calert.moveToLast()) {

                HoraMarcacionP = calert.getString(calert.getColumnIndex("FechaEsperada"));
                FechaEsperada = calert.getString(calert.getColumnIndex("FechaEsperada"));
                FechaProxima = calert.getString(calert.getColumnIndex("FechaProxima"));
                EstadoBoton = calert.getString(calert.getColumnIndex("EstadoBoton"));
            }
            calert.close();
            dbalert.close();


            if (EstadoBoton.equals("false")){

                String proximaFecha;

                DateFormat df = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
                Calendar cal  = Calendar.getInstance();
                cal.setTime(df.parse(FechaEsperada));
                Calendar calf = cal;
                SimpleDateFormat formatProx = new SimpleDateFormat("HH:mm");
                SimpleDateFormat formatProxFecha = new SimpleDateFormat("dd-MM-yyyy");

                Proxima = formatProx.format(calf.getTime());
                proximaFecha = formatProxFecha.format(calf.getTime());

                textHora.setText(Proxima);
                if(flagMostrarFecha==true){
                    textHoraFecha.setText(proximaFecha);
                }

            } else {

                String proximaFecha;

                DateFormat df = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
                Calendar cal  = Calendar.getInstance(); //--
                cal.setTime(df.parse(FechaProxima));
                Calendar calf = cal;
                SimpleDateFormat formatProx = new SimpleDateFormat("HH:mm");
                SimpleDateFormat formatProxFecha = new SimpleDateFormat("dd-MM-yyyy");

                Proxima = formatProx.format(calf.getTime());
                proximaFecha = formatProxFecha.format(calf.getTime());

                textHora.setText(Proxima);

                if(flagMostrarFecha==true){
                    textHoraFecha.setText(proximaFecha);
                }
            }

        }catch (Exception e){}
    }

    public void habilitarBoton(){

        try {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbA = dataBaseHelper.getReadableDatabase();
            String selectQueryA = "SELECT EstadoBoton, FechaEsperadaIso, FechaEsperadaIsoFin, Estado FROM Alert";
            Cursor cA = dbA.rawQuery(selectQueryA, new String[]{});

            if (cA.moveToLast()) {
                EstadoBoton = cA.getString(cA.getColumnIndex("EstadoBoton"));
                EstadoE  = cA.getString(cA.getColumnIndex("Estado"));
                FechaEsperadaIso = cA.getString(cA.getColumnIndex("FechaEsperadaIso"));
                FechaEsperadaIsoFin = cA.getString(cA.getColumnIndex("FechaEsperadaIsoFin"));
            }
            cA.close();
            dbA.close();

        }catch (Exception e){}

        /*Log.e("-- --- --- --- --- : ", "----------------");
        Log.e("-- FechaEsperadaIso ", FechaEsperadaIso);
        Log.e("-- FechaEsperadaIsoFin ", FechaEsperadaIsoFin);
        Log.e("-- --- --- --- --- : ", "----------------");*/
        // Convertimos la fecha EsperadaIso a Calendar para poder comparar
        horaIso = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            horaIso.setTime(sdf.parse(FechaEsperadaIso));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // OBTENGO LA HORA ACTUAL
        Calendar horaActual = Calendar.getInstance();

        // Agrago el intervalo de tiempo de 3 minutos a la horaIso extraido de la BD
        horaIni = horaIso;


        // ------------------------------------------------------------------------------------
        // Convertimos la fecha EsperadaIso a Calendar para poder comparar
        Calendar horaFinPre = Calendar.getInstance();
        SimpleDateFormat sdfpre = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            horaFinPre.setTime(sdfpre.parse(FechaEsperadaIsoFin));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        horaFin = horaFinPre;

        // COMPARO LA FECHA DEL SISTEMA CON LOS INTERVALOS

        int dia = horaActual.get(Calendar.DAY_OF_MONTH);
        int diaFin = horaFinPre.get(Calendar.DAY_OF_MONTH);

        int minHoraActual = horaActual.get(Calendar.MINUTE);
        int minBotonActivo = horaIni.get(Calendar.MINUTE);

        int segHoraActual = horaActual.get(Calendar.SECOND);
        int segBotonActivo = horaIni.get(Calendar.SECOND);

        int difBoton = Math.abs(minBotonActivo - minHoraActual);

        int difseg = segHoraActual - segBotonActivo;

        int difsegBoton = 59 - difseg;

        if(dia!=diaFin){
            flagMostrarFecha = true;
        }

        if(horaActual.after(horaIni) && horaActual.before(horaFin) && EstadoBoton.equals("false")){
            //Log.e("-- ESTADO : ", EstadoBoton);
            //playBeepSound();

            try {

                btnMarcacion.setEnabled(true);
                btnMarcacion.setText("Marcaci\u00F3n");
                btnMarcacion.setBackgroundColor(getResources().getColor(R.color.verde));
                btnMarcacion.setTextColor(Color.WHITE);
                FlagTiempo="1";
                MargenAceptado="1";

            }catch (Exception e){}

        } else if (EstadoBoton.equals("false") && horaActual.after(horaFin)) {

            try {

                btnMarcacion.setEnabled(true);
                btnMarcacion.setText("Marcaci\u00F3n");
                btnMarcacion.setBackgroundColor(getResources().getColor(R.color.red));
                btnMarcacion.setTextColor(Color.WHITE);
                FlagTiempo = "0";
                MargenAceptado = "1";

            }catch (Exception e){}

        } else {
            btnMarcacion.setEnabled(true);
            btnMarcacion.setBackgroundColor(getResources().getColor(R.color.boton_deshabilitado));
            if(difsegBoton>9){
                btnMarcacion.setText(difBoton+ ":"+difsegBoton);
            } else {
                btnMarcacion.setText(difBoton+ ":0"+difsegBoton);
            }

            btnMarcacion.setEnabled(false);
            btnMarcacion.setTextColor(getResources().getColor(R.color.black_overlay));
        }
    }

    public void dialogoNoMarco(){

        try {

            String hora, fecha, fechaEsp = null;

            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbalert = dataBaseHelper.getWritableDatabase();
            String selectQueryAlert = "SELECT FechaEsperada, FechaProxima, EstadoBoton FROM Alert";

            Cursor calert = dbalert.rawQuery(selectQueryAlert, new String[]{});

            if (calert.moveToLast()) {

                fechaEsp = calert.getString(calert.getColumnIndex("FechaEsperada"));
            }
            calert.close();
            dbalert.close();


            DateFormat df = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
            Calendar cal  = Calendar.getInstance(); //--
            try {
                cal.setTime(df.parse(fechaEsp));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar calf = cal;
            SimpleDateFormat formatProx = new SimpleDateFormat("HH:mm");
            SimpleDateFormat formatProxFecha = new SimpleDateFormat("dd-MM-yyyy");

            hora = formatProx.format(calf.getTime());
            fecha = formatProxFecha.format(calf.getTime());


            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setCancelable(false);
            builder.setTitle("Marcaci\u00F3n a destiempo");
            builder.setMessage("\u00C9sta marcaci\u00F3n corresponde a las "+hora+" del d\u00EDa "+fecha+"");
            builder.setPositiveButton("Aceptar", null);
            builder.show();
        } catch (Exception e){}
    }

    @Override
    public void onResume() {
        super.onResume();
        mostrarHora();
        updateCountDown();
    }
}