package com.idslatam.solmar.ImageClass;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Toast;

import com.idslatam.solmar.R;
import com.otaliastudios.cameraview.AspectRatio;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraLogger;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Control;
import com.otaliastudios.cameraview.SessionType;
import com.otaliastudios.cameraview.Size;
import com.otaliastudios.cameraview.SizeSelector;
import com.otaliastudios.cameraview.SizeSelectors;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by ronaldsalazar on 3/9/18.
 */

public class ImageCamera extends AppCompatActivity implements View.OnClickListener {

    private CameraView camera;
    private ViewGroup controlPanel;

    private boolean mCapturingPicture;
    private boolean mCapturingVideo;

    // To show stuff in the callback
    private Size mCaptureNativeSize;
    private long mCaptureTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        setContentView(R.layout.activity_image);
        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE);

        camera = findViewById(R.id.camera);

        SizeSelector width = SizeSelectors.maxWidth(1024);
        SizeSelector height = SizeSelectors.maxHeight(1024);
        SizeSelector dimensions = SizeSelectors.and(width, height); // Matches sizes bigger than 1000x2000.
        SizeSelector ratio = SizeSelectors.aspectRatio(AspectRatio.of(1, 1), 0);

        SizeSelector result = SizeSelectors.or(
                dimensions,//SizeSelectors.and(ratio, dimensions), // Try to match both constraints
                //ratio, // If none is found, at least try to match the aspect ratio
                SizeSelectors.smallest() // If none is found, take the biggest
        );

        camera.setPictureSize(result);

        camera.addCameraListener(new CameraListener() {

            public void onCameraOpened(CameraOptions options) {
                //onOpened();
            }
            public void onPictureTaken(byte[] jpeg) {
                Log.e("Image", "onPictureTaken: ");
                //onPicture(jpeg);
            }

            @Override
            public void onVideoTaken(File video) {
                super.onVideoTaken(video);
                //onVideo(video);
            }
        });

        //findViewById(R.id.edit).setOnClickListener(this);
        findViewById(R.id.capturePhoto).setOnClickListener(this);
        //findViewById(R.id.captureVideo).setOnClickListener(this);
        //findViewById(R.id.toggleCamera).setOnClickListener(this);

        /*
        controlPanel = findViewById(R.id.controls);

        ViewGroup group = (ViewGroup) controlPanel.getChildAt(0);
        Control[] controls = Control.values();
        for (Control control : controls) {
            ControlView view = new ControlView(this, control, this);
            group.addView(view, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        controlPanel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                BottomSheetBehavior b = BottomSheetBehavior.from(controlPanel);
                b.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });
        */
        //camera.capturePicture();
        //finish();
    }

    private void onPicture(byte[] jpeg) {

        Log.e("Image", "Evento Picture" );

        mCapturingPicture = false;
        long callbackTime = System.currentTimeMillis();
        if (mCapturingVideo) {
            message("Captured while taking video. Size="+mCaptureNativeSize, false);
            return;
        }

        // This can happen if picture was taken with a gesture.
        if (mCaptureTime == 0) mCaptureTime = callbackTime - 300;
        if (mCaptureNativeSize == null) mCaptureNativeSize = camera.getPictureSize();


        ProgressDialog dialog;
        dialog = new ProgressDialog(ImageCamera.this);
        dialog.setMessage("Width: "+String.valueOf(mCaptureNativeSize.getWidth())+"Height:"+String.valueOf(mCaptureNativeSize.getHeight()));
        //dialog.setIndeterminate(false);
        //dialog.setCancelable(false);
        dialog.show();

        //Grabar en Disco

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("image", Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }

        File mypath = new File(directory, "thumbnail.png");

        FileOutputStream fos = null;
        try {
            Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
            fos = new FileOutputStream(mypath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            Log.e("SAVE_IMAGE", e.getMessage(), e);
        }

        //Uri.parse(mypath.toString()).toString();

        //Fin Grabar en disco

        /*
        PicturePreviewActivity.setImage(jpeg);
        */
        Intent intent = new Intent(ImageCamera.this, PicturePreviewActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra("delay", callbackTime - mCaptureTime);
        intent.putExtra("nativeWidth", mCaptureNativeSize.getWidth());
        intent.putExtra("nativeHeight", mCaptureNativeSize.getHeight());
        intent.putExtra("setUri", mypath.toString());

        dialog.cancel();
        startActivity(intent);
        finish();


        mCaptureTime = 0;
        mCaptureNativeSize = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            //case R.id.edit: edit(); break;
            case R.id.capturePhoto: capturePhoto(); break;
            //case R.id.captureVideo: captureVideo(); break;
            //case R.id.toggleCamera: toggleCamera(); break;

        }
    }

    private void capturePhoto() {
        if (mCapturingPicture) return;
        mCapturingPicture = true;
        //mCaptureTime = System.currentTimeMillis();
        //mCaptureNativeSize = camera.getPictureSize();
        message("Capturing picture...", false);

        Log.e("Image", "Apertura de Foto");

        camera.capturePicture();
        Log.e("Image", "take de Foto");
    }

    private void message(String content, boolean important) {
        int length = important ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast.makeText(this, content, length).show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean valid = true;
        for (int grantResult : grantResults) {
            valid = valid && grantResult == PackageManager.PERMISSION_GRANTED;
        }
        if (valid && !camera.isStarted()) {
            camera.start();
        }
    }
}
