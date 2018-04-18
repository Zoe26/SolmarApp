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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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

//import com.sandrios.sandriosCamera.internal.SandriosCamera;
//import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;

import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraLogger;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.SessionType;
import com.otaliastudios.cameraview.Size;

import java.io.File;
import java.text.SimpleDateFormat;

public class Image extends Activity {
    private CameraView camera;
    protected String URL_API;
    Context mContext;

    private String GuidDipositivo;
    private String filePath = null;
    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss")
            , formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    String DispositivoIdFile, LatitudFile, LongitudFile, NumeroFile, DispositivoId,CodigoEmpleado;
    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private Point mSize;

    private static final int CAPTURE_MEDIA = 368;

    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        activity = this;
        mContext = this;

        /*Display display = getWindowManager().getDefaultDisplay();
        mSize = new Point();
        display.getSize(mSize);*/

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();

        //launch();

        //requestForCameraPermission();

        camera = findViewById(R.id.camera);

        camera.addCameraListener(new CameraListener() {
            //public void onCameraOpened(CameraOptions options) { onOpened(); }
            public void onPictureTaken(byte[] jpeg) { onPicture(jpeg); }

            /*
            @Override
            public void onVideoTaken(File video) {
                super.onVideoTaken(video);
                onVideo(video);
            }
            */
        });
    }

    private void onPicture(byte[] jpeg) {
        /*
        mCapturingPicture = false;
        long callbackTime = System.currentTimeMillis();
        if (mCapturingVideo) {
            message("Captured while taking video. Size="+mCaptureNativeSize, false);
            return;
        }

        // This can happen if picture was taken with a gesture.
        if (mCaptureTime == 0) mCaptureTime = callbackTime - 300;
        if (mCaptureNativeSize == null) mCaptureNativeSize = camera.getPictureSize();

        PicturePreviewActivity.setImage(jpeg);
        Intent intent = new Intent(CameraActivity.this, PicturePreviewActivity.class);
        intent.putExtra("delay", callbackTime - mCaptureTime);
        intent.putExtra("nativeWidth", mCaptureNativeSize.getWidth());
        intent.putExtra("nativeHeight", mCaptureNativeSize.getHeight());
        startActivity(intent);

        mCaptureTime = 0;
        mCaptureNativeSize = null;*/
    }

    public void requestForCameraPermission() {
        final String permission = Manifest.permission.CAMERA;
        if (ContextCompat.checkSelfPermission(Image.this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Image.this, permission)) {
                showPermissionRationaleDialog("Test", permission);
            } else {
                requestForPermission(permission);
            }
        } else {
            launch();
        }
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
/*
        new SandriosCamera(activity, CAPTURE_MEDIA)
                .setShowPicker(false)
                .setMediaAction(CameraConfiguration.MEDIA_ACTION_PHOTO)
                .setMediaQuality(CameraConfiguration.MEDIA_QUALITY_MEDIUM)
                .enableImageCropping(false)
                .launchCamera();
*/
        /*Intent startCustomCameraIntent = new Intent(Image.this, CameraActivity.class);
        startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);*/
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
                    //launch();

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
            String selectQuery = "SELECT NumeroCel, Latitud, Longitud, GuidDipositivo, CodigoEmpleado FROM Configuration";
            Cursor c = sqlVolumen.rawQuery(selectQuery, new String[]{});

            if (c.moveToFirst()) {
                NumeroFile = c.getString(c.getColumnIndex("NumeroCel"));
                LatitudFile = c.getString(c.getColumnIndex("Latitud"));
                LongitudFile = c.getString(c.getColumnIndex("Longitud"));
                GuidDipositivo = c.getString(c.getColumnIndex("GuidDipositivo"));
                CodigoEmpleado = c.getString(c.getColumnIndex("CodigoEmpleado"));
            }

            c.close();
            sqlVolumen.close();

        }catch (Exception e){
            Log.e("-- |EXCEPTION | ", e.getMessage());
        }

        try {

            Log.e(" filePath ", String.valueOf(filePath));
            Log.e("GuidDipositivo ", GuidDipositivo);

            String filePathAux = filePath; //decodeFile(filePath,660, 880);
            //Log.e(" filePathAux ", String.valueOf(filePathAux));

            String URLB = URL_API.concat("/api/Image/file");

            Ion.with(mContext)

                    .load(URLB)
                    .setTimeout(1000*10)
                    .setMultipartParameter("DispositivoId", GuidDipositivo)
                    .setMultipartParameter("Latitud", LatitudFile)
                    .setMultipartParameter("Longitud", LongitudFile)
                    .setMultipartParameter("Numero", NumeroFile)
                    .setMultipartParameter("CodigoEmpleado", CodigoEmpleado)
                    .setMultipartFile("file", new File(filePathAux))
                    .asString()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> response) {

                            if (e != null){
                                try {
                                    if (dialog.isShowing()) {
                                        dialog.dismiss();
                                    }
                                }catch (Exception e7){}

                                mensajeError(filePathAux);
                                Log.e("Excepction ", " --- ");
                                return;
                            }

                            if(response.getHeaders().code()==200){
                                Gson gson = new Gson();
                                JsonObject result = gson.fromJson(response.getResult(), JsonObject.class);

                                showAlert(result.get("Mensaje").getAsString(), filePathAux);

                                try {
                                    if (dialog.isShowing()) {
                                        dialog.dismiss();
                                    }
                                }catch (Exception e7){}

                            } else {

                                try {
                                    if (dialog != null && dialog.isShowing()) {
                                        dialog.dismiss();
                                    }
                                } catch (Exception edsv){}

                                mensajeError(filePathAux);
                                Log.e("Exception ", "Finaliza "+ e.getMessage());
                                finish();
                            }

                            try {
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                            } catch (Exception edsv){}
                        }
                    });


        } catch (Exception e) {

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK){
            finish();
            return;
        }

        Log.e("MENUPRINCIPAL", "RESULT");
        if (requestCode == CAPTURE_MEDIA && resultCode == RESULT_OK) {
            /*
            Log.e("File", "" + data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH));

            String photoUri  = data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH);
            filePath = photoUri;
*/
            uploadImage();
        }

        Log.e("MENUPRINCIPAL", "RESULT FIN");

        super.onActivityResult(requestCode, resultCode, data);

    }

    private void showAlert(String message, String fileFoto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Respuesta de Servidor")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        File fdelete = new File(fileFoto);

                            if (fdelete.exists()) {
                                if (fdelete.delete()) {
                                    Log.e("file Deleted :", fileFoto);
                                } else {
                                    Log.e("file not Deleted :", fileFoto);
                                }
                            }
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void mensajeError(String fileFoto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Se ha terminado el tiempo de espera para el envío del image. Por favor intente nuevamente.")
                .setTitle("Problemas al enviar")
                .setCancelable(false)
                .setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        File fdelete = new File(fileFoto);

                        if (fdelete.exists()) {
                            if (fdelete.delete()) {
                                Log.e("file Deleted :", fileFoto);
                            } else {
                                Log.e("file not Deleted :", fileFoto);
                            }
                        }

                        finish();
                    }
                })
                .setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        File fdelete = new File(fileFoto);

                        if (fdelete.exists()) {
                            if (fdelete.delete()) {
                                Log.e("file Deleted :", fileFoto);
                            } else {
                                Log.e("file not Deleted :", fileFoto);
                            }
                        }

                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera.destroy();
    }

}
