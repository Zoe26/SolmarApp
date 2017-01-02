package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Alert;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Luis on 26/10/2016.
 */

public class AlertCrud {

    private DBHelper dbHelper;

    public AlertCrud(Context context) {

        dbHelper = new DBHelper(context);
    }
    public int insertS(Alert alert) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Alert.KEY_NumeroA, alert.NumeroA);
        values.put(Alert.KEY_FechaMarcacion, alert.FechaMarcacion);
        values.put(Alert.KEY_FechaEsperada, alert.FechaEsperada);
        values.put(Alert.KEY_FechaProxima, alert.FechaProxima);
        values.put(Alert.KEY_FlagTiempo, alert.FlagTiempo);
        values.put(Alert.KEY_MargenAceptado, alert.MargenAceptado);
        values.put(Alert.KEY_LatitudA, alert.LatitudA);
        values.put(Alert.KEY_LongitudA, alert.LongitudA);
        values.put(Alert.KEY_EstadoA, alert.EstadoA);
        values.put(Alert.KEY_EstadoBoton, alert.EstadoBoton);
        values.put(Alert.KEY_FechaEsperadaIso, alert.FechaEsperadaIso);
        values.put(Alert.KEY_FechaEsperadaIsoFin, alert.FechaEsperadaIsoFin);
        values.put(Alert.KEY_DispositivoId, alert.DispositivoId);
        values.put(Alert.KEY_CodigoEmpleado, alert.CodigoEmpleado);

        long AlertId = db.insert(Alert.TABLE_ALERT, null, values);
        db.close();
        return (int) AlertId;

    }

    public int insert(Alert alert) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Alert.KEY_NumeroA, alert.NumeroA);
        values.put(Alert.KEY_FechaMarcacion, alert.FechaMarcacion);
        values.put(Alert.KEY_FechaEsperada, alert.FechaEsperada);
        values.put(Alert.KEY_FechaProxima, alert.FechaProxima);
        values.put(Alert.KEY_FlagTiempo, alert.FlagTiempo);
        values.put(Alert.KEY_MargenAceptado, alert.MargenAceptado);
        //values.put(Alert.KEY_LatitudA, alert.LatitudA);
        //values.put(Alert.KEY_LongitudA, alert.LongitudA);
        values.put(Alert.KEY_EstadoA, alert.EstadoA);
        values.put(Alert.KEY_EstadoBoton, alert.EstadoBoton);
        values.put(Alert.KEY_FechaEsperadaIso, alert.FechaEsperadaIso);
        values.put(Alert.KEY_FechaEsperadaIsoFin, alert.FechaEsperadaIsoFin);
        values.put(Alert.KEY_DispositivoId, alert.DispositivoId);
        values.put(Alert.KEY_CodigoEmpleado, alert.CodigoEmpleado);
        values.put(Alert.KEY_FinTurno, alert.FinTurno);

        long AlertId = db.insert(Alert.TABLE_ALERT, null, values);
        db.close();
        return (int) AlertId;

    }

    public void update(Alert alert) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Alert.KEY_FechaMarcacion, alert.FechaMarcacion);
        //values.put(Alert.KEY_FechaEsperada, alert.FechaEsperada);
        //values.put(Alert.KEY_FechaProxima, alert.FechaProxima);
        values.put(Alert.KEY_FlagTiempo, alert.FlagTiempo);
        values.put(Alert.KEY_MargenAceptado, alert.MargenAceptado);
        values.put(Alert.KEY_EstadoBoton, alert.EstadoBoton);

        db.update(Alert.TABLE_ALERT, values, Alert.KEY_ID_ALERT + "=" + alert.AlertId, null);
        db.close();

    }

    public ArrayList<HashMap<String, String>> getAlertList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  " +
                Alert.KEY_ID_ALERT + "," +
                Alert.KEY_FechaMarcacion + "," +
                Alert.KEY_FechaEsperada + "," +
                Alert.KEY_FechaProxima + "," +
                Alert.KEY_FlagTiempo + "," +
                Alert.KEY_MargenAceptado +
                " FROM " + Alert.TABLE_ALERT
                + " ORDER BY " +
                Alert.KEY_ID_ALERT + " DESC ";

        ArrayList<HashMap<String, String>> alertList = new ArrayList<HashMap<String, String>>();


        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> alert = new HashMap<String, String>();
                alert.put("_AlerId", cursor.getString(cursor.getColumnIndex(Alert.KEY_ID_ALERT)));
                alert.put("_FechaMarcacion", cursor.getString(cursor.getColumnIndex(Alert.KEY_FechaMarcacion)));
                alert.put("_FechaEsperada", cursor.getString(cursor.getColumnIndex(Alert.KEY_FechaEsperada)));
                alert.put("_FechaProxima", cursor.getString(cursor.getColumnIndex(Alert.KEY_FechaProxima)));
                alert.put("_FlagTiempo", cursor.getString(cursor.getColumnIndex(Alert.KEY_FlagTiempo)));
                alert.put("_MargenAceptado", cursor.getString(cursor.getColumnIndex(Alert.KEY_MargenAceptado)));
                alertList.add(alert);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return alertList;
    }

    public void delete(int alert_Id) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(Alert.TABLE_ALERT, Alert.KEY_ID_ALERT + "=" + alert_Id, null);
        db.close();
    }

    public void updateMarcacion(Alert alert) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Alert.KEY_FechaMarcacion, alert.FechaMarcacion);
        values.put(Alert.KEY_FlagTiempo, alert.FlagTiempo);
        values.put(Alert.KEY_MargenAceptado, alert.MargenAceptado);

        db.update(Alert.TABLE_ALERT, values, Alert.KEY_ID_ALERT + "=" + alert.AlertId, null);
        db.close();

    }

    public void updateEstado(Alert alert) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Alert.KEY_EstadoA, alert.EstadoA);

        db.update(Alert.TABLE_ALERT, values, Alert.KEY_ID_ALERT + "=" + alert.AlertId, null);
        db.close();
    }

    public void updateEstadoReenvio(Alert alert) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Alert.KEY_EstadoA, alert.EstadoA);

        db.update(Alert.TABLE_ALERT, values, Alert.KEY_ID_ALERT + "=" + alert.AlertId, null);
        db.close();
    }
}
