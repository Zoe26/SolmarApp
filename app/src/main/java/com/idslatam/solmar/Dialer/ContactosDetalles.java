package com.idslatam.solmar.Dialer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.TextView;

import com.idslatam.solmar.R;

import java.lang.reflect.Method;

public class ContactosDetalles extends Activity {

    Bundle b;
    String Nom, NumP, NumS;

    TextView Nombre, NumeroP, NumeroS;

    int primero, segundo;

    private int currentApiVersion = android.os.Build.VERSION.SDK_INT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactos_detalles);

        b = getIntent().getExtras();
        Nom = b.getString("Nombre");
        NumP = b.getString("NumP");
        NumS = b.getString("NumS");

        Nombre = (TextView)findViewById(R.id.Nombre_detail);
        NumeroP = (TextView)findViewById(R.id.Num_principal_detail);
        NumeroS  = (TextView)findViewById(R.id.Num_secundario_detail);

        Nombre.setText(Nom);
        NumeroP.setText(NumP);
        NumeroS.setText(NumS);


    }

    public void callPrincipal(View view){

        primero = Integer.parseInt(NumeroP.getText().toString());
        launchDialer(primero);

    }

    public void callSecundario(View view){

        segundo = Integer.parseInt(NumeroS.getText().toString());
        launchDialer(segundo);

    }

    public void launchDialer(int number) {
        String numberToDial = "tel:" + number;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            this.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(numberToDial)));
            return;
        }
        this.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(numberToDial)));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        try
        {
            if(!hasFocus)
            {
                Object service  = getSystemService("statusbar");
                Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                Method collapse;

                //Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                if (currentApiVersion <= 16) {
                    collapse = statusbarManager.getMethod("collapse");
                    collapse.invoke(service);
                    collapse .setAccessible(true);
                    collapse .invoke(service);

                } else {
                    collapse = statusbarManager.getMethod("collapsePanels");
                    collapse.invoke(service);
                    collapse.setAccessible(true);
                    collapse.invoke(service);

                }


                //Method collapse = statusbarManager.getMethod("collapse");

            }
        }
        catch(Exception ex)
        {
            if(!hasFocus)
            {
                try {

                    Object service  = getSystemService("statusbar");
                    Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                    Method collapse;

                    //Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                    if (currentApiVersion <= 16) {
                        collapse = statusbarManager.getMethod("collapse");
                        collapse.invoke(service);
                        collapse.setAccessible(true);
                        collapse.invoke(service);

                    } else {
                        collapse = statusbarManager.getMethod("collapsePanels");
                        collapse.invoke(service);
                        collapse.setAccessible(true);
                        collapse.invoke(service);

                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                ex.printStackTrace();
            }
        }
    }

}
