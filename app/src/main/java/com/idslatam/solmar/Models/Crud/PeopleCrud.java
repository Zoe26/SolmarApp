package com.idslatam.solmar.Models.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.PatrolPrecinto;
import com.idslatam.solmar.Models.Entities.People;

/**
 * Created by desarrollo03 on 6/13/17.
 */

public class PeopleCrud {

    private DBHelper dbHelper;

    public PeopleCrud(Context context) {

        dbHelper = new DBHelper(context);
    }
    public int insert(People people) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(People.KEY_Initial, people.Initial);

        long PeopleId = db.insert(People.TABLE_PEOPLE, null, values);
        db.close();
        return (int) PeopleId;

    }
}
