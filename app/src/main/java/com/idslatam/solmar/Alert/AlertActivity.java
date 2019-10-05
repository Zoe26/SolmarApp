package com.idslatam.solmar.Alert;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Models.Crud.AlertCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Alert;
import com.idslatam.solmar.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.body.StreamBody;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class AlertActivity extends AppCompatActivity {

    Context mContext;

    TextView txtMargen, txtUltimaMarcacion, txtEstadoUltimaMarcacion, txtProximaMarcacionHora,
            txtProximaMarcacionFecha, txtUlltimaMarcacionFecha;

    ImageView caritaEstado;

    Button btnMarcacion;

    int tiempoMargen, tiempoMarcacion, NroAlertas;

    int _Alert_Id, _Alert_id_Consultado, _Alert_id_Consultado_Aux, NroAlertasAux;

    String Numero, DispositivoId, CodigoEmpleado;

    String fechaEsperada, fechaProxima;
    String fechaUltimaX;

    String flagTiempo, margenAceptado, flagCarita;

    String NumeroE,FechaMarcacionE, FechaEsperadaE, Latitud, Longitud, DispositivoIdE, CodigoEmpleadoE;
    //**********************************************************************************************

    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss")
            , formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            , formatoHora = new SimpleDateFormat("HH:mm")
            , formatoFecha = new SimpleDateFormat("dd-MM-yyyy");

    protected String URL_API;

    CountDownTimer contadorEnMargen, contadorFueraMargen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        this.mContext = this;
        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

    }

    @Override
    public void onStart() {
        super.onStart();
        UI();
        consultasIniciales();

    }

    public void UI(){

        txtProximaMarcacionHora = (TextView) findViewById(R.id.txtProximaMarcacionHora);
        txtProximaMarcacionFecha = (TextView) findViewById(R.id.txtProximaMarcacionFecha);
        txtMargen = (TextView) findViewById(R.id.txtMargen);
        txtEstadoUltimaMarcacion = (TextView) findViewById(R.id.txtEstadoUltimaMarcacion);
        caritaEstado = (ImageView) findViewById(R.id.carita);

        txtUltimaMarcacion  = (TextView) findViewById(R.id.txtUltimaMarcacion);
        txtUlltimaMarcacionFecha = (TextView) findViewById(R.id.txtUlltimaMarcacionFecha);

        btnMarcacion = (Button) findViewById(R.id.btn_marcacion);

        btnMarcacion.setEnabled(false);
        btnMarcacion.setBackgroundColor(getResources().getColor(R.color.boton_deshabilitado));
        btnMarcacion.setTextColor(Color.WHITE);
        btnMarcacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btnMarcacion.setEnabled(false);

                Calendar currentMarcacion = Calendar.getInstance();

                try {
                    DBHelper dataBaseHelper = new DBHelper(mContext);
                    SQLiteDatabase existeDatos = dataBaseHelper.getReadableDatabase();
                    String selectQueryconfiguration = "SELECT AlertId FROM Alert";
                    Cursor cA = existeDatos.rawQuery(selectQueryconfiguration, new String[]{});
                    NroAlertas = cA.getCount();
                    if (cA.moveToLast()) {
                        _Alert_id_Consultado = cA.getInt(cA.getColumnIndex("AlertId"));
                    }
                    cA.close();
                    existeDatos.close();

                } catch (Exception e) {Log.e("Exception ", e.getMessage());}

                try {
                    DBHelper dataBaseHelper = new DBHelper(mContext);
                    SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                    db.execSQL("UPDATE Alert SET FlagTiempo = '"+flagTiempo+"' WHERE AlertId = "+_Alert_id_Consultado+"");
                    db.execSQL("UPDATE Alert SET EstadoBoton = 'true' WHERE AlertId = "+_Alert_id_Consultado+"");
                    db.execSQL("UPDATE Alert SET FechaMarcacion = '"+formatoIso.format(currentMarcacion.getTime())+"' WHERE AlertId = "+_Alert_id_Consultado+"");
                    db.close();
                } catch (Exception sdlb){}

                registrarAlertaApi(_Alert_id_Consultado);

                try {contadorFueraMargen.cancel();} catch (NullPointerException dsf){}
                consultaMarcacion(currentMarcacion);


            }
        });

    }

    public void consultasIniciales(){

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT IntervaloMarcacionTolerancia, IntervaloMarcacion FROM Configuration";
            Cursor c = db.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {
                tiempoMargen = c.getInt(c.getColumnIndex("IntervaloMarcacionTolerancia"));
                tiempoMarcacion = c.getInt(c.getColumnIndex("IntervaloMarcacion"));

            }
            c.close();
            db.close();

        } catch (Exception e) {}

        if(tiempoMargen == 0){Log.e("--- tiempoMargen ", String.valueOf(tiempoMargen));return;}

        txtMargen.setText(String.valueOf(tiempoMargen));

        Calendar currentMarcacion = Calendar.getInstance();

        //consultaMarcacion(currentMarcacion);


        //PRUEBA -----------------------------------------------------------------------------------
        //SI EXISTE UN REGISTRO SIN ENVIAR MOSTRAR DE CASO CONTRARIO CREAR

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase existeDatos = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT FechaMarcacion FROM Alert WHERE FinTurno = 'false' AND EstadoBoton ='false'";
            Cursor cA = existeDatos.rawQuery(selectQueryconfiguration, new String[]{});
            NroAlertasAux = cA.getCount();
            cA.close();
            dataBaseHelper.close();
            cA.close();
            existeDatos.close();

        } catch (Exception e) {}

        if (NroAlertasAux>0){
            mostrarDatos();
        } else {
            consultaMarcacion(currentMarcacion);
        }



    }

    public void consultaMarcacion(Calendar currentMarcacion){

        UI();

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase existeDatos = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT FechaMarcacion FROM Alert";
            Cursor cA = existeDatos.rawQuery(selectQueryconfiguration, new String[]{});
            NroAlertas = cA.getCount();
            cA.close();
            dataBaseHelper.close();
            cA.close();
            existeDatos.close();

        } catch (Exception e) {}

        Log.e(" NroAlertas ", String.valueOf(NroAlertas));

        //Calendar current = Calendar.getInstance();
        Calendar currentEsperada = Calendar.getInstance();
        Calendar currentProximaFecha = Calendar.getInstance();

        if (NroAlertas!=0){

            try {
                DBHelper dataBaseHelper = new DBHelper(this);
                SQLiteDatabase existeDatos = dataBaseHelper.getReadableDatabase();
                String selectQueryconfiguration = "SELECT FechaEsperada, FechaProxima FROM Alert WHERE FinTurno = 'false'";
                Cursor cA = existeDatos.rawQuery(selectQueryconfiguration, new String[]{});

                if (cA.moveToLast()) {
                    fechaEsperada = cA.getString(cA.getColumnIndex("FechaEsperada"));
                    fechaProxima = cA.getString(cA.getColumnIndex("FechaProxima"));
                }

                cA.close();
                existeDatos.close();

            } catch (Exception e) {
                Log.e("Exception ", e.getMessage());
            }

            try {currentProximaFecha.setTime(formatoIso.parse(fechaProxima));}
            catch (ParseException e) {Log.e(" EXCEPTION ", "Error al Parsear FECHAAA ");e.printStackTrace();}

            try {currentEsperada.setTime(formatoIso.parse(fechaEsperada));}
            catch (ParseException e) {Log.e(" EXCEPTION ", "Error al Parsear FECHAAA ");e.printStackTrace();}

            Log.e("-- CONSULTA ------","--------");
            Log.e("-- currentEsperada ",formatoIso.format(currentEsperada.getTime()));
            Log.e("-- currentProximaFecha ",formatoIso.format(currentProximaFecha.getTime()));
            Log.e("-- CONSULTA FIN ---","--------");


            //VALIDAR SI YA EXISTE ALERTA NO SE DEBE CREAR MIENTRAS NO SE ENVIE LA ULTIMA

            if (currentMarcacion.before(currentProximaFecha)){
                crearRegistro(currentEsperada, currentMarcacion);
            } else {
                crearRegistro(currentMarcacion, currentMarcacion);
            }

        } else {

            //CASO NO EXISTE NINGUNA ALERTA
            //VALIDAR SI YA EXISTE ALERTA NO SE DEBE CREAR MIENTRAS NO SE ENVIE LA ULTIMA
            crearRegistro(currentEsperada, currentMarcacion);
        }

    }

    public void crearRegistro(Calendar currentEsperada, Calendar currentMarcacion){

        Log.e("--- FECHA ", formatoIso.format(currentEsperada.getTime()));

        Calendar currentFechaProxima = Calendar.getInstance();

        int aux = 0;
        int minuto = currentEsperada.get(Calendar.MINUTE);

        //Log.e("--- MINUTO ", String.valueOf(minuto));

        int resto;
        if(minuto == tiempoMarcacion){minuto++;Log.e("--- MINUTO ++ ", String.valueOf(minuto));}

        if (minuto > tiempoMarcacion) {resto = minuto%tiempoMarcacion;
            aux = tiempoMarcacion - resto;
        } else {
            aux = tiempoMarcacion - minuto;
        }

        int minutosCalculado = aux + minuto;

        //Log.e("--- AUX ", String.valueOf(aux));
        //Log.e("--- MINUTO CALCULADO ", String.valueOf(minutosCalculado));
        currentEsperada.set(Calendar.MINUTE, minutosCalculado);
        currentEsperada.set(Calendar.SECOND, 00);


        //FECHAR PROXIMA -------
        currentFechaProxima.setTimeInMillis(currentEsperada.getTimeInMillis());
        currentFechaProxima.add(Calendar.MINUTE, tiempoMarcacion);
        currentFechaProxima.set(Calendar.SECOND, 00);

        Log.e("--- FECHA MARCACION ", formatoIso.format(currentMarcacion.getTime()));
        Log.e("--- FECHA  ESPERADA ", formatoIso.format(currentEsperada.getTime()));
        Log.e("--- FECHA   PROXIMA ", formatoIso.format(currentFechaProxima.getTime()));

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT Latitud, Longitud, CodigoEmpleado, IntervaloMarcacionTolerancia" +
                    ",NumeroCel, GuidDipositivo FROM Configuration";
            Cursor c = db.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {
                //Latitud = c.getString(c.getColumnIndex("Latitud"));
                //Longitud = c.getString(c.getColumnIndex("Longitud"));
                Numero = c.getString(c.getColumnIndex("NumeroCel"));
                DispositivoId = c.getString(c.getColumnIndex("GuidDipositivo"));
                CodigoEmpleado = c.getString(c.getColumnIndex("CodigoEmpleado"));

            }
            c.close();
            db.close();

        } catch (Exception e) {}

        try {

            AlertCrud alertCrud = new AlertCrud(mContext);

            Alert alert = new Alert();
            alert.NumeroA = Numero;
            alert.FechaMarcacion = formatoIso.format(currentMarcacion.getTime());
            alert.FechaEsperada = formatoIso.format(currentEsperada.getTime());
            alert.FechaProxima = formatoIso.format(currentFechaProxima.getTime());
            alert.FlagTiempo = "0";
            alert.MargenAceptado = "1";
            alert.EstadoA = "false";
            alert.EstadoBoton = "false";
            alert.DispositivoId = DispositivoId;
            alert.CodigoEmpleado = CodigoEmpleado;
            alert.FinTurno = "false";
            _Alert_Id = alertCrud.insertPrueba(alert);
            Log.e("FIN ", "GregorianCalendar");

        } catch (Exception e) {
            Log.e("--- Exception Alert ", " GUARDAR " + e.getMessage());
        }

        mostrarDatos();

    }
