package com.idslatam.solmar.Dialer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.idslatam.solmar.Pruebas.Data.DataTracking;
import com.idslatam.solmar.R;

import java.util.List;

/**
 * Created by desarrollo03 on 3/19/17.
 */

public class AdapterContacts extends BaseAdapter {

    private Context mContext;
    private List<ContactsData> mDataContactsList;

    public AdapterContacts(Context mContext, List<ContactsData> mDataContactsList) {
        this.mContext = mContext;
        this.mDataContactsList = mDataContactsList;
    }


    @Override
    public int getCount() {
        return mDataContactsList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataContactsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = View.inflate(mContext, R.layout.contacts_items, null);
        TextView ContactoNombre = (TextView)v.findViewById(R.id.contacts_Nombre);
        TextView NumeroP = (TextView)v.findViewById(R.id.primerNum);
        TextView NumeroS = (TextView)v.findViewById(R.id.segundoNum);

        ContactoNombre.setText(mDataContactsList.get(position).getNombre());
        NumeroP.setText(mDataContactsList.get(position).getNumeroP());
        NumeroS.setText(mDataContactsList.get(position).getNumeroS());
        v.setTag(mDataContactsList.get(position).getContactsId());

        ImageButton call = (ImageButton) v.findViewById(R.id.btn_click);
        ImageButton call_detail = (ImageButton) v.findViewById(R.id.btn_click_detalle);

        // Change icon based on name
        call.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                        int numP = Integer.parseInt(NumeroP.getText().toString());
                        launchDialer(numP);
                        Log.e("--- NumeroP ", String.valueOf(NumeroP.getText()));

            }
        });

        // Change icon based on name
        call_detail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                        Log.e("--- info Call ", String.valueOf(NumeroP.getText()));

                        Intent intent = new Intent(mContext, ContactosDetalles.class);
                        intent.putExtra("Nombre", ContactoNombre.getText().toString());
                        intent.putExtra("NumP", NumeroP.getText().toString());
                        intent.putExtra("NumS", NumeroS.getText().toString());
                        mContext.startActivity(intent);

            }
        });

        return v;
    }


    public void launchDialer(int number) {
        String numberToDial = "tel:" + number;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            mContext.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(numberToDial)));
            return;
        }
        mContext.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(numberToDial)));
    }

}
