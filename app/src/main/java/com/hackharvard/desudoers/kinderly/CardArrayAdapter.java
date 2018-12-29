package com.hackharvard.desudoers.kinderly;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CardArrayAdapter  extends ArrayAdapter<Card> {
    private static final String TAG = "CardArrayAdapter";
    static List<Card> cardList = new ArrayList<Card>();

    static class CardViewHolder {
        TextView line1;
        TextView line2;
        ImageView image;
        ImageView rating;
    }

    public CardArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    @Override
    public void add(Card object) {
        cardList.add(object);
        super.add(object);
    }

    @Override
    public int getCount() {
        return this.cardList.size();
    }

    @Override
    public Card getItem(int index) {
        return this.cardList.get(index);
    }

    static public Card getCard(int index) { return cardList.get(index); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final CardViewHolder viewHolder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.list_item_card, parent, false);
            viewHolder = new CardViewHolder();
            viewHolder.line1 = (TextView) row.findViewById(R.id.price_line);
            viewHolder.line2 = (TextView) row.findViewById(R.id.address_line);
            viewHolder.image = (ImageView) row.findViewById(R.id.view_pager);
            viewHolder.rating = (ImageView) row.findViewById(R.id.rating);
            row.setTag(viewHolder);
        } else {
            viewHolder = (CardViewHolder)row.getTag();
        }
        final Card card = getItem(position);

        viewHolder.line1.setText(card.getPrice());
        viewHolder.line2.setText(card.getAddress());
//        ImageAdapter adapter = new ImageAdapter(getContext(), card.getImages());
//        viewHolder.image.setAdapter(adapter);
        viewHolder.image.setImageBitmap(card.getImages()[0]);
        viewHolder.rating.setImageDrawable(getContext().getDrawable(card.isFavourite() ? R.drawable.star_brown : R.drawable.star_grey));

        viewHolder.rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean set = card.isFavourite();
                SetFavourite setFavourite = new SetFavourite(!set, card.getPropertyId(), getContext().getString(R.string.url)+"favourite", getContext());
                try {
                    if (setFavourite.execute().get()) {
                        card.setFavourite(!set);
                        viewHolder.rating.setImageDrawable(getContext().getDrawable(set ? R.drawable.star_grey : R.drawable.star_brown));
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        return row;
    }
}

class SetFavourite extends AsyncTask<Void, Void, Boolean> {

    private final String mPropertyId;
    private final boolean mFavourite;
    private final String mUrl;
    private SharedPreferences sp_login;

    SetFavourite(boolean favourite, String propertyId, String url, Context cxt) {
        mFavourite = favourite;
        mPropertyId = propertyId;
        mUrl = url;
        sp_login = cxt.getSharedPreferences("login", Context.MODE_PRIVATE);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // TODO: attempt authentication against a network service.
        boolean result = false;
        JSONObject postData = new JSONObject();
        try{
            postData.put("property_id", mPropertyId);
            postData.put("favourite", mFavourite);
            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) new URL(mUrl).openConnection();
                httpURLConnection.addRequestProperty("cookie", sp_login.getString("token2", ""));
                httpURLConnection.addRequestProperty("cookie", sp_login.getString("token", ""));
                httpURLConnection.setRequestMethod("PUT");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                int response = httpURLConnection.getResponseCode();
                if(response == HttpURLConnection.HTTP_OK){
                    result = true;
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("favourite", e.toString());
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

        return result;
    }
}