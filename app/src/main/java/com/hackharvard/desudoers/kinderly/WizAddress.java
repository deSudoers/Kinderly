package com.hackharvard.desudoers.kinderly;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class WizAddress extends Fragment{
    TextView pageTitle;
    EditText blockNo;
    EditText building;
    EditText street;
    EditText city;
    EditText state;
    RadioButton apt;
    RadioButton bung;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstancestate) {
        View view =  inflater.inflate(R.layout.fragment_wiz_address, container, false);
        blockNo = view.findViewById(R.id.block_no);
        building = view.findViewById(R.id.building);
        street = view.findViewById(R.id.street);
        city = view.findViewById(R.id.city);
        state = view.findViewById(R.id.state);
        apt = view.findViewById(R.id.apt);
        bung = view.findViewById(R.id.bung);
        return view;
    }

    public void getData()
    {
        SharedPreferences sp;
        sp = getActivity().getSharedPreferences("letProperty",Context.MODE_PRIVATE);
        sp.edit().putString("blockNo",blockNo.getText().toString()).apply();
        sp.edit().putString("building",building.getText().toString()).apply();
        sp.edit().putString("street",street.getText().toString()).apply();
        sp.edit().putString("city",city.getText().toString()).apply();
        sp.edit().putString("state",state.getText().toString()).apply();
        if(apt.isChecked())
            sp.edit().putString("type","Apartment").apply();
        else if(bung.isChecked())
            sp.edit().putString("type","Bungalow").apply();
        else
            sp.edit().putString("type",null).apply();
    }
}