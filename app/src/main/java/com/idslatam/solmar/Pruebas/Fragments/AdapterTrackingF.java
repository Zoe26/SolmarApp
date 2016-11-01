package com.idslatam.solmar.Pruebas.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Pruebas.Adapters.AdapterTracking;
import com.idslatam.solmar.Pruebas.Data.DataTracking;
import com.idslatam.solmar.R;
import com.idslatam.solmar.View.Fragments.HomeFragment;

import java.util.ArrayList;
import java.util.List;

public class AdapterTrackingF extends android.app.Fragment implements  View.OnClickListener{

    Context thiscontext;
    View myView;

    int _Tracking_Id =0;

    private ListView lvDatost;
    private AdapterTracking adapter;
    private List<DataTracking> mDataTracking;

    public static AdapterTrackingF newInstance(String text) {
        Bundle args = new Bundle();
        args.putString("", text);

        AdapterTrackingF imageFragment = new AdapterTrackingF();
        imageFragment.setArguments(args);

        return imageFragment;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        thiscontext = container.getContext();

        myView = inflater.inflate(R.layout.tracking_list_view, container, false);

        findViewsById(myView);

        mDataTracking = new ArrayList<>();

        String FechaCelular, Latitud, Longitud, Velocidad, Bateria, Precision, GpsHabilitado, WifiHabilitado,
        DatosHabilitado, FechaAlarma, Time, ElapsedRealtimeNanos, Altitude, Bearing, Actividad, Valido, Intervalo, EstadoEnvio;


        try {

            DBHelper dataBaseHelper = new DBHelper(thiscontext);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            String selectQuery = "SELECT TrackingId, FechaCelular, Latitud, Longitud, Velocidad, Bateria, Precision, GpsHabilitado" +
                    ", WifiHabilitado, DatosHabilitado, FechaAlarma, Time, ElapsedRealtimeNanos, Altitude, Bearing, Actividad, Valido" +
                    ", Intervalo, EstadoEnvio  FROM Tracking ORDER BY ElapsedRealtimeNanos DESC";
            Cursor c = db.rawQuery(selectQuery, new String[]{});

            //Nos aseguramos de que existe al menos un registro
            if (c.moveToFirst()) {
                //Recorremos el cursor hasta que no haya más registros
                do {
                    _Tracking_Id = c.getInt(c.getColumnIndex("TrackingId"));

                    FechaCelular = c.getString(c.getColumnIndex("FechaCelular"));
                    Latitud = c.getString(c.getColumnIndex("Latitud"));
                    Longitud = c.getString(c.getColumnIndex("Longitud"));
                    Velocidad = c.getString(c.getColumnIndex("Velocidad"));
                    Bateria = c.getString(c.getColumnIndex("Bateria"));
                    Precision = c.getString(c.getColumnIndex("Precision"));
                    GpsHabilitado = c.getString(c.getColumnIndex("GpsHabilitado"));
                    WifiHabilitado = c.getString(c.getColumnIndex("WifiHabilitado"));
                    DatosHabilitado = c.getString(c.getColumnIndex("DatosHabilitado"));
                    FechaAlarma = c.getString(c.getColumnIndex("FechaAlarma"));
                    Time = c.getString(c.getColumnIndex("Time"));
                    ElapsedRealtimeNanos = c.getString(c.getColumnIndex("ElapsedRealtimeNanos"));
                    Altitude = c.getString(c.getColumnIndex("Altitude"));
                    Bearing = c.getString(c.getColumnIndex("Bearing"));
                    Actividad = c.getString(c.getColumnIndex("Actividad"));
                    Valido = c.getString(c.getColumnIndex("Valido"));
                    Intervalo = c.getString(c.getColumnIndex("Intervalo"));
                    EstadoEnvio = c.getString(c.getColumnIndex("EstadoEnvio"));

                    mDataTracking.add(new DataTracking(_Tracking_Id, "FechaCelular: "+ FechaCelular, "Latitud: "+ Latitud, "Longitud: "+Longitud,
                            "Velocidad (m/s): "+Velocidad, "Bateria: "+Bateria, "Precision: "+Precision, "GpsHabilitado: "+GpsHabilitado, "WifiHabilitado: "+WifiHabilitado,
                            "DatosHabilitado: "+DatosHabilitado, "FechaAlarma: "+FechaAlarma, "Time: "+Time, "ElapsedRealtimeNanos: "+ElapsedRealtimeNanos, "Altitude: "+Altitude,
                            "Bearing: "+ Bearing, "Actividad: "+ Actividad, "Valido: "+ Valido, "Intervalo: "+ Intervalo, "EstadoEnvio: "+EstadoEnvio));

                } while(c.moveToNext());
            }

        }catch (Exception e){}


        adapter = new AdapterTracking(thiscontext, mDataTracking);
        lvDatost.setAdapter(adapter);

        return myView;
    }

    private void findViewsById(View myView) {
        lvDatost = (ListView) myView.findViewById(R.id.tracking_ListView);
    }

    public void onClick(View v) {

    }

}

