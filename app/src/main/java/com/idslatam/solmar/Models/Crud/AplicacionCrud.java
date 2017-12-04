package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Aplicaciones;

/**
 * Created by desarrollo03 on 8/28/17.
 */

public class AplicacionCrud {

    private DBHelper dbHelper;

    public AplicacionCrud(Context context) {

        dbHelper = new DBHelper(context);
    }

    public int insert(Aplicaciones aplicaciones) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Aplicaciones.KEY_Nombre, aplicaciones.Nombre);

        long AplicacionesId = db.insert(Aplicaciones.TABLE_APLICACIONES , null, values);
        db.close();
        return (int) AplicacionesId;

    }

}
