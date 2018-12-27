package com.hackharvard.desudoers.kinderly;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ResponseFragmentLet extends Fragment {

    TextView text;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstancestate) {
        View view = inflater.inflate(R.layout.fragment_responses_let,container,false);
        text = view.findViewById(R.id.pageText);
        SharedPreferences sp = getActivity().getSharedPreferences("letProperty",Context.MODE_PRIVATE);
        text.setText(sp.getString("propertyid","default"));
        return view;

        // TODO: Response page for Let
    }
}
