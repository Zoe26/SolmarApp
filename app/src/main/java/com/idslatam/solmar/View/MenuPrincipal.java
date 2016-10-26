package com.idslatam.solmar.View;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.R;

public class MenuPrincipal extends AppCompatActivity {

    protected String URL_API;
    View myView;
    GridView gridView;
    String fotocheck, URLGlobal;
    long totalSize = 0;

    private String filePath = null;
    static final String[] MOBILE_OS = new String[] { "Alert", "Images", "Jobs", "BravoPapa", "Tracking" };

    // ************************************************************************************************************
    // VARIABLES PARA CAMARA

    private static final String TAG = MenuPrincipal.class.getSimpleName();

    // Camera activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;

    public static final int MEDIA_TYPE_IMAGE = 1;

    private Uri fileUri; // file url to store image/video

    static String PATH_ORIGINAL_IMAGE;
    // *********************************************************************************************


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();
        int buscaP;



    }

}
