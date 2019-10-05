package com.idslatam.solmar.Tracking.Services.Foreground;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.idslatam.solmar.R;
import com.idslatam.solmar.View.MostrarFecha;

public class Foreground extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foreground);

    }

    public void startService(View v){
        Intent serviceIntent = new Intent(this, Servicio.class);
        ContextCompat.startForegroundService(this,serviceIntent);
    }

    public void VerTiempo(View v){
        Intent verTiempo = new Intent(Foreground.this, MostrarFecha.class);
        startActivity(verTiempo);
    }
}
