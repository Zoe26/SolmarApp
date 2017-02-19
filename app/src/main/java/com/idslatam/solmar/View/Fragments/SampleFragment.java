package com.idslatam.solmar.View.Fragments;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
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

import com.google.gson.JsonObject;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Api.Parser.JsonParser;
import com.idslatam.solmar.Models.Crud.AlertCrud;
import com.idslatam.solmar.Models.Crud.ConfigurationCrud;
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
import com.idslatam.solmar.Models.Entities.Configuration;
import com.idslatam.solmar.R;
import com.idslatam.solmar.View.Bienvenido;
import com.idslatam.solmar.View.Login;
import com.idslatam.solmar.View.RegisterNumber;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SampleFragment extends Fragment implements  View.OnClickListener {


    Context mContext;

    Button btnMarcacion;
    TextView textHora, textUltimaMarcacion, textUltimaMarcacionFecha, textHoraFecha, texttolerancia, textflag_tiempo;

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

    CountDownTimer cdt5, cdtBtn;


    //+++++++++++++++++++

    Calendar calendarCurrentG = null;

    boolean flagCancel = false;
    boolean flagCancelBtn = false;


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

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        myView = inflater.inflate(R.layout.fragment_alert, container, false);
        textHora = (TextView) myView.findViewById(R.id.hora);
        textHoraFecha = (TextView) myView.findViewById(R.id.hora_fecha);
        texttolerancia = (TextView) myView.findViewById(R.id.tolerancia);
        textflag_tiempo = (TextView) myView.findViewById(R.id.flag_tiempo);
        caritaEstado = (ImageView) myView.findViewById(R.id.carita);

        textUltimaMarcacion  = (TextView) myView.findViewById(R.id.ultima_marcacion);
        textUltimaMarcacionFecha = (TextView) myView.findViewById(R.id.ultima_marcacion_fecha);

        btnMarcacion = (Button) myView.findViewById(R.id.btn_marcacion);
        btnMarcacion.setOnClickListener(this);
        btnMarcacion.setEnabled(false);
        btnMarcacion.setBackgroundColor(getResources().getColor(R.color.boton_deshabilitado));
        btnMarcacion.setTextColor(Color.WHITE);

        Log.e("onCreate Alert", "Ingresó");

        load();

        return myView;
    }


    public void load(){

        Alert alertload = ultimoRegistro();

        if(obtenerDatos() == true){

            if(!alertload.FechaMarcacion.isEmpty()){

                ultimaMarcacion();
                Log.e("Alert Data", alertload.FechaEsperada);
                Log.e("Alert Data ISO", alertload.FechaEsperadaIso);

            } else {
                calendarCurrentG = Calendar.getInstance();
                sendAsistencia();
                crearRegistro();
                Log.e("Alert Vacio", "Sin datos en Alert");
            }

            mostrarHora();

            if (!flagCancel){
                updateCountDown();
            }

        } else {

            Toast.makeText(mContext, "Error al obtener datos de configuración", Toast.LENGTH_LONG).show();
        }

    }

    public Boolean obtenerDatos(){

        //Log.e("--- Ingresó ", "obtenerDatos");

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

            //tiempoGuardado=30;
            Log.e("--- tiempoGuardado ", String.valueOf(tiempoGuardado));

            return  false;
        }

        if(tiempoIntervalo == 0){

            //tiempoIntervalo=1;
            Log.e("--- tiempoGuardado ", String.valueOf(tiempoIntervalo));

            return  false;
        }


        if(CodigoEmpleado==null){
            Log.e("--- CodigoEmpleado IF ", String.valueOf(CodigoEmpleado));
            return  false;
        }

        if(DispositivoId==null){
            Log.e("--- DispositivoId IF ", String.valueOf(DispositivoId));
            return  false;
        }

        //Log.e("--- tiempoGuardado ", String.valueOf(tiempoGuardado));
        //Log.e("--- tiempoGuardado ", String.valueOf(tiempoIntervalo));
        //Log.e("--- CodigoEmpleado IF ", String.valueOf(CodigoEmpleado));
        //Log.e("--- DispositivoId IF ", String.valueOf(DispositivoId));

        //Log.e("--- obtenerDatos ", "Fin");

        return true;
    }

    public Boolean crearRegistro(){

        Log.e("ºººººººººººººººººººººº", "ººººººººººººººººººººººººº");
        Log.e("--- CREATE REGISTRO ---", " INGRESÓ");

        int aux = 0;
        int minuto = calendarCurrentG.get(Calendar.MINUTE);
        int resto;

        if (minuto > tiempoGuardado) {
            resto = minuto%tiempoGuardado;
            if(resto==0){
                aux = 0;
            } else {
                aux = tiempoGuardado - resto;
            }

        } else { // SI minuto es menor que tiempoGuardado

            aux = tiempoGuardado - minuto;

        }

        int minutoT = aux + minuto;

        Log.e("-----------------", "-----------------");
        Log.e("--- AUX ", String.valueOf(aux));
        Log.e("--- MINUTO T ", String.valueOf(minutoT));

        Calendar choraEsperadaGL = Calendar.getInstance();
        Calendar choraEsperadaIsoGL = Calendar.getInstance();

        int hour = calendarCurrentG.get(Calendar.HOUR_OF_DAY);
        int hourAct = choraEsperadaGL.get(Calendar.HOUR_OF_DAY);

        if (hour>hourAct){
            choraEsperadaGL.set(Calendar.HOUR_OF_DAY, hour);
            choraEsperadaIsoGL.set(Calendar.HOUR_OF_DAY, hour);
        }

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

        Log.e("------------- REGISTRO ", " CREADO -------------");

        Log.e("--- H. ESP G ", String.valueOf(horaEsperadaG));
        Log.e("--- H. ESP ISO ", String.valueOf(horaEsperadaIsoG));
        Log.e("--- H. FIN ", String.valueOf(horaEsperadaIsoFinG));

        Log.e("--------- FIN REGISTRO ", " CREADO -------------");


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

        Log.e("--- FIN ", " CREATE Registro ");

        return true;
    }

    public void mostrarHora() {

        Log.e("--- MostrarHora ", " INGRESO ");

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

            String tol = Integer.toString(tiempoIntervalo);

            texttolerancia.setText(tol);

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

        }catch (Exception e){
            Log.e("--- MostrarHora Except ", e.getMessage());
        }


        Log.e("--- MostrarHora ", " FIN ");
    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_marcacion:

                btnMarcacion.setEnabled(false);
                btnMarcacion.setBackgroundColor(getResources().getColor(R.color.boton_deshabilitado));
                btnMarcacion.setTextColor(Color.WHITE);

                try {

                    enviarMarcacion();

                }catch (Exception e){
                    Log.e("--- ++EXCEPTION++ ", " +++ enviarMarcacion+++");
                }

                Alert alertload = ultimoRegistro();

                if(obtenerDatos() == true){

                    if(!alertload.FechaMarcacion.isEmpty()){
                        //calendarCurrentG = fechaEsperada();
                        //Log.e("Alert Data", alertload.FechaEsperada);
                        //Log.e("Alert Data ISO", alertload.FechaEsperadaIso);

                        Calendar cExiste = Calendar.getInstance();
                        Calendar horaCur = Calendar.getInstance();

                        try {

                            cExiste.setTime(formatoIso.parse(alertload.FechaEsperadaIso));
                            cExiste.add(Calendar.MINUTE, tiempoIntervalo);

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        long fE = cExiste.getTimeInMillis();
                        long fC = horaCur.getTimeInMillis();

                        Calendar e = Calendar.getInstance();
                        Calendar h = Calendar.getInstance();

                        e.setTimeInMillis(fE);
                        h.setTimeInMillis(fC);

                        Log.e("**** f Current ", formatoIso.format(horaCur.getTime()));
                        Log.e("**** f Esperad ", formatoIso.format(cExiste.getTime()));


                        if (horaCur.before(cExiste)) {

                            try {

                                SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
                                String dateInString = formatoIso.format(cExiste.getTime());

                                Log.e("+++++ dateInString ", dateInString);

                                Date date = sdf.parse(dateInString);

                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(date);

                                calendarCurrentG.setTime(date);

                            } catch (Exception e6){
                                Log.e("+++++ Exception ", "a6");
                            }

                            Log.e("+++++ CONSULTA ", "fC <= fE + EXISTE");

                        } else {

                            calendarCurrentG =Calendar.getInstance();
                        }

                        crearRegistro();

                        mostrarHora();

                        if (flagCancelBtn){
                            cdtBtn.onFinish();
                        }

                        if (flagCancel){
                            cdt5.cancel();
                        }

                        compararProximaAlarma();
                        updateCountDown();
                        ultimaMarcacion();

                    }

                } else {

                    Toast.makeText(mContext, "Error al obtener datos de configuración", Toast.LENGTH_LONG).show();
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

            //Log.e("--- TRUE ", " Alert update");

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
                FlagTiempoE =  cA.getString(cA.getColumnIndex("FlagTiempo"));
                MargenAceptadoE = cA.getString(cA.getColumnIndex("MargenAceptado"));
                DispositivoIdE = cA.getString(cA.getColumnIndex("DispositivoId"));
                CodigoEmpleadoE = cA.getString(cA.getColumnIndex("CodigoEmpleado"));

            }
            //Log.e("--- TRUE ", " Alert Select");
            cA.close();
            dbA.close();

        }catch (Exception e){
            Log.e("--- Alert Select", e.getMessage());
        }

        // CONSULTA TRACKING -----------------------------------------
        try {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbtracking = dataBaseHelper.getWritableDatabase();
            String selectQueryTracking = "SELECT Latitud, Longitud FROM Configuration";
            Cursor ctracking = dbtracking.rawQuery(selectQueryTracking, new String[]{});

            if (ctracking.moveToLast()) {
                LatitudE = ctracking.getString(ctracking.getColumnIndex("Latitud"));
                LongitudE = ctracking.getString(ctracking.getColumnIndex("Longitud"));
            }

            ctracking.close();
            dbtracking.close();

        } catch (Exception e) {}


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
        Log.e("--------- FIN SEND  ","ALERT---------");


        String URL = URL_API.concat("api/alert");

        JsonObject json = new JsonObject();
        json.addProperty("Numero", NumeroE);
        json.addProperty("FechaMarcacion", FechaMarcacionE);
        json.addProperty("FechaEsperada", FechaEsperadaE);
        json.addProperty("FechaProxima", FechaProximaE);
        json.addProperty("FlagTiempo", FlagTiempoE);
        json.addProperty("MargenAceptado", MargenAceptadoE);
        json.addProperty("Latitud", LatitudE);
        json.addProperty("Longitud", LongitudE);
        json.addProperty("DispositivoId", DispositivoIdE);
        json.addProperty("CodigoEmpleado", CodigoEmpleadoE);

        Ion.with(this)
                .load("POST", URL)
                .setJsonObjectBody(json)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> response) {

                        if (response.getHeaders().code() == 200) {

                            Log.e("JsonObject ", response.getResult().toString());

                            JSONObject j = null;
                            try {
                                j = new JSONObject(response.getResult().toString());
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }

                            String Configuracion = null;
                            try {
                                Configuracion = j.getString("Configuracion");
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }

                            JSONArray jsonA = null;

                            try {
                                jsonA = new JSONArray(Configuracion);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }

                            int a=0, b=0;
                            int []valores = new int[2];

                            for(int i=0;i<jsonA.length();i++){

                                JSONObject c = null;

                                try {
                                    c = jsonA.getJSONObject(i);

                                    valores[i] = c.getInt("Valor");
                                    if(c.getInt("ConfiguracionId")==3){
                                        DBHelper dataBaseHelper = new DBHelper(mContext);
                                        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                                        db.execSQL("UPDATE Configuration SET IntervaloMarcacion = '" + c.getInt("Valor") + "'");
                                        db.close();

                                        a = c.getInt("Valor");
//                            Log.e("-- M[" + i + "]= ", String.valueOf(c.getInt("Valor")));
                                    }

                                    if(c.getInt("ConfiguracionId")==4){
                                        DBHelper dataBaseHelper = new DBHelper(mContext);
                                        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                                        db.execSQL("UPDATE Configuration SET IntervaloMarcacionTolerancia = '" + c.getInt("Valor") + "'");
                                        db.close();

                                        b = c.getInt("Valor");
//                            Log.e("-- M[" + i + "]= ", String.valueOf(c.getInt("Valor")));
                                    }
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }

                            int te = a;
                            int tes = b;

                            Log.e("AlerF Interv/ Toleranc ", String.valueOf(te)+"| "+String.valueOf(tes));

                        } else {

                            AlertCrud alertCrud = new AlertCrud(mContext);
                            Alert alert = new Alert();
                            alert.EstadoA = "false";
                            alert.AlertId = _AlertUpdate_Id;
                            alertCrud.updateEstado(alert);

                        }
                    }
                });

        return true;
    }

    public Alert ultimoRegistro(){

        Alert alert = new Alert();

        alert.FechaMarcacion = "";

        if(alert.FechaMarcacion == ""){
            Log.e("Alert ", alert.FechaMarcacion);
        }


        try {

            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase existeDatos = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT NumeroCel, FechaMarcacion, FechaEsperada, " +
                    "FechaProxima, FlagTiempo, MargenAceptado, DispositivoId, CodigoEmpleado, " +
                    "FechaEsperadaIso FROM Alert WHERE  FinTurno = 'false' AND EstadoBoton ='true'";
            Cursor cA = existeDatos.rawQuery(selectQueryconfiguration, new String[]{});

            //Log.e("Consulta ", "If");

            if(alert.FechaMarcacion == "")
                Log.e("Alert ", alert.FechaMarcacion);

            if (cA.moveToLast()) {

                Log.e("FechaEsperada ", "");

                alert.NumeroA = cA.getString(cA.getColumnIndex("NumeroCel"));
                alert.FechaMarcacion = cA.getString(cA.getColumnIndex("FechaMarcacion"));
                alert.FechaEsperada = cA.getString(cA.getColumnIndex("FechaEsperada"));
                alert.FechaProxima = cA.getString(cA.getColumnIndex("FechaProxima"));
                alert.FlagTiempo = cA.getString(cA.getColumnIndex("FlagTiempo"));
                alert.MargenAceptado = cA.getString(cA.getColumnIndex("MargenAceptado"));
                alert.DispositivoId = cA.getString(cA.getColumnIndex("DispositivoId"));
                alert.CodigoEmpleado = cA.getString(cA.getColumnIndex("CodigoEmpleado"));
                alert.FechaEsperadaIso = cA.getString(cA.getColumnIndex("FechaEsperadaIso"));

            }

            cA.close();
            existeDatos.close();

        } catch (Exception e) {}

        return alert;

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
            btnMarcacion.setTextColor(getResources().getColor(R.color.black_overlay));

            return ;

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

                                flagCancel = true;
                                int seconds = (int) (startTime / 1000) % 60 ;
                                int minutes = (int) ((startTime / (1000*60)) % 60);
                                int hours   = (int) ((startTime / (1000*60*60)) % 24);

                                btnMarcacion.setTextColor(Color.BLACK);

                                btnMarcacion.setText(minutes+":"+seconds);

                            }

                            @Override
                            public void onFinish() {
                                flagCancel = false;
                                btnMarcacion.setEnabled(true);
                                btnMarcacion.setText("Marcaci\u00F3n");
                                btnMarcacion.setBackgroundColor(getResources().getColor(R.color.verde));
                                btnMarcacion.setTextColor(Color.WHITE);
                                //btnMarcacion.setText("Time's Up!");

                                if (flagCancelBtn){
                                    cdtBtn.cancel();
                                }
                                botomCountDown();

                            }
                        }.start();
                    }
                });


            }catch (Exception e){
                Log.e("EXCEPTION "," count "+ e.getMessage());
            }

    }

    public void botomCountDown(){

        Log.e("--- Ingresó ", "botomCountDown");

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

        int minBtn = tiempoEnvio+2;

        try {
            horaAux.setTime(sdf.parse(Fecha));
            horaAux.add(Calendar.MINUTE, minBtn);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar c = Calendar.getInstance();

        long startTime = horaAux.getTimeInMillis() - c.getTimeInMillis();

        Log.e(" --- startTime Lim --- ",String.valueOf(startTime));
        Log.e(" -- fechaLimite -- ",formatoIso.format(horaAux.getTime()));


        try {

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    cdtBtn  = new CountDownTimer(startTime,1000) {

                        @Override
                        public void onTick(long startTime) {
                            // TODO Auto-generated method stub

                            FlagTiempo="1";
                            MargenAceptado="1";

                        }

                        @Override
                        public void onFinish() {

                            FlagTiempo = "0";
                            MargenAceptado = "1";

                            btnMarcacion.setEnabled(true);
                            btnMarcacion.setText("Marcaci\u00F3n");
                            btnMarcacion.setBackgroundColor(getResources().getColor(R.color.red));
                            btnMarcacion.setTextColor(Color.WHITE);

                            Log.e(" ---- cdtBtn ----- "," FIN");

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

    public void ultimaMarcacion(){

        String horaUltimaMarcacion = null;
        String estadoMarcacion = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbalert = dataBaseHelper.getWritableDatabase();
            String selectQueryAlert = "SELECT FechaEsperada, FlagTiempo FROM Alert WHERE  FinTurno = 'false' AND EstadoBoton ='true'";

            Cursor calert = dbalert.rawQuery(selectQueryAlert, new String[]{});

            if (calert.moveToLast()) {

                horaUltimaMarcacion = calert.getString(calert.getColumnIndex("FechaEsperada"));
                estadoMarcacion = calert.getString(calert.getColumnIndex("FlagTiempo"));
            }
            calert.close();
            dbalert.close();

        } catch (Exception e){}

        String ultiHora, ultiHoraFecha;

        DateFormat df = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
        Calendar cal  = Calendar.getInstance(); //--
        try {
            cal.setTime(df.parse(horaUltimaMarcacion));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calf = cal;
        SimpleDateFormat formatProx = new SimpleDateFormat("HH:mm");
        SimpleDateFormat formatProxFecha = new SimpleDateFormat("dd-MM-yyyy");

        ultiHora = formatProx.format(calf.getTime());
        ultiHoraFecha = formatProxFecha.format(calf.getTime());


        int estadoM = Integer.valueOf(estadoMarcacion);



        if(estadoM==1){
            textflag_tiempo.setTextColor(getResources().getColor(R.color.negro_general));
            textflag_tiempo.setText("A tiempo");

            textUltimaMarcacion.setTextColor(getResources().getColor(R.color.negro_general));

            if(flagMostrarFecha==true) {
                textUltimaMarcacionFecha.setTextColor(getResources().getColor(R.color.negro_general));
            }

            caritaEstado.setImageResource(R.drawable.ic_feliz);

        }else{
            textflag_tiempo.setText("A destiempo");
            textflag_tiempo.setTextColor(getResources().getColor(R.color.red));

            textUltimaMarcacion.setTextColor(getResources().getColor(R.color.red));

            if(flagMostrarFecha==true) {
                textUltimaMarcacionFecha.setTextColor(getResources().getColor(R.color.red));
            }

            caritaEstado.setImageResource(R.drawable.ic_triste);
        }


        textUltimaMarcacion.setText(ultiHora);
        if(flagMostrarFecha==true) {
            textUltimaMarcacionFecha.setText(ultiHoraFecha);
        }
    }

    public void sendAsistencia(){

        String Num = null, DisId = null;
        String FechaInicioCelular = formatoGuardar.format(new Date());

        try {

            FechaInicioCelular = formatoGuardar.format(new Date());
            DBHelper dbhGUID = new DBHelper(mContext);
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

        String URL = URL_API.concat("api/Attendance");

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

                            String AsistenciaId = null;
                            //Log.e("JsonObject ", response.toString());

                            AsistenciaId = response.get("AsistenciaId").getAsString();
                            //AsistenciaId
                            //String AsistenciaId = json.get("AsistenciaId").getAsString();

                            try {

                                ConfigurationCrud configurationCRUD = new ConfigurationCrud(mContext);

                                Configuration configuration = new Configuration();
                                configuration.AsistenciaId= AsistenciaId;
                                //configuration.CodigoEmpleado= pass;
                                configuration.ConfigurationId = 1;
                                configurationCRUD.updateAsistencia(configuration);

                            } catch (Exception e5) {}
                            Log.e("JsonObject ", response.toString());

                        } else  {
                            Log.e("Exception ", "Finaliza" );
                        }
                    }
                });

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

    public Calendar fechaEsperada() {

        Calendar fecha;
        Log.e("--- Ingreso Comparar ", " compararExisteReg");
        //**************************************************************************************************

        String fchExiste = null;
        try {

            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbA = dataBaseHelper.getReadableDatabase();
            // si aun no termina sesion y presionó boton
            String selectQueryA = "SELECT FechaEsperada FROM Alert WHERE FinTurno = 'false'"; // AND EstadoBoton = 'true
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

        //DateFormat df = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");

        Log.e("**** fchExiste ", fchExiste);

        try {
            cExiste.setTime(formatoGuardar.parse(fchExiste));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long fE = cExiste.getTimeInMillis();
        long fC = horaCur.getTimeInMillis();

        Calendar e = Calendar.getInstance();
        Calendar h = Calendar.getInstance();

        e.setTimeInMillis(fE);
        h.setTimeInMillis(fC);

        Log.e("**** fC ", formatoIso.format(h.getTime()));
        Log.e("**** fE ", formatoIso.format(e.getTime()));


        if (fC <= fE) {

            calendarCurrentG = cExiste;
            Log.e("+++++ CONSULTA ", "fC <= fE + EXISTE");

        }

        fecha = calendarCurrentG;

        return fecha;
    }

    @Override
    public void onResume() {

        if (flagCancelBtn){
            cdtBtn.cancel();
        }

        if (flagCancel){
            cdt5.cancel();
            updateCountDown();
        }

        mostrarHora();
        super.onResume();
    }

    @Override
    public void onPause() {

        if (flagCancelBtn){
            cdtBtn.cancel();
        }

        if (flagCancel){
            cdt5.cancel();
            //updateCountDown();
        }

        //mostrarHora();
        super.onPause();
    }

    @Override
    public void onStop() {

        if (flagCancelBtn){
            cdtBtn.cancel();
        }

        if (flagCancel){
            cdt5.cancel();
            //updateCountDown();
        }
        //mostrarHora();
        super.onStop();
    }

    @Override
    public void onDestroy() {

        if (flagCancelBtn){
            cdtBtn.cancel();
        }

        if (flagCancel){
            cdt5.cancel();
        }
        super.onDestroy();
    }
}