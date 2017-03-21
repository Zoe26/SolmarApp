package com.idslatam.solmar.Dialer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Pruebas.Adapters.AdapterTracking;
import com.idslatam.solmar.Pruebas.Data.DataTracking;
import com.idslatam.solmar.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ContactosActivity extends Activity {

    Context thiscontext;
    View myView;

    int _Contacts_Id = 0;

    private ListView lvDatost;
    private AdapterContacts adapter;
    private List<ContactsData> mDataContacto;

    ImageButton filtrar;
    EditText nombreFiltro;
    boolean isFiltro = false;

    String nomFil;

    private int currentApiVersion = android.os.Build.VERSION.SDK_INT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactos);

        thiscontext = this;
        lvDatost = (ListView) findViewById(R.id.contacts_ListView);

        Consult();

        nombreFiltro = (EditText)findViewById(R.id.edt_buscar);

        filtrar = (ImageButton) findViewById(R.id.btnFiltrar);

        filtrar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e("FILTRO ", "CLICK");

                isFiltro = true;

                nomFil = nombreFiltro.getText().toString();
                if(nomFil.matches("")){
                    isFiltro = false;
                }

                Consult();
            }
        });

    }

    public void Consult() {

        Log.e("CONSULT", "CLICK");

        String Nombre, Pnumero, Snumero;
        String selectQuery = null;

        mDataContacto = new ArrayList<>();

        try {

            DBHelper dataBaseHelper = new DBHelper(thiscontext);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

            if (isFiltro){
                selectQuery = "SELECT ContactosId, Nombre, PrimerNumero, SegundoNumero FROM Contactos WHERE Nombre LIKE '%"+nomFil+"%' ORDER BY Nombre DESC";
            } else {
                selectQuery = "SELECT ContactosId, Nombre, PrimerNumero, SegundoNumero FROM Contactos ORDER BY Nombre DESC";
            }

            Cursor c = db.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {

                do {
                    _Contacts_Id = c.getInt(c.getColumnIndex("ContactosId"));

                    Nombre = c.getString(c.getColumnIndex("Nombre"));
                    Pnumero = c.getString(c.getColumnIndex("PrimerNumero"));
                    Snumero = c.getString(c.getColumnIndex("SegundoNumero"));

                    Log.e("FILTRO ", Nombre);

                    mDataContacto.add(new ContactsData(_Contacts_Id, Nombre, Pnumero, Snumero));

                } while (c.moveToNext());
            }

        } catch (Exception e) {
            Log.e("EXCPTIO", e.toString());
        }


        Log.e("EXCPTIO", "LAST");

        adapter = new AdapterContacts(thiscontext, mDataContacto);
        lvDatost.setAdapter(adapter);

        lvDatost.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ContactsData person = (ContactsData) parent.getItemAtPosition(position);

                Log.e("FILTRO ", String.valueOf(person.getNombre()));
                Log.e("N1 ", String.valueOf(person.getNumeroP()));
                Log.e("N2 ", String.valueOf(person.getNumeroS()));

                int numP = Integer.parseInt(person.getNumeroP().toString());

                launchDialer(numP);

            }
        });

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
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(numberToDial)));
            return;
        }
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(numberToDial)));
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
