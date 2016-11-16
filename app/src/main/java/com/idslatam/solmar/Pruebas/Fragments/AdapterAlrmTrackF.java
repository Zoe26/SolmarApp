package com.idslatam.solmar.Pruebas.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Pruebas.Adapters.AdapterAlarmTrack;
import com.idslatam.solmar.Pruebas.Data.DataAlarmTrack;
import com.idslatam.solmar.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luis on 16/11/2016.
 */

public class AdapterAlrmTrackF extends android.app.Fragment {

    Context thiscontext;
    View myView;

    int _AlarmTrack_Id =0;

    private ListView lvDatosAlrms;
    private AdapterAlarmTrack adapter;
    private List<DataAlarmTrack> mDataAlarmTrack;

    protected SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss"),
            formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static AdapterAlrmTrackF newInstance(String text) {
        Bundle args = new Bundle();
        args.putString("", text);

        AdapterAlrmTrackF imageFragment = new AdapterAlrmTrackF();
        imageFragment.setArguments(args);

        return imageFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        thiscontext = container.getContext();
        myView = inflater.inflate(R.layout.alarmtracking_list_view, container, false);
        findViewsById(myView);

        mDataAlarmTrack = new ArrayList<>();

        String Fecha, Estado;

        try {

            DBHelper dataBaseHelper = new DBHelper(thiscontext);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

            String selectQuery = "SELECT AlarmTrackId, FechaAlarm, Estado FROM AlarmTrack ORDER BY FechaAlarm DESC";

            Cursor c = db.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {

                do {
                    _AlarmTrack_Id = c.getInt(c.getColumnIndex("AlarmTrackId"));

                    Fecha = c.getString(c.getColumnIndex("FechaAlarm"));
                    Estado = c.getString(c.getColumnIndex("Estado"));

                    Log.e("FILTRO ", Fecha);

                    mDataAlarmTrack.add(new DataAlarmTrack(_AlarmTrack_Id, "Fecha: "+ Fecha, "Estado: "+ Estado));

                } while(c.moveToNext());
            }

        }catch (Exception e){
            Log.e("EXCPTIO", e.toString());
        }


        Log.e("EXCPTIO", "LAST");

        adapter = new AdapterAlarmTrack(thiscontext, mDataAlarmTrack);
        lvDatosAlrms.setAdapter(adapter);

        return myView;
    }

    private void findViewsById(View myView) {

        lvDatosAlrms = (ListView) myView.findViewById(R.id.alarmtrack_ListView);
    }
}