//--------------------------
    public void mostrarDatos(){

        Calendar hora = Calendar.getInstance();
        Calendar fecha = Calendar.getInstance();

        try {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase existeDatos = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT AlertId FROM Alert";
            Cursor cA = existeDatos.rawQuery(selectQueryconfiguration, new String[]{});
            NroAlertas = cA.getCount();
            cA.close();
            existeDatos.close();

        } catch (Exception e) {Log.e("Exception ", e.getMessage());}

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase existeDatos = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT AlertId, FechaEsperada, FechaProxima FROM Alert WHERE FinTurno = 'false'";
            Cursor cA = existeDatos.rawQuery(selectQueryconfiguration, new String[]{});

            if (cA.moveToLast()) {
                _Alert_id_Consultado_Aux = cA.getInt(cA.getColumnIndex("AlertId"));
                fechaEsperada = cA.getString(cA.getColumnIndex("FechaEsperada"));
                fechaProxima = cA.getString(cA.getColumnIndex("FechaProxima"));
            }

            cA.close();
            existeDatos.close();

        } catch (Exception e) {
            Log.e("Exception ", e.getMessage());
        }

        try {hora.setTime(formatoIso.parse(fechaEsperada));}
        catch (ParseException e) {Log.e(" EXCEPTION ", "Error al Parsear HORA MO ");e.printStackTrace();}
        try {fecha.setTime(formatoIso.parse(fechaEsperada));}
        catch (ParseException e) {Log.e(" EXCEPTION ", "Error al Parsear FECHA MO ");e.printStackTrace();}

        txtProximaMarcacionHora.setText(formatoHora.format(hora.getTime()));
        txtProximaMarcacionFecha.setText(formatoFecha.format(fecha.getTime()));

        //INICIAR TEMPORIZADOR
        temporizadorMargenTiempo();

        //SI ES LA PRIMERA ALARMA CREADA SE GENERA UN RETUR PARA NO MOSTRAR NADA EN LA ULTIMA MARCACION
        _Alert_id_Consultado_Aux--;
        if (NroAlertas==0){
            Log.e(" _Alert_id "," 0");
            return;
        }

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase existeDatos = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT FechaEsperada, FlagTiempo FROM Alert WHERE AlertId = "+_Alert_id_Consultado_Aux+"";
            Cursor cA = existeDatos.rawQuery(selectQueryconfiguration, new String[]{});

            if (cA.moveToLast()) {
                fechaUltimaX = cA.getString(cA.getColumnIndex("FechaEsperada"));
                flagCarita  = cA.getString(cA.getColumnIndex("FlagTiempo"));
            }
            cA.close();
            existeDatos.close();

        } catch (Exception e) {Log.e("Exception ", e.getMessage());}

        if (fechaUltimaX==null){
            Log.e(" fechaUltimaX "," null");
            return;
        }

        Calendar horaUltima = Calendar.getInstance();
        Calendar fechaUltima = Calendar.getInstance();

        try {horaUltima.setTime(formatoIso.parse(fechaUltimaX));}
        catch (ParseException e) {Log.e(" EXCEPTION ", "Error al Parsear HORA UL ");e.printStackTrace();}
        try {fechaUltima.setTime(formatoIso.parse(fechaUltimaX));}
        catch (ParseException e) {Log.e(" EXCEPTION ", "Error al Parsear FECHA UL ");e.printStackTrace();}

        txtUltimaMarcacion.setText(formatoHora.format(horaUltima.getTime()));
        txtUlltimaMarcacionFecha.setText(formatoFecha.format(fechaUltima.getTime()));

        Log.e("flagCarita ", flagCarita);
        if(flagCarita.equalsIgnoreCase("1")){
            txtEstadoUltimaMarcacion.setText("A tiempo");
            txtEstadoUltimaMarcacion.setTextColor(getResources().getColor(R.color.negro_general));
            txtUltimaMarcacion.setTextColor(getResources().getColor(R.color.negro_general));
            txtUlltimaMarcacionFecha.setTextColor(getResources().getColor(R.color.negro_general));

            caritaEstado.setImageResource(R.drawable.ic_feliz);

        }else{
            txtEstadoUltimaMarcacion.setText("A destiempo");
            txtEstadoUltimaMarcacion.setTextColor(getResources().getColor(R.color.red));
            txtUltimaMarcacion.setTextColor(getResources().getColor(R.color.red));
            txtUlltimaMarcacionFecha.setTextColor(getResources().getColor(R.color.red));

            caritaEstado.setImageResource(R.drawable.ic_triste);
        }

    }

    public void registrarAlertaApi(int _Alert_id_Consultado){

        Log.e("INGRESO ENVIO ", "ALERTA API "+ _Alert_id_Consultado);

        String fechaEspera = null, numeroCel = null, fechaMarcacion = null,
                fechaProxima = null, dispositivoId = null, codigoEmpleado = null,
                finTurno = null, flagTiempo = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase existeDatos = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT NumeroCel, FechaEsperada, FechaMarcacion, FechaProxima, " +
                    "DispositivoId, CodigoEmpleado, FinTurno, FlagTiempo FROM Alert WHERE AlertId ="+_Alert_id_Consultado+"";
            Cursor cA = existeDatos.rawQuery(selectQueryconfiguration, new String[]{});
            if (cA.moveToLast()) {
                numeroCel = cA.getString(cA.getColumnIndex("NumeroCel"));
                fechaEspera = cA.getString(cA.getColumnIndex("FechaEsperada"));
                fechaMarcacion = cA.getString(cA.getColumnIndex("FechaMarcacion"));
                fechaProxima = cA.getString(cA.getColumnIndex("FechaProxima"));
                dispositivoId = cA.getString(cA.getColumnIndex("DispositivoId"));
                codigoEmpleado = cA.getString(cA.getColumnIndex("CodigoEmpleado"));
                finTurno = cA.getString(cA.getColumnIndex("FinTurno"));
                flagTiempo = cA.getString(cA.getColumnIndex("FlagTiempo"));
            }
            cA.close();
            existeDatos.close();

        } catch (Exception e) {Log.e("Exception ", e.getMessage());}

        // CONSULTA TRACKING -----------------------------------------
        try {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbtracking = dataBaseHelper.getWritableDatabase();
            String selectQueryTracking = "SELECT Latitud, Longitud FROM Configuration";
            Cursor ctracking = dbtracking.rawQuery(selectQueryTracking, new String[]{});

            if (ctracking.moveToLast()) {
                Latitud = ctracking.getString(ctracking.getColumnIndex("Latitud"));
                Longitud = ctracking.getString(ctracking.getColumnIndex("Longitud"));
            }

            ctracking.close();
            dbtracking.close();

        } catch (Exception e) {}


        if (Latitud == null ) {
            Toast.makeText(mContext, " LatitudE NULL ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Longitud == null ) {
            Toast.makeText(mContext, " LongitudE NULL ", Toast.LENGTH_SHORT).show();
            return;
        }

        if(flagTiempo==null){flagTiempo = "1";}
        if(margenAceptado==null){margenAceptado = "1";}

        try {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            db.execSQL("UPDATE Alert SET Latitud = '"+Latitud+"' WHERE AlertId = "+_Alert_id_Consultado+"");
            db.execSQL("UPDATE Alert SET Longitud = '"+Longitud+"' WHERE AlertId = "+_Alert_id_Consultado+"");
            db.close();
        } catch (Exception sdlb){}


        Calendar currentMarcacion = Calendar.getInstance();
        Calendar currentEsperada = Calendar.getInstance();
        Calendar currentProxima = Calendar.getInstance();

        try {currentMarcacion.setTime(formatoIso.parse(fechaMarcacion));}
        catch (ParseException e) {Log.e(" EXCEPTION ", "Error al Parsear FECHAAA ");e.printStackTrace();}

        try {currentEsperada.setTime(formatoIso.parse(fechaEspera));}
        catch (ParseException e) {Log.e(" EXCEPTION ", "Error al Parsear FECHAAA ");e.printStackTrace();}

        try {currentProxima.setTime(formatoIso.parse(fechaProxima));}
        catch (ParseException e) {Log.e(" EXCEPTION ", "Error al Parsear FECHAAA ");e.printStackTrace();}

        Log.e("-----------SEND  ","ALERT-----------");
        Log.e("--- Numero ", numeroCel);
        Log.e("--- FechaMarcacion ", formatoGuardar.format(currentMarcacion.getTime()));
        Log.e("--- FechaEsperada ", formatoGuardar.format(currentEsperada.getTime()));
        Log.e("--- FechaProxima ", formatoGuardar.format(currentProxima.getTime()));
        Log.e("--- FlagTiempo ", flagTiempo);
        Log.e("--- MargenAceptado ", margenAceptado);
        Log.e("--- Latitud ", Latitud);
        Log.e("--- Longitud ", Longitud);
        Log.e("--- DispositivoId ", dispositivoId);
        Log.e("--- CodigoEmpleado ", codigoEmpleado);
        Log.e("--------- FIN SEND  ","ALERT---------");


        String URL = URL_API.concat("api/alert");

        JsonObject json = new JsonObject();
        json.addProperty("Numero", numeroCel);
        json.addProperty("FechaMarcacion", formatoGuardar.format(currentMarcacion.getTime()));
        json.addProperty("FechaEsperada", formatoGuardar.format(currentEsperada.getTime()));
        json.addProperty("FechaProxima", formatoGuardar.format(currentProxima.getTime()));
        json.addProperty("FlagTiempo", flagTiempo);
        json.addProperty("MargenAceptado", margenAceptado);
        json.addProperty("Latitud", Latitud);
        json.addProperty("Longitud", Longitud);
        json.addProperty("DispositivoId", dispositivoId);
        json.addProperty("CodigoEmpleado", codigoEmpleado);

        Ion.with(this)
                .load("POST", URL)
                .setJsonObjectBody(json)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> response) {

                        if(response == null){
                            Log.e(" responde Alert ", " NULL ");
                            return;
                        }

                        if (response.getHeaders().code() == 200) {

                            AlertCrud alertCrud = new AlertCrud(mContext);
                            Alert alert = new Alert();
                            alert.EstadoA = "true";
                            alert.AlertId = _Alert_id_Consultado;
                            alertCrud.updateEstado(alert);

                            Log.e("Alerta", response.getResult().toString());

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
                                    }

                                    if(c.getInt("ConfiguracionId")==4){
                                        DBHelper dataBaseHelper = new DBHelper(mContext);
                                        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                                        db.execSQL("UPDATE Configuration SET IntervaloMarcacionTolerancia = '" + c.getInt("Valor") + "'");
                                        db.close();

                                        b = c.getInt("Valor");
                                    }
                                } catch (JSONException e1) {e1.printStackTrace();}
                            }

                            int te = a;
                            int tes = b;

                            Log.e("AlerF Interv/ Toleranc ", String.valueOf(te)+"| "+String.valueOf(tes));

                        }
                    }
                });

    }

    public void temporizadorMargenTiempo(){

        try {contadorEnMargen.cancel();} catch (NullPointerException dsf){}

        Log.e("--- Ingresó ", "contadorMargenTiempo");

        String Fecha = null;

        // CONSULTA DE DATOS DEL SQLITE PARA ENVIO AL SERVIDOR
        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbA = dataBaseHelper.getReadableDatabase();
            String selectQueryA = "SELECT FechaEsperada FROM Alert";
            Cursor cA = dbA.rawQuery(selectQueryA, new String[]{});
            if (cA.moveToLast()) {
                Fecha = cA.getString(cA.getColumnIndex("FechaEsperada"));
            }
            cA.close();
            dbA.close();

        }catch (Exception e){
            Log.e("--- Exception ", "Fecha");
        }

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT IntervaloMarcacionTolerancia FROM Configuration";
            Cursor c = db.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {
                tiempoMargen = c.getInt(c.getColumnIndex("IntervaloMarcacionTolerancia"));

            }
            c.close();
            db.close();

        } catch (Exception e) {}

        Log.e(" -- FECHA MARGEN -- ",String.valueOf(tiempoMargen));

        Calendar currentTemporizadorMargen = Calendar.getInstance();

        try {currentTemporizadorMargen.setTime(formatoIso.parse(Fecha));}
        catch (ParseException e) {Log.e(" EXCEPTION ", "Error al Parsear TEMP M ");e.printStackTrace();}
        currentTemporizadorMargen.add(Calendar.MINUTE, -tiempoMargen);

        Calendar c = Calendar.getInstance();

        long milisegundosMargen = currentTemporizadorMargen.getTimeInMillis() - c.getTimeInMillis();

        Log.e(" -- FECHA ESPERADA -- ",formatoIso.format(currentTemporizadorMargen.getTime()));
        Log.e(" -- FECHA EN MILIS -- ",String.valueOf(milisegundosMargen));

        try {

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    contadorEnMargen  = new CountDownTimer(milisegundosMargen, 1000) {

                        @Override
                        public void onTick(long milisegundosMargen) {

                            int seconds = (int) (milisegundosMargen / 1000) % 60 ;
                            int minutes = (int) ((milisegundosMargen / (1000*60)) % 60);
                            int hours   = (int) ((milisegundosMargen / (1000*60*60)) % 24);

                            try {btnMarcacion.setTextColor(getResources().getColor(R.color.black_overlay));} catch (Exception e){}

                            String horaM = null, minutoM = null, segundoM = null;

                            horaM = String.valueOf(hours);
                            minutoM = String.valueOf(minutes);
                            segundoM = String.valueOf(seconds);

                            if (hours<10){horaM = String.valueOf("0"+hours);}

                            if (minutes<10){minutoM = String.valueOf("0"+minutes);}

                            if (seconds<10){segundoM = String.valueOf("0"+seconds);}

                            System.out.println(horaM+"h "+minutoM+"m "+segundoM+"s");
                            try {btnMarcacion.setText(horaM+":"+minutoM+":"+segundoM);} catch (Exception e){}
                        }

                        @Override
                        public void onFinish() {

                            btnMarcacion.setEnabled(true);
                            btnMarcacion.setText("Marcaci\u00F3n");
                            btnMarcacion.setBackgroundColor(getResources().getColor(R.color.verde));
                            btnMarcacion.setTextColor(Color.WHITE);

                            temporizadorFueraMargen();

                        }
                    }.start();
                }
            });


        }catch (Exception e){
            Log.e("EXCEPTION "," count "+ "Alert Activity Handler");
        }

    }

    public void temporizadorFueraMargen(){

        try {contadorFueraMargen.cancel();} catch (NullPointerException dsf){}

        Log.e("--- Ingresó ", "contadorFueraMargen");

        String Fecha = null;

        // CONSULTA DE DATOS DEL SQLITE PARA ENVIO AL SERVIDOR
        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbA = dataBaseHelper.getReadableDatabase();
            String selectQueryA = "SELECT FechaEsperada FROM Alert";
            Cursor cA = dbA.rawQuery(selectQueryA, new String[]{});
            if (cA.moveToLast()) {
                Fecha = cA.getString(cA.getColumnIndex("FechaEsperada"));
            }
            cA.close();
            dbA.close();

        }catch (Exception e){
            Log.e("--- Exception ", "Fecha");
        }

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT IntervaloMarcacionTolerancia FROM Configuration";
            Cursor c = db.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {
                tiempoMargen = c.getInt(c.getColumnIndex("IntervaloMarcacionTolerancia"));
            }
            c.close();
            db.close();

        } catch (Exception e) {}

        Log.e(" -- FECHA MARGEN -- ",String.valueOf(tiempoMargen));

        Calendar currentTemporizadorMargen = Calendar.getInstance();

        try {currentTemporizadorMargen.setTime(formatoIso.parse(Fecha));}
        catch (ParseException e) {Log.e(" EXCEPTION ", "Error al Parsear TEMP M ");e.printStackTrace();}
        currentTemporizadorMargen.add(Calendar.MINUTE, tiempoMargen);

        Calendar c = Calendar.getInstance();

        long milisegundosMargen = currentTemporizadorMargen.getTimeInMillis() - c.getTimeInMillis();

        Log.e(" -- FECHA ESPERADA -- ",formatoIso.format(currentTemporizadorMargen.getTime()));
        Log.e(" -- FECHA EN MILIS -- ",String.valueOf(milisegundosMargen));

        try {

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    contadorFueraMargen  = new CountDownTimer(milisegundosMargen, 1000) {

                        @Override
                        public void onTick(long milisegundosMargen) {

                            flagTiempo = "1";
                            margenAceptado ="1";

                            int seconds = (int) (milisegundosMargen / 1000) % 60 ;
                            int minutes = (int) ((milisegundosMargen / (1000*60)) % 60);
                            int hours   = (int) ((milisegundosMargen / (1000*60*60)) % 24);

                            System.out.println(hours+"h "+minutes+"m "+seconds+"s");

                        }

                        @Override
                        public void onFinish() {

                            flagTiempo = "0";
                            margenAceptado = "1";

                            btnMarcacion.setEnabled(true);
                            btnMarcacion.setText("Marcaci\u00F3n");
                            btnMarcacion.setBackgroundColor(getResources().getColor(R.color.red));
                            btnMarcacion.setTextColor(Color.WHITE);

                        }
                    }.start();
                }
            });

        }catch (Exception e){
            Log.e("EXCEPTION "," count "+ "Alert Activity Handler");
        }

    }

    //METODOS QUE PIDEN PARA HILOS
    @Override
    protected void onResume() {
        super.onResume();

        try {contadorEnMargen.cancel();} catch (NullPointerException dsf){}
        try {contadorFueraMargen.cancel();} catch (NullPointerException dsf){}

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {contadorEnMargen.cancel();} catch (NullPointerException dsf){}
        try {contadorFueraMargen.cancel();} catch (NullPointerException dsf){}
    }




    /*

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public void saveError(Alert marker){

        Log.e("-- INGRESÓ ", "saveError");

        try {

            AlertCrud alertCRUD = new AlertCrud(mContext);
            marker.EstadoA = "false";
            marker.AlertId = _Alert_Id_Pos;
            _Alert_Id_Pos = alertCRUD.insert(marker);

        }catch (Exception e){}

    }

    public  Boolean deleteAlert(int id) {
        DBHelper dbgelperDeete = new DBHelper(mContext);
        SQLiteDatabase sqldbDelete = dbgelperDeete.getWritableDatabase();
        sqldbDelete.execSQL("DELETE FROM  Alert WHERE AlertId = "+id);
        sqldbDelete.close();
        return true;
    }

    public void sendSave() {

        int i =0;
        Log.e("--|| Reenvio ", "sendSave ||");

        try {

            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            String selectQuery = "SELECT AlertId, NumeroCel, FechaMarcacion, FechaEsperada, FechaProxima, FlagTiempo, " +
                    "MargenAceptado, Latitud, Longitud, DispositivoId, CodigoEmpleado  FROM Alert WHERE Estado = 'false'";
            Cursor c = db.rawQuery(selectQuery, new String[]{});
            if (c.moveToFirst()) {
                do {
                    Alert alertPos = new Alert();
                    _AlertRee_Id = c.getInt(c.getColumnIndex("AlertId"));

                    alertPos.NumeroA = c.getString(c.getColumnIndex("NumeroCel"));
                    alertPos.FechaMarcacion = c.getString(c.getColumnIndex("FechaMarcacion"));
                    alertPos.FechaEsperada = c.getString(c.getColumnIndex("FechaEsperada"));
                    alertPos.FechaProxima = c.getString(c.getColumnIndex("FechaProxima"));
                    alertPos.FlagTiempo = c.getString(c.getColumnIndex("FlagTiempo"));
                    alertPos.MargenAceptado = c.getString(c.getColumnIndex("MargenAceptado"));
                    alertPos.LatitudA = c.getString(c.getColumnIndex("Latitud"));
                    alertPos.LongitudA = c.getString(c.getColumnIndex("Longitud"));
                    alertPos.DispositivoId = c.getString(c.getColumnIndex("DispositivoId"));
                    alertPos.CodigoEmpleado = c.getString(c.getColumnIndex("CodigoEmpleado"));

                    deleteAlert(_AlertRee_Id);

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

                                    if(response == null){

                                        saveError(alertPos);

                                        return;
                                    }

                                    if (response.getHeaders().code() != 200) {

                                        saveError(alertPos);
                                        Log.e("Exception ", "Finaliza SaveError");
                                        Log.e("JsonObject ", response.getResult().toString());

                                    }
                                }
                            });
                    i++;

                } while(c.moveToNext() && i<30);

            }
            c.close();
            db.close();

        } catch (Exception e){}

    }

    public Boolean consultaSinConexion(){

        Log.e("-- COSULTA SIN ", "--|CONEXION|--");

        try {
            DBHelper dataBaseHelper = new DBHelper(mContext);
            SQLiteDatabase dbN = dataBaseHelper.getWritableDatabase();
            String selectQueryBuscaN = "SELECT NumeroCel FROM Alert WHERE Estado = 'false'";
            Cursor cbuscaN = dbN.rawQuery(selectQueryBuscaN, new String[]{}, null);
            int contador = cbuscaN.getCount();
            cbuscaN.close();
            dbN.close();

            //Log.e("-- POR SEND ", String.valueOf(contador));

            if (contador>0) {
                sendSave();
                Log.e("-- if ", "--||--");
            }

        }catch (Exception e){
            Log.e("-- Error Reenvio Track", e.getMessage());
        }

        return true;
    }

    public void compararProximaAlarma(){

        Log.e("--- Ingreso Comparar ", " Proxima Alarma");
        /*//**************************************************************************************************

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
            Log.e("--- Error Consulta ", "FechaProxima");
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
            flagMostrarFecha = true;
            //createNewAlert();
        }
        /*//**************************************************************************************************

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
     */

}
