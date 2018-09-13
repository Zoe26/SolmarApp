package com.idslatam.solmar.CameraNative;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.idslatam.solmar.Api.Http.Constants;
import com.idslatam.solmar.ImageClass.Image;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;

public class cameraNative extends AppCompatActivity {

    private String path1 = Environment.getExternalStorageDirectory() + "/Solgis/Image/takepicOrig.jpg";
    private String path2 = Environment.getExternalStorageDirectory() + "/Solgis/Image/";
    Context mContext;
    public boolean pictureTaken;
    protected static final String PHOTO_TAKEN = "photo_taken";
    String DispositivoIdFile, LatitudFile, LongitudFile, NumeroFile, DispositivoId,CodigoEmpleado,GuidDipositivo;
    protected String URL_API;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_native);
        //pictureTaken = false;
        // take a photo button
        //Button btnPhoto = (Button) findViewById(R.id.photo);
        //btnPhoto.setOnClickListener( new CameraClickHandler() );

        final File newFile = new File(Environment.getExternalStorageDirectory() + "/Solgis/Image");
        newFile.mkdirs();

        mContext = this;

        Constants globalClass = new Constants();
        URL_API = globalClass.getURL();
        imageView = findViewById(R.id.image_pre);

        callCamera();
    }

    /*
    @Override
    public void onResume(){
        super.onResume();
        Log.e("On Resume:","Image");
        Log.e("Picture Taken:",String.valueOf(pictureTaken));
        if(pictureTaken==false){
            finish();
        }
        // put your code here...

    }*/

    public void callCamera(){
        File file = new File( path1 );
        Uri outputFileUri = Uri.fromFile( file );
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
        //intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra( MediaStore.EXTRA_OUTPUT, outputFileUri );
        startActivityForResult( intent, 0 );
    }
    /*
    // picture taking handler
    public class CameraClickHandler implements View.OnClickListener {
        public void onClick( View view ){
            File file = new File( path1 );
            Uri outputFileUri = Uri.fromFile( file );
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
            intent.putExtra( MediaStore.EXTRA_OUTPUT, outputFileUri );
            startActivityForResult( intent, 0 );
        }
    }
    */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.e("Picture Taken AR:",String.valueOf(pictureTaken));


        switch( resultCode ) {
            case 0:
                finish();
                break;
            case -1:
                pictureTaken = true;
                Log.e("Picture Taken:",String.valueOf(pictureTaken));
                onPhotoTaken();
                break;
        }
    }

    protected void onPhotoTaken() {
        pictureTaken = true;

        int width = 0;
        int height = 0;
        Bitmap bitmapOrig = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        //adjust for camera orientation

        try{
            bitmapOrig = BitmapFactory.decodeFile( path1);

            if(bitmapOrig == null){
                bitmapOrig = BitmapFactory.decodeFile(path1, options);

                width = bitmapOrig.getWidth();
                height = bitmapOrig.getHeight();
            }
            else{
                width = bitmapOrig.getWidth();
                height = bitmapOrig.getHeight();
            }
        }
        catch (Exception e){

        }

        ExifInterface exif = null;

        try
        {
            exif = new ExifInterface(path1);
        }
        catch (IOException e)
        {
            //Error
            e.printStackTrace();
        }

        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) :  ExifInterface.ORIENTATION_NORMAL;

        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

        // the following are reverse because we are going to rotate the image 90 due to portrait pics always used
        //int newWidth = 300;
        int newHeight = 650;
        // calculate the scale
        float newWidth = (((float) newHeight) * width)/height;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        //matrix.setRotate(90);
        matrix.postRotate(rotationAngle);
        matrix.postScale(scaleWidth, scaleHeight);

        // save a scaled down Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrig, 0, 0, width, height, matrix, true);

        File file2 = new File (path2 + "imageReduced.jpg");

        try {
            FileOutputStream out = new FileOutputStream(file2);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            imageView.setImageURI(Uri.parse(path2 + "imageReduced.jpg"));
            uploadImage();

        } catch (Exception e) {
            e.printStackTrace();
        }



        /*
        // pop up a dialog so we can get a name for the new pic and upload it
        final Dialog dialogName = new Dialog(cameraNative.this);
        //dialogName.setContentView(R.layout.newpic_name);
        dialogName.setCancelable(true);
        dialogName.setCanceledOnTouchOutside(true);

        // lets scale the title on the popup box
        String tit = getString(R.string.new_pic_title);
        SpannableStringBuilder ssBuilser = new SpannableStringBuilder(tit);
        StyleSpan span = new StyleSpan(Typeface.BOLD);
        ScaleXSpan span1 = new ScaleXSpan(2);
        ssBuilser.setSpan(span, 0, tit.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        ssBuilser.setSpan(span1, 0, tit.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        dialogName.setTitle(ssBuilser);

        TextView picText=(TextView) dialogName.findViewById(R.id.newPicText);
        picText.setText(getString(R.string.new_pic_text1));

        Button picCancel = (Button) dialogName.findViewById(R.id.newPicCancel);
        picCancel.setText(getString(R.string.cancel));
        picCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialogName.dismiss();
            }
        });

        Button picSave = (Button) dialogName.findViewById(R.id.newPicAdd);

        picSave.setText(getString(R.string.save));
        picSave.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EditText nameET = (EditText) dialogName.findViewById(R.id.newPicEdit);
                String name = nameET.getText().toString();
                name = name.replaceAll("[^\\p{L}\\p{N}]", "");
                if (name.equalsIgnoreCase("")) name = "newpic";
                name = name.toLowerCase() + ".jpg";
                selectedPicName = name;

                //adjust for camera orientation
                Bitmap bitmapOrig = BitmapFactory.decodeFile( path1);
                int width = bitmapOrig.getWidth();
                int height = bitmapOrig.getHeight();
                // the following are reverse because we are going to rotate the image 90 due to portrait pics always used
                int newWidth = 150;
                int newHeight = 225;
                // calculate the scale
                float scaleWidth = ((float) newWidth) / width;
                float scaleHeight = ((float) newHeight) / height;
                // create a matrix for the manipulation
                Matrix matrix = new Matrix();
                // resize the bit map
                matrix.postScale(scaleWidth, scaleHeight);

                // save a scaled down Bitmap
                Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrig, 0, 0, width, height, matrix, true);

                File file2 = new File (path2 + selectedPicName);

                try {
                    FileOutputStream out = new FileOutputStream(file2);
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // update the picture
                ImageView img = (ImageView) findViewById(R.id.spinnerImg);
                img.setImageBitmap(resizedBitmap);

                // save new name
                TextView txt = (TextView) findViewById(R.id.selectedTitle);
                txt.setText(name);

                txt = (TextView) findViewById(R.id.selectedURL);
                txt.setText(serverPicBase + name);

                spinList.add(name);

                // upload the new picture to the server
                new fileUpload().execute();

                dialogName.dismiss();
            }
        });

        dialogName.show();
        */

    }

    public void uploadImage(){

        ProgressDialog dialog;

        dialog = new ProgressDialog(cameraNative.this);
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

            Log.e(" filePath ", String.valueOf(path2 + "imageReduced.jpg"));
            Log.e("GuidDipositivo ", GuidDipositivo);

            String filePathAux =  path2 + "imageReduced.jpg"; //decodeFile(filePath,660, 880);
            Uri urlt = Uri.parse(filePathAux);
            //Log.e(" filePathAux ", String.valueOf(filePathAux));

            Log.e("File Send ", filePathAux);
            Log.e("File Send Uri", urlt.toString());
            Log.e("DispositivoId", GuidDipositivo);
            Log.e("Latitud", LatitudFile);
            Log.e("Longitud", LongitudFile);
            Log.e("Numero", NumeroFile);
            Log.e("CodigoEmpleado", CodigoEmpleado);



            String URLB = URL_API.concat("api/Image/file");

            Log.e("Url:",URLB);

            Ion.with(mContext)
                    .load(URLB)
                    .setTimeout(1000*10)
                    .setMultipartParameter("DispositivoId", GuidDipositivo)
                    .setMultipartParameter("Latitud", LatitudFile)
                    .setMultipartParameter("Longitud", LongitudFile)
                    .setMultipartParameter("Numero", NumeroFile)
                    .setMultipartParameter("CodigoEmpleado", CodigoEmpleado)
                    .setMultipartFile("file", new File(urlt.toString()))
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

                                //finish();
                                mensajeError(filePathAux);
                                Log.e("Excepction ", e.toString());
                                //Log.e("Excepction 2", response.toString());
                                //callCamera();
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
    protected void onRestoreInstanceState( Bundle savedInstanceState) {
        if( savedInstanceState.getBoolean( PHOTO_TAKEN ) ) {
            onPhotoTaken();
        }
    }
/*
    @Override
    protected void onSaveInstanceState( Bundle outState ) {
        outState.putBoolean( PHOTO_TAKEN, pictureTaken );
    }
    */
}
