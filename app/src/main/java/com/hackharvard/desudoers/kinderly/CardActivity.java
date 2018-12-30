package com.hackharvard.desudoers.kinderly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CardActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Context cxt;
    private int index;
    private Card card;
    private LinearLayout linearLayout;
    private TextView address_view;
    private TextView price_view;
    private LinearLayout linearLayoutRoom;

    private ProgressBar progressBar;
    private FloatingActionButton fab;
    private ImageView rating;

    private SupportMapFragment mapFragment;
    private String address;
    private double latitude, longitude;

    private TextView contact_name_view;
    private TextView contact_number_view;
    private String contact_name;
    private long contact_number;
    private ImageView contact_call;
    private ImageView contact_msg;

    private String property_id;

    private SharedPreferences sp_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        cxt = this;

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        Bundle bundle = getIntent().getExtras();
        index = bundle.getInt("id");

        card = CardArrayAdapter.getCard(index);

        linearLayout = (LinearLayout) findViewById(R.id.linear_layout);

        for(Bitmap bitmap: card.getImages()){
            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(bitmap);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(1200, LinearLayout.LayoutParams.MATCH_PARENT));
            linearLayout.addView(imageView);
        }

        sp_login = getSharedPreferences("login", MODE_PRIVATE);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+contact_number));
                startActivity(intent);
            }
        });

        rating = (ImageView) findViewById(R.id.rating);
        rating.setImageDrawable(getDrawable(card.isFavourite() ? R.drawable.star_brown : R.drawable.star_grey));
        rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean set = card.isFavourite();
                SetFavourite setFavourite = new SetFavourite(!set, card.getPropertyId(), getString(R.string.url)+"favourite", cxt);
                try {
                    if (setFavourite.execute().get()) {
                        card.setFavourite(!set);
                        rating.setImageDrawable(getDrawable(set ? R.drawable.star_grey : R.drawable.star_brown));
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        contact_call = (ImageView) findViewById(R.id.contact_call);
        contact_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+contact_number));
                startActivity(intent);
            }
        });

        contact_msg = (ImageView) findViewById(R.id.contact_msg);
        contact_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("sms:"+contact_number));
                startActivity(intent);
            }
        });

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        address_view = (TextView) findViewById(R.id.address_card);
        price_view = (TextView) findViewById(R.id.price_card);

        linearLayoutRoom = (LinearLayout) findViewById(R.id.rooms_layout);

        contact_name_view = (TextView) findViewById(R.id.contact_name);
        contact_number_view = (TextView) findViewById(R.id.contact_number);

        property_id = card.getPropertyId();

        new QueryTask(getString(R.string.url)+"property/"+property_id).execute();
    }

    public void update(String change){
        Log.e("cardact", change);
        try{
            JSONObject json = new JSONObject(change);
            address = json.getString("address");
            address_view.setText(address);
            price_view.setText("₹  "+json.getInt("price"));
            JSONObject jsonRooms = new JSONObject(json.getString("rooms"));;
            try{
                for(int i = 0;;i++){
                    JSONObject jsonRoom = new JSONObject(jsonRooms.getString(String.valueOf(i)));
                    int capacity = jsonRoom.getInt("capacity");
                    boolean attachedbathroom = jsonRoom.getBoolean("has_attach_bath");
                    boolean ac = jsonRoom.getBoolean("has_ac");
                    View view = getLayoutInflater().inflate(R.layout.list_item_room, null);
                    TextView tv = (TextView) view.findViewById(R.id.price_line);
                    tv.setText("Capacity: "+capacity);
                    ImageView iv2 = (ImageView) view.findViewById(R.id.line2_image);
                    iv2.setImageDrawable(getDrawable(attachedbathroom ? R.drawable.tick : R.drawable.cross));
                    ImageView iv3 = (ImageView) view.findViewById(R.id.line3_image);
                    iv3.setImageDrawable(getDrawable(ac ? R.drawable.tick : R.drawable.cross));
                    TextView tv2 = (TextView) view.findViewById(R.id.room_id);
                    tv2.setText("#Room " + (i+1));
                    linearLayoutRoom.addView(view);
                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }
            String location[] = json.getString("location").split(",");
            latitude = Double.parseDouble(location[0]);
            longitude = Double.parseDouble(location[1]);
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mymap);
            mapFragment.getMapAsync(this);

            JSONObject jsonContact = new JSONObject(json.getString("user"));
            contact_name = jsonContact.getString("first_name")+" "+jsonContact.getString("last_name");
            contact_number = jsonContact.getLong("mobile");
            contact_name_view.setText(contact_name);
            contact_number_view.setText(contact_number+"");
        }
        catch (Exception e){

        }
        ((CoordinatorLayout)progressBar.getParent()).removeView(progressBar);
        fab.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent i = new Intent(this, RentActivity.class);
        startActivity(i);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        GoogleMap mMap = googleMap;
        Marker locationMarker;

        LatLng myLocation = new LatLng(latitude, longitude);
        locationMarker = mMap.addMarker(new MarkerOptions().position(myLocation));
        if(myLocation.longitude == 0 && myLocation.latitude == 0)
        {

        }
        else {

        }

        locationMarker.showInfoWindow();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10));
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
