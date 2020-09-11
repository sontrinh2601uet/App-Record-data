package com.example.sensorrecord.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sensorrecord.MainActivity;
import com.example.sensorrecord.R;

import static com.example.sensorrecord.fragment.RecordFragment.mainActivity;

public class DetectFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detected_behavior, container, false);
        mainActivity = (MainActivity) getActivity();
        mainActivity.navigationView.setCheckedItem(R.id.nav_start);

        //Set actionbar title
        mainActivity.setTitle("Behavior Detection");
        return view;
    }


}
