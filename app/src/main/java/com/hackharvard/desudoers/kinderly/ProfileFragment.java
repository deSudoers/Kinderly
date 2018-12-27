package com.hackharvard.desudoers.kinderly;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment implements View.OnClickListener{
    Button changePicGallery;
    Button changePicCamera;
    ImageView proPic;
    TextView name,age,mobileNum;
    ProgressDialog pd;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstancestate) {
        View view =  inflater.inflate(R.layout.fragment_profile,container,false);
        changePicGallery = view.findViewById(R.id.change_pro_pic_gallery);
        changePicCamera = view.findViewById(R.id.change_pro_pic_camera);
        proPic = view.findViewById(R.id.profile_photo);
        changePicGallery.setOnClickListener(this);
        changePicCamera.setOnClickListener(this);
        name = view.findViewById(R.id.user_name);
        age = view.findViewById(R.id.age);
        mobileNum = view.findViewById(R.id.phone_number);
        SharedPreferences sp = getActivity().getSharedPreferences("profile",Context.MODE_PRIVATE);
        String details = sp.getString("data",null);
        if(details == null)
            new JsonTask(getString(R.string.url)+"profile").execute((Void)null);
        else
            updateProfileInfo(details);
        return view;
    }

    public void updateProfileInfo(String details)
    {
        String userName = null;
        String userMobile = null;
        try {
            JSONObject profileData = new JSONObject(details);
            userName = profileData.getJSONObject("user").getString("first_name");
            userName += " " + profileData.getJSONObject("user").getString("last_name");
            userMobile = profileData.getJSONObject("user").getString("mobile");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        finally {
            name.setText(userName);
            mobileNum.setText(userMobile);
            age.setText("180");
        }
    }

    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.change_pro_pic_gallery:
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);
                break;

            case R.id.change_pro_pic_camera:
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 0);
                break;
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
                        proPic.setImageURI(selectedImage);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                break;
            case 1:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    proPic.setImageURI(selectedImage);
                }
                break;
        }
    }


    private class JsonTask extends AsyncTask<Void, Void, String> {

        private final String profileUrl;

        JsonTask(String s)
        {
            profileUrl = s;
        }

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(getActivity());
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String data = "Error";
            try{
                HttpURLConnection httpURLConnection = null;
                try {
                    httpURLConnection = (HttpURLConnection) new URL(profileUrl).openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    SharedPreferences sp = getActivity().getSharedPreferences("login",Context.MODE_PRIVATE);
                    httpURLConnection.addRequestProperty("cookie", sp.getString("token2", ""));
                    httpURLConnection.addRequestProperty("cookie", sp.getString("token", ""));
                    httpURLConnection.setDoOutput(false);
                    httpURLConnection.setDoInput(true);
                    String line;
                    data = "";
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        data += line;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("login", e.toString());
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
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
            updateProfileInfo(result);
            SharedPreferences sp = getActivity().getSharedPreferences("profile",Context.MODE_PRIVATE);
            sp.edit().putString("data",result).apply();
        }
    }
}
