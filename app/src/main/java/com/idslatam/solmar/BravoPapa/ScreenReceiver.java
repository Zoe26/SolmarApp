package com.idslatam.solmar.BravoPapa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenReceiver extends BroadcastReceiver {
    public ScreenReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("--- INGRESÓ ", "Screen");

        if ("android.intent.action.SCREEN_OFF".equals(intent.getAction()))
        {
            Log.e("---", "Screen off");
            intent = new Intent(context, SoundService.class);
            intent.putExtra("action", 0);
            context.startService(intent);
        }

        if ("android.intent.action.SCREEN_ON".equals(intent.getAction()))
        {
            Log.e("---", "Screen on");
            intent = new Intent(context, SoundService.class);
            intent.putExtra("action", 1);
            context.startService(intent);
        }
    }
}
