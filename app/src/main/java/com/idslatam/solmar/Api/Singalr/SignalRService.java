package com.idslatam.solmar.Api.Singalr;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.idslatam.solmar.Models.Crud.TrackingCrud;
import com.idslatam.solmar.Models.Database.DBHelper;
import com.idslatam.solmar.Models.Entities.Tracking;

import java.util.concurrent.ExecutionException;

import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.ErrorCallback;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;

/**
 * Created by ronaldsalazar on 10/21/16.
 */

public class SignalRService extends Service {
    @Nullable

    private HubConnection mHubConnection;
    private HubProxy mHubProxy;
    private Handler mHandler; // to display Toast message
    private final IBinder mBinder = new LocalBinder(); // Binder given to client
    private int countConex=0, _Tracking_Id=0;
    Context mContext;

    public SignalRService() {
        Log.e("Signalr", "onCreate");
    }

    @Override
    public void onCreate() {
        Log.e("Signalr", "onCreate");
        super.onCreate();
        this.mContext = this;
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Signalr", "on start");
        int result = super.onStartCommand(intent, flags, startId);
        startSignalR();
        Log.e("Signalr", "Execute");
        return result;
    }

    @Override
    public void onDestroy() {
        mHubConnection.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service.
        startSignalR();
        return mBinder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public SignalRService getService() {
            // Return this instance of SignalRService so clients can call public methods
            return SignalRService.this;
        }
    }

    /**
     * method for clients (activities)
     */
    public void sendMessage(Tracking marker) {
//        String SERVER_METHOD_SEND = "addMarker";
//        mHubProxy.invoke(SERVER_METHOD_SEND, marker);
//        Log.e("Tracking", marker.Longitud.toString() );
        DBHelper dataBaseHelper = new DBHelper(this);
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
        db.execSQL("UPDATE Configuration SET Longitud = '" + marker.Longitud.toString() + "'");
        db.execSQL("UPDATE Configuration SET Latitud = '" + marker.Latitud.toString() + "'");
        db.close();

        Log.e("SimpleSignalR", mHubConnection.getState().toString());

//        if (countConex > 0){
//            countConex --;
//        }

        if(mHubConnection.getState().toString() == "Disconnected"){

            try {

                TrackingCrud trackingCRUD = new TrackingCrud(this);
                marker.EstadoEnvio = "false";
                marker.TrackingId = _Tracking_Id;
                _Tracking_Id = trackingCRUD.insert(marker);

            }catch (Exception e){}

//            if (countConex==0){
//                countConex = 10;
//            }
        }

        if (mHubConnection.getState().toString() == "Disconnected"){
            try {
                startSignalR();
            } catch (Exception e) {}
        }

        if(mHubConnection.getState().toString()=="Connected"){

//            countConex = 0;

            mHubProxy.invoke(String.class, "addMarker", marker).done(new Action<String>() {
                @Override
                public void run(String s) throws Exception {

                    String[] arrayParametros = s.split(",");

                    for (int i = 0; i < arrayParametros.length; i++) {
                        Log.e("Signal R Intervalo ", arrayParametros[0]);
                        Log.e("Signal R Precision ", arrayParametros[1]);
                    }

                    DBHelper dataBaseHelper = new DBHelper(mContext);
                    SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                    db.execSQL("UPDATE Configuration SET IntervaloTracking = '" + arrayParametros[0] + "'");
                    db.execSQL("UPDATE Configuration SET Precision = '" + arrayParametros[1] + "'");
                    db.close();
                    Log.e("Signal R", s);
                    //Log.e("SimpleSignalR", mHubConnection.getState().toString());
                }
            }).onError(new ErrorCallback() {
                @Override
                public void onError(Throwable throwable) {
                    Log.e("SimpleSignalR", throwable.toString());
                }
            });
        }
    }

    private void startSignalR() {

        Platform.loadPlatformComponent(new AndroidPlatformComponent());
        /*Credentials credentials = new Credentials() {
            @Override
            public void prepareRequest(Request request) {
                request.addHeader("User-Name", "BNK");
            }
        };*/

        String serverUrl = "http://solmar.azurewebsites.net/";
        mHubConnection = new HubConnection(serverUrl);
        //mHubConnection.setCredentials(credentials);
        String SERVER_HUB_CHAT = "trackingHub";
        mHubProxy = mHubConnection.createHubProxy(SERVER_HUB_CHAT);
        ClientTransport clientTransport = new ServerSentEventsTransport(mHubConnection.getLogger());
        SignalRFuture<Void> signalRFuture = mHubConnection.start(clientTransport);
        Log.e("Signal R", signalRFuture.toString());
        try {
            signalRFuture.get();
            Log.e("Try", "startSignalR");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Log.e("Close SignalR", "Closed 1");
            return;
        }

        /*
        String CLIENT_METHOD_BROADAST_MESSAGE = "broadcastMessage";
        mHubProxy.on(CLIENT_METHOD_BROADAST_MESSAGE,
                new SubscriptionHandler1<Tracking>() {
                    @Override
                    public void run(final Tracking msg) {
                        final String finalMsg = msg.Classx;
                        // display Toast message
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("Signal R RespMsg", finalMsg);
                                Toast.makeText(getApplicationContext(), finalMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                , Tracking.class);*/
    }

}