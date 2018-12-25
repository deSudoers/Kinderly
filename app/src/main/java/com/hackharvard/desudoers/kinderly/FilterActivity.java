package com.hackharvard.desudoers.kinderly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
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

    private SharedPreferences sp_filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        mApplyFilter = (Button) findViewById(R.id.filter_button);
        mApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRentActivity();
            }
        });

        sp_filter = getSharedPreferences("filter", MODE_PRIVATE);

        autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setHint(getString(R.string.search_by_location));
        if(sp_filter.getBoolean("location", false)){
            autocompleteFragment.setText(sp_filter.getString("location", ""));
        }

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
            }
        });

        // Get noticed while dragging
        mSeekBarRange.setNotifyWhileDragging(true);

        mSeekBarRoom = (SeekBar) findViewById(R.id.roomSeekbar);
        mSeekBarCapacity = (SeekBar) findViewById(R.id.capacitySeekbar);
        mAttachedBathroom = (Switch) findViewById(R.id.attachedBathroom);

    }

    private void goToRentActivity(){
        Intent i = new Intent(this, RentActivity.class);
        startActivity(i);
    }
}
