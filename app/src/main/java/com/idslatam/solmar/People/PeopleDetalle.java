package com.idslatam.solmar.People;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.idslatam.solmar.R;

public class PeopleDetalle extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_detalle);
    }

    public void salir(View view){
        finish();
    }
}