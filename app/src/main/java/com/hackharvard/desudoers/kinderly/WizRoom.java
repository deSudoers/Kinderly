package com.hackharvard.desudoers.kinderly;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WizRoom extends Fragment{

    int roomNumber;
    TextView pageTitle;

    public static final WizRoom newInstance(int roomNumber) {
        WizRoom wr =  new WizRoom();
        Bundle bundle = new Bundle(1);
        bundle.putInt("roomNumber",roomNumber);
        wr.setArguments(bundle);
        return wr;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstancestate) {
        View view =  inflater.inflate(R.layout.fragment_wiz_room, container, false);
        pageTitle = view.findViewById(R.id.pageTitle);
        pageTitle.setText("Add Room "+getArguments().getInt("roomNumber"));
//        pageTitle.startAnimation(AnimationUtils.loadAnimation(getContext(),android.R.anim.fade_in));
        return view;
    }
}