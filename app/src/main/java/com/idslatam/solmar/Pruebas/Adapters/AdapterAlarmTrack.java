package com.idslatam.solmar.Pruebas.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.idslatam.solmar.Pruebas.Data.DataAlarmTrack;
import com.idslatam.solmar.R;

import java.util.List;

/**
 * Created by Luis on 16/11/2016.
 */

public class AdapterAlarmTrack extends BaseAdapter {

    private Context mContext;
    private List<DataAlarmTrack> mDataAlarmTrackList;

    public AdapterAlarmTrack(Context mContext, List<DataAlarmTrack> mDataAlarmTrackList) {
        this.mContext = mContext;
        this.mDataAlarmTrackList = mDataAlarmTrackList;
    }

    @Override
    public int getCount() {
        return mDataAlarmTrackList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataAlarmTrackList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = View.inflate(mContext, R.layout.alarmtracking_items, null);
        TextView FechaCelular = (TextView)v.findViewById(R.id.alarm_Fecha);
        TextView EstadoEnvio = (TextView)v.findViewById(R.id.alarm_Estado);

        FechaCelular.setText(mDataAlarmTrackList.get(position).getFechaAlarm());
        EstadoEnvio.setText(mDataAlarmTrackList.get(position).getEstado());

        v.setTag(mDataAlarmTrackList.get(position).getAlarmTrackId());

        return v;
    }
}
