package com.idslatam.solmar.View.Settings;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.R;
import com.idslatam.solmar.View.Bienvenido;
import com.idslatam.solmar.View.Login;
import com.idslatam.solmar.View.RegisterNumber;

public class AccessSettings extends AppCompatActivity {

    EditText password;
    Button acceso ;

    String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_access_settings);

    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(AccessSettings.this, Bienvenido.class);
        startActivity(intent);
        finish();

        return;
    }
}
