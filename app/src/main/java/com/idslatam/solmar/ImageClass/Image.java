package com.idslatam.solmar.ImageClass;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import com.desmond.squarecamera.CameraActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.R;
import com.idslatam.solmar.View.Code.CodeBar;
import com.idslatam.solmar.View.MenuPrincipal;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;

public class Image extends Activity {

    protected String URL_API;
    private Toolbar toolbar;
    String fotocheckCod;
    Bundle b;
    Context mContext;

    private Uri fileUri;
    private String filePath = null;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss")
            , formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    String DispositivoIdFile, LatitudFile, LongitudFile, NumeroFile, DispositivoId;

    private int currentApiVersion = android.os.Build.VERSION.SDK_INT;

    boolean state;


    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private Point mSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        mContext = this;

        Display display = getWindowManager().getDefaultDisplay();
        mSize = new Point();
        display.getSize(mSize);

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();


        requestForCameraPermission();
    }


    public void requestForCameraPermission() {
        Log.e("MENUPRINCIPAL", "requestForCameraPermission");

        final String permission = Manifest.permission.CAMERA;
        if (ContextCompat.checkSelfPermission(Image.this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Image.this, permission)) {
                showPermissionRationaleDialog("Test", permission);
                Log.e("MENUPRINCIPAL", "ActivityCompat");
            } else {
                requestForPermission(permission);
            }
        } else {
            launch();
            Log.e("MENUPRINCIPAL ", "launch");
        }

        Log.e("MENUPRINCIPAL", "requestForCameraPermission FIN");
    }

    private void showPermissionRationaleDialog(final String message, final String permission) {
        new android.support.v7.app.AlertDialog.Builder(Image.this)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Image.this.requestForPermission(permission);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    private void requestForPermission(final String permission) {
        ActivityCompat.requestPermissions(Image.this, new String[]{permission}, REQUEST_CAMERA_PERMISSION);
    }

    private void launch() {
        Log.e("launch","launch.....");
        Intent startCustomCameraIntent = new Intent(this, CameraActivity.class);
        startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);

        Log.e("launch","launch FIN.....");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                final int numOfRequest = grantResults.length;
                final boolean isGranted = numOfRequest == 1
                        && PackageManager.PERMISSION_GRANTED == grantResults[numOfRequest - 1];
                if (isGranted) {
                    launch();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void uploadImage(){

        ProgressDialog dialog;

        dialog = new ProgressDialog(Image.this);
        dialog.setMessage("Enviando Foto...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();

        try{

            DBHelper dbHelperVolumen = new DBHelper(mContext);
            SQLiteDatabase sqlVolumen = dbHelperVolumen.getWritableDatabase();
            String selectQuery = "SELECT NumeroCel, Latitud, Longitud, GuidDipositivo FROM Configuration";
            Cursor c = sqlVolumen.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {
                NumeroFile = c.getString(c.getColumnIndex("NumeroCel"));
                LatitudFile = c.getString(c.getColumnIndex("Latitud"));
                LongitudFile = c.getString(c.getColumnIndex("Longitud"));
                DispositivoIdFile = c.getString(c.getColumnIndex("GuidDipositivo"));
            }

            c.close();
            sqlVolumen.close();

        }catch (Exception e){
            Log.e("-- |EXCEPTION | ", e.getMessage());
        }

        try {

            filePath = fileUri.getPath();

            Log.e(" filePath ", String.valueOf(filePath));

            String filePathAux = filePath; //decodeFile(filePath,660, 880);
            //Log.e(" filePathAux ", String.valueOf(filePathAux));

            String URLB = URL_API.concat("/api/Image/file");

            Ion.with(mContext)
                    .load(URLB)
                    .setMultipartParameter("DispositivoId", DispositivoIdFile)
                    .setMultipartParameter("Latitud", LatitudFile)
                    .setMultipartParameter("Longitud", LongitudFile)
                    .setMultipartParameter("Numero", NumeroFile)

                    .setMultipartFile("file", new File(filePathAux))
                    .asString()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> response) {

                            File fdelete = new File(filePathAux);

                            if (fdelete.exists()) {
                                if (fdelete.delete()) {
                                    Log.e("file Deleted :", filePathAux);
                                } else {
                                    Log.e("file not Deleted :", filePathAux);
                                }
                            }

                            if(response!=null){

                                try {
                                    if (dialog.isShowing()) {
                                        dialog.dismiss();
                                    }
                                }catch (Exception e7){}

                                try {
                                    JSONObject json = new JSONObject(response.getResult().toString());

                                    showAlert(json.getString("Mensaje"));

                                } catch (JSONException edd){

                                }

                                Log.e("JsonObject ", response.getResult().toString());
                            } else  {
                                Toast.makeText(mContext, "Error al enviar imagen. Por favor revise su conexión.", Toast.LENGTH_SHORT).show();
                                Log.e("Exception ", "Finaliza "+ e.getMessage());
                            }

                            try {
                                if (dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                            }catch (Exception ee){}


                            finish();
                        }
                    });


        } catch (Exception e) {

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        Log.e("MENUPRINCIPAL", "RESULT");

        if (resultCode != RESULT_OK){
            finish();
            return;
        }


        if (requestCode == REQUEST_CAMERA) {
            Log.e("MENUPRINCIPAL", "REQUEST_CAMERA");
            Uri photoUri = data.getData();
            // Get the bitmap in according to the width of the device
            //Bitmap bitmap = ImageUtility.decodeSampledBitmapFromPath(photoUri.getPath(), mSize.x, mSize.x);
            //((ImageView) findViewById(R.id.image)).setImageBitmap(bitmap);
            Log.e("MENUPRINCIPAL", "REQUEST_CAMERA FIN " + String.valueOf(photoUri));
            fileUri = photoUri;

            uploadImage();

        }

        Log.e("MENUPRINCIPAL", "RESULT FIN");

        super.onActivityResult(requestCode, resultCode, data);

    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Respuesta de Servidor")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
