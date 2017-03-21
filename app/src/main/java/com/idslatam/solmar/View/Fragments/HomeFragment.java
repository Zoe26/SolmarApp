package com.idslatam.solmar.View.Fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.idslatam.solmar.Dialer.ContactosActivity;
import com.idslatam.solmar.ImageClass.Image;
import com.idslatam.solmar.Menu.MenuAdapter;
import com.idslatam.solmar.R;
import com.idslatam.solmar.View.Code.Scan;

import java.util.ArrayList;
import java.util.Calendar;

import static android.R.attr.fadingEdgeLength;
import static android.R.attr.fragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    Context mContext;

    boolean isFragment = true;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String text) {
        Bundle args = new Bundle();
        args.putString("", text);

        HomeFragment imageFragment = new HomeFragment();
        imageFragment.setArguments(args);

        return imageFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        this.mContext = container.getContext();

        View view = inflater.inflate(R.layout.fragment_home,container,false);

        MenuAdapter adapterViewAndroid = new MenuAdapter(getActivity(), gridViewString, gridViewImageId);
        androidGridView=(GridView)view.findViewById(R.id.grid_view_image_text);
        androidGridView.setAdapter(adapterViewAndroid);
        androidGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int i, long id) {

                Fragment fragment = null;
                Class fragmentClass = null;


                if (gridViewString[+i].equalsIgnoreCase("Alert")){
                    isFragment = true;
                    fragmentClass = SampleFragment.class;

                } else if (gridViewString[+i].equalsIgnoreCase("Image")){
                    isFragment = false;

                    try {

                        startActivity(new Intent(mContext, Image.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else if (gridViewString[+i].equalsIgnoreCase("Jobs")){
                    isFragment = false;
                    //fragmentClass = SampleFragment.class;
                }

                if (gridViewString[+i].equalsIgnoreCase("Bars")){
                    isFragment = false;

                    try {

                        startActivity(new Intent(mContext, Scan.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (gridViewString[+i].equalsIgnoreCase("Llamadas")){
                    isFragment = false;

                    try {

                        startActivity(new Intent(mContext, ContactosActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (isFragment){

                    try {
                        fragment = (Fragment) fragmentClass.newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Insert the fragment by replacing any existing fragment
                    android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit();

                }

                //Toast.makeText(getActivity(), "GridView : " + gridViewString[+i], Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    GridView androidGridView;

    String[] gridViewString = {
            "Alert", "Image", "Jobs", "Bars", "Llamadas", "Mensajes",
    } ;


    int[] gridViewImageId = {
            R.mipmap.aler_ic, R.mipmap.ic_imagea, R.mipmap.ic_imgjobs,
            R.mipmap.ic_barsa, R.mipmap.ic_llamada, R.mipmap.ic_mje,

    };

}
