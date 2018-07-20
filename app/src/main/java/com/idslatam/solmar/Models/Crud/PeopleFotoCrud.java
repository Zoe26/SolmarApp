package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.PeopleFoto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PeopleFotoCrud {

    private DBHelper dbHelper;
    private String formatofecha = "yyyy-MM-dd HH:mm:ss";
    static final long ONE_MINUTE_IN_MILLIS=60000;//millisecs

    public PeopleFotoCrud(Context mContext) {
        dbHelper = new DBHelper(mContext);
    }

    public List<PeopleFoto> insertConValor(String codigoSincronizacion, String valor) {

        //OBTENER VALORES DEL PEOPLE:
        List<PeopleFoto> cargoFotos = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(PeopleFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
        values.put(PeopleFoto.KEY_TIPO_FOTO, "1");
        values.put(PeopleFoto.KEY_FILE_PATH, valor);
        values.put(PeopleFoto.KEY_INDICE, "0");

        String created = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
        values.put(PeopleFoto.KEY_CREATED, created);

        long PeopleFotoId = db.insert(PeopleFoto.TABLE_NAME, null, values);
        db.close();

        PeopleFoto cargoFoto_d = new PeopleFoto(PeopleFotoId,codigoSincronizacion,1,"0",valor);
        cargoFotos.add(cargoFoto_d);

        return cargoFotos;

    }

    public List<PeopleFoto> insertConVehiculo(String codigoSincronizacion, String vehiculo,String guantera, String maletera) {

        //OBTENER VALORES DEL PEOPLE:
        List<PeopleFoto> cargoFotos = new ArrayList<>();

        //Vehiculo
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(PeopleFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
        values.put(PeopleFoto.KEY_TIPO_FOTO, "2");
        values.put(PeopleFoto.KEY_FILE_PATH, vehiculo);
        values.put(PeopleFoto.KEY_INDICE, "0");

        String created = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
        values.put(PeopleFoto.KEY_CREATED, created);

        long PeopleFotoId = db.insert(PeopleFoto.TABLE_NAME, null, values);
        db.close();

        PeopleFoto cargoFoto_d = new PeopleFoto(PeopleFotoId,codigoSincronizacion,2,"0",vehiculo);
        cargoFotos.add(cargoFoto_d);

        //Guantera
        SQLiteDatabase dbg = dbHelper.getWritableDatabase();
        ContentValues valuesg = new ContentValues();

        valuesg.put(PeopleFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
        valuesg.put(PeopleFoto.KEY_TIPO_FOTO, "3");
        valuesg.put(PeopleFoto.KEY_FILE_PATH, vehiculo);
        valuesg.put(PeopleFoto.KEY_INDICE, "1");

        String createdg = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
        valuesg.put(PeopleFoto.KEY_CREATED, createdg);

        long PeopleFotoIdg = dbg.insert(PeopleFoto.TABLE_NAME, null, valuesg);
        dbg.close();

        PeopleFoto cargoFoto_g = new PeopleFoto(PeopleFotoIdg,codigoSincronizacion,3,"1",maletera);
        cargoFotos.add(cargoFoto_g);


        //Maletera
        SQLiteDatabase dbm = dbHelper.getWritableDatabase();
        ContentValues valuesm = new ContentValues();

        valuesm.put(PeopleFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
        valuesm.put(PeopleFoto.KEY_TIPO_FOTO, "4");
        valuesm.put(PeopleFoto.KEY_FILE_PATH, vehiculo);
        valuesm.put(PeopleFoto.KEY_INDICE, "1");

        String createdm = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
        valuesm.put(PeopleFoto.KEY_CREATED, createdm);

        long PeopleFotoIdm = dbm.insert(PeopleFoto.TABLE_NAME, null, valuesm);
        dbm.close();

        PeopleFoto cargoFoto_m = new PeopleFoto(PeopleFotoIdm,codigoSincronizacion,4,"2",guantera);
        cargoFotos.add(cargoFoto_m);

        return cargoFotos;

    }

    public List<PeopleFoto> insertAll(String codigoSincronizacion,String valor, String vehiculo,String guantera, String maletera) {

        //OBTENER VALORES DEL PEOPLE:
        List<PeopleFoto> cargoFotos = new ArrayList<>();

        //Valor
        SQLiteDatabase dbvl = dbHelper.getWritableDatabase();
        ContentValues valuesvl = new ContentValues();

        valuesvl.put(PeopleFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
        valuesvl.put(PeopleFoto.KEY_TIPO_FOTO, "1");
        valuesvl.put(PeopleFoto.KEY_FILE_PATH, vehiculo);
        valuesvl.put(PeopleFoto.KEY_INDICE, "0");

        String createdvl = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
        valuesvl.put(PeopleFoto.KEY_CREATED, createdvl);

        long PeopleFotoIdvl = dbvl.insert(PeopleFoto.TABLE_NAME, null, valuesvl);
        dbvl.close();

        PeopleFoto cargoFoto_vl = new PeopleFoto(PeopleFotoIdvl,codigoSincronizacion,1,"0",vehiculo);
        cargoFotos.add(cargoFoto_vl);

        //Vehiculo
        SQLiteDatabase dbv = dbHelper.getWritableDatabase();
        ContentValues valuesv = new ContentValues();

        valuesv.put(PeopleFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
        valuesv.put(PeopleFoto.KEY_TIPO_FOTO, "2");
        valuesv.put(PeopleFoto.KEY_FILE_PATH, vehiculo);
        valuesv.put(PeopleFoto.KEY_INDICE, "1");

        String createdv = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
        valuesv.put(PeopleFoto.KEY_CREATED, createdv);

        long PeopleFotoIdv = dbv.insert(PeopleFoto.TABLE_NAME, null, valuesv);
        dbv.close();

        PeopleFoto cargoFoto_v = new PeopleFoto(PeopleFotoIdv,codigoSincronizacion,2,"1",vehiculo);
        cargoFotos.add(cargoFoto_v);

        //Guantera
        SQLiteDatabase dbg = dbHelper.getWritableDatabase();
        ContentValues valuesg = new ContentValues();

        valuesg.put(PeopleFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
        valuesg.put(PeopleFoto.KEY_TIPO_FOTO, "3");
        valuesg.put(PeopleFoto.KEY_FILE_PATH, vehiculo);
        valuesg.put(PeopleFoto.KEY_INDICE, "2");

        String createdg = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
        valuesg.put(PeopleFoto.KEY_CREATED, createdg);

        long PeopleFotoIdg = dbg.insert(PeopleFoto.TABLE_NAME, null, valuesg);
        dbg.close();

        PeopleFoto cargoFoto_g = new PeopleFoto(PeopleFotoIdg,codigoSincronizacion,3,"2",maletera);
        cargoFotos.add(cargoFoto_g);


        //Maletera
        SQLiteDatabase dbm = dbHelper.getWritableDatabase();
        ContentValues valuesm = new ContentValues();

        valuesm.put(PeopleFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
        valuesm.put(PeopleFoto.KEY_TIPO_FOTO, "4");
        valuesm.put(PeopleFoto.KEY_FILE_PATH, vehiculo);
        valuesm.put(PeopleFoto.KEY_INDICE, "3");

        String createdm = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
        valuesm.put(PeopleFoto.KEY_CREATED, createdm);

        long PeopleFotoIdm = dbm.insert(PeopleFoto.TABLE_NAME, null, valuesm);
        dbm.close();

        PeopleFoto cargoFoto_m = new PeopleFoto(PeopleFotoIdm,codigoSincronizacion,4,"3",guantera);
        cargoFotos.add(cargoFoto_m);

        return cargoFotos;

    }

    public List<PeopleFoto> listFotosForSync(){
        List<PeopleFoto> peopleFotos = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Calendar date = Calendar.getInstance();
        long t= date.getTimeInMillis();
        Date afterAddingTenMins=new Date(t - (5 * ONE_MINUTE_IN_MILLIS));

        String createdq = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(afterAddingTenMins);

        String selectQuery = "SELECT peopleFotoId,codigoSincronizacion,tipoFoto,indice,filePath,created FROM PeopleFoto WHERE created <= '"+createdq+"' LIMIT 10";
        Cursor c = db.rawQuery(selectQuery, new String[]{});

        if (c.moveToFirst()) {

            do {
                Long cargoFotoId = c.getLong(c.getColumnIndex("peopleFotoId"));
                String codigoSincronizacion = c.getString(c.getColumnIndex("codigoSincronizacion"));
                int tipoFoto = c.getInt(c.getColumnIndex("tipoFoto"));
                String indice = c.getString(c.getColumnIndex("indice"));
                String filePath = c.getString(c.getColumnIndex("filePath"));
                String created = c.getString(c.getColumnIndex("created"));

                Log.e("created", created );

                PeopleFoto peopleFoto_p = new PeopleFoto(cargoFotoId,codigoSincronizacion,tipoFoto,indice,filePath);
                peopleFotos.add(peopleFoto_p);

            } while (c.moveToNext());

        }

        c.close();
        db.close();

        return  peopleFotos;
    }

    public void removePeopleFoto(PeopleFoto peopleFoto) {

        Log.e("Eliminar Foto",String.valueOf(peopleFoto.getPeopleFotoId()));
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(PeopleFoto.TABLE_NAME, PeopleFoto.KEY_ID + "=" +  String.valueOf(peopleFoto.getPeopleFotoId()), null);
    }
}
