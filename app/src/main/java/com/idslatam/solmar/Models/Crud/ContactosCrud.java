package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Alert;
import com.idslatam.solmar.Models.Entities.Contactos;

/**
 * Created by desarrollo03 on 3/20/17.
 */

public class ContactosCrud {

    private DBHelper dbHelper;

    public ContactosCrud(Context context) {

        dbHelper = new DBHelper(context);
    }
    public int insert(Contactos contactos) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Contactos.KEY_Nombre, contactos.Nombre);
        values.put(Contactos.KEY_PrimerNumero, contactos.PrimerNumero);
        values.put(Contactos.KEY_SegundoNumero, contactos.SegundoNumero);

        long ContactosId = db.insert(Contactos.TABLE_CONTACTOS, null, values);
        db.close();
        return (int) ContactosId;

    }
}
