package com.idslatam.solmar.Patrol.Contenedor;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.idslatam.solmar.Cargo.Precinto.PrecintoDataModel;
import com.idslatam.solmar.ImageClass.ImageConverter;
import com.idslatam.solmar.R;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.Transform;

import java.util.ArrayList;

/**
 * Created by desarrollo03 on 5/22/17.
 */

public class CustomAdapter extends ArrayAdapter<DataModel>{

    private ArrayList<DataModel> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        ImageView item_info;
    }

    public CustomAdapter(ArrayList<DataModel> data, Context context) {
        super(context, R.layout.row_item_precinto_patrol, data);
        this.dataSet = data;
        this.mContext=context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        DataModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item_precinto_patrol, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.req_nombre);
            viewHolder.item_info = (ImageView) convertView.findViewById(R.id.item_info);


            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.txtName.setText(dataModel.getName());
        //viewHolder.item_info.setText(dataModel.getName());

        try {

            try {

                Ion.with(viewHolder.item_info)
                        .placeholder(R.drawable.ic_camare_add)
                        .error(R.drawable.ic_camare_add)
                        .load(dataModel.getUri());

            } catch (Exception e){}

        } catch (Exception es){}

        // Return the completed view to render on screen
        return convertView;
    }
}
