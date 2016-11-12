package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Tracking;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Luis on 30/10/2016.
 */

public class TrackingCrud {
    private DBHelper dbHelper;

    public TrackingCrud(Context context) {

        dbHelper = new DBHelper(context);
    }

    public int insert(Tracking tracking) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Tracking.KEY_Numero, tracking.Numero);
        values.put(Tracking.KEY_DispositivoId, tracking.DispositivoId);
        values.put(Tracking.KEY_FechaCelular, tracking.FechaCelular);
        values.put(Tracking.KEY_Latitud, tracking.Latitud);
        values.put(Tracking.KEY_Longitud, tracking.Longitud);
        values.put(Tracking.KEY_EstadoCoordenada, tracking.EstadoCoordenada);
        values.put(Tracking.KEY_OrigenCoordenada, tracking.OrigenCoordenada);
        values.put(Tracking.KEY_Velocidad, tracking.Velocidad);
        values.put(Tracking.KEY_Bateria, tracking.Bateria);
        values.put(Tracking.KEY_Precision, tracking.Precision);
        values.put(Tracking.KEY_SenialCelular, tracking.SenialCelular);
        values.put(Tracking.KEY_GpsHabilitado, tracking.GpsHabilitado);
        values.put(Tracking.KEY_WifiHabilitado, tracking.WifiHabilitado);
        values.put(Tracking.KEY_DatosHabilitado, tracking.DatosHabilitado);
        values.put(Tracking.KEY_ModeloEquipo, tracking.ModeloEquipo);
        values.put(Tracking.KEY_Imei, tracking.Imei);
        values.put(Tracking.KEY_VersionApp, tracking.VersionApp);
        values.put(Tracking.KEY_FechaEjecucionAlarm, tracking.FechaAlarma);
        values.put(Tracking.KEY_Time, tracking.Time);
        values.put(Tracking.KEY_ElapsedRealtimeNanos, tracking.ElapsedRealtimeNanos);
        values.put(Tracking.KEY_Altitude, tracking.Altitude);
        values.put(Tracking.KEY_Bearing, tracking.Bearing);
        values.put(Tracking.KEY_Extras, tracking.Extras);
        values.put(Tracking.KEY_Classx, tracking.Classx);
        values.put(Tracking.KEY_Actividad, tracking.Actividad);
        values.put(Tracking.KEY_Valido, tracking.Valido);
        values.put(Tracking.KEY_Intervalo, tracking.Intervalo);
        values.put(Tracking.KEY_EstadoEnvio, tracking.EstadoEnvio);

        long TrakingId = db.insert(Tracking.TABLE, null, values);
        db.close();
        return (int) TrakingId;
    }

    public int insertAll(Tracking tracking) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Tracking.KEY_Numero, tracking.Numero);
        values.put(Tracking.KEY_DispositivoId, tracking.DispositivoId);
        values.put(Tracking.KEY_FechaCelular, tracking.FechaCelular);
        values.put(Tracking.KEY_Latitud, tracking.Latitud);
        values.put(Tracking.KEY_Longitud, tracking.Longitud);
        values.put(Tracking.KEY_EstadoCoordenada, tracking.EstadoCoordenada);
        values.put(Tracking.KEY_OrigenCoordenada, tracking.OrigenCoordenada);
        values.put(Tracking.KEY_Velocidad, tracking.Velocidad);
        values.put(Tracking.KEY_Bateria, tracking.Bateria);
        values.put(Tracking.KEY_Precision, tracking.Precision);
        values.put(Tracking.KEY_SenialCelular, tracking.SenialCelular);
        values.put(Tracking.KEY_GpsHabilitado, tracking.GpsHabilitado);
        values.put(Tracking.KEY_WifiHabilitado, tracking.WifiHabilitado);
        values.put(Tracking.KEY_DatosHabilitado, tracking.DatosHabilitado);
        values.put(Tracking.KEY_ModeloEquipo, tracking.ModeloEquipo);
        values.put(Tracking.KEY_Imei, tracking.Imei);
        values.put(Tracking.KEY_VersionApp, tracking.VersionApp);
        values.put(Tracking.KEY_FechaEjecucionAlarm, tracking.FechaAlarma);
        values.put(Tracking.KEY_Time, tracking.Time);
        values.put(Tracking.KEY_ElapsedRealtimeNanos, tracking.ElapsedRealtimeNanos);
        values.put(Tracking.KEY_Altitude, tracking.Altitude);
        values.put(Tracking.KEY_Bearing, tracking.Bearing);
        values.put(Tracking.KEY_Extras, tracking.Extras);
        values.put(Tracking.KEY_Classx, tracking.Classx);
        values.put(Tracking.KEY_Actividad, tracking.Actividad);
        values.put(Tracking.KEY_Valido, tracking.Valido);
        values.put(Tracking.KEY_Intervalo, tracking.Intervalo);
        values.put(Tracking.KEY_FechaIso, tracking.FechaIso);

        long TrakingId = db.insert(Tracking.TABLE, null, values);
        db.close();
        return (int) TrakingId;
    }

    public void update(Tracking tracking) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Tracking.KEY_EstadoEnvio, tracking.EstadoEnvio);

        db.update(Tracking.TABLE, values, Tracking.KEY_ID + "=" + tracking.TrackingId, null);

        db.close();
    }

    public void updateSinConexion(Tracking tracking) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Tracking.KEY_EstadoEnvio, tracking.EstadoEnvio);

        db.update(Tracking.TABLE, values, Tracking.KEY_ID + "=" + tracking.TrackingId, null);
        db.close();
    }

}
