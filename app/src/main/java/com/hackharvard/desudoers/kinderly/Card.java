package com.hackharvard.desudoers.kinderly;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class Card {
    private String price;
    private String address;
    private Bitmap image;
    private int property_id;
    private SetBitmapImage set;
    private boolean favourite;
    private int num_rooms;
    private String type;

    public Card(String price, String address, int property_id, List<String> urls, boolean favourite, int num_rooms, String type, Context cxt) {
        set = new SetBitmapImage();
        this.price = price;
        this.address = address;
        this.property_id = property_id;
        if(urls.size() > 0)
            set.execute(cxt.getString(R.string.url)+"static/images/"+this.property_id+"/"+urls.get(0));
        this.num_rooms = num_rooms;
        this.favourite = favourite;
        this.type = type;
    }

    public String getPrice() { return price; }

    public String getAddress() {
        return address;
    }

    public Bitmap getImage() { return image; }

    public int getPropertyId() { return property_id;}

    public boolean isFavourite() {
        return favourite;
    }

    public int getNumRooms() { return num_rooms; }

    public String getType() { return type; }

    public void setFavourite(boolean fav) { favourite = fav; }

    public void stop(){
        set.cancel(true);
    }

    class SetBitmapImage extends AsyncTask<String, Void, Bitmap>  {
        HttpURLConnection httpURLConnection = null;
        URL url = null;
        InputStream inputStream;

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            if(set.isCancelled()) {
                return null;
            }
            try {
                url = new URL(params[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                inputStream = httpURLConnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            image = result;
        }
    }
}