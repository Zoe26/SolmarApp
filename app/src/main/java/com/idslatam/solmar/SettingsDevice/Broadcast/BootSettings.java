package com.idslatam.solmar.SettingsDevice.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.idslatam.solmar.View.Bienvenido;

public class BootSettings extends BroadcastReceiver {
    public BootSettings() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        try {

            Intent i = new Intent(context, Bienvenido.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

        } catch (Exception e) {
            Log.e("Auto Arranque ", " Falló!");
        }
    }
}
