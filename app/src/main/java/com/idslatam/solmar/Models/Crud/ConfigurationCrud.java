package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Configuration;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Luis on 22/10/2016.
 */

public class ConfigurationCrud {

    private DBHelper dbHelper;

    public ConfigurationCrud(Context context) {

        dbHelper = new DBHelper(context);
    }
    public int insert(Configuration configuration) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Configuration.KEY_GuidDipositivo, configuration.GuidDispositivo);
        values.put(Configuration.KEY_EstaAcceso, configuration.EstaAcceso);
        values.put(Configuration.KEY_NumeroCel, configuration.NumeroCel);

        long ConfigurationId = db.insert(Configuration.TABLE_CONFIGURATION, null, values);
        db.close();
        return (int) ConfigurationId;

    }

    public void update(Configuration configuration) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Configuration.KEY_EstaAcceso, configuration.EstaAcceso);
        values.put(Configuration.KEY_GuidDipositivo, configuration.GuidDispositivo);
        values.put(Configuration.KEY_IntervaloTrackingSinConex, configuration.IntervaloTrackingSinConex);
        values.put(Configuration.KEY_IntervaloTracking, configuration.IntervaloTracking);

        db.update(Configuration.TABLE_CONFIGURATION, values, Configuration.KEY_ID_CONFIGURATION + "=" + configuration.ConfigurationId, null);
        db.close();

    }

    public ArrayList<HashMap<String, String>> getConfigurationList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT  " +
                Configuration.KEY_ID_CONFIGURATION + "," +
                Configuration.KEY_NumeroCel + "," +
                Configuration.KEY_EstaAcceso + "," +
                Configuration.KEY_GuidDipositivo +
                " FROM " + Configuration.TABLE_CONFIGURATION
                + " LIMIT 1;" ;

        ArrayList<HashMap<String, String>> configurationList = new ArrayList<HashMap<String, String>>();

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> configuration = new HashMap<String, String>();
                configuration.put("id", cursor.getString(cursor.getColumnIndex(Configuration.KEY_ID_CONFIGURATION)));
                configuration.put("numero", cursor.getString(cursor.getColumnIndex(Configuration.KEY_NumeroCel)));
                configuration.put("estadoAcc", cursor.getString(cursor.getColumnIndex(Configuration.KEY_EstaAcceso)));
                configuration.put("guidCelo", cursor.getString(cursor.getColumnIndex(Configuration.KEY_GuidDipositivo)));
                configurationList.add(configuration);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return configurationList;
    }

    public void delete(int configuration_Id) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(Configuration.TABLE_CONFIGURATION, Configuration.KEY_ID_CONFIGURATION + "=" + configuration_Id, null);
        db.close();
    }

    public void updateNumero(Configuration configuration) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Configuration.KEY_NumeroCel, configuration.NumeroCel);

        db.update(Configuration.TABLE_CONFIGURATION, values, Configuration.KEY_ID_CONFIGURATION + "=" + configuration.ConfigurationId, null);
        db.close();

    }

    public void updateToken(Configuration configuration) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Configuration.KEY_Token, configuration.Token);
        values.put(Configuration.KEY_CodigoEmpleado, configuration.CodigoEmpleado);

        db.update(Configuration.TABLE_CONFIGURATION, values, Configuration.KEY_ID_CONFIGURATION + "=" + configuration.ConfigurationId, null);
        db.close();

    }


    public void updateReistalacion(Configuration configuration) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Configuration.KEY_GuidDipositivo, configuration.GuidDispositivo);
        values.put(Configuration.KEY_NumeroCel, configuration.NumeroCel);

        db.update(Configuration.TABLE_CONFIGURATION, values, Configuration.KEY_ID_CONFIGURATION + "=" + configuration.ConfigurationId, null);
        db.close();

    }

    public void updateAsistencia(Configuration configuration) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Configuration.KEY_AsistenciaId, configuration.AsistenciaId);
        //values.put(Configuration.KEY_CodigoEmpleado, configuration.CodigoEmpleado);

        db.update(Configuration.TABLE_CONFIGURATION, values, Configuration.KEY_ID_CONFIGURATION + "=" + configuration.ConfigurationId, null);
        db.close();

    }

    public void updateIntervaloTracking(Configuration configuration) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Configuration.KEY_IntervaloTracking, configuration.IntervaloTracking);

        db.update(Configuration.TABLE_CONFIGURATION, values, Configuration.KEY_ID_CONFIGURATION + "=" + configuration.ConfigurationId, null);
        db.close();

    }

    public void updateIntervaloTrackingSinConex(Configuration configuration) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Configuration.KEY_IntervaloTrackingSinConex, configuration.IntervaloTrackingSinConex);

        db.update(Configuration.TABLE_CONFIGURATION, values, Configuration.KEY_ID_CONFIGURATION + "=" + configuration.ConfigurationId, null);
        db.close();

    }

    public void updateIntervaloMarcacion(Configuration configuration) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Configuration.KEY_IntervaloMarcacion, configuration.IntervaloMarcacion);

        db.update(Configuration.TABLE_CONFIGURATION, values, Configuration.KEY_ID_CONFIGURATION + "=" + configuration.ConfigurationId, null);
        db.close();

    }

    public void updateIntervaloMarcacionTolerancia(Configuration configuration) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Configuration.KEY_IntervaloMarcacionTolerancia, configuration.IntervaloMarcacionTolerancia);

        db.update(Configuration.TABLE_CONFIGURATION, values, Configuration.KEY_ID_CONFIGURATION + "=" + configuration.ConfigurationId, null);
        db.close();

    }

    public void updateIntervaloAlertInicial(Configuration configuration) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Configuration.KEY_IntervaloMarcacion, configuration.IntervaloMarcacion);
        values.put(Configuration.KEY_IntervaloMarcacionTolerancia, configuration.IntervaloMarcacionTolerancia);

        db.update(Configuration.TABLE_CONFIGURATION, values, Configuration.KEY_ID_CONFIGURATION + "=" + configuration.ConfigurationId, null);
        db.close();

    }
}

