package com.hackharvard.desudoers.kinderly;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class HomeFragmentLet extends Fragment implements View.OnClickListener {

    Button button;
    public HomeFragmentLet() {

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstancestate) {
        View view =  inflater.inflate(R.layout.fragment_home_let, container, false);
        button = (Button) view.findViewById(R.id.let_button);
        button.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view)
    {
        Intent i = new Intent(view.getContext(),LetWizard.class);
        startActivity(i);
    }

}
