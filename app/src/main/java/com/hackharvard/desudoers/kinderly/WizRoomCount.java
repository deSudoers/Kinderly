package com.hackharvard.desudoers.kinderly;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class WizRoomCount extends Fragment{
    EditText room_count;
    TextView pageTitle;
    SharedPreferences sp;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstancestate) {
        View view =  inflater.inflate(R.layout.fragment_wiz_room_count, container, false);
        pageTitle = view.findViewById(R.id.pageTitle);
//        pageTitle.startAnimation(AnimationUtils.loadAnimation(getContext(),android.R.anim.fade_in))
        room_count = view.findViewById(R.id.room_count);
        return view;
    }


    public int getNumberOfRooms()
    {
        int numOfRooms;
        String s = room_count.getText().toString();
        if(!s.equals(""))
            numOfRooms = Integer.valueOf(s);
        else {
            numOfRooms = 0;
            room_count.setError(getString(R.string.error_field_required));
            room_count.requestFocus();
        }
        return numOfRooms;
    }
}