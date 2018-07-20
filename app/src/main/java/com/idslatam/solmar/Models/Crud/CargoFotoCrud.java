package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.CargoFoto;
import com.idslatam.solmar.Models.Entities.DTO.Cargo.CargoPrecintoDBList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CargoFotoCrud {

    private DBHelper dbHelper;
    private String formatofecha = "yyyy-MM-dd HH:mm:ss";
    static final long ONE_MINUTE_IN_MILLIS=60000;//millisecs

    public CargoFotoCrud(Context context) {

        dbHelper = new DBHelper(context);
    }

    public List<CargoFoto> insertFotosSinCarga(String codigoSincronizacion, String delantera, String panoramica) {

        //OBTENER VALORES DEL CARGO:
        List<CargoFoto> cargoFotos = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(CargoFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
        values.put(CargoFoto.KEY_TIPO_FOTO, "1");
        values.put(CargoFoto.KEY_FILE_PATH, delantera);
        values.put(CargoFoto.KEY_INDICE, "0");

        String created = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
        values.put(CargoFoto.KEY_CREATED, created);

        long CargoFotoId = db.insert(CargoFoto.TABLE_NAME, null, values);
        db.close();

        CargoFoto cargoFoto_d = new CargoFoto(CargoFotoId,codigoSincronizacion,1,delantera,"0");
        cargoFotos.add(cargoFoto_d);

        SQLiteDatabase dbx = dbHelper.getWritableDatabase();
        ContentValues valuesx = new ContentValues();

        valuesx.put(CargoFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
        valuesx.put(CargoFoto.KEY_TIPO_FOTO, "3");
        valuesx.put(CargoFoto.KEY_FILE_PATH, panoramica);
        valuesx.put(CargoFoto.KEY_INDICE, "0");

        String createdp = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
        valuesx.put(CargoFoto.KEY_CREATED, createdp);

        long CargoFotoIdx = dbx.insert(CargoFoto.TABLE_NAME, null, valuesx);
        dbx.close();

        CargoFoto cargoFoto_p = new CargoFoto(CargoFotoIdx,codigoSincronizacion,3,panoramica,"0");
        cargoFotos.add(cargoFoto_p);

        return cargoFotos;

    }

    public List<CargoFoto> insertFotosCargaSuelta(String codigoSincronizacion, String delantera, String panoramica,String trasera) {

        //OBTENER VALORES DEL CARGO:
        List<CargoFoto> cargoFotos = new ArrayList<>();

        //Foto Delantera:
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(CargoFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
        values.put(CargoFoto.KEY_TIPO_FOTO, "1");
        values.put(CargoFoto.KEY_FILE_PATH, delantera);
        values.put(CargoFoto.KEY_INDICE, "0");

        String created = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
        values.put(CargoFoto.KEY_CREATED, created);

        long CargoFotoId = db.insert(CargoFoto.TABLE_NAME, null, values);
        db.close();

        CargoFoto cargoFoto_d = new CargoFoto(CargoFotoId,codigoSincronizacion,1,delantera,"0");
        cargoFotos.add(cargoFoto_d);

        //Foto Trasera:
        SQLiteDatabase dby = dbHelper.getWritableDatabase();
        ContentValues valuesy = new ContentValues();

        valuesy.put(CargoFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
        valuesy.put(CargoFoto.KEY_TIPO_FOTO, "2");
        valuesy.put(CargoFoto.KEY_FILE_PATH, trasera);
        valuesy.put(CargoFoto.KEY_INDICE, "0");

        String createdt = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
        valuesy.put(CargoFoto.KEY_CREATED, createdt);

        long CargoFotoIdy = dby.insert(CargoFoto.TABLE_NAME, null, valuesy);
        dby.close();

        CargoFoto cargoFoto_t = new CargoFoto(CargoFotoIdy,codigoSincronizacion,2,trasera,"0");
        cargoFotos.add(cargoFoto_t);


        //Foto Panorámica
        SQLiteDatabase dbx = dbHelper.getWritableDatabase();
        ContentValues valuesx = new ContentValues();

        valuesx.put(CargoFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
        valuesx.put(CargoFoto.KEY_TIPO_FOTO, "3");
        valuesx.put(CargoFoto.KEY_FILE_PATH, panoramica);
        valuesx.put(CargoFoto.KEY_INDICE, "0");

        String createdp = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
        valuesx.put(CargoFoto.KEY_CREATED, createdp);

        long CargoFotoIdx = dbx.insert(CargoFoto.TABLE_NAME, null, valuesx);
        dbx.close();

        CargoFoto cargoFoto_p = new CargoFoto(CargoFotoIdx,codigoSincronizacion,3,panoramica,"0");
        cargoFotos.add(cargoFoto_p);

        return cargoFotos;

    }

    public List<CargoFoto> insertFotosContenedorVacio(String codigoSincronizacion, String delantera, String panoramica,String trasera,List<CargoPrecintoDBList> precintos) {

        //OBTENER VALORES DEL CARGO:
        List<CargoFoto> cargoFotos = new ArrayList<>();

        //int i = 0;

        for (CargoPrecintoDBList precinto: precintos) {
            //Foto Panorámica
            SQLiteDatabase dbpr = dbHelper.getWritableDatabase();
            ContentValues valuespr = new ContentValues();

            valuespr.put(CargoFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
            valuespr.put(CargoFoto.KEY_TIPO_FOTO, "4");
            valuespr.put(CargoFoto.KEY_FILE_PATH, precinto.filePath);
            valuespr.put(CargoFoto.KEY_INDICE, String.valueOf(precinto.indice));

            String created = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
            valuespr.put(CargoFoto.KEY_CREATED, created);

            long CargoFotoIdpr = dbpr.insert(CargoFoto.TABLE_NAME, null, valuespr);
            dbpr.close();

            CargoFoto cargoFoto_pr = new CargoFoto(CargoFotoIdpr,codigoSincronizacion,4,precinto.filePath,String.valueOf(precinto.indice));
            cargoFotos.add(cargoFoto_pr);

            //i++;
        }

        //Foto Delantera:
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(CargoFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
        values.put(CargoFoto.KEY_TIPO_FOTO, "1");
        values.put(CargoFoto.KEY_FILE_PATH, delantera);
        values.put(CargoFoto.KEY_INDICE, "0");

        String created = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
        values.put(CargoFoto.KEY_CREATED, created);

        long CargoFotoId = db.insert(CargoFoto.TABLE_NAME, null, values);
        db.close();

        CargoFoto cargoFoto_d = new CargoFoto(CargoFotoId,codigoSincronizacion,1,delantera,"0");
        cargoFotos.add(cargoFoto_d);

        //Foto Trasera:
        SQLiteDatabase dby = dbHelper.getWritableDatabase();
        ContentValues valuesy = new ContentValues();

        valuesy.put(CargoFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
        valuesy.put(CargoFoto.KEY_TIPO_FOTO, "2");
        valuesy.put(CargoFoto.KEY_FILE_PATH, trasera);
        valuesy.put(CargoFoto.KEY_INDICE, "0");

        String createdy = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
        valuesy.put(CargoFoto.KEY_CREATED, createdy);

        long CargoFotoIdy = dby.insert(CargoFoto.TABLE_NAME, null, valuesy);
        dby.close();

        CargoFoto cargoFoto_t = new CargoFoto(CargoFotoIdy,codigoSincronizacion,2,trasera,"0");
        cargoFotos.add(cargoFoto_t);


        //Foto Panorámica
        SQLiteDatabase dbx = dbHelper.getWritableDatabase();
        ContentValues valuesx = new ContentValues();

        valuesx.put(CargoFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
        valuesx.put(CargoFoto.KEY_TIPO_FOTO, "5");
        valuesx.put(CargoFoto.KEY_FILE_PATH, panoramica);
        valuesx.put(CargoFoto.KEY_INDICE, "0");

        String createdpn = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
        valuesx.put(CargoFoto.KEY_CREATED, createdpn);

        long CargoFotoIdx = dbx.insert(CargoFoto.TABLE_NAME, null, valuesx);
        dbx.close();

        CargoFoto cargoFoto_p = new CargoFoto(CargoFotoIdx,codigoSincronizacion,5,panoramica,"0");
        cargoFotos.add(cargoFoto_p);



        return cargoFotos;

    }

    public List<CargoFoto> insertFotosContenedorLleno(String codigoSincronizacion, String delantera, String panoramica,String trasera,List<CargoPrecintoDBList> precintos) {

        //OBTENER VALORES DEL CARGO:
        List<CargoFoto> cargoFotos = new ArrayList<>();

        //int i = 0;

        for (CargoPrecintoDBList precinto: precintos) {
            //Foto Panorámica
            SQLiteDatabase dbpr = dbHelper.getWritableDatabase();
            ContentValues valuespr = new ContentValues();

            valuespr.put(CargoFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
            valuespr.put(CargoFoto.KEY_TIPO_FOTO, "4");
            valuespr.put(CargoFoto.KEY_FILE_PATH, precinto.filePath);
            valuespr.put(CargoFoto.KEY_INDICE, String.valueOf(precinto.indice));

            String created = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
            valuespr.put(CargoFoto.KEY_CREATED, created);

            long CargoFotoIdpr = dbpr.insert(CargoFoto.TABLE_NAME, null, valuespr);
            dbpr.close();

            CargoFoto cargoFoto_pr = new CargoFoto(CargoFotoIdpr,codigoSincronizacion,4,precinto.filePath,String.valueOf(precinto.indice));
            cargoFotos.add(cargoFoto_pr);

            //i++;
        }

        //Foto Delantera:
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(CargoFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
        values.put(CargoFoto.KEY_TIPO_FOTO, "1");
        values.put(CargoFoto.KEY_FILE_PATH, delantera);
        values.put(CargoFoto.KEY_INDICE, "0");

        String created = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
        values.put(CargoFoto.KEY_CREATED, created);

        long CargoFotoId = db.insert(CargoFoto.TABLE_NAME, null, values);
        db.close();

        CargoFoto cargoFoto_d = new CargoFoto(CargoFotoId,codigoSincronizacion,1,delantera,"0");
        cargoFotos.add(cargoFoto_d);

        //Foto Trasera:
        SQLiteDatabase dby = dbHelper.getWritableDatabase();
        ContentValues valuesy = new ContentValues();

        valuesy.put(CargoFoto.KEY_CODIGO_SINCRONIZACION, codigoSincronizacion);
        valuesy.put(CargoFoto.KEY_TIPO_FOTO, "2");
        valuesy.put(CargoFoto.KEY_FILE_PATH, trasera);
        valuesy.put(CargoFoto.KEY_INDICE, "0");

        String createdx = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());
        valuesy.put(CargoFoto.KEY_CREATED, createdx);

        long CargoFotoIdy = dby.insert(CargoFoto.TABLE_NAME, null, valuesy);
        dby.close();

        CargoFoto cargoFoto_t = new CargoFoto(CargoFotoIdy,codigoSincronizacion,2,trasera,"0");
        cargoFotos.add(cargoFoto_t);

        return cargoFotos;

    }

    public List<CargoFoto> listFotosForSync(){
        List<CargoFoto> cargoFotos = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Calendar date = Calendar.getInstance();
        long t= date.getTimeInMillis();
        Date afterAddingTenMins=new Date(t - (5 * ONE_MINUTE_IN_MILLIS));

        String createdq = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(afterAddingTenMins);

        String selectQuery = "SELECT cargoFotoId,codigoSincronizacion,tipoFoto,indice,filePath,created FROM CargoFoto WHERE created <= '"+createdq+"' LIMIT 10";
        Cursor c = db.rawQuery(selectQuery, new String[]{});


        if (c.moveToFirst()) {

            do {
                Long cargoFotoId = c.getLong(c.getColumnIndex("cargoFotoId"));
                String codigoSincronizacion = c.getString(c.getColumnIndex("codigoSincronizacion"));
                int tipoFoto = c.getInt(c.getColumnIndex("tipoFoto"));
                String indice = c.getString(c.getColumnIndex("indice"));
                String filePath = c.getString(c.getColumnIndex("filePath"));
                String created = c.getString(c.getColumnIndex("created"));

                Log.e("created", created );

                CargoFoto cargoFoto_p = new CargoFoto(cargoFotoId,codigoSincronizacion,tipoFoto,filePath,indice);
                cargoFotos.add(cargoFoto_p);

            } while (c.moveToNext());

        }

        c.close();
        db.close();


        return  cargoFotos;
    }
    // Deleting Employee
    public void removeCargoFoto(CargoFoto cargoFoto) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(CargoFoto.TABLE_NAME, CargoFoto.KEY_ID + "=" + cargoFoto.cargoFotoId, null);
    }
}
