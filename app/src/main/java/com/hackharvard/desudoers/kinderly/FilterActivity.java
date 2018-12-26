package com.hackharvard.desudoers.kinderly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar;

public class FilterActivity extends AppCompatActivity {
    private Button mApplyFilter;
    private PlaceAutocompleteFragment autocompleteFragment;
    private RangeSeekBar<Float> mSeekBarRange;
    private TextView mSeekBarRangeMin;
    private TextView mSeekBarRangeMax;
    private SeekBar mSeekBarRoom;
    private SeekBar mSeekBarCapacity;
    private Switch mAttachedBathroom;
    private LinearLayout linearLayout1;
    private LinearLayout linearLayout2;
    private LinearLayout linearLayout3;
    private View view_location, view_range, view_rooms, view_capacity, view_attachedbathroom, view_clear;
    private ImageView button_location, button_range, button_rooms, button_capacity, button_attachedbathroom, button_clear;
    private EditText text_location, text_range, text_rooms, text_capacity, text_attachedbathroom, text_clear;
    private Point size;

    private SharedPreferences sp_filter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        Display display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);

        mApplyFilter = (Button) findViewById(R.id.filter_button);
        mApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRentActivity();
            }
        });

        linearLayout1 = (LinearLayout) findViewById(R.id.linearlayout);
        linearLayout2 = (LinearLayout) findViewById(R.id.linearlayout3);
        linearLayout3 = (LinearLayout) findViewById(R.id.linearlayout2);

        view_location = getLayoutInflater().inflate(R.layout.filter_button, null);
        view_range = getLayoutInflater().inflate(R.layout.filter_button, null);
        view_rooms = getLayoutInflater().inflate(R.layout.filter_button, null);
        view_capacity = getLayoutInflater().inflate(R.layout.filter_button, null);
        view_attachedbathroom = getLayoutInflater().inflate(R.layout.filter_button, null);
        view_clear = getLayoutInflater().inflate(R.layout.filter_button, null);

        button_location = view_location.findViewById(R.id.ivClearSearchText);
        button_range = view_range.findViewById(R.id.ivClearSearchText);
        button_rooms = view_rooms.findViewById(R.id.ivClearSearchText);
        button_capacity = view_capacity.findViewById(R.id.ivClearSearchText);
        button_attachedbathroom = view_attachedbathroom.findViewById(R.id.ivClearSearchText);
        button_clear = view_clear.findViewById(R.id.ivClearSearchText);
        button_clear.setVisibility(View.INVISIBLE);

        text_location = view_location.findViewById(R.id.etSearchToolbar);
        text_range = view_range.findViewById(R.id.etSearchToolbar);
        text_rooms = view_rooms.findViewById(R.id.etSearchToolbar);
        text_capacity = view_capacity.findViewById(R.id.etSearchToolbar);
        text_attachedbathroom = view_attachedbathroom.findViewById(R.id.etSearchToolbar);
        text_clear = view_clear.findViewById(R.id.etSearchToolbar);
        text_clear.setText("Clear");

        button_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp_filter.edit().putBoolean("location_bool", false).apply();
                autocompleteFragment.setText("");
                refresh();
            }
        });

        button_range.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp_filter.edit().putFloat("minValue", 0.00f).apply();
                sp_filter.edit().putFloat("maxValue", 99.99f).apply();
                sp_filter.edit().putBoolean("range_bool", false).apply();
                mSeekBarRange.setSelectedMinValue(0.0f);
                mSeekBarRange.setSelectedMaxValue(99.99f);
                mSeekBarRangeMin.setText("Rs 0.0k");
                mSeekBarRangeMax.setText("Rs 99.99k");
                refresh();
            }
        });

        button_rooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp_filter.edit().putBoolean("rooms_bool", false).apply();
                mSeekBarRoom.setProgress(0);
                refresh();
            }
        });

        button_capacity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp_filter.edit().putBoolean("capacity_bool", false).apply();
                mSeekBarCapacity.setProgress(0);
                refresh();
            }
        });

        button_attachedbathroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp_filter.edit().putBoolean("attachedbathroom_bool", false).apply();
                mAttachedBathroom.setChecked(false);
                refresh();
            }
        });

        text_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp_filter.edit().putBoolean("location_bool", false).apply();
                sp_filter.edit().putBoolean("range_bool", false).apply();
                sp_filter.edit().putBoolean("rooms_bool", false).apply();
                sp_filter.edit().putBoolean("capacity_bool", false).apply();
                sp_filter.edit().putBoolean("attachedbathroom_bool", false).apply();
                refresh();
            }
        });

        sp_filter = getSharedPreferences("filter", MODE_PRIVATE);

        refresh();

        autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setHint(getString(R.string.search_by_location));
        if(sp_filter.getBoolean("location_bool", false)){
            autocompleteFragment.setText(sp_filter.getString("location", ""));
        }

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                sp_filter.edit().putBoolean("location_bool", true).apply();
                sp_filter.edit().putString("location_name", place.getName().toString()).apply();
                sp_filter.edit().putString("location", place.getAddress().toString()).apply();
                refresh();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
