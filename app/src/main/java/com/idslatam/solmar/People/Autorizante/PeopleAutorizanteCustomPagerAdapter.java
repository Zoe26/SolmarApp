package com.idslatam.solmar.People.Autorizante;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.idslatam.solmar.Models.Entities.DTO.People.Autorizante.PeopleAutorizanteItemDTO;

import java.util.ArrayList;

public class PeopleAutorizanteCustomPagerAdapter extends ArrayAdapter<PeopleAutorizanteItemDTO> {
    private Context mContext;
    ArrayList<PeopleAutorizanteItemDTO> autorizantes;
    private int selectedPosition = -1;

    public PeopleAutorizanteCustomPagerAdapter(Context context,int TextViewResourceId, ArrayList<PeopleAutorizanteItemDTO> _autorizantes) {

        super(context, TextViewResourceId, _autorizantes);
        this.mContext = context;
        this.autorizantes = _autorizantes;

    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Nullable
    @Override
    public PeopleAutorizanteItemDTO getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getPosition(@Nullable PeopleAutorizanteItemDTO item) {
        return super.getPosition(item);
    }

    public void removeSelection(){

        selectedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        PeopleAutorizanteItemDTO autorizanteItem = getItem(position);
        // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        // Then you can get the current item using the values array (Users array) and the current position
        // You can NOW reference each method you has created in your bean object (User class)
        label.setText(autorizanteItem.getNombre());

        // And finally return your dynamic (or custom) view for each spinner item
        return label;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        PeopleAutorizanteItemDTO autorizanteItem = getItem(position);

        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(autorizanteItem.getNombre());

        return label;
    }
}
