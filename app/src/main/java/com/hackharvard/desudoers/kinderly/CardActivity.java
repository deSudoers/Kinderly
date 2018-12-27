package com.hackharvard.desudoers.kinderly;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CardActivity extends AppCompatActivity {
    private int index;
    private Card card;
    private LinearLayout linearLayout;
    private TextView address;
    private TextView price;

    private String property_id;

    private SharedPreferences sp_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        index = bundle.getInt("id");

        card = CardArrayAdapter.getCard(index);

        linearLayout = (LinearLayout) findViewById(R.id.linear_layout);

        for(Bitmap bitmap: card.getImages()){
            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(bitmap);
            linearLayout.addView(imageView);
        }

        sp_login = getSharedPreferences("login", MODE_PRIVATE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        address = (TextView) findViewById(R.id.address_card);
        price = (TextView) findViewById(R.id.price_card);

        property_id = card.getPropertyId();

        try{
            for(int i = 0; ; ++i){
                CardArrayAdapter.cardList.get(i).stop();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        new QueryTask(getString(R.string.url)+"property/"+property_id).execute();
    }

    public void update(String change){
        try{
            JSONObject json = new JSONObject(change);
            address.setText(json.getString("address"));
            price.setText("Rs. "+json.getInt("price"));
        }
        catch (Exception e){

        }
    }

    public class QueryTask extends AsyncTask<Void, Void, String> {

        private String mUrl;

        QueryTask(String url) {
            mUrl = url;
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String data = "";
            try{
                HttpURLConnection httpURLConnection = null;
                try {
                    httpURLConnection = (HttpURLConnection) new URL(mUrl).openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.addRequestProperty("cookie", sp_login.getString("token2", ""));
                    httpURLConnection.addRequestProperty("cookie", sp_login.getString("token", ""));
                    httpURLConnection.setDoOutput(false);
                    httpURLConnection.setDoInput(true);

                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        data += line;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
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
        protected void onPostExecute(final String success) {
            update(success);
        }

        @Override
        protected void onCancelled() {

        }
    }
}
