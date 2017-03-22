package com.idslatam.solmar.View.Code;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.idslatam.solmar.R;

import java.lang.reflect.Method;

public class Scan extends Activity {

    private int currentApiVersion = android.os.Build.VERSION.SDK_INT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        scanBarcode();
    }

    public void scanBarcode() {
        new IntentIntegrator(this).initiateScan();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        Log.e("MENUPRINCIPAL", "RESULT");

        if (resultCode != RESULT_OK){
            finish();
            return;
        }

        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Scanned");
                Intent i = new Intent(this, CodeBar.class);
                i.putExtra("epuzzle", result.getContents());
                i.putExtra("format", result.getFormatName());
                startActivity(i);
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        }
        // This is important, otherwise the result will not be passed to the fragment

        Log.e("MENUPRINCIPAL", "RESULT FIN");

        super.onActivityResult(requestCode, resultCode, data);

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
