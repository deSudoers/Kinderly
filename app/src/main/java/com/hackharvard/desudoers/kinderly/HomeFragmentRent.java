package com.hackharvard.desudoers.kinderly;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragmentRent extends Fragment {
    private CardArrayAdapter cardArrayAdapter;
    private ListView listView;
    private SupportPlaceAutocompleteFragment autocompleteFragment;

    private SharedPreferences sp_login, sp_filter;

    private String last_action = "ACTION_MOVE";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstancestate) {
        return inflater.inflate(R.layout.fragment_home_rent,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        sp_login = getContext().getSharedPreferences("login", MODE_PRIVATE);
        sp_filter = getContext().getSharedPreferences("filter", MODE_PRIVATE);

        autocompleteFragment = new SupportPlaceAutocompleteFragment();
        android.support.v4.app.FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment, autocompleteFragment);
        ft.commit();

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                sp_filter.edit().putString("location", place.getAddress().toString()).apply();
                sp_filter.edit().putString("location_name", place.getName().toString()).apply();
                sp_filter.edit().putBoolean("location_bool", true).apply();
                queryHomes();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
//                Log.i(TAG, "An error occurred: " + status);
            }
        });


        listView = (ListView) getView().findViewById(R.id.cardList);
        listView.setNestedScrollingEnabled(true);
        listView.setDivider(null);
        cardArrayAdapter = new CardArrayAdapter(getContext(), R.layout.list_item_card);

        Button mFilterButton = (Button) getView().findViewById(R.id.filter);
        mFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFilterActivity();
            }
        });

        Button mSortButton = (Button) getView().findViewById(R.id.sort);
        mSortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        queryHomes();
    }

    private void goToFilterActivity(){
        Intent i = new Intent(getContext(), FilterActivity.class);
        startActivity(i);
    }

    private void queryHomes(){
//        if(!sp_filter.getBoolean("location_bool", false)){
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            builder.setMessage("Please add a location to continue");
//            // Add the buttons
//            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int id) {
//                    // User clicked OK button
//
//                }
//            });
//            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int id) {
//                    // User cancelled the dialog
//                }
//            });
//            AlertDialog dialog = builder.create();
//            dialog.show();
//            return;
//        }
        String location = sp_filter.getString("location", "MIT");
        float minValue = sp_filter.getFloat("minValue", 0.0f) * 1000;
        float maxValue = sp_filter.getFloat("maxValue", 99.99f) * 1000;
        int rooms = sp_filter.getInt("rooms", 1);
        int capacity = sp_filter.getInt("capacity", 1);
        boolean attachedbathroom = sp_filter.getInt("attachedbathroom", 0) == 1;

        new QueryTask(location, minValue, maxValue, rooms, capacity, attachedbathroom, getString(R.string.url)+"properties").execute();
    }

    private void updateCards(String homes){
        Log.e("homes", homes);

        JSONObject jsonMsg = null;
        try {
            jsonMsg = new JSONObject(homes);
            cardArrayAdapter.clear();
            CardArrayAdapter.cardList.clear();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; ; i++) {

            try {
                JSONObject msg = jsonMsg.getJSONObject(String.valueOf(i));
                String price = "Rs " + msg.getInt("price");
                String address = msg.getString("address");
                String property_id = msg.getString("property_id");

                JSONObject jsonMsgUrl = null;
                try {
                    jsonMsgUrl = new JSONObject(msg.getString("images"));
                }
                catch (JSONException e){
                    e.printStackTrace();
                }

                List<String> urls = new ArrayList<>();
                for(int j = 0; ; j++){
                    try {
                        urls.add(jsonMsgUrl.getString(String.valueOf(j)));
                    }
                    catch (JSONException e){
                        e.printStackTrace();
                        break;
                    }
                }

                JSONObject jsonMsgRoom = null;
                try {
                    jsonMsgRoom = new JSONObject(msg.getString("rooms"));
                }
                catch (JSONException e){
                    e.printStackTrace();
                }

                List<Room> rooms = new ArrayList<>();
                for(int j = 0; ; j++){
                    try {
                        JSONObject jsonMsgRoomDetails = new JSONObject(jsonMsgRoom.getString(String.valueOf(j)));
                        String room_id = jsonMsgRoomDetails.getString("room_id");
                        int capacity = jsonMsgRoomDetails.getInt("capacity");
                        boolean attachedbathroom = jsonMsgRoomDetails.getBoolean("has_attach_bath");
                        boolean ac = jsonMsgRoomDetails.getBoolean("has_ac");
                        rooms.add(new Room(room_id, capacity, attachedbathroom, ac));
                    }
                    catch (JSONException e){
                        e.printStackTrace();
                        break;
                    }
                }
                Card card = null;
                card = new Card(price, address, property_id, urls, rooms);
                cardArrayAdapter.add(card);
            } catch (Exception e) {
                break;
            }
        }
        listView.setAdapter(cardArrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getActivity().finish();
                Intent i = new Intent(getContext(), CardActivity.class);
                i.putExtra("id", position);
                startActivity(i);
            }
        });
    }

    public class QueryTask extends AsyncTask<Void, Void, String> {

        private final String mLocation;
        private final float mMinValue;
        private final float mMaxValue;
        private final int mRooms;
        private final int mCapacity;
        private final boolean mAttachedBathroom;
        private String mUrl;

        QueryTask(String location, float minValue, float maxValue, int rooms, int capacity, boolean attachedbathroom, String url) {
            mLocation = location;
            mMinValue = minValue;
            mMaxValue = maxValue;
            mRooms = rooms;
            mCapacity = capacity;
            mAttachedBathroom = attachedbathroom;
            mUrl = url;
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String data = "";
            JSONObject postData = new JSONObject();
            try{
                postData.put("address", "MIT");
                postData.put("min_price", mMinValue);
                postData.put("max_price", mMaxValue);
                postData.put("rooms", mRooms);
                postData.put("capacity", mCapacity);
                postData.put("attachedbathroom", mAttachedBathroom);

                Log.e("homes", postData.toString());
                HttpURLConnection httpURLConnection = null;
                try {
                    httpURLConnection = (HttpURLConnection) new URL(mUrl).openConnection();
                    httpURLConnection.addRequestProperty("cookie", sp_login.getString("token2", ""));
                    httpURLConnection.addRequestProperty("cookie", sp_login.getString("token", ""));
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                    wr.writeBytes(postData.toString());
                    wr.flush();
                    wr.close();

                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        data += line;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("homes", e.toString());
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
            if(!success.equals("")){
                updateCards(success);
            }
        }

        @Override
        protected void onCancelled() {

        }
    }

}
