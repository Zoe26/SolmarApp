package com.idslatam.solmar.People.AreaAcceso;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.idslatam.solmar.Models.Crud.CargoCrud;
import com.idslatam.solmar.Models.Entities.DTO.People.AreaAcceso.AreaAccesoItemDTO;
import com.idslatam.solmar.R;

import java.util.ArrayList;

public class AreaAccesoCustomPagerAdapter extends ArrayAdapter<AreaAccesoItemDTO> {

    private Context mContext;
    ArrayList<AreaAccesoItemDTO> areaAcceso;
    private int selectedPosition = -1;

    public AreaAccesoCustomPagerAdapter(Context context,int TextViewResourceId, ArrayList<AreaAccesoItemDTO> _areaAcceso) {

        super(context, TextViewResourceId, _areaAcceso);
        this.mContext = context;
        this.areaAcceso = _areaAcceso;

    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Nullable
    @Override
    public AreaAccesoItemDTO getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getPosition(@Nullable AreaAccesoItemDTO item) {
        return super.getPosition(item);
    }

    public void removeSelection(){

        selectedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        AreaAccesoItemDTO areaAccesoItem = getItem(position);
        // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        // Then you can get the current item using the values array (Users array) and the current position
        // You can NOW reference each method you has created in your bean object (User class)
        label.setText(areaAccesoItem.getNombre());

        // And finally return your dynamic (or custom) view for each spinner item
        return label;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        AreaAccesoItemDTO areaAccesoItem = getItem(position);

        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(areaAccesoItem.getNombre());

        return label;
    }
}
