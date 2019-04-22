package com.idslatam.solmar.People.Motivo;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.idslatam.solmar.Models.Entities.DTO.People.Motivo.PeopleMotivoItemDTO;

import java.util.ArrayList;

public class PeopleMotivoCustomPagerAdapter extends ArrayAdapter<PeopleMotivoItemDTO> {
    private Context mContext;
    ArrayList<PeopleMotivoItemDTO> motivos;
    private int selectedPosition = -1;

    public PeopleMotivoCustomPagerAdapter(Context context,int TextViewResourceId, ArrayList<PeopleMotivoItemDTO> _motivos) {

        super(context, TextViewResourceId, _motivos);
        this.mContext = context;
        this.motivos = _motivos;

    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Nullable
    @Override
    public PeopleMotivoItemDTO getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getPosition(@Nullable PeopleMotivoItemDTO item) {
        return super.getPosition(item);
    }

    public void removeSelection(){

        selectedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        PeopleMotivoItemDTO motivoItem = getItem(position);
        // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        // Then you can get the current item using the values array (Users array) and the current position
        // You can NOW reference each method you has created in your bean object (User class)
        label.setText(motivoItem.getNombre());

        // And finally return your dynamic (or custom) view for each spinner item
        return label;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        PeopleMotivoItemDTO motivoItem = getItem(position);

        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(motivoItem.getNombre());

        return label;
    }
}
