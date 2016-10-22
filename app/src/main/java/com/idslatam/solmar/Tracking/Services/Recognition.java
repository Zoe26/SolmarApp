package com.idslatam.solmar.Tracking.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.idslatam.solmar.Api.Http.Constants;

import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class Recognition extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    protected static final String TAG = "Recognition";

    public Recognition() {super(TAG);}


    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
        double actividadMayor =0;
        int iposition=0;
        String actividadM = null;

        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        for (DetectedActivity da: detectedActivities) {
            if(da.getConfidence()>=actividadMayor)
            {
                actividadMayor = da.getConfidence();
                iposition = detectedActivities.indexOf(da);
                actividadM = Constants.getActivityString(getApplicationContext(), da.getType());
            }
        }

        detectedActivities.get(iposition).getType();
        detectedActivities.get(iposition);

//        try {
//
//            DBHelper dataBaseHelper = new DBHelper(this);
//            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
//            db.execSQL("UPDATE Configuration SET TipoActividad = '" + actividadM + "'");
//            db.close();
//
//        } catch (Exception e) {}

        Log.e("---| Actividad | " + actividadM , " " + String.valueOf(actividadMayor));

        // Broadcast the list of detected activities.
        localIntent.putExtra(Constants.ACTIVITY_EXTRA, detectedActivities);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

}