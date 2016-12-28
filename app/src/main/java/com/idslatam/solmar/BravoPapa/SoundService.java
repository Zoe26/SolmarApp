package com.idslatam.solmar.BravoPapa;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.idslatam.solmar.R;

public class SoundService extends Service {

    private class WakeHandler extends Handler
    {

        final SoundService this$0;

        public void handleMessage(Message message)
        {
            Log.e("---", "WakeHandler");

            switch (message.what)
            {
                default:
                    return;

                case 0: // '\0'
                    Log.e("", "do playback");
                    doPlayback();
                    return;

                case 1: // '\001'
                    Log.e("", "stopPlayback");
                    stopPlayback();
                    return;


                case 2: // '\002'
                    stopService();
                    return;
            }
        }

        public WakeHandler(Looper looper)
        {
            this$0 = SoundService.this;
        }
    }


    private Handler mHandler;
    private Looper mLooper;
    private MediaPlayer mp;

    public SoundService()
    {
    }

    private void doPlayback()
    {
        if (mp != null)
        {
            try
            {
                mp.release();
            }
            catch (Exception exception) { }
        }
        Log.e("", "do playback");
        mp = MediaPlayer.create(this, R.raw.empty);
        mp.setLooping(true);
        //mp.setWakeMode(this, 1);
        try
        {
            mp.start();
            return;
        }
        catch (IllegalStateException illegalstateexception)
        {
            illegalstateexception.printStackTrace();
        }
    }

    private void stopPlayback()
    {
        if (mp != null)
        {
            mp.stop();
        }
        if (mp != null)
        {
            mp.release();
            mp = null;
        }
        _L2:
        return;

    }

    private void stopService()
    {
        if (mp != null)
        {
            stopPlayback();
        }
        stopForeground(true);
        stopSelf();
    }

    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void onCreate()
    {
        super.onCreate();
        HandlerThread handlerthread = new HandlerThread("background");
        handlerthread.start();
        mLooper = handlerthread.getLooper();
        mHandler = new WakeHandler(mLooper);
        Log.e("---", "Sound Service");
        //prepareReceivers();
    }

    public void onDestroy()
    {
        mLooper.quit();
        android.content.SharedPreferences.Editor editor = getSharedPreferences("com.teliapp.PREFS", 0).edit();
        editor.putBoolean("pref_enable", false);
        editor.commit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int i) {

        i = -1;
        if (intent != null)
        {
            i = intent.getIntExtra("action", -1);
        }

        Message msg = mHandler.obtainMessage(i);
        msg.arg1 = i;
        msg.obj = intent.getIntExtra("action", -1);
        mHandler.dispatchMessage(msg);

        // intent = mHandler.obtainMessage(i);
        // mHandler.dispatchMessage(intent);

        Log.e("--- onStartCommand", String.valueOf(i));
        return START_NOT_STICKY;
    }

}
