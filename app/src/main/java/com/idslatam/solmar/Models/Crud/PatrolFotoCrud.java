package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.DTO.Patrol.PatrolPrecintoDBList;
import com.idslatam.solmar.Models.Entities.PatrolFoto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatrolFotoCrud {

    private DBHelper dbHelper;
    private String formatofecha = "yyyy-MM-dd HH:mm:ss";
    static final long ONE_MINUTE_IN_MILLIS=60000;

    public PatrolFotoCrud(Context context) {

        dbHelper = new DBHelper(context);
    }

    public List<PatrolFoto> insertFotosPatrol(String codigoSincronizacion, List<PatrolPrecintoDBList> precintos) {

        //OBTENER VALORES DEL CARGO:
        List<PatrolFoto> patrolFotos = new ArrayList<>();

        //int i = 0;

        for (PatrolPrecintoDBList precinto: precintos) {
            //Foto Panorámica
            SQLiteDatabase dbpr = dbHelper.getWritableDatabase();
            ContentValues valuespr = new ContentValues();

            valuespr.put(PatrolFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
            valuespr.put(PatrolFoto.KEY_FILE_PATH, precinto.filePath);
            valuespr.put(PatrolFoto.KEY_INDICE, String.valueOf(precinto.indice));

            String created = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
            valuespr.put(PatrolFoto.KEY_CREATED, created);

            long PatrolFotoIdpr = dbpr.insert(PatrolFoto.TABLE_NAME, null, valuespr);
            dbpr.close();

            PatrolFoto patrolFoto_pr = new PatrolFoto(PatrolFotoIdpr,codigoSincronizacion,String.valueOf(precinto.indice),precinto.filePath);
            patrolFotos.add(patrolFoto_pr);

            //i++;
        }

        return patrolFotos;

    }

    public List<PatrolFoto> listFotosForSync(){

        List<PatrolFoto> patrolFotos = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Calendar date = Calendar.getInstance();
        long t= date.getTimeInMillis();
        Date afterAddingTenMins=new Date(t - (5 * ONE_MINUTE_IN_MILLIS));

        String createdq = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(afterAddingTenMins);

        String selectQuery = "SELECT patrolFotoId,codigoSincronizacion,indice,filePath,created FROM PatrolFoto WHERE created <= '"+createdq+"' LIMIT 10";
        Cursor c = db.rawQuery(selectQuery, new String[]{});

        if (c.moveToFirst()) {

            do {
                Long patrolFotoId = c.getLong(c.getColumnIndex("patrolFotoId"));
                String codigoSincronizacion = c.getString(c.getColumnIndex("codigoSincronizacion"));
                String indice = c.getString(c.getColumnIndex("indice"));
                String filePath = c.getString(c.getColumnIndex("filePath"));
                String created = c.getString(c.getColumnIndex("created"));

                Log.e("created", created );

                PatrolFoto cargoFoto_p = new PatrolFoto(patrolFotoId ,codigoSincronizacion,indice,filePath);
                patrolFotos.add(cargoFoto_p);

            } while (c.moveToNext());

        }

        c.close();
        db.close();


        return  patrolFotos;
    }

    public void removePatrolFoto(PatrolFoto patrolFoto) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(PatrolFoto.TABLE_NAME, PatrolFoto.KEY_ID + "=" + patrolFoto.patrolFotoId, null);

    }
}
