package com.hackharvard.desudoers.kinderly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class FavouriteFragment extends Fragment {
    private CardArrayAdapter cardArrayAdapter;
    private ListView listView;

    private SharedPreferences sp_login;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstancestate) {
        View view =  inflater.inflate(R.layout.fragment_favourite, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        sp_login = getContext().getSharedPreferences("login", MODE_PRIVATE);
        listView = (ListView) getView().findViewById(R.id.cardList);
        listView.setNestedScrollingEnabled(true);
        listView.setDivider(null);
        cardArrayAdapter = new CardArrayAdapter(getContext(), R.layout.list_item_card);

        new QueryTask(getString(R.string.url)+"favourite").execute();
    }

    private void updateCards(String homes){
        Log.e("favourites", homes);

        JSONObject jsonMsg = null;
        try {
            jsonMsg = new JSONObject(homes);
            CardArrayAdapter.cardList.clear();
            cardArrayAdapter.notifyDataSetChanged();
            for (int i = 0; ; i++) {

                try {
                    JSONObject msg = jsonMsg.getJSONObject(String.valueOf(i));
                    String price = (int)msg.getDouble("price")+"";
                    String address = msg.getString("address");
                    int property_id = msg.getInt("property_id");

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
                    Card card = new Card(price, address, property_id, urls, true, getContext());
                    cardArrayAdapter.add(card);
                    cardArrayAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
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

        private String mUrl;

        QueryTask(String url) {
            mUrl = url;
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String data = "";
            JSONObject postData = new JSONObject();
            try{
                Log.e("homes", postData.toString());
                HttpURLConnection httpURLConnection = null;
                try {
                    httpURLConnection = (HttpURLConnection) new URL(mUrl).openConnection();
                    httpURLConnection.addRequestProperty("cookie", sp_login.getString("token2", ""));
                    httpURLConnection.addRequestProperty("cookie", sp_login.getString("token", ""));
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setDoOutput(false);
                    httpURLConnection.setDoInput(true);

                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        data += line;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("favourites", e.toString());
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
