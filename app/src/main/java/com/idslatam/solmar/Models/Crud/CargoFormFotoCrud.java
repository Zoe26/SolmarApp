package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.CargoFormFoto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CargoFormFotoCrud {
    private DBHelper dbHelper;
    private String formatofecha = "yyyy-MM-dd HH:mm:ss";
    static final long ONE_MINUTE_IN_MILLIS=60000;//millisecs

    public CargoFormFotoCrud(Context context) {

        dbHelper = new DBHelper(context);
    }

    public List<CargoFormFoto> getListForServer(String codigoSincronizacion){
        List<CargoFormFoto> lista = new ArrayList<>();

        //Precintos
        try {

            int ipr = 1;
            SQLiteDatabase dbst = dbHelper.getReadableDatabase();
            String selectQuery = "SELECT Foto FROM CargoPrecinto";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {

                do {
                    String foto = c.getString(c.getColumnIndex("Foto"));
                    String indice = String.valueOf(ipr);
                    CargoFormFoto _element = new CargoFormFoto(0,codigoSincronizacion,null,indice,foto);
                    long id = insertPrecinto(_element);
                    _element.setCargoFormFotoId(id);
                    lista.add(_element);
                    ipr++;
                } while (c.moveToNext());

            }

            c.close();
            dbst.close();


        } catch (Exception e) {}

        //Fotos Tipo
        try {

            int itf = 1;
            SQLiteDatabase dbst = dbHelper.getReadableDatabase();
            String selectQuery = "SELECT FilePath,ClienteCargaFotoId FROM CargoTipoFoto";
            Cursor c = dbst.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {

                do {
                    String foto = c.getString(c.getColumnIndex("FilePath"));
                    String clienteCargaFotoId = c.getString(c.getColumnIndex("ClienteCargaFotoId"));
                    String indice = String.valueOf(itf);
                    CargoFormFoto _element = new CargoFormFoto(0,codigoSincronizacion,clienteCargaFotoId,indice,foto);
                    long id = insertTipoFoto(_element);
                    _element.setCargoFormFotoId(id);
                    lista.add(_element);
                    itf++;
                } while (c.moveToNext());

            }

            c.close();
            dbst.close();


        } catch (Exception e) {}

        return lista;
    }

    public long insertPrecinto(CargoFormFoto element){

        SQLiteDatabase dbpr = dbHelper.getWritableDatabase();
        ContentValues valuespr = new ContentValues();

        String created = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());

        valuespr.put(CargoFormFoto.KEY_CODIGO_SINCRONIZACION, element.codigoSincronizacion);
        //valuespr.put(CargoFormFoto.KEY_TIPO_FOTO, "4");
        valuespr.put(CargoFormFoto.KEY_FILE_PATH, element.filePath);
        valuespr.put(CargoFormFoto.KEY_INDICE, element.indice);
        valuespr.put(CargoFormFoto.KEY_CREATED, created);

        long CargoFotoIdpr = dbpr.insert(CargoFormFoto.TABLE_NAME, null, valuespr);
        dbpr.close();

        return CargoFotoIdpr;

    }

    public long insertTipoFoto(CargoFormFoto element){

        SQLiteDatabase dbpr = dbHelper.getWritableDatabase();
        ContentValues valuespr = new ContentValues();

        String created = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(new Date());

        valuespr.put(CargoFormFoto.KEY_CODIGO_SINCRONIZACION, element.codigoSincronizacion);
        valuespr.put(CargoFormFoto.KEY_TIPO_FOTO, element.clienteCargaFotoId);
        valuespr.put(CargoFormFoto.KEY_FILE_PATH, element.filePath);
        valuespr.put(CargoFormFoto.KEY_INDICE, element.indice);
        valuespr.put(CargoFormFoto.KEY_CREATED, created);

        long CargoFotoIdpr = dbpr.insert(CargoFormFoto.TABLE_NAME, null, valuespr);
        dbpr.close();

        return CargoFotoIdpr;
    }

    public void removeCargoFoto(CargoFormFoto cargoFormFoto) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(CargoFormFoto.TABLE_NAME, CargoFormFoto.KEY_ID + "=" + cargoFormFoto.cargoFormFotoId, null);
    }

    public List<CargoFormFoto> listFotosForSync(){
        List<CargoFormFoto> cargoFotos = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Calendar date = Calendar.getInstance();
        long t= date.getTimeInMillis();
        Date afterAddingTenMins=new Date(t - (5 * ONE_MINUTE_IN_MILLIS));

        String createdq = new SimpleDateFormat(formatofecha, Locale.getDefault()).format(afterAddingTenMins);

        //clienteCargaFotoId
        String selectQuery = "SELECT cargoFormFotoId,codigoSincronizacion,clienteCargaFotoId,indice,filePath,created FROM CargoFormFoto WHERE created <= '"+createdq+"' LIMIT 10";
        Cursor c = db.rawQuery(selectQuery, new String[]{});

        if (c.moveToFirst()) {

            do {
                Long cargoFotoId = c.getLong(c.getColumnIndex("cargoFormFotoId"));
                String codigoSincronizacion = c.getString(c.getColumnIndex("codigoSincronizacion"));
                String clienteCargaFotoId = c.getString(c.getColumnIndex("clienteCargaFotoId"));
                //int tipoFoto = c.getInt(c.getColumnIndex("tipoFoto"));
                String indice = c.getString(c.getColumnIndex("indice"));
                String filePath = c.getString(c.getColumnIndex("filePath"));
                String created = c.getString(c.getColumnIndex("created"));

                //Log.e("created", created );

                CargoFormFoto cargoFormFoto_p = new CargoFormFoto(cargoFotoId,codigoSincronizacion,clienteCargaFotoId,indice,filePath);
                cargoFotos.add(cargoFormFoto_p);

            } while (c.moveToNext());

        }

        c.close();
        db.close();


        return  cargoFotos;
    }

}
