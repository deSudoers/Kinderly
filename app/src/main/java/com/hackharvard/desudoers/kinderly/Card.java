package com.hackharvard.desudoers.kinderly;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.net.URL;

public class Card {
    private String line1;
    private String line2;
    private Bitmap[] images;

    public Card(String line1, String line2, String[] urls) {
        this.line1 = line1;
        this.line2 = line2;
        this.images = new Bitmap[urls.length];
        for(int i = 0; i < urls.length; ++i){
            this.images[i] = null;
            new setBitmapImage().execute(urls[i], i+"");
        }
    }

    public String getLine1() { return line1; }

    public String getLine2() {
        return line2;
    }

    public Bitmap[] getImages() { return images; }

    class setBitmapImage extends AsyncTask<String, Void, Bitmap> {
        private int index = 0;
        @Override
        protected Bitmap doInBackground(String... params) {
            index = Integer.parseInt(params[1]);
            Bitmap bitmap = null;
            try {
                URL url = new URL(params[0]);
                bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            images[index] = result;
        }
    }
}
