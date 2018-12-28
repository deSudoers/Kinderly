package com.hackharvard.desudoers.kinderly;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.net.URL;
import java.util.ArrayList;

public class GridImageAdapter extends BaseAdapter {
    private Context mContext;
    public ArrayList<Uri> imgUri = new ArrayList<>();

    public GridImageAdapter(Context c) {

        mContext = c;
    }

    public void getImageURI(Uri inputImage){

        imgUri.add(inputImage);
    }

    public int getCount() {

        return imgUri.size();
    }

    public Object getItem(int position) {

        return null;
    }

    public long getItemId(int position) {

        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(330, 305));
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageURI(imgUri.get(position));
        return imageView;
    }
}