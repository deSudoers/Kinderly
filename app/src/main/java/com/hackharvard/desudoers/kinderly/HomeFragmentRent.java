package com.hackharvard.desudoers.kinderly;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragmentRent extends Fragment implements SortDialogFragment.SortDialogListener{
    private CardArrayAdapter cardArrayAdapter;
    private ListView listView;
    private SupportPlaceAutocompleteFragment autocompleteFragment;
    private LinearLayout linearLayout;
    private android.support.v7.widget.Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private View view_location, view_range, view_rooms, view_capacity, view_attachedbathroom;
    private EditText text_location, text_range, text_rooms, text_capacity, text_attachedbathroom;


    private SharedPreferences sp_login, sp_filter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstancestate) {
        return inflater.inflate(R.layout.fragment_home_rent,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        sp_login = getContext().getSharedPreferences("login", MODE_PRIVATE);
        sp_filter = getContext().getSharedPreferences("filter", MODE_PRIVATE);

        linearLayout = getView().findViewById(R.id.filter_buttons);

        toolbar = getView().findViewById(R.id.toolbar);
        appBarLayout = getView().findViewById(R.id.app_bar);

        view_location = getLayoutInflater().inflate(R.layout.filter_button_empty, null);
        view_range = getLayoutInflater().inflate(R.layout.filter_button_empty, null);
        view_rooms = getLayoutInflater().inflate(R.layout.filter_button_empty, null);
        view_capacity = getLayoutInflater().inflate(R.layout.filter_button_empty, null);
        view_attachedbathroom = getLayoutInflater().inflate(R.layout.filter_button_empty, null);

        text_location = view_location.findViewById(R.id.etSearchToolbar);
        text_range = view_range.findViewById(R.id.etSearchToolbar);
        text_rooms = view_rooms.findViewById(R.id.etSearchToolbar);
        text_capacity = view_capacity.findViewById(R.id.etSearchToolbar);
        text_attachedbathroom = view_attachedbathroom.findViewById(R.id.etSearchToolbar);

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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getActivity().finish();
                Intent i = new Intent(getContext(), CardActivity.class);
                i.putExtra("id", position);
                startActivity(i);
            }
        });

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
                showNoticeDialog();
            }
        });

        queryHomes();
    }

    @Override
    public void onPause(){
        super.onPause();
        for(int i = 0; i < CardArrayAdapter.cardList.size(); ++i) {
            try {
                CardArrayAdapter.cardList.get(i).stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new SortDialogFragment();
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), "SortDialogFragment");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(int id) {
        // User touched the dialog's positive button
        switch (id){
            case 0: ascending();
                    break;
            case 1: descending();
                    break;
            case 2: favourite();
                    break;
        }
    }

    private void ascending(){
        int size = cardArrayAdapter.getCount();
        Card cards[] = new Card[size];
        CardArrayAdapter.cardList.toArray(cards);
        for(int i = 0; i < size; ++i){
            for(int j=0; j < size - i - 1; ++j){
                if(Double.parseDouble(cards[j].getPrice()) > Double.parseDouble(cards[j+1].getPrice())){
                    Card temp = cards[j];
                    cards[j] = cards[j+1];
                    cards[j+1] = temp;
                }
            }
        }
        CardArrayAdapter.cardList = Arrays.asList(cards);
        cardArrayAdapter.notifyDataSetChanged();
        listView.setAdapter(cardArrayAdapter);
    }

    private void descending(){
        int size = cardArrayAdapter.getCount();
        Card cards[] = new Card[size];
        CardArrayAdapter.cardList.toArray(cards);
        for(int i = 0; i < size; ++i){
            for(int j=0; j < size - i - 1; ++j){
                if(Double.parseDouble(cards[j].getPrice()) < Double.parseDouble(cards[j+1].getPrice())){
                    Card temp = cards[j];
                    cards[j] = cards[j+1];
                    cards[j+1] = temp;
                }
            }
        }
        CardArrayAdapter.cardList = Arrays.asList(cards);
        cardArrayAdapter.notifyDataSetChanged();
        listView.setAdapter(cardArrayAdapter);
    }

    private void favourite(){
        int size = cardArrayAdapter.getCount();
        Card cards[] = new Card[size];
        int start = 0, end = size;
        for(int j = 0; j < size; ++j){
            Card card = cardArrayAdapter.getItem(j);
            if(card.isFavourite())
                cards[start++] = card;
            else
                cards[--end] = card;
        }
        CardArrayAdapter.cardList = Arrays.asList(cards);
        cardArrayAdapter.notifyDataSetChanged();
        listView.setAdapter(cardArrayAdapter);
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
        String location = sp_filter.getString("location_name", "");
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
            CardArrayAdapter.cardList.clear();
            cardArrayAdapter.notifyDataSetChanged();
            for (int i = 0; ; i++) {

                try {
                    JSONObject msg = jsonMsg.getJSONObject(String.valueOf(i));
                    String price = msg.getInt("price")+"";
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
//                boolean favourite = msg.getBoolean("favourite");
                    boolean favourite = true;
                    Card card = null;
                    card = new Card(price, address, property_id, urls, favourite);
                    cardArrayAdapter.add(card);
                    cardArrayAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    break;
                }
            }
            listView.setAdapter(cardArrayAdapter);

            boolean flag = false;
            linearLayout.removeAllViews();
            if(sp_filter.getBoolean("location_bool", false)){
                flag = true;
                linearLayout.addView(view_location);
                text_location.setText(sp_filter.getString("location_name", ""));
            }
            if(sp_filter.getBoolean("range_bool", false)){
                flag = true;
                linearLayout.addView(view_range);
                text_range.setText(sp_filter.getFloat("minValue", 0.0f)+"k - "+ sp_filter.getFloat("maxValue", 99.99f) +"k");
            }
            if(sp_filter.getBoolean("rooms_bool", false)){
                flag = true;
                linearLayout.addView(view_rooms);
                text_rooms.setText("Rooms: " + sp_filter.getInt("rooms", 1));
            }
            if(sp_filter.getBoolean("capacity_bool", false)){
                flag = true;
                linearLayout.addView(view_capacity);
                text_capacity.setText("Capacity: " + sp_filter.getInt("capacity", 1));
            }
            if(sp_filter.getBoolean("attachedbathroom_bool", false)){
                flag = true;
                linearLayout.addView(view_attachedbathroom);
                text_attachedbathroom.setText(sp_filter.getInt("attachedbathroom", 1) == 1 ? "Attached Bathroom" : "No Bathroom");
            }

            if(flag){
                toolbar.setLayoutParams(new CollapsingToolbarLayout.LayoutParams(CollapsingToolbarLayout.LayoutParams.MATCH_PARENT, 430, Gravity.BOTTOM));
                appBarLayout.setLayoutParams(new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, 730));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                if(sp_filter.getBoolean("location_bool", false))
                    postData.put("address", mLocation);
                if(sp_filter.getBoolean("range_bool", false)) {
                    postData.put("min_price", mMinValue);
                    postData.put("max_price", mMaxValue);
                }
                if(sp_filter.getBoolean("rooms_bool", false))
                    postData.put("num_rooms", mRooms);
                if(sp_filter.getBoolean("capacity_bool", false))
                    postData.put("capacity", mCapacity);
                if(sp_filter.getBoolean("attachedbathroom_bool", false))
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
                    Log.e("filter", postData.toString());
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
