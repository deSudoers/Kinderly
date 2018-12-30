package com.hackharvard.desudoers.kinderly;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class WizPictures extends Fragment{
    TextView pageTitle;
    GridView imageGrid;
    Button camera;
    Button gallery;
    GridImageAdapter gridImageAdapter;
    ArrayList<String> inputImages;
    String inputImage;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstancestate) {
        View view =  inflater.inflate(R.layout.fragment_wiz_pics, container, false);
        imageGrid = view.findViewById(R.id.image_grid);
        gridImageAdapter = new GridImageAdapter(getActivity());
        gridImageAdapter.getImageURI(null);
        imageGrid.setAdapter(gridImageAdapter);
        pageTitle = view.findViewById(R.id.pageTitle);
        viewAddedPictures();

        imageGrid.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View v,int position, long id)
            {

                if(position == gridImageAdapter.getCount()-1)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Add property pictures");
                    builder.setMessage("Select images using");
                    builder.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent,"Select Picture"),1);
                        }
                    });
                    builder.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 0);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else
                {
                    gridImageAdapter.imgUri.remove(position);
                    removeImageUri(position);
                    renderGridView();
                }
            }
        });

        return view;
    }


    public void viewAddedPictures() {
        SharedPreferences sp = getActivity().getSharedPreferences("letProperty",Context.MODE_PRIVATE);
        String strImages = sp.getString("propImages",null);
        Log.e("LOG_IMG","here");
        if(strImages!=null)
        {
            try{
                JSONArray images = new JSONArray(strImages);
                for(int i=0;i<images.length();i++)
                {
                    Uri imageUri = Uri.parse(images.get(i).toString());
                    Log.e("LOG_IMG",i+"");
                    gridImageAdapter.getImageURI(imageUri);
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void renderGridView()
    {
        imageGrid.setAdapter(gridImageAdapter);
        if(gridImageAdapter.getCount()>=1 && gridImageAdapter.getCount()<=4) {
            pageTitle.setText("A few more would be amazing!");
        }
        if(gridImageAdapter.getCount()>4) {
            pageTitle.setText("Already looks great!");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {

                    try {
                        Bitmap bitmap = (Bitmap) imageReturnedIntent.getExtras().get("data");
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);


                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        File appDirectory = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name));
                        if (!(appDirectory.exists() && appDirectory.isDirectory())) {
                            try {
                                appDirectory.mkdir();
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            }
                        }
                        File destination = new File(Environment.getExternalStorageDirectory() + "/" +
                                getString(R.string.app_name), "IMG-" + timeStamp + ".jpg");
                        FileOutputStream fo;
                        try {
                            destination.createNewFile();
                            fo = new FileOutputStream(destination);
                            fo.write(bytes.toByteArray());
                            fo.flush();
                            fo.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String imgPath = destination.getAbsolutePath();
                        Uri selectedImage = Uri.parse(imgPath);
                        gridImageAdapter.getImageURI(selectedImage);
                        addImageUri(selectedImage);
                        renderGridView();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    inputImages = new ArrayList<String>();
                    if (imageReturnedIntent.getData() != null) {
                        Uri mImageUri = imageReturnedIntent.getData();
                        Cursor cursor = getActivity().getContentResolver().query(mImageUri,
                                filePathColumn, null, null, null);
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        inputImage = cursor.getString(columnIndex);
                        cursor.close();
                        gridImageAdapter.getImageURI(mImageUri);
                        addImageUri(mImageUri);
                    } else {
                        if (imageReturnedIntent.getClipData() != null) {
                            ClipData mClipData = imageReturnedIntent.getClipData();
                            ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                            for (int i = 0; i < mClipData.getItemCount(); i++) {
                                ClipData.Item item = mClipData.getItemAt(i);
                                Uri uri = item.getUri();
                                mArrayUri.add(uri);

                                Cursor cursor = getActivity().getContentResolver().query(uri, filePathColumn, null, null, null);

                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                inputImage = cursor.getString(columnIndex);
                                inputImages.add(inputImage);
                                cursor.close();
                            }
                            Log.v("LOG_TAG", "Selected Images" + mArrayUri.size());
                            for(Uri i:mArrayUri) {
                                gridImageAdapter.getImageURI(i);
                                addImageUri(i);
                            }
                        }
                    }
                    renderGridView();
                }
        }
    }


    private void addImageUri(Uri imageUri)
    {
        SharedPreferences sp = getActivity().getSharedPreferences("letProperty",Context.MODE_PRIVATE);
        String strImages = sp.getString("propImages",null);
        try {

            if(strImages == null)
            {
                JSONArray images = new JSONArray();
                images.put(imageUri);
                Log.e("LOG_IMG",images.toString());
                sp.edit().putString("propImages",images.toString()).apply();
            }
            else
            {
                JSONArray images = new JSONArray(strImages);
                images.put(imageUri);
                Log.e("LOG_IMG",images.toString());
                sp.edit().putString("propImages",images.toString()).apply();
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void removeImageUri(int position)
    {
        SharedPreferences sp = getActivity().getSharedPreferences("letProperty",Context.MODE_PRIVATE);
        String strImages = sp.getString("propImages",null);
        try {

            if(strImages == null)
            {
                JSONArray images = new JSONArray();
                images.remove(gridImageAdapter.getCount()-position-1);
                Log.e("LOG_IMG",images.toString());
                sp.edit().putString("propImages",images.toString()).apply();
            }
            else
            {
                JSONArray images = new JSONArray(strImages);
                images.remove(gridImageAdapter.getCount()-position-1);
                Log.e("LOG_IMG",images.toString());
                sp.edit().putString("propImages",images.toString()).apply();
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}