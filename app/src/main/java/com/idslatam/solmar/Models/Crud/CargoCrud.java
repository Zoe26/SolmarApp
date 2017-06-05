package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Cargo;
import com.idslatam.solmar.Models.Entities.Menu;

/**
 * Created by desarrollo03 on 6/4/17.
 */

public class CargoCrud {

    private DBHelper dbHelper;

    public CargoCrud(Context context) {

        dbHelper = new DBHelper(context);
    }
    public int insert(Cargo cargo) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Cargo.KEY_Initial, cargo.Initial);
        values.put(Cargo.KEY_TipoCarga, cargo.TipoCarga);
        values.put(Cargo.KEY_isLicencia, cargo.isLicencia);
        values.put(Cargo.KEY_isCarga, cargo.isCarga);
        values.put(Cargo.KEY_EppCasco, cargo.EppCasco);
        values.put(Cargo.KEY_EppChaleco, cargo.EppChaleco);
        values.put(Cargo.KEY_EppBotas, cargo.EppBotas);


        long CargoId = db.insert(Cargo.TABLE_CARGO, null, values);
        db.close();
        return (int) CargoId;

    }
}
