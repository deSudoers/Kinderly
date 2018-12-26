package com.hackharvard.desudoers.kinderly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.aware.WifiAwareSession;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class LetWizard extends AppCompatActivity implements View.OnClickListener{

    Button nextButton;
    Button prevButton;
    int pageNumber = 0;
    int numOfPages = 4;
    int numOfRooms = 1;
    TextView pageTitle;
    private SharedPreferences sp;

    WizRoomCount wrc = new WizRoomCount();
    WizAddress waddr = new WizAddress();
    WizPictures wpics = new WizPictures();
    ArrayList<WizRoom> wr = new ArrayList<>();
    WizExtraFeatures wef = new WizExtraFeatures();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_let_wizard);
        nextButton = findViewById(R.id.nextButton);
        prevButton = findViewById(R.id.prevButton);
        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);

        sp = getSharedPreferences("letProperty",MODE_PRIVATE);
        sp.edit().putInt("numOfRooms",numOfRooms).apply();

        pageTitle = findViewById(R.id.pageTitle);
        pageTitle.setText("Greetings!");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                pageTitle = findViewById(R.id.pageTitle);
                pageTitle.startAnimation(AnimationUtils.loadAnimation(LetWizard.this,android.R.anim.fade_out));
                pageTitle.setText("Let's start by adding your home address");
                pageTitle.startAnimation(AnimationUtils.loadAnimation(LetWizard.this,android.R.anim.fade_in));
            }
        }, 1200);



    }


    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.nextButton:
                updateValues();
                if(pageNumber+1>numOfPages+numOfRooms)
                    break;
                pageNumber++;
                useFragment();
                break;
            case R.id.prevButton:
                if(pageNumber==0)
                    super.finish();
                pageNumber--;
                getSupportFragmentManager().popBackStackImmediate();
                break;
        }
    }

    private void updateValues() {
        int page = getPage();
        switch (page){
            case 1: waddr.getData();
                    break;
            case 3: numOfRooms = wrc.getNumberOfRooms();
                    sp.edit().putInt("numOfRooms",numOfRooms).apply();
                    if(wr.size()<numOfRooms)
                        for(int i=0;i<numOfRooms;i++) {
                            wr.add(new WizRoom().newInstance(i+1));
                        }
                    sp.edit().putString("roomInfo",null).apply();
                    break;
            case 4: try{
                        wr.get(pageNumber-4).getData();
                    }
                    catch(Exception e) {
                        Log.e("XYZ",e.toString());
                    }
                    break;
        }
    }

    private void useFragment() {
        int page = getPage();
        switch (page)
        {
            case 1: loadFragment(waddr);
                    break;
            case 2: loadFragment(wpics);
                    break;
            case 3: loadFragment(wrc);
                    break;
            case 4: loadFragment(wr.get(pageNumber-4));
                    break;
            case 5: if(numOfRooms>0)
                        loadFragment(wef);
                    else
                        pageNumber--;
                    break;
        }
    }

    private int getPage() {
        int page;
        if(pageNumber > 3 && pageNumber <= 3+numOfRooms)
            page = 4;
        else if(pageNumber>3+numOfRooms)
            page = pageNumber-numOfRooms+1;
        else
            page = pageNumber;
        return page;
    }

    private void loadFragment(Fragment fragment)
    {
        getSupportFragmentManager().beginTransaction().replace(R.id.wizard_container,
                fragment).addToBackStack(null).commit();
    }
}
