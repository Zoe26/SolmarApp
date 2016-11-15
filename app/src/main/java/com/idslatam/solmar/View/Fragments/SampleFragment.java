package com.idslatam.solmar.View.Fragments;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Api.Parser.JsonParser;
import com.idslatam.solmar.Models.Crud.AlertCrud;
import com.idslatam.solmar.Models.Crud.ConfigurationCrud;
import com.idslatam.solmar.Models.Database.DBHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.idslatam.solmar.Models.Entities.Alert;
import com.idslatam.solmar.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SampleFragment extends Fragment implements  View.OnClickListener {


    Context thiscontext;
    AlertCrud alertCRUD;

    final Handler handler = new Handler();

    Button btnMarcacion;
    TextView textHora, textUltimaMarcacion, textUltimaMarcacionFecha, textHoraFecha, textintervalo, textflag_tiempo;

    View myView;
    ImageView caritaEstado;

    String Proxima;
    String HoraMarcacionP;

    String EstadoBoton, FechaEsperadaIso, FechaEsperadaIsoFin, EstadoE;
    Calendar horaIni, horaFin, horaIso;

    String EstadoBotonMostrar, FechaEsperada, FechaProxima, FlagTiempo, MargenAceptado;


    ConfigurationCrud configurationCRUD = new ConfigurationCrud(thiscontext);
    DBHelper dataBaseHelper = new DBHelper(thiscontext);


    static final String[] MOBILE_ALERT = new String[] { "AlertImagen", "AlertMargen",
            "AlertUmarcacion", "AlertProxMarcacion"};

    //**********************************************************************************************

    int tiempoEnvio, tiempoIntervalo, tiempoGuardado, intervaloResta, intervaloInicial;

    private int _Alert_Id = 0;
    private int _AlertUpdate_Id = 0;

    Calendar choraProximaG, choraEsperadaG, choraIso, choraIsoFin;

    String  horaEsperadaG, horaProximaG, horaEsperadaIsoG, horaEsperadaIsoFinG;
    String LatitudG, LongitudG, NumeroG, DispositivoId, CodigoEmpleado;

    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss")
            , formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    boolean flagMostrarFecha = false;

    protected String URL_API;

    String FechaMarcacionE, FechaEsperadaE, FechaProximaE, FlagTiempoE, MargenAceptadoE, LatitudE, LongitudE, NumeroE;
    String DispositivoIdE, CodigoEmpleadoE;

    int ValorTemporal;

    public SampleFragment() {
    }

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
        TextView textView = new TextView(getActivity());
//        textView.setText(getArguments().getString(STARTING_TEXT));

        thiscontext = container.getContext();
        alertCRUD = new AlertCrud(thiscontext);

        try {

            DBHelper dataBaseHelper = new DBHelper(thiscontext);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT IntervaloMarcacion, IntervaloMarcacionTolerancia FROM Configuration";
            Cursor c = db.rawQuery(selectQuery, new String[]{});

            if (c.moveToLast()) {

                tiempoEnvio = c.getInt(c.getColumnIndex("IntervaloMarcacion"));
                tiempoIntervalo = c.getInt(c.getColumnIndex("IntervaloMarcacionTolerancia"));

            }

            c.close();
            db.close();

        }catch (Exception e){}

        if(tiempoIntervalo==0){tiempoIntervalo=1;}
//        if(tiempoEnvio==0){tiempoEnvio=30;}
        Log.e("---! tiempoIntervalo ", String.valueOf(tiempoIntervalo));
        //--------------------------------------------------------------------

//        ComprobacionFocus();

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

        startRepeatingTask();
        mostrarHora();

        btnMarcacion = (Button) myView.findViewById(R.id.btn_marcacion);

        btnMarcacion.setOnClickListener(this);

        btnMarcacion.setEnabled(false);
        btnMarcacion.setBackgroundColor(Color.WHITE);

        String interval = String.valueOf(tiempoIntervalo);

        textintervalo.setText(interval);
        return myView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();

    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_marcacion:

                enviarMarcacion();
                mostrarHora();
                btnMarcacion.setEnabled(false);
                btnMarcacion.setBackgroundColor(Color.WHITE);
                btnMarcacion.setTextColor(Color.WHITE);
                compararProximaAlarma();

                break;
        }
    }

    public void compararProximaAlarma(){

//        Log.e("--- Ingreso Comparar ", " Proxima Alarma");
        //**************************************************************************************************

        String fchaProx = null;
        try {

            DBHelper dataBaseHelper = new DBHelper(thiscontext);
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

//        Log.e("--- Fecha Consulta ", " Reinicio " + String.valueOf(cal));
//        Log.e("--- Fecha Actual ", " Reinicio " + String.valueOf(horaAct));

        if (horaAct.after(cal)) {

//            Log.e("--- Condicon ", " Antes Guardar " + fchaProx);
//            guardaAlarmaReinicio();
            dialogoNoMarco();
            guardarAlertaX();
//            Log.e("--- Condicon ", " Despues Guardar " + fchaProx);
        } else {
            guardarAlertaXAlternativo();
        }
        //**************************************************************************************************

    }

    public void dialogoNoMarco(){

        try {

            String hora, fecha, fechaEsp = null;

            DBHelper dataBaseHelper = new DBHelper(thiscontext);
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


            AlertDialog.Builder builder = new AlertDialog.Builder(thiscontext);
            builder.setCancelable(false);
            builder.setTitle("Marcaci\u00F3n a destiempo");
            builder.setMessage("\u00C9sta marcaci\u00F3n corresponde a las "+hora+" del d\u00EDa "+fecha+"");
            builder.setPositiveButton("Aceptar", null);
            builder.show();
        } catch (Exception e){}
    }

    void startRepeatingTask()
    {
        runnable.run();
    }

    Runnable runnable = new Runnable() {
        public void run() {
            try {
                habilitarBoton();
                mostrarHora();
                ultimaMarcacion();
            }catch (Exception e){}

            handler.postDelayed(runnable, 100 * 1);
        }
    };

    void stopRepeatingTask()
    {
        handler.removeCallbacks(runnable);
    }

    public void guardarAlertaX() {

        int CantidadAlert = 0;
        try {

            DBHelper dataBaseHelper = new DBHelper(thiscontext);
            SQLiteDatabase Adb = dataBaseHelper.getReadableDatabase();
            //Sin fin de turno y boton en falso.
            String selectQueryconfiguration = "SELECT AlertId FROM Alert WHERE FinTurno = 'false' AND EstadoBoton = 'false' ";
            Cursor cta = Adb.rawQuery(selectQueryconfiguration, new String[]{});
            CantidadAlert = cta.getCount();
            cta.close();
            Adb.close();

        } catch (Exception e) {
        }

        Log.e("Cantidad Alert: ", String.valueOf(CantidadAlert));
        //Si se tiene un alert ya registrado en la Base de Datos y no ha terminado turno
        if(CantidadAlert > 0){
            return;
        }

        //1 calcular fecha proxima marcación.
        fechaProximaMarcacion();
        dataAlert();
        //Se crea clase Alert
        try {
            //Ejemplo 5:47
            Alert alert = new Alert();
            alert.NumeroA = NumeroG;//Done
            alert.FechaMarcacion = "1900,01,01,00,00,00";
            alert.FechaEsperada = horaEsperadaG;//5:50 //Done
            alert.FechaProxima = horaProximaG;//5:55 //Done
            alert.FlagTiempo = "0";
            alert.MargenAceptado = "0";
            alert.LatitudA = LatitudG;//Done
            alert.LongitudA = LongitudG;//Done
            alert.EstadoA = "false";
            alert.EstadoBoton = "false";
            alert.FechaEsperadaIso = horaEsperadaIsoG;//Done
            alert.FechaEsperadaIsoFin = horaEsperadaIsoFinG;//Done
            alert.DispositivoId = DispositivoId;//Done
            alert.CodigoEmpleado = CodigoEmpleado;//Done
            alert.FinTurno = "false";

            _Alert_Id = alertCRUD.insert(alert);

        } catch (Exception e) {
        }

    }

    public void guardarAlertaXAlternativo() {

        int CantidadAlert = 0;
        try {

            DBHelper dataBaseHelper = new DBHelper(thiscontext);
            SQLiteDatabase Adb = dataBaseHelper.getReadableDatabase();
            //Sin fin de turno y boton en falso.
            String selectQueryconfiguration = "SELECT AlertId FROM Alert WHERE FinTurno = 'false' AND EstadoBoton = 'false' ";
            Cursor cta = Adb.rawQuery(selectQueryconfiguration, new String[]{});
            CantidadAlert = cta.getCount();
            cta.close();
            Adb.close();

        } catch (Exception e) {}

        Log.e("Cantidad Alert: ", String.valueOf(CantidadAlert));
        //Si se tiene un alert ya registrado en la Base de Datos y no ha terminado turno
        if(CantidadAlert > 0){
            return;
        }

        //1 calcular fecha proxima marcación.
        fechaProximaMarcacionAlternativo();
        dataAlert();
        //Se crea clase Alert
        try {
            //Ejemplo 5:47
            Alert alert = new Alert();
            alert.NumeroA = NumeroG;//Done
            alert.FechaMarcacion = "1900,01,01,00,00,00";
            alert.FechaEsperada = horaEsperadaG;//5:50 //Done
            alert.FechaProxima = horaProximaG;//5:55 //Done
            alert.FlagTiempo = "0";
            alert.MargenAceptado = "0";
            alert.LatitudA = LatitudG;//Done
            alert.LongitudA = LongitudG;//Done
            alert.EstadoA = "false";
            alert.EstadoBoton = "false";
            alert.FechaEsperadaIso = horaEsperadaIsoG;//Done
            alert.FechaEsperadaIsoFin = horaEsperadaIsoFinG;//Done
            alert.DispositivoId = DispositivoId;//Done
            alert.CodigoEmpleado = CodigoEmpleado;//Done
            alert.FinTurno = "false";

            _Alert_Id = alertCRUD.insert(alert);

        } catch (Exception e) {}

    }

    public Boolean dataAlert(){

        try {
            DBHelper dataBaseHelper = new DBHelper(thiscontext);
            SQLiteDatabase dbConfiguration = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT Fotocheck FROM Asistencia";
            Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

            if (cConfiguration.moveToFirst()) {
                CodigoEmpleado = cConfiguration.getString(cConfiguration.getColumnIndex("Fotocheck"));
            }

            cConfiguration.close();
            dbConfiguration.close();

        } catch (Exception e) {}

        // SE OBTIENE EL NUMERO DEL CELULAR
        try {
            DBHelper dataBaseHelper = new DBHelper(thiscontext);
            SQLiteDatabase dbConfiguration = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT NumeroCel, GuidDipositivo FROM Configuration";
            Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

            if (cConfiguration.moveToFirst()) {
//                CodigoEmpleado = cConfiguration.getString(cConfiguration.getColumnIndex("CodigoEmpleado"));
                NumeroG = cConfiguration.getString(cConfiguration.getColumnIndex("NumeroCel"));
                DispositivoId = cConfiguration.getString(cConfiguration.getColumnIndex("GuidDipositivo"));
            }

            cConfiguration.close();
            dbConfiguration.close();

        } catch (Exception e) {}

        // CONSULTA TRACKING -----------------------------------------
        try {
            DBHelper dataBaseHelper = new DBHelper(thiscontext);
            SQLiteDatabase dbtracking = dataBaseHelper.getWritableDatabase();
            String selectQueryTracking = "SELECT Latitud, Longitud FROM Configuration";
            Cursor ctracking = dbtracking.rawQuery(selectQueryTracking, new String[]{});

            if (ctracking.moveToFirst()) {
                LatitudG = ctracking.getString(ctracking.getColumnIndex("Latitud"));
                LongitudG = ctracking.getString(ctracking.getColumnIndex("Longitud"));
            }

            ctracking.close();
            dbtracking.close();

        } catch (Exception e) {
        }

        return true;
    }

    public Boolean fechaProximaMarcacion(){

        choraEsperadaG = Calendar.getInstance();//Fecha Actual
        Calendar choraEsperadaIsoG = Calendar.getInstance(); //Fecha Utilizada para los límites.

        int minuto = choraEsperadaG.get(Calendar.MINUTE);//Minuto de la hora actual

        //int Valor = minuto;
        ValorTemporal = 0;
        //int Intervalo = tiempoEnvio; //Intervalo de tiempo para enviar Alertas.

        if (minuto >= tiempoEnvio) {

            int resto = minuto%tiempoEnvio;

            if(resto==0){
                ValorTemporal = 0;
            }
            else {
                ValorTemporal = tiempoEnvio - resto;
            }

        } else {

            ValorTemporal = tiempoEnvio - minuto;
        }

        int minutoT = ValorTemporal + minuto;

        choraEsperadaG.set(Calendar.MINUTE, minutoT);
        choraEsperadaG.set(Calendar.SECOND, 00);
        choraEsperadaIsoG.set(Calendar.MINUTE, minutoT);

        horaEsperadaG = formatoGuardar.format(choraEsperadaG.getTime());

        choraProximaG = choraEsperadaG; //Calendar.getInstance();
        choraProximaG.add(Calendar.MINUTE, tiempoEnvio);
        choraProximaG.set(Calendar.SECOND, 00);
        horaProximaG = formatoGuardar.format(choraProximaG.getTime());

        choraIso = choraEsperadaIsoG; //Calendar.getInstance();
        choraIso.set(Calendar.MINUTE, minutoT);
        choraIso.set(Calendar.SECOND, 00);
        choraIso.add(Calendar.MINUTE, -tiempoIntervalo);

        horaEsperadaIsoG = formatoIso.format(choraEsperadaIsoG.getTime());

        // --- Hora IsoFin
        choraIsoFin = choraEsperadaIsoG; //Calendar.getInstance();
        choraIsoFin.set(Calendar.MINUTE, minutoT);
        choraIsoFin.add(Calendar.MINUTE, tiempoIntervalo);

        horaEsperadaIsoFinG = formatoIso.format(choraEsperadaIsoG.getTime());

        return  true;
    }

    public Boolean fechaProximaMarcacionAlternativo(){

        try {

            DBHelper dataBaseHelper = new DBHelper(thiscontext);
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT IntervaloMarcacion, IntervaloMarcacionTolerancia FROM Configuration";
            Cursor c = db.rawQuery(selectQuery, new String[]{});

            if (c.moveToLast()) {

                tiempoEnvio = c.getInt(c.getColumnIndex("IntervaloMarcacion"));
                tiempoIntervalo = c.getInt(c.getColumnIndex("IntervaloMarcacionTolerancia"));

            }

            c.close();
            db.close();

        }catch (Exception e){}

        Log.e("ALTERN tiempoEnvio : ", String.valueOf(tiempoEnvio));

        choraEsperadaG = Calendar.getInstance();//Fecha Actual
        Calendar choraEsperadaIsoG = Calendar.getInstance(); //Fecha Utilizada para los límites.

        int minuto = choraEsperadaG.get(Calendar.MINUTE)+tiempoIntervalo;//Minuto de la hora actual

        //int Valor = minuto;
        int ValorTemporal = 0;
        //int Intervalo = tiempoEnvio; //Intervalo de tiempo para enviar Alertas.

        if (minuto >= tiempoEnvio) {
            int resto = minuto%tiempoEnvio;
            ValorTemporal = tiempoEnvio-resto;
        } else {
            ValorTemporal = tiempoEnvio - minuto;
        }

        int minutoT = ValorTemporal + minuto;

        choraEsperadaG.set(Calendar.MINUTE, minutoT);
        choraEsperadaG.set(Calendar.SECOND, 00);
        choraEsperadaIsoG.set(Calendar.MINUTE, minutoT);

        horaEsperadaG = formatoGuardar.format(choraEsperadaG.getTime());

        choraProximaG = choraEsperadaG; //Calendar.getInstance();
        choraProximaG.add(Calendar.MINUTE, tiempoEnvio);
        choraProximaG.set(Calendar.SECOND, 00);
        horaProximaG = formatoGuardar.format(choraProximaG.getTime());

        choraIso = choraEsperadaIsoG; //Calendar.getInstance();
        choraIso.set(Calendar.MINUTE, minutoT);
        choraIso.set(Calendar.SECOND, 00);
        choraIso.add(Calendar.MINUTE, -tiempoIntervalo);

        horaEsperadaIsoG = formatoIso.format(choraEsperadaIsoG.getTime());

        // --- Hora IsoFin
        choraIsoFin = choraEsperadaIsoG; //Calendar.getInstance();
        choraIsoFin.set(Calendar.MINUTE, minutoT);
        choraIsoFin.add(Calendar.MINUTE, tiempoIntervalo);

        horaEsperadaIsoFinG = formatoIso.format(choraEsperadaIsoG.getTime());

        return  true;
    }

    public void habilitarBoton(){

        try {
            DBHelper dataBaseHelper = new DBHelper(thiscontext);
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
        SimpleDateFormat formatoSistema = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String horaActualSistema = formatoSistema.format(horaActual.getTime());

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

        int difBoton = minBotonActivo - minHoraActual;

        if(dia!=diaFin){
            flagMostrarFecha = true;
        }

        if(horaActual.after(horaIni) && horaActual.before(horaFin) && EstadoBoton.equals("false")){

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
            btnMarcacion.setText("Activaci\u00F3n en " + difBoton+ " min.");
            btnMarcacion.setEnabled(false);
            btnMarcacion.setTextColor(getResources().getColor(R.color.black_overlay));
        }
    }

    public void mostrarHora() {


        // CONSULTA ALERT ---------------------------------------------
        try {
            DBHelper dataBaseHelper = new DBHelper(thiscontext);
            SQLiteDatabase dbalert = dataBaseHelper.getWritableDatabase();
            String selectQueryAlert = "SELECT FechaEsperada, FechaProxima, EstadoBoton FROM Alert";

            Cursor calert = dbalert.rawQuery(selectQueryAlert, new String[]{});

            if (calert.moveToLast()) {

                HoraMarcacionP = calert.getString(calert.getColumnIndex("FechaEsperada"));
                FechaEsperada = calert.getString(calert.getColumnIndex("FechaEsperada"));
                FechaProxima = calert.getString(calert.getColumnIndex("FechaProxima"));
                EstadoBotonMostrar = calert.getString(calert.getColumnIndex("EstadoBoton"));
            }
            calert.close();
            dbalert.close();

            if (EstadoBotonMostrar.equals("false")){

                String proximaFecha;

                DateFormat df = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
                Calendar cal  = Calendar.getInstance(); //--
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

    public void enviarMarcacion(){

        String horaActual = null;

        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
            horaActual = sdf.format(new Date());

        }catch (Exception e){}

        // CONSULTA DE DATOS DEL SQLITE PARA ENVIO AL SERVIDOR
        try {
            DBHelper dataBaseHelper = new DBHelper(thiscontext);
            SQLiteDatabase dbA = dataBaseHelper.getReadableDatabase();
            String selectQueryA = "SELECT AlertId FROM Alert";
            Cursor cA = dbA.rawQuery(selectQueryA, new String[]{});

            if (cA.moveToLast()) {
                _AlertUpdate_Id = cA.getInt(cA.getColumnIndex("AlertId"));
            }
            cA.close();
            dbA.close();

        }catch (Exception e){}

        try {

            String est = "true";
            Alert alert = new Alert();
            alert.FechaMarcacion = horaActual;
            alert.FlagTiempo = FlagTiempo;
            alert.MargenAceptado = MargenAceptado;
            alert.EstadoBoton = est;
            alert.AlertId = _AlertUpdate_Id;
            alertCRUD.update(alert);

        } catch (Exception e){}

        // CONSULTA DE DATOS DEL SQLITE PARA ENVIO AL SERVIDOR
        try {
            DBHelper dataBaseHelper = new DBHelper(thiscontext);
            SQLiteDatabase dbA = dataBaseHelper.getReadableDatabase();
//            String selectQueryA = "SELECT AlertId, Numero, FechaMarcacion, FechaEsperada, FechaProxima, FlagTiempo, MargenAceptado, Latitud, Longitud, DispositivoId, CodigoEmpleado  FROM Alert WHERE AlertId ='"+_AlertUpdate_Id+"'";
            String selectQueryA = "SELECT AlertId, Numero, FechaMarcacion, FechaEsperada, FechaProxima, FlagTiempo, MargenAceptado, Latitud, Longitud, DispositivoId, CodigoEmpleado  FROM Alert";
            Cursor cA = dbA.rawQuery(selectQueryA, new String[]{});

            if (cA.moveToLast()) {
                _AlertUpdate_Id = cA.getInt(cA.getColumnIndex("AlertId"));

                FechaMarcacionE = cA.getString(cA.getColumnIndex("FechaMarcacion"));
                FechaEsperadaE = cA.getString(cA.getColumnIndex("FechaEsperada"));
                FechaProximaE = cA.getString(cA.getColumnIndex("FechaProxima"));
                FlagTiempoE = cA.getString(cA.getColumnIndex("FlagTiempo"));
                MargenAceptadoE = cA.getString(cA.getColumnIndex("MargenAceptado"));
                LatitudE = cA.getString(cA.getColumnIndex("Latitud"));
                LongitudE = cA.getString(cA.getColumnIndex("Longitud"));
                NumeroE = cA.getString(cA.getColumnIndex("Numero"));
                DispositivoIdE = cA.getString(cA.getColumnIndex("DispositivoId"));
                CodigoEmpleadoE = cA.getString(cA.getColumnIndex("CodigoEmpleado"));
            }
            cA.close();
            dbA.close();

        }catch (Exception e){}


        new PostAsync().execute(NumeroE, FechaMarcacionE, FechaEsperadaE, FechaProximaE, FlagTiempoE, MargenAceptadoE, LatitudE, LongitudE, DispositivoIdE, CodigoEmpleadoE);

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

                if (json != null) {

                    Log.e("Alert ", json.toString());

                    consultaIntervalos();
                    String Configuracion = json.getString("Configuracion");
                    String estadoEnvio = json.getString("Estado");

//                    Log.e("-- Estado Envio "+estadoEnvio, "-- IdAlert "+_AlertUpdate_Id);

                    try {

                        Alert alert = new Alert();
                        alert.EstadoA = estadoEnvio;
                        alert.AlertId = _AlertUpdate_Id;
                        alertCRUD.updateEstado(alert);

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

    public void consultaIntervalos(){

        try {

            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String selectQuery = "SELECT IntervaloMarcacion, IntervaloMarcacionTolerancia FROM Configuration";

            Cursor c = db.rawQuery(selectQuery, new String[]{});

            if (c.moveToLast()) {
                tiempoGuardado = c.getInt(c.getColumnIndex("IntervaloMarcacion"));
                tiempoIntervalo = c.getInt(c.getColumnIndex("IntervaloMarcacionTolerancia"));
            }
            c.close();
            db.close();

        } catch (Exception e) {}

        String inte = String.valueOf(tiempoIntervalo);
        textintervalo.setText(inte);

    }

    public void ultimaMarcacion(){

        String horaUltimaMarcacion = null;
        String estadoMarcacion = null;

        try {
            DBHelper dataBaseHelper = new DBHelper(thiscontext);
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
}