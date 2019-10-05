package com.idslatam.solmar.View;

import android.view.View.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


import android.widget.TextView;

import com.idslatam.solmar.R;
import com.idslatam.solmar.Tracking.Services.Foreground.Foreground;
import com.idslatam.solmar.Tracking.Services.Foreground.Servicio;

import java.sql.Time;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;

public class MostrarFecha extends AppCompatActivity implements View.OnClickListener {

  /* Mostrarfecha{
     int id;
     String fecha;
     String tiempo;
    }*/

  /*  @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fecha_foreground);
        View boton = findViewById(R.id.irTiempo);
        boton.setOnClickListener(this);
    }*/

    public void onClick(View v){
        if(v.getId()==findViewById(R.id.irTiempo).getId())
        {
            GregorianCalendar gcalendar = new GregorianCalendar();
            /*Calendar calendar = Calendar.getInstance();*/
            //String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());//
            /*String currentTime = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
            /*String hour = String.valueOf(gcalendar.get(Calendar.HOUR));*/
            /*String min = String.valueOf(gcalendar.get(Calendar.MINUTE));*/
            /*String sec = String.valueOf(gcalendar.get(Calendar.SECOND));*/

            /*fecha = currentTime;*/
            /*tiempo = (""+hour+":"+min+":"+sec+"");*/

            TextView textViewDate = findViewById(R.id.tiempoTV) ;
           /* textViewDate.setText(""+fecha+" - "+tiempo+"");*/
        }
    }


    /*public void irTiempo(){

        /*BasedeDatos basedeDatos = new BasedeDatos(this,"DBFecha", null,1);
        SQLiteDatabase db = basedeDatos.getWritableDatabase();
        if(db != null){
            Cursor c = db.rawQuery("select * from Tiempo", null);
            int cantidad = c.getCount();
            int i = 0;
            String[] arreglo = new String(cantidad);
            if(c.moveToFirst()){
                do{
                    String linea = c.getInt(0)+ " " + c.getString(1) + " " + c.getString(2);

                    arreglo[1] = linea;
                    i++;
                }while(c.moveToNext());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter (this, android.layout.simple_list_item_1, arreglo);
            ListView lista = (ListView) findViewById(R.id.MostrarFecha);
            lista.setAdapter(adapter);
        }*/


    public void stopService(View v){
        Intent serviceIntent = new Intent(this, Servicio.class);
        stopService(serviceIntent);
    }

}
