package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.CargoTipoFoto;

public class CargoTipoFotoCrud {

    private DBHelper dbHelper;

    public CargoTipoFotoCrud(Context context) {

        dbHelper = new DBHelper(context);
    }

    public int insert(CargoTipoFoto cargoTipoFoto) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(CargoTipoFoto.KEY_NOMBRE, cargoTipoFoto.getNombre());
        values.put(CargoTipoFoto.KEY_CLIENTE_CARGA_FOTO_ID, cargoTipoFoto.getClienteCargaFotoId());
        //values.put(CargoPrecinto.KEY_Foto, cargoPrecinto.Foto);

        long CargoId = db.insert(CargoTipoFoto.TABLE_NAME, null, values);
        db.close();
        return (int) CargoId;

    }

    public void deleteAll(){

        SQLiteDatabase sqldbDelete = dbHelper.getWritableDatabase();
        sqldbDelete.execSQL("DELETE FROM "+CargoTipoFoto.TABLE_NAME);
        sqldbDelete.close();
    }

    public int getFotosTomadas(){

        int candFotos = 0;
        SQLiteDatabase sqlite = dbHelper.getWritableDatabase();
        String selectQuery = "SELECT FilePath FROM CargoTipoFoto WHERE FilePath IS NOT NULL";
        Cursor ca = sqlite.rawQuery(selectQuery, new String[]{});
        candFotos = ca.getCount();
        ca.close();
        sqlite.close();

        return candFotos;
    }

    public int getFotosTotal(){

        int candFotos = 0;
        SQLiteDatabase sqlite = dbHelper.getWritableDatabase();
        String selectQuery = "SELECT FilePath FROM CargoTipoFoto";
        Cursor ca = sqlite.rawQuery(selectQuery, new String[]{});
        candFotos = ca.getCount();
        ca.close();
        sqlite.close();

        return candFotos;
    }

}
