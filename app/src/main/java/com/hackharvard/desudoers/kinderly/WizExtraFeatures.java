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
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class WizExtraFeatures extends Fragment {
    TextView pageTitle;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstancestate) {
        View view =  inflater.inflate(R.layout.fragment_wiz_extra_features,container,false);
        pageTitle = view.findViewById(R.id.pageTitle);

        //For display purpose
        SharedPreferences sp = getActivity().getSharedPreferences("letProperty",Context.MODE_PRIVATE);
        String s = sp.getString("blockNo","default");
        s = s + ", " + sp.getString("building","default");
        s = s + ", " + sp.getString("street","default");
        s = s + ", " + sp.getString("city","default");
        s = s + ", " + sp.getString("state","default");
        s = s + ", " + sp.getString("type","default");
        s = s + ", " + sp.getInt("numOfRooms",0);
        s = sp.getString("roomInfo","default");
        pageTitle.setText(sp.getString("property","default"));

//        try {
//            JSONObject jsonObject = new JSONObject(s);
//            String x = jsonObject.toString();
////            Log.e("ABC",x);
////            pageTitle.setText(x);
//            pageTitle.setText(jsonObject.getBoolean("has_ac")+"");
//        } catch (JSONException e) {
//            Log.e("ABC",e.toString());
//        }

        return view;
        //TODO: Add content for extra features
    }
}
