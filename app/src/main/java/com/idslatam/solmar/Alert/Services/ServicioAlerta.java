package com.idslatam.solmar.Alert.Services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

import com.idslatam.solmar.Models.Crud.AlertCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Alert;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ServicioAlerta extends Service {

    int tiempoEnvio, tiempoIntervalo;

    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss"), formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private int _Alert_Id = 0;

    Calendar choraProximaG, choraIso, choraIsoFin;

    String  horaEsperadaG, horaProximaG, horaEsperadaIsoG, horaEsperadaIsoFinG;

    String Latitud, Longitud, Numero;
    String LatitudG, LongitudG, NumeroG, DispositivoId, CodigoEmpleado;

    AlertCrud alertCRUD = new AlertCrud(this);
    DBHelper dataBaseHelper = new DBHelper(this);

    public static Boolean serviceRunningAlerta= false;

    String URLGlobal;


    private PendingIntent pendingIntent;

    @Override
    public void onCreate() {

        super.onCreate();

//        Intent alarmIntent = new Intent(ServicioAlerta.this, AlarmReceiver.class);
//        pendingIntent = PendingIntent.getBroadcast(ServicioAlerta.this, 0, alarmIntent, 0);

        try {

            DBHelper dataBaseHelper = new DBHelper(this);
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


//        GlobalClass globalClass = new GlobalClass();
//        URLGlobal = globalClass.getUrl();

        guardarAlertaX();
        Log.e("Last Consulta", " Guardar ");
    }

    @Override
    public IBinder onBind(Intent intencion) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        serviceRunningAlerta = true;

        SQLiteDatabase dbToken = dataBaseHelper.getReadableDatabase();
        String selectQueryToken = "SELECT Token FROM Configuration WHERE Token is null or Token = ''";
        Cursor cbuscaToken = dbToken.rawQuery(selectQueryToken, new String[]{}, null);
        int buscaToken = cbuscaToken.getCount();
        cbuscaToken.close();
        dbToken.close();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceRunningAlerta = false;
//        Log.e("Servicio Alerta --- ", "Detenido--");
    }



    public Boolean guardarAlertaX() {

        int CantidadAlert = 0;
        try {

            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase Adb = dataBaseHelper.getReadableDatabase();
            //Sin fin de turno y boton en falso.
            String selectQueryconfiguration = "SELECT AlertId FROM Alert WHERE FinTurno = 'false' AND EstadoBoton = 'false'";
            Cursor cta = Adb.rawQuery(selectQueryconfiguration, new String[]{});
            CantidadAlert = cta.getCount();
            cta.close();
            Adb.close();

        } catch (Exception e) {
            Log.e("Error Consulta Guardar ", e.getMessage());
        }

        Log.e("Cantidad Alert: ", String.valueOf(CantidadAlert));
        //Si se tiene un alert ya registrado en la Base de Datos y no ha terminado turno
        if(CantidadAlert > 0){
            return true;
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

            //Si en algún momento es necesario ejecutar Alert
            //startAt10(alert);

        } catch (Exception e) {
        }

        return true;
    }

    public Boolean dataAlert(){

        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbConfiguration = dataBaseHelper.getReadableDatabase();
            String selectQueryconfiguration = "SELECT Fotocheck FROM Asistencia";
            Cursor cConfiguration = dbConfiguration.rawQuery(selectQueryconfiguration, new String[]{});

            if (cConfiguration.moveToFirst()) {
                CodigoEmpleado = cConfiguration.getString(cConfiguration.getColumnIndex("Fotocheck"));
            }

            cConfiguration.close();
            dbConfiguration.close();

        } catch (Exception e) {
        }

        // SE OBTIENE EL NUMERO DEL CELULAR
        try {
            DBHelper dataBaseHelper = new DBHelper(this);
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

        } catch (Exception e) {
        }

        // CONSULTA TRACKING -----------------------------------------
        try {
            DBHelper dataBaseHelper = new DBHelper(this);
            SQLiteDatabase dbtracking = dataBaseHelper.getWritableDatabase();
            String selectQueryTracking = "SELECT Latitud , Longitud FROM Tracking";
            Cursor ctracking = dbtracking.rawQuery(selectQueryTracking, new String[]{});

            if (ctracking.moveToLast()) {
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

        Calendar choraEsperadaG = Calendar.getInstance();//Fecha Actual
        Calendar choraEsperadaIsoG = Calendar.getInstance(); //Fecha Utilizada para los límites.

        int minuto = choraEsperadaG.get(Calendar.MINUTE);//Minuto de la hora actual

        //int Valor = minuto;
        int ValorTemporal = 0;
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
}



