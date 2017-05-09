package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Menu;

/**
 * Created by desarrollo03 on 5/2/17.
 */

public class MenuCrud {

    private DBHelper dbHelper;

    public MenuCrud(Context context) {

        dbHelper = new DBHelper(context);
    }
    public int insert(Menu menu) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Menu.KEY_Nombre, menu.Nombre);
        values.put(Menu.KEY_Code, menu.Code);

        long MenuId = db.insert(Menu.TABLE_MENU, null, values);
        db.close();
        return (int) MenuId;

    }
}
