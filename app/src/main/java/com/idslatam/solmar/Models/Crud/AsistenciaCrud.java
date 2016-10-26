package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Asistencia;

/**
 * Created by Luis on 26/10/2016.
 */

public class AsistenciaCrud {

    private DBHelper dbHelper;

    public AsistenciaCrud(Context context) {

        dbHelper = new DBHelper(context);
    }
    public int insert(Asistencia asistencia) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Asistencia.KEY_Numero, asistencia.NumeroAs);
        values.put(Asistencia.KEY_DispositivoId, asistencia.DispositivoId);
        values.put(Asistencia.KEY_FechaInicio, asistencia.FechaInicio);
        values.put(Asistencia.KEY_Fotocheck, asistencia.Fotocheck);

        long AsistenciaId = db.insert(Asistencia.TABLE_ASISTENCIA, null, values);
        db.close();
        return (int) AsistenciaId;

    }

    public void update(Asistencia asistencia) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Asistencia.KEY_Asistencia, asistencia.Asistencia);
        values.put(Asistencia.KEY_Fotocheck, asistencia.Fotocheck);

        db.update(Asistencia.TABLE_ASISTENCIA, values, Asistencia.KEY_ID_Asistencia + "=" + asistencia.AsistenciaId, null);
        db.close();

    }
}
