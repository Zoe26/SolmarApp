package com.idslatam.solmar.Pruebas.Adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.idslatam.solmar.Pruebas.Data.DataTracking;

import java.util.List;

import com.idslatam.solmar.R;

/**
 * Created by Luis on 01/11/2016.
 */

public class AdapterTracking extends BaseAdapter {

    private Context mContext;
    private List<DataTracking> mDataTrackingList;

    public AdapterTracking(Context mContext, List<DataTracking> mDataTrackingList) {
        this.mContext = mContext;
        this.mDataTrackingList = mDataTrackingList;
    }


    @Override
    public int getCount() {
        return mDataTrackingList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataTrackingList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = View.inflate(mContext, R.layout.tracking_items, null);
        TextView FechaCelular = (TextView)v.findViewById(R.id.tracking_lv_FechaCelular);
        TextView Latitud = (TextView)v.findViewById(R.id.tracking_lv_Latitud);
        TextView Longitud = (TextView)v.findViewById(R.id.tracking_lv_Longitud);
        TextView Velocidad = (TextView)v.findViewById(R.id.tracking_lv_Velocidadd);
        TextView Bateria = (TextView)v.findViewById(R.id.tracking_lv_Bateria);
        TextView Precision = (TextView)v.findViewById(R.id.tracking_lv_Precision);
        TextView GpsHabilitado = (TextView)v.findViewById(R.id.tracking_lv_GpsHabilitado);
        TextView WifiHabilitado = (TextView)v.findViewById(R.id.tracking_lv_WifiHabilitado);
        TextView DatosHabilitado = (TextView)v.findViewById(R.id.tracking_lv_DatosHabilitado);
        TextView FechaAlarma = (TextView)v.findViewById(R.id.tracking_lv_FechaAlarma);
        TextView Time = (TextView)v.findViewById(R.id.tracking_lv_Time);
        TextView ElapsedRealtimeNanos = (TextView)v.findViewById(R.id.tracking_lv_ElapsedRealtimeNanos);
        TextView Altitude = (TextView)v.findViewById(R.id.tracking_lv_Altitude);
        TextView Bearing = (TextView)v.findViewById(R.id.tracking_lv_Bearing);
        TextView Actividad = (TextView)v.findViewById(R.id.tracking_lv_Actividad);
        TextView Valido = (TextView)v.findViewById(R.id.tracking_lv_Valido);
        TextView Intervalo = (TextView)v.findViewById(R.id.tracking_lv_Intervalo);
        TextView EstadoEnvio = (TextView)v.findViewById(R.id.tracking_lv_EstadoEnvio);

        FechaCelular.setText(mDataTrackingList.get(position).getFechaCelular());
        Latitud.setText(mDataTrackingList.get(position).getLatitud());
        Longitud.setText(mDataTrackingList.get(position).getLongitud());
        Velocidad.setText(mDataTrackingList.get(position).getVelocidad());
        Bateria.setText(mDataTrackingList.get(position).getBateria());
        Precision.setText(mDataTrackingList.get(position).getPrecision());
        GpsHabilitado.setText(mDataTrackingList.get(position).getGpsHabilitado());
        WifiHabilitado.setText(mDataTrackingList.get(position).getWifiHabilitado());
        DatosHabilitado.setText(mDataTrackingList.get(position).getDatosHabilitado());
        FechaAlarma.setText(mDataTrackingList.get(position).getFechaAlarma());
        Time.setText(mDataTrackingList.get(position).getTime());
        ElapsedRealtimeNanos.setText(mDataTrackingList.get(position).getElapsedRealtimeNanos());
        Altitude.setText(mDataTrackingList.get(position).getAltitude());
        Bearing.setText(mDataTrackingList.get(position).getBearing());
        Actividad.setText(mDataTrackingList.get(position).getActividad());
        Valido.setText(mDataTrackingList.get(position).getValido());
        Intervalo.setText(mDataTrackingList.get(position).getIntervalo());
        EstadoEnvio.setText(mDataTrackingList.get(position).getEstadoEnvio());

        v.setTag(mDataTrackingList.get(position).getTrackingId());

        return v;
    }
}