package com.hackharvard.desudoers.kinderly;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class WizRoomCount extends Fragment{
    TextView pageTitle;
    SharedPreferences sp;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstancestate) {
        View view =  inflater.inflate(R.layout.fragment_wiz_room_count, container, false);
//        pageTitle = view.findViewById(R.id.pageTitle);
//        pageTitle.startAnimation(AnimationUtils.loadAnimation(getContext(),android.R.anim.fade_in))

            //TODO: Dynamically Integreate Number of rooms
//        EditText e = view.findViewById(R.id.room_count);
//        int numOfRooms = Integer.valueOf(e.getText().toString());
//        sp = this.getActivity().getSharedPreferences("letRooms",Context.MODE_PRIVATE);
//        sp.edit().putInt("numOfRooms",numOfRooms);
        return view;
    }
}