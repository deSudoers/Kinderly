package com.hackharvard.desudoers.kinderly;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class WizRoom extends Fragment{

    TextView pageTitle;
    EditText capacity;
    RadioButton yes,no,ac_yes,ac_no,h_yes,h_no;

    public final WizRoom newInstance(int roomNumber) {
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
        capacity = view.findViewById(R.id.capacity);
        yes = view.findViewById(R.id.yes);
        no = view.findViewById(R.id.no);
        ac_yes = view.findViewById(R.id.ac_yes);
        ac_no = view.findViewById(R.id.ac_no);
        h_yes = view.findViewById(R.id.h_yes);
        h_no = view.findViewById(R.id.h_no);
//        pageTitle.startAnimation(AnimationUtils.loadAnimation(getContext(),android.R.anim.fade_in));
        return view;
    }

    public void getData()
    {
        SharedPreferences sp;
        sp = getActivity().getSharedPreferences("letProperty", Context.MODE_PRIVATE);
        int cap = Integer.valueOf(capacity.getText().toString());
        boolean ab = checkOption(yes,no);
        boolean ac = checkOption(ac_yes,ac_no);
        boolean h = checkOption(h_yes,h_no);
        String roomInfo = sp.getString("roomInfo",null);
        /*
        if(roomInfo==null)
            roomInfo = cap + ", ab: " + ab + ", ac: " + ac + ", heater: " + h + "\n";
        else
            roomInfo += cap + ", ab: " + ab + ", ac: " + ac + ", heater: " + h + "\n";
        */
        roomInfo = "{\"property_id\":12345,\"capacity\":"+cap+",\"has_attach_bath\":"+ab+",\"has_ac\":"+ac+",\"has_heater\":"+h+"}";
        sp.edit().putString("roomInfo", roomInfo).apply();
    }

    private boolean checkOption(RadioButton yes,RadioButton no) {
        if(yes.isChecked())
            return true;
        else
            return false;
    }
}