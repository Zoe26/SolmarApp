package com.idslatam.solmar.Cargo.Precinto;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.idslatam.solmar.R;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

/**
 * Created by desarrollo03 on 5/22/17.
 */

public class PrecintoCustomAdapter extends ArrayAdapter<PrecintoDataModel>{

    private ArrayList<PrecintoDataModel> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtNum;
        ImageView img;
    }

    public PrecintoCustomAdapter(ArrayList<PrecintoDataModel> data, Context context) {
        super(context, R.layout.row_item_precinto, data);
        this.dataSet = data;
        this.mContext=context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        PrecintoDataModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item_precinto, parent, false);
            viewHolder.txtNum = (TextView) convertView.findViewById(R.id.item_num_precinto);
            viewHolder.img = (ImageView) convertView.findViewById(R.id.item_info);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();

            result=convertView;
        }

        viewHolder.txtNum.setText(dataModel.getNum());
        //viewHolder.txtDoi.setText(dataModel.getDoi());

        try {

            try {

                //viewHolder.img.setImageDrawable(null);
                ImageView imageX = (ImageView) convertView.findViewById(R.id.item_info);
                imageX.setImageDrawable(null);
                imageX.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_camare_add));

                /*
                Ion.with(viewHolder.img)
                        .placeholder(R.drawable.ic_camare_add)
                        .error(R.drawable.ic_camare_add)
                        .load(dataModel.getFoto());
                */
                imageX.setImageURI(Uri.parse(dataModel.getFoto()));

            } catch (Exception e){}

        } catch (Exception es){}

        // Return the completed view to render on screen
        return convertView;
    }
}
