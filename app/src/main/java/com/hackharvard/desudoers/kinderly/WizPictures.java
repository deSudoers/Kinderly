package com.hackharvard.desudoers.kinderly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

public class WizPictures extends Fragment implements View.OnClickListener{
    TextView pageTitle;
    GridView imageGrid;
    Button camera;
    Button gallery;
    GridImageAdapter gridImageAdapter;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstancestate) {
        View view =  inflater.inflate(R.layout.fragment_wiz_pics, container, false);
        imageGrid = view.findViewById(R.id.image_grid);
        gridImageAdapter = new GridImageAdapter(getActivity());
        imageGrid.setAdapter(gridImageAdapter);
        camera = view.findViewById(R.id.camera);
        gallery = view.findViewById(R.id.gallery);
        gallery.setOnClickListener(this);
        camera.setOnClickListener(this);
        pageTitle = view.findViewById(R.id.pageTitle);

//        imageGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View v,
//                                    int position, long id) {
//                Toast.makeText(getContext(), "" + position,
//                        Toast.LENGTH_SHORT).show();
//            }
//        });

        return view;
    }

    @Override
    public void onClick(View view){
        switch(view.getId())
        {
            case R.id.gallery:
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);
                break;

            case R.id.camera:
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 0);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK){

                    try {
                        Bitmap bitmap = (Bitmap) imageReturnedIntent.getExtras().get("data");
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);


                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        File appDirectory = new File(Environment.getExternalStorageDirectory()+"/"+getString(R.string.app_name));
                        if(!(appDirectory.exists() && appDirectory.isDirectory()))
                        {
                            try{
                                appDirectory.mkdir();
                            }
                            catch (SecurityException e) {
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
                        renderGridView();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                break;
            case 1:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
//                    SharedPreferences sp = getActivity().getSharedPreferences("propertyPics",Context.MODE_PRIVATE);
//                    sp.edit().putString("images",selectedImage.toString()).apply();
                    gridImageAdapter.getImageURI(selectedImage);
                    renderGridView();
                }
                break;
        }
    }

    public void renderGridView()
    {
        imageGrid.setAdapter(gridImageAdapter);
        if(gridImageAdapter.getCount()>=1 && gridImageAdapter.getCount()<=3) {
//            pageTitle.startAnimation(AnimationUtils.loadAnimation(getActivity(),android.R.anim.fade_out));
            pageTitle.setText("A few more would be amazing!");
//            pageTitle.startAnimation(AnimationUtils.loadAnimation(getActivity(),android.R.anim.fade_in));
        }
        if(gridImageAdapter.getCount()>3) {
//            pageTitle.startAnimation(AnimationUtils.loadAnimation(getActivity(),android.R.anim.fade_out));
            pageTitle.setText("Already looks great!");
//            pageTitle.startAnimation(AnimationUtils.loadAnimation(getActivity(),android.R.anim.fade_in));
        }
    }
}