//                Log.i(TAG, "An error occurred: " + status);
            }
        });

        mSeekBarRange = (RangeSeekBar<Float>) findViewById(R.id.rangeSeekbar);
        mSeekBarRange.setRangeValues(0.00f, 99.99f);
        mSeekBarRangeMin = (TextView) findViewById(R.id.seekbarmin);
        mSeekBarRangeMax = (TextView) findViewById(R.id.seekbarmax);
        mSeekBarRangeMin.setText("Rs 0.0k");
        mSeekBarRangeMax.setText("Rs 99.99k");
        sp_filter.edit().putFloat("minValue", 0.0f).apply();
        sp_filter.edit().putFloat("maxValue", 99.99f).apply();

        mSeekBarRange.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Float>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Float minValue, Float maxValue) {
                //Now you have the minValue and maxValue of your RangeSeekbar
                mSeekBarRangeMin.setText("Rs "+minValue+"k");
                mSeekBarRangeMax.setText("Rs "+maxValue+"k");
                sp_filter.edit().putFloat("minValue", minValue).apply();
                sp_filter.edit().putFloat("maxValue", maxValue).apply();
                sp_filter.edit().putBoolean("range_bool", true).apply();
                refresh();
            }
        });

        // Get noticed while dragging
        mSeekBarRange.setNotifyWhileDragging(true);

        mSeekBarRoom = (SeekBar) findViewById(R.id.roomSeekbar);
        mSeekBarCapacity = (SeekBar) findViewById(R.id.capacitySeekbar);
        mAttachedBathroom = (Switch) findViewById(R.id.attachedBathroom);

        mSeekBarRoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    sp_filter.edit().putBoolean("rooms_bool", true).apply();
                    sp_filter.edit().putInt("rooms", progress + 1).apply();
                    refresh();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBarCapacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    sp_filter.edit().putBoolean("capacity_bool", true).apply();
                    sp_filter.edit().putInt("capacity", progress + 1).apply();
                    refresh();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mAttachedBathroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp_filter.edit().putBoolean("attachedbathroom_bool", true).apply();
                sp_filter.edit().putInt("attachedbathroom", mAttachedBathroom.isChecked() ? 1 : 0).apply();
                refresh();
            }
        });
    }

    private void goToRentActivity(){
        Intent i = new Intent(this, RentActivity.class);
        startActivity(i);
    }

    private void refresh(){
        linearLayout1.removeAllViews();
        linearLayout2.removeAllViews();
        linearLayout3.removeAllViews();
        boolean flag = false;
        float width = 0;
        int row = 0;
        if(sp_filter.getBoolean("location_bool", false)){
            flag = true;
            linearLayout1.addView(view_location);
            text_location.setText(sp_filter.getString("location_name", ""));
            width += textToWidth(text_location.getText().toString());
        }

        if(sp_filter.getBoolean("range_bool", false)){
            flag = true;
            text_range.setText(sp_filter.getFloat("minValue", 0.0f)+"k -"+sp_filter.getFloat("maxValue", 99.99f)+"k");
            width += 385;
            if(width > size.x - 200) {
                width = 385;
                linearLayout2.addView(view_range);
                row = 1;
            }
            else
                linearLayout1.addView(view_range);
        }

        if(sp_filter.getBoolean("rooms_bool", false)){
            flag = true;
            text_rooms.setText("Rooms: "+sp_filter.getInt("rooms", 1));
            width += 320;
            if(width > size.x - 200){
                width = 320;
                if(row == 0){
                    row = 1;
                    linearLayout2.addView(view_rooms);
                }
                else {
                    row = 2;
                    linearLayout3.addView(view_rooms);
                }
            }
            else if(row == 0){
                linearLayout1.addView(view_rooms);
            }
            else {
                linearLayout2.addView(view_rooms);
            }
        }

        if(sp_filter.getBoolean("capacity_bool", false)){
            flag = true;
            text_capacity.setText("Capacity: "+sp_filter.getInt("capacity", 1));
            width += 350;
            if(width > size.x - 200){
                width = 350;
                if(row == 0){
                    row = 1;
                    linearLayout2.addView(view_capacity);
                }
                else if(row == 1){
                    row = 2;
                    linearLayout3.addView(view_capacity);
                }
            }
            else if(row == 0){
                linearLayout1.addView(view_capacity);
            }
            else if(row == 1){
                linearLayout2.addView(view_capacity);
            }
            else{
                linearLayout3.addView(view_capacity);
            }
        }

        if(sp_filter.getBoolean("attachedbathroom_bool", false)){
            flag = true;
            text_attachedbathroom.setText(sp_filter.getInt("attachedbathroom", 0) == 1 ? "Attached Bathroom" : "No Bathroom");
            width += textToWidth(text_attachedbathroom.getText().toString());
            if(width > size.x - 200){
                width = textToWidth(text_attachedbathroom.getText().toString());
                if(row == 0){
                    row = 1;
                    linearLayout2.addView(view_attachedbathroom);
                }
                else if(row == 1){
                    row = 2;
                    linearLayout3.addView(view_attachedbathroom);
                }
            }
            else if(row == 0){
                linearLayout1.addView(view_attachedbathroom);
            }
            else if(row == 1){
                linearLayout2.addView(view_attachedbathroom);
            }
            else{
                linearLayout3.addView(view_attachedbathroom);
            }
        }

        if(flag){
            width += 230;
            if(width > size.x - 200){
                if(row == 0){
                    linearLayout2.addView(view_clear);
                }
                else if(row == 1){
                    linearLayout3.addView(view_clear);
                }
            }
            else if(row == 0){
                linearLayout1.addView(view_clear);
            }
            else if(row == 1){
                linearLayout2.addView(view_clear);
            }
            else{
                linearLayout3.addView(view_clear);
            }
        }
    }
    
    private int textToWidth(String text){
        Log.e("sizeee", text.length()*18+110+text);
        return text.length()*18+110;
    }
}
