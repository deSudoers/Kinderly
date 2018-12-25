package com.hackharvard.desudoers.kinderly;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RadioButton;
import android.widget.TextView;

public class WizAddress extends Fragment{
    TextView pageTitle;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstancestate) {
        View view =  inflater.inflate(R.layout.fragment_wiz_address, container, false);
//        pageTitle = view.findViewById(R.id.pageTitle);
//        pageTitle.startAnimation(AnimationUtils.loadAnimation(getContext(),android.R.anim.fade_in));
        return view;
    }
//    public void onRadioButtonClicked(View view) {
//        boolean checked = ((RadioButton) view).isChecked();
//
//        switch(view.getId()) {
//            case R.id.apt:  if (checked){}
//                            break;
//            case R.id.bung: if (checked){}
//                            break;
//        }
//    }
}
