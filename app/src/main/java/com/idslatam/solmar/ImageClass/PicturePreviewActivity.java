package com.idslatam.solmar.ImageClass;

/**
 * Created by ronaldsalazar on 3/9/18.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.R;
import com.idslatam.solmar.View.Perfil;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.otaliastudios.cameraview.AspectRatio;
import com.otaliastudios.cameraview.CameraUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;

public class PicturePreviewActivity extends  Activity {
    private static WeakReference<byte[]> image;
    Bitmap file_b;

    //Inicio variables globales
    protected String URL_API;
    Context mContext;
    private String GuidDipositivo;
    private String filePath = null;
    SimpleDateFormat formatoGuardar = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss")
            , formatoIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    String DispositivoIdFile, LatitudFile, LongitudFile, NumeroFile, DispositivoId,CodigoEmpleado;
    //Fin variables globales

    public static void setImage(@Nullable byte[] im) {
        image = im != null ? new WeakReference<>(im) : null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_preview);

        final ImageView imageView = findViewById(R.id.image_pre);
        //final MessageView nativeCaptureResolution = findViewById(R.id.nativeCaptureResolution);
        // final MessageView actualResolution = findViewById(R.id.actualResolution);
        final MessageView approxUncompressedSize = findViewById(R.id.approxUncompressedSize);
        //final MessageView captureLatency = findViewById(R.id.captureLatency);

        final long delay = getIntent().getLongExtra("delay", 0);
        final int nativeWidth = getIntent().getIntExtra("nativeWidth", 0);
        final int nativeHeight = getIntent().getIntExtra("nativeHeight", 0);
        final String setUri = getIntent().getStringExtra("setUri");



        try{
            imageView.setImageURI(Uri.parse(setUri));
/*
            ProgressDialog dialog;

            dialog = new ProgressDialog(PicturePreviewActivity.this);
            dialog.setMessage("Width: "+String.valueOf(nativeWidth)+"Height:"+String.valueOf(nativeHeight));
            //dialog.setIndeterminate(false);
            //dialog.setCancelable(false);
            dialog.show();


        byte[] b = image == null ? null : image.get();
        if (b == null) {
            finish();
            return;
        }

            //Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            //imageView.setImageBitmap(bitmap);

            CameraUtils.decodeBitmap(b, 500, 500, new CameraUtils.BitmapCallback() {
                @Override
                public void onBitmapReady(Bitmap bitmap) {
                    imageView.setImageBitmap(bitmap);



                    ContextWrapper cw = new ContextWrapper(getApplicationContext());
                    File directory = cw.getDir("image", Context.MODE_PRIVATE);
                    if (!directory.exists()) {
                        directory.mkdir();
                    }

                    File mypath = new File(directory, "thumbnail.png");

                    Log.e("Image Path", mypath.toString());


                    //FileOutputStream fos = null;
                    //try {
                    //    fos = new FileOutputStream(mypath);
                    //    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    //    fos.close();
                    //} catch (Exception e) {
                    //    Log.e("SAVE_IMAGE", e.getMessage(), e);
                    //}


                    approxUncompressedSize.setTitle("Approx. uncompressed size");
                    approxUncompressedSize.setMessage(getApproximateFileMegabytes(bitmap) + " - Kb");

                    Log.e("Image Path", "Success");
                    //file_b = bitmap;
                    //ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    //bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);

                    //String path = MediaStore.Images.Media.insertImage(mContext.getContentResolver(), bos, "Fotex", null);
                    filePath = Uri.parse(mypath.toString()).toString();

                    Log.e("Image Path Uri", filePath);





                //captureLatency.setTitle("Approx. capture latency");
                //captureLatency.setMessage(delay + " milliseconds");

                // ncr and ar might be different when cropOutput is true.
                //AspectRatio nativeRatio = AspectRatio.of(nativeWidth, nativeHeight);
                //nativeCaptureResolution.setTitle("Native capture resolution");
                //nativeCaptureResolution.setMessage(nativeWidth + "x" + nativeHeight + " (" + nativeRatio + ")");


                    // AspectRatio finalRatio = AspectRatio.of(bitmap.getWidth(), bitmap.getHeight());
                    // actualResolution.setTitle("Actual resolution");
                    // actualResolution.setMessage(bitmap.getWidth() + "x" + bitmap.getHeight() + " (" + finalRatio + ")");
                }
            });
*/

        }
        catch (Exception e){
            ProgressDialog dialog;

            dialog = new ProgressDialog(PicturePreviewActivity.this);
            dialog.setMessage("Error en tamaño de imagen");
            //dialog.setIndeterminate(false);
            //dialog.setCancelable(false);
            dialog.show();
        }


        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();
        findViewById(R.id.sendPhotoImage).setOnClickListener(onClickListener);
        findViewById(R.id.cancelPhotoImage).setOnClickListener(onClickListener);
        mContext = this;

    }

    private static float getApproximateFileMegabytes(Bitmap bitmap) {
        return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            switch(v.getId()){
                case R.id.sendPhotoImage:
                    //DO something
                    uploadImage();
                    //Log.e("Image Preview", "enviar foto");

                    break;
                case R.id.cancelPhotoImage:

                    //DO something
                    /*Intent intent = new Intent(mContext, Perfil.class);
                    intent.putExtra("State", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    */
                    finish();
                    break;
            }

        }
    };

    public void uploadImage(){

        ProgressDialog dialog;

        dialog = new ProgressDialog(PicturePreviewActivity.this);
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

            Log.e("filePath ", String.valueOf(filePath));
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
                    .setMultipartFile("file", new File(filePath))
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
                                } catch (Exception edsv){
                                    Log.e("Exception ", "Finaliza edsv"+edsv.toString());
                                }

                                mensajeError(filePathAux);
                                Log.e("Exception ", "Finaliza con error");
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

                        //go to Bienvenido:
                        /*
                        Intent intent = new Intent(mContext, Perfil.class);
                        intent.putExtra("State", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        //intent.putExtra("delay", callbackTime - mCaptureTime);
                        //intent.putExtra("nativeWidth", mCaptureNativeSize.getWidth());
                        //intent.putExtra("nativeHeight", mCaptureNativeSize.getHeight());
                        startActivity(intent);
                        */
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

}
