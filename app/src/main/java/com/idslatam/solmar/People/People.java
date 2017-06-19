package com.idslatam.solmar.People;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.idslatam.solmar.R;

public class People extends AppCompatActivity {

    EditText people_edt_dni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);

        people_edt_dni = (EditText)findViewById(R.id.people_edt_dni);

    }

    public void scanBarcode(View view) {
        new IntentIntegrator(this).initiateScan();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK){
            return;
        }

        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.e("MainActivity", "Scanned");
                people_edt_dni.setText(result.getContents());
            }
        }
        // This is important, otherwise the result will not be passed to the fragment

        Log.e("MENUPRINCIPAL", "RESULT FIN");

        super.onActivityResult(requestCode, resultCode, data);

    }

    public void buscarPeople(View view){

        Intent i = new Intent(this, PeopleDetalle.class);
        startActivity(i);

    }
}
