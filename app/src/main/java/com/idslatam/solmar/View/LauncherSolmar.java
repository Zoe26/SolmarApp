package com.idslatam.solmar.View;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.idslatam.solmar.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class LauncherSolmar extends Activity implements AdapterView.OnItemClickListener {

    private GridView gridview;
    private List<ApplicationInfo> applist = null;
    private PackageManager packageManager = null;
    private ApplicationAdapter listadaptor = null;

    private int currentApiVersion = android.os.Build.VERSION.SDK_INT;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher_solmar);

        gridview = (GridView) findViewById(R.id.grid_view);
        packageManager = getPackageManager();
        new LoadApplications().execute();

    }

    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            applist = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
            Log.e("list size", String.valueOf(applist.size()));
            listadaptor = new ApplicationAdapter(LauncherSolmar.this, R.layout.list_item, applist);
            System.out.println("adapter="+listadaptor);
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result) {

            gridview.setAdapter(listadaptor);
            gridview.setOnItemClickListener(LauncherSolmar.this);

            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        ApplicationInfo app = applist.get(position);
        try {

            Intent intent = packageManager.getLaunchIntentForPackage(app.packageName);

            if (null != intent) {
                startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(LauncherSolmar.this, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(LauncherSolmar.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list) {
        ArrayList<ApplicationInfo> applist = new ArrayList<ApplicationInfo>();
        for (ApplicationInfo info : list) {
            try {
                if (null != packageManager.getLaunchIntentForPackage(info.packageName)) {

                    //Log.e("class name", String.valueOf(info.describeContents()));

                    if (info.packageName.equals("com.idslatam.solmar") ||  info.packageName.equals("com.android.contacts")
                            || info.packageName.equals("com.android.mms") || info.packageName.equals("com.android.settings")
                            || info.packageName.contains("com.jrdcom.torch")){
                        applist.add(info);
                    }


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return applist;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        try
        {
            if(!hasFocus)
            {
                Object service  = getSystemService("statusbar");
                Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                Method collapse;

                //Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                if (currentApiVersion <= 16) {
                    collapse = statusbarManager.getMethod("collapse");
                    collapse.invoke(service);
                    collapse .setAccessible(true);
                    collapse .invoke(service);

                } else {
                    collapse = statusbarManager.getMethod("collapsePanels");
                    collapse.invoke(service);
                    collapse.setAccessible(true);
                    collapse.invoke(service);

                }


                //Method collapse = statusbarManager.getMethod("collapse");

            }
        }
        catch(Exception ex)
        {
            if(!hasFocus)
            {
                try {

                    Object service  = getSystemService("statusbar");
                    Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                    Method collapse;

                    //Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                    if (currentApiVersion <= 16) {
                        collapse = statusbarManager.getMethod("collapse");
                        collapse.invoke(service);
                        collapse.setAccessible(true);
                        collapse.invoke(service);

                    } else {
                        collapse = statusbarManager.getMethod("collapsePanels");
                        collapse.invoke(service);
                        collapse.setAccessible(true);
                        collapse.invoke(service);

                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                ex.printStackTrace();
            }
        }
    }

}
