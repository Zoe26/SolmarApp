package com.idslatam.solmar.View.Code;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.idslatam.solmar.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CodeBar extends Activity {

    TextView contect, formatxt, currentNow;
    Calendar currenCodeBar;

    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss")
            , formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_bar);

        Intent intent = getIntent();
        String easyPuzzle = intent.getExtras().getString("epuzzle");
        String form = intent.getExtras().getString("format");
        currenCodeBar = Calendar.getInstance();

        contect = (TextView) findViewById(R.id.txt_content);
        formatxt = (TextView) findViewById(R.id.txt_format);
        currentNow = (TextView) findViewById(R.id.txt_fecha);

        contect.setText(easyPuzzle);
        formatxt.setText("Formato: "+form);
        currentNow.setText("Fecha: "+formatoIso.format(currenCodeBar.getTime()));

    }
}
