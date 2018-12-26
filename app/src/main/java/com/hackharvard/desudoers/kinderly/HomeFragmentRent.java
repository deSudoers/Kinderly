package com.hackharvard.desudoers.kinderly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
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

import org.w3c.dom.Text;

import java.net.URL;

public class HomeFragmentRent extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstancestate) {
        return inflater.inflate(R.layout.fragment_home_rent,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        SupportPlaceAutocompleteFragment autocompleteFragment = new SupportPlaceAutocompleteFragment();
        android.support.v4.app.FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment, autocompleteFragment);
        ft.commit();

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
//                Log.i(TAG, "An error occurred: " + status);
            }
        });

        ListView listView = (ListView) getView().findViewById(R.id.cardList);
        listView.setNestedScrollingEnabled(true);
        listView.setDivider(null);
        CardArrayAdapter cardArrayAdapter = new CardArrayAdapter(getContext(), R.layout.list_item_card);

        String url = "https://www.gettyimages.ie/gi-resources/images/Homepage/Hero/UK/CMS_Creative_164657191_Kingfisher.jpg";
        String url2 = "https://nroer.gov.in/media/e/a/c/c4ce9ba9211d05e48f0bf447a346dde5f0c79a31c163d540ce9c282b43138.jpeg";
        String urls[] = {url, url2};

        for (int i = 0; i < 10; i++) {

            Card card = null;
            card = new Card("Card " + (i+1) + " Line 1", "Card " + (i+1) + " Line 2", urls);
            cardArrayAdapter.add(card);
        }
        listView.setAdapter(cardArrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getContext(), CardActivity.class);
                startActivity(i);
            }
        });

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
    }

    private void goToFilterActivity(){
        Intent i = new Intent(getContext(), FilterActivity.class);
        startActivity(i);
    }
}
