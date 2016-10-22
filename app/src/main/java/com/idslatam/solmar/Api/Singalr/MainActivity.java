package com.idslatam.solmar.Api.Singalr;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.idslatam.solmar.R;

public class MainActivity extends AppCompatActivity {

    private final Context mContext = this;
    private SignalRService mService;
    private boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //this.mContext = this;

        Log.e("Signalr", "Main Execute");
        //Platform.loadPlatformComponent(new AndroidPlatformComponent());
        //Intent intent = new Intent();
        //intent.setClass(this, SignalRService.class);
        //bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


        startService(new Intent(this, SignalRService.class));

        Log.e("Signalr", "Main Execute");
    }

    @Override
    protected void onStop() {
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        super.onStop();
    }

    /*
    public void sendMessage(View view) {
        if (mBound) {
            // Call a method from the SignalRService.
            // However, if this call were something that might hang, then this request should
            // occur in a separate thread to avoid slowing down the activity performance.
            EditText editText = (EditText) findViewById(R.id.edit_message);
            if (editText != null && editText.getText().length() > 0) {
                String message = editText.getText().toString();
                mService.sendMessage(message);
            }
        }
    }
    */

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to SignalRService, cast the IBinder and get SignalRService instance
            SignalRService.LocalBinder binder = (SignalRService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
