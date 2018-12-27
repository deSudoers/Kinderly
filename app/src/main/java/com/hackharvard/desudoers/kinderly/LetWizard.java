package com.hackharvard.desudoers.kinderly;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.aware.WifiAwareSession;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class LetWizard extends AppCompatActivity implements View.OnClickListener{

    Button nextButton;
    Button prevButton;
    int pageNumber = 0;
    int numOfPages = 5;
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
        sp.edit().putString("propRooms",null).apply();

        pageTitle = findViewById(R.id.pageTitle);
        pageTitle.setText(R.string.greetings);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                pageTitle = findViewById(R.id.pageTitle);
                pageTitle.startAnimation(AnimationUtils.loadAnimation(LetWizard.this,android.R.anim.fade_out));
                pageTitle.setText(R.string.address_text);
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
                        wr.get(pageNumber-4).getData(pageNumber-3);
                    }
                    catch(Exception e) {
                        Log.e("XYZ",e.toString());
                    }
                    break;
        }
    }

    private void useFragment() {
        int page = getPage();
        Button button = findViewById(R.id.nextButton);
        button.setText(R.string.next);
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
                    {
                        loadFragment(wef);
                        button.setText(R.string.finish);
                    }
                    else
                        pageNumber--;
                    break;
            case 6: JSONObject property = null;
                    try {
                        String addr = sp.getString("propAddress", null);
                        JSONObject rooms = new JSONObject(sp.getString("propRooms", null));
                        property = new JSONObject();
                        property.put("address",addr);
                        property.put("price",20000);
                        property.put("rooms",rooms);
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                        Log.e("ABCJ",e.toString());
                    }
                    finally {
                        new uploadPropertyData(getString(R.string.url)+"property",property.toString()).execute((Void)null);
//                        sp.edit().putString("propertyid",property.toString()).apply();
                    }
                    super.finish();
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

    public class uploadPropertyData extends AsyncTask<Void, Void, String> {

        private JSONObject jsonData;
        private String url;
        ProgressDialog pd;
        uploadPropertyData(String url,String data) {
            try{
                this.url = url;
                jsonData = new JSONObject(data);
            }
            catch(JSONException e)
            {
                e.printStackTrace();
            }
        }

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(LetWizard.this);
            pd.setMessage("Adding Property");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String data = "";
            try{
                Log.d("PROP", jsonData.toString());
                HttpURLConnection httpURLConnection = null;
                try {
                    httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
                    SharedPreferences sp = getSharedPreferences("login",MODE_PRIVATE);
                    httpURLConnection.addRequestProperty("cookie", sp.getString("token2", ""));
                    httpURLConnection.addRequestProperty("cookie", sp.getString("token", ""));
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);

                    DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                    wr.writeBytes(jsonData.toString());
                    wr.flush();
                    wr.close();

                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        data += line;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("ABC", e.toString());
                }
                finally {
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(final String data) {
            super.onPostExecute(data);
            if (pd.isShowing()){
                pd.dismiss();
            }
            if(!data.equals("")){
                SharedPreferences sp = getSharedPreferences("letProperty",MODE_PRIVATE);
                Log.e("ABC",data);
                sp.edit().putString("propertyid",data).apply();
            }
        }

        @Override
        protected void onCancelled() {

        }
    }
}
