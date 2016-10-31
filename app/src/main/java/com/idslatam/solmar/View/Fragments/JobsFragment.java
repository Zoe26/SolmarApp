package com.idslatam.solmar.View.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idslatam.solmar.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class JobsFragment extends Fragment {


    public JobsFragment() {
        // Required empty public constructor
    }

    public static JobsFragment newInstance(String text) {
        Bundle args = new Bundle();
        args.putString("", text);

        JobsFragment imageFragment = new JobsFragment();
        imageFragment.setArguments(args);

        return imageFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_jobs, container, false);
    }

}
