package com.hackharvard.desudoers.kinderly;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class LetWizard extends AppCompatActivity {

    Button nextButton;
    Button prevButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_let_wizard);
        nextButton = findViewById(R.id.nextButton);
        prevButton = findViewById(R.id.prevButton);
    }

    private void loadFragment(Fragment fragment,String fname)
    {
        getSupportFragmentManager().beginTransaction().replace(R.id.wizard_container,
                fragment).addToBackStack(fname).commit();
    }
}
