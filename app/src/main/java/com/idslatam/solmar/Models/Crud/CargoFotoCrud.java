package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.CargoFoto;
import com.idslatam.solmar.Models.Entities.DTO.Cargo.CargoPrecintoDBList;

import java.util.ArrayList;
import java.util.List;

public class CargoFotoCrud {

    private DBHelper dbHelper;

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

        long CargoFotoIdy = dby.insert(CargoFoto.TABLE_NAME, null, valuesy);
        dby.close();

        CargoFoto cargoFoto_t = new CargoFoto(CargoFotoIdy,codigoSincronizacion,2,trasera,"0");
        cargoFotos.add(cargoFoto_t);

        return cargoFotos;

    }

    // Deleting Employee
    public void removeCargoFoto(CargoFoto cargoFoto) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(CargoFoto.TABLE_NAME, CargoFoto.KEY_ID + "=" + cargoFoto.cargoFotoId, null);
    }
}
