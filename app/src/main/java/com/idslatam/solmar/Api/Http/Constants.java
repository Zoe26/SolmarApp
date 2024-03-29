package com.idslatam.solmar.Api.Http;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.location.DetectedActivity;
import com.idslatam.solmar.R;

/**
 * Created by Luis on 22/10/2016.
 */

public class Constants {

    public String URL = "http://solmartest.azurewebsites.net/webapi/";
    //public String URL = "http://190.116.178.163:85/webapi/";//"http://190.116.178.163:85/webapi/"; //"http://solmar.azurewebsites.net/WebApi/";//"http://190.116.178.163:85/webapi/";// "http://solmar.azurewebsites.net/WebApi/"; http://mdmwebapi2.azurewebsites.net/
    //public String URL = "http://190.116.178.163:85/webapiTest/";
    long STATUS_TIMEOUT_SECONDS = 25;
    public static final String PACKAGE_NAME = "com.google.android.gms.location.activityrecognition";

    public static final String BROADCAST_ACTION = PACKAGE_NAME + ".BROADCAST_ACTION";

    public static final String ACTIVITY_EXTRA = PACKAGE_NAME + ".ACTIVITY_EXTRA";

    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES";

    public static final String ACTIVITY_UPDATES_REQUESTED_KEY = PACKAGE_NAME +
            ".ACTIVITY_UPDATES_REQUESTED";

    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 0;

    public static String getActivityString(Context context, int detectedActivityType) {
        Resources resources = context.getResources();
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.on_bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.on_foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.UNKNOWN:
                return resources.getString(R.string.unknown);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            default:
                return resources.getString(R.string.unidentifiable_activity, detectedActivityType);
        }
    }

    public String getURL() {
        return URL;
    }

}
