package com.idslatam.solmar.Cargo.Precinto;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.idslatam.solmar.R;

import java.util.ArrayList;

/**
 * Created by desarrollo03 on 5/22/17.
 */

public class PrecintoCustomAdapter extends ArrayAdapter<PrecintoDataModel>{

    private ArrayList<PrecintoDataModel> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        //TextView txtDoi;
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
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.req_nombre);
            //viewHolder.txtDoi = (TextView) convertView.findViewById(R.id.req_doi);


            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }


        viewHolder.txtName.setText(dataModel.getName());
        //viewHolder.txtDoi.setText(dataModel.getDoi());

        // Return the completed view to render on screen
        return convertView;
    }
}
