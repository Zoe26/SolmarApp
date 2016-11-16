package com.idslatam.solmar.Pruebas.Crud;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Pruebas.Entities.AlarmTrack;

/**
 * Created by Luis on 16/11/2016.
 */

public class AlarmTrackCrud {

    private DBHelper dbHelper;

    public AlarmTrackCrud(Context context) {
        dbHelper = new DBHelper(context);
    }

    public int insert(AlarmTrack alarmTrack) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AlarmTrack.KEY_FechaAlarm, alarmTrack.FechaAlarm);
        values.put(AlarmTrack.KEY_Estado, alarmTrack.Estado);

        long AlarmTrackId = db.insert(AlarmTrack.TABLE_ALARM_TRACK, null, values);
        db.close();
        return (int) AlarmTrackId;
    }

}
