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
import android.widget.TextView;

public class WizPrice extends Fragment{
    EditText price;
    TextView pageTitle;
    SharedPreferences sp;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstancestate) {
        View view =  inflater.inflate(R.layout.fragment_wiz_price, container, false);
        pageTitle = view.findViewById(R.id.pageTitle);
        price = view.findViewById(R.id.rent_input);
        return view;
    }


    public boolean getPrice()
    {
        SharedPreferences sp = getActivity().getSharedPreferences("letProperty",Context.MODE_PRIVATE);
        String s = price.getText().toString();
        Log.e("ABC",s);
        if(!s.equals(""))
            sp.edit().putInt("propPrice",Integer.valueOf(s)).apply();
        else {
            sp.edit().putInt("propPrice",0).apply();
            return false;
        }
        return true;
    }

    public void showError()
    {
        price.setError(getString(R.string.error_field_required));
        price.requestFocus();
    }
}