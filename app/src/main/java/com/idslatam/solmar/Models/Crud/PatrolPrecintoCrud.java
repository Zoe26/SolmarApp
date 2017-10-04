package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.CargoPrecinto;
import com.idslatam.solmar.Models.Entities.PatrolPrecinto;

/**
 * Created by desarrollo03 on 6/13/17.
 */

public class PatrolPrecintoCrud {

    private DBHelper dbHelper;

    public PatrolPrecintoCrud(Context context) {

        dbHelper = new DBHelper(context);
    }
    public int insert(PatrolPrecinto patrolPrecinto) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(PatrolPrecinto.KEY_indice, patrolPrecinto.Indice);
        //values.put(PatrolPrecinto.KEY_Foto, patrolPrecinto.Foto);

        long PatrolPrecintoId = db.insert(PatrolPrecinto.TABLE_PATROL_PRECINTO, null, values);
        db.close();
        return (int) PatrolPrecintoId;

    }
}
