package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.CargoPrecinto;
import com.idslatam.solmar.Models.Entities.PatrolContenedor;
import com.idslatam.solmar.Models.Entities.PatrolPrecinto;

/**
 * Created by desarrollo03 on 6/13/17.
 */

public class PatrolContenedorCrud {

    private DBHelper dbHelper;

    public PatrolContenedorCrud(Context context) {

        dbHelper = new DBHelper(context);
    }
    public int insert(PatrolContenedor patrolContenedor) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(PatrolContenedor.KEY_ContenedorId, patrolContenedor.ContenedorId);
        values.put(PatrolContenedor.KEY_Codigo, patrolContenedor.Codigo);
        values.put(PatrolContenedor.KEY_ClienteMaterialId, patrolContenedor.ClienteMaterialId);

        long PatrolContenedorId = db.insert(PatrolContenedor.TABLE_PATROL_CONTENEDOR, null, values);
        db.close();
        return (int) PatrolContenedorId;

    }
}
