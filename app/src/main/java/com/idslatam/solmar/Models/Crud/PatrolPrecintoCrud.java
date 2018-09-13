package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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

    public void insert(PatrolPrecinto patrolPrecinto) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(PatrolPrecinto.KEY_indice, patrolPrecinto.Indice);
        values.put(PatrolPrecinto.KEY_ClienteMaterialFotoId, patrolPrecinto.ClienteMaterialFotoId);

        Log.e("CMFotoId id DB",patrolPrecinto.ClienteMaterialFotoId);


        db.insert(PatrolPrecinto.TABLE_PATROL_PRECINTO, null, values);
        db.close();
        //return (int) PatrolPrecintoId;
    }

    public void deleteAll(){

        SQLiteDatabase sqldbDelete = dbHelper.getWritableDatabase();
        sqldbDelete.execSQL("DELETE FROM PatrolPrecinto");
        sqldbDelete.close();
    }
}
