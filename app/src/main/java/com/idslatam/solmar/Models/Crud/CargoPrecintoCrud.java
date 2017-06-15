package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.CargoPrecinto;

/**
 * Created by desarrollo03 on 6/13/17.
 */

public class CargoPrecintoCrud {

    private DBHelper dbHelper;

    public CargoPrecintoCrud(Context context) {

        dbHelper = new DBHelper(context);
    }
    public int insert(CargoPrecinto cargoPrecinto) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        //values.put(CargoPrecinto.KEY_indice, cargoPrecinto.Indice);
        values.put(CargoPrecinto.KEY_Foto, cargoPrecinto.Foto);

        long CargoId = db.insert(CargoPrecinto.TABLE_CARGO_PRECINTO, null, values);
        db.close();
        return (int) CargoId;

    }
}
