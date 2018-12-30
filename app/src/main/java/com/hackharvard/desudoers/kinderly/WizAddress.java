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

import org.json.JSONObject;

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
        sp.edit().putString("propBlockNo",blockNo.getText().toString()).apply();
        sp.edit().putString("propBuilding",building.getText().toString()).apply();
        sp.edit().putString("propStreet",street.getText().toString()).apply();
        sp.edit().putString("propCity",city.getText().toString()).apply();
        sp.edit().putString("propState",state.getText().toString()).apply();
        String address = blockNo.getText().toString();
        address += ", " + building.getText().toString();
        address += ", " + street.getText().toString();
        address += ", " + city.getText().toString();
        address += ", " + state.getText().toString();
        address += ", India";

        if(state.getText().toString().isEmpty()){
            state.setError(getString(R.string.error_field_required));
            state.requestFocus();
        }
        if(city.getText().toString().isEmpty()){
            city.setError(getString(R.string.error_field_required));
            city.requestFocus();
        }
        if(street.getText().toString().isEmpty()){
            street.setError(getString(R.string.error_field_required));
            street.requestFocus();
        }
        if(building.getText().toString().isEmpty()){
            building.setError(getString(R.string.error_field_required));
            building.requestFocus();
        }
        if(blockNo.getText().toString().isEmpty()){
            blockNo.setError(getString(R.string.error_field_required));
            blockNo.requestFocus();
        }
        sp.edit().putString("propAddress",address).apply();
        if(apt.isChecked())
            sp.edit().putString("propType","Apartment").apply();
        else if(bung.isChecked())
            sp.edit().putString("propType","Bungalow").apply();
        else
            sp.edit().putString("type",null).apply();
    }
}
