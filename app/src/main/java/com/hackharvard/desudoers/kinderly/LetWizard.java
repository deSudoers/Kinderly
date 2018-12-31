package com.hackharvard.desudoers.kinderly;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.net.wifi.aware.WifiAwareSession;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class LetWizard extends AppCompatActivity implements View.OnClickListener{

    Button nextButton;
    Button prevButton;
    int pageNumber = 0;
    int numOfPages = 6;
    int numOfRooms = 1;
    boolean nextRoom=true;
    boolean buttonChanged=false;
    TextView pageTitle;
    private SharedPreferences sp;

    WizRoomCount wrc = new WizRoomCount();
    WizAddress waddr = new WizAddress();
    WizPictures wpics = new WizPictures();
    ArrayList<WizRoom> wr = new ArrayList<>();
    WizExtraFeatures wef = new WizExtraFeatures();
    WizPrice wp = new WizPrice();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_let_wizard);
        nextButton = findViewById(R.id.nextButton);
        prevButton = findViewById(R.id.prevButton);
        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);

        sp = getSharedPreferences("letProperty",MODE_PRIVATE);
        sp.edit().putInt("numOfRooms",numOfRooms).apply();
        sp.edit().putString("propRooms",null).apply();
        sp.edit().putString("propImages",null).apply();
        sp.edit().putString("propId",null).apply();
        sp.edit().putInt("propPrice",0).apply();
        sp.edit().putString("propBlockNo",null).apply();
        sp.edit().putString("propBuilding",null).apply();
        sp.edit().putString("propStreet",null).apply();
        sp.edit().putString("propCity",null).apply();
        sp.edit().putString("propState",null).apply();
        sp.edit().putString("propType",null).apply();

        pageTitle = findViewById(R.id.pageTitle);
        pageTitle.setText(R.string.greetings);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                pageTitle = findViewById(R.id.pageTitle);
                pageTitle.startAnimation(AnimationUtils.loadAnimation(LetWizard.this,android.R.anim.fade_out));
                pageTitle.setText(R.string.address_text);
                pageTitle.startAnimation(AnimationUtils.loadAnimation(LetWizard.this,android.R.anim.fade_in));
            }
        }, 1200);



    }

    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.nextButton:
                updateValues();
                if(pageNumber+1>numOfPages+numOfRooms)
                    break;
                if(pageNumber>3 && pageNumber<=3+numOfRooms+1)
                {
                    if(nextRoom)
                    {
                        pageNumber++;
                        useFragment();
                    }
                }
                else
                {
                    pageNumber++;
                    useFragment();
                }

                break;
            case R.id.prevButton:
                if(pageNumber==0)
                    super.finish();
                if(buttonChanged==true) {
                    Button button = findViewById(R.id.nextButton);
                    button.setBackgroundResource(R.drawable.next);
                    buttonChanged=false;
                }
                pageNumber--;
                getSupportFragmentManager().popBackStackImmediate();
                break;
        }
    }

    private void updateValues() {
        int page = getPage();
        switch (page){
            case 1: waddr.getData();
                    pageNumber = checkNonEmptyData(1)?pageNumber:pageNumber-1;
                    break;
            case 3: numOfRooms = wrc.getNumberOfRooms();
                    sp.edit().putInt("numOfRooms",numOfRooms).apply();
                    if(wr.size()<numOfRooms)
                        for(int i=0;i<numOfRooms;i++) {
                            wr.add(new WizRoom().newInstance(i+1));
                        }
                    sp.edit().putString("roomInfo",null).apply();
                    break;
            case 4: boolean check=false;
                    try {
                        check = wr.get(pageNumber - 4).getData(pageNumber - 3);
                    } catch (Exception e) {
                        Log.e("XYZ", e.toString());
                    }
                    if(!check) {
                        nextRoom = false;
                        wr.get(pageNumber - 4).showError();
                    }
                    else
                        nextRoom=true;
                    break;
            case 5: boolean b = wp.getPrice();
                    if(!b)
                    {
                        nextRoom = false;
                        wp.showError();
                    }
                    else
                        nextRoom=true;


                    break;
        }
    }

    private void useFragment() {
        int page = getPage();
        Button button = findViewById(R.id.nextButton);
        switch (page)
        {
            case 1: loadFragment(waddr);
                    sp.getString("blockNo","");
                    break;
            case 2: loadFragment(wpics);
                    break;
            case 3: loadFragment(wrc);
                    break;
            case 4: loadFragment(wr.get(pageNumber-4));
                    break;
            case 5: if(numOfRooms>0)
                        loadFragment(wp);
                    else
                        pageNumber--;
                    break;
            case 6: loadFragment(wef);
                    button.setBackgroundResource(R.drawable.ic_done);
                    buttonChanged=true;
                    break;
            case 7: pageTitle.setText(R.string.wait);
                    JSONObject property = null;
                    String data = null;
                    String propId = null;
                    try {
                        String addr = sp.getString("propAddress", null);
                        property = new JSONObject();
                        property.put("address",addr);
                        property.put("price",sp.getInt("propPrice",0));
                        property.put("type",sp.getString("propType",null));
                        data = property.toString();
                        property.put("street",sp.getString("propType",null));
                        property.put("city",sp.getString("propType",null));
                        property.put("state",sp.getString("propType",null));
                        Log.e("ABCJ","sending data");
                        propId = new uploadPropertyData(getString(R.string.url)+"property",data,-1).execute((Void)null).get();
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                        Log.e("ABCJ",e.toString());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    JSONObject roomsData = null;
                    JSONObject room = null;
                    try {
                        int id = Integer.parseInt(new JSONObject(propId).getString("property_id"));
                        Log.e("ABC",id+"");
                        String roomStr = sp.getString("propRooms", null);
                        roomsData = new JSONObject(roomStr);
                        for (int i = 0; i < numOfRooms; i++)
                        {
                            room = roomsData.getJSONObject(i+"");
                            room.put("property_id",id);
                            String x = new uploadPropertyData(getString(R.string.url)+"room",room.toString(),i).execute((Void)null).get();
                        }
                    }
                    catch (JSONException e){
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    JSONArray propPics = null;
                    try {
                        int id = Integer.parseInt(new JSONObject(propId).getString("property_id"));
                        Log.e("ABC",id+"");
                        String propPicsStr = sp.getString("propImages", null);
                        propPics = new JSONArray(propPicsStr);
                        String s = propPics.toString();
                        Log.e("ABC_IMG","here"+s);
                        for (int i = 0; i < propPics.length(); i++)
                        {
                            Uri img = Uri.parse(propPics.get(i).toString());
//                            boolean b = new uploadPropertyPicture(img, getString(R.string.url)+"property/"+id+"/image").execute((Void)null).get();
                            new uploadPropertyPicture(img, getString(R.string.url)+"property/"+id+"/image").execute((Void)null);
                        }
                    }
                    catch (JSONException e){
                        e.printStackTrace();
                    }
//                    catch (InterruptedException e) {
//                        e.printStackTrace();
//                    } catch (ExecutionException e) {
//                        e.printStackTrace();
//                    }
                    getSupportFragmentManager().popBackStack(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    break;
        }
    }

    private boolean checkNonEmptyData(int option)
    {
        switch(option)
        {
            case 1:
                String x[] = new String[6];
                x[0] = sp.getString("propBlockNo",null);
                x[1] = sp.getString("propBuilding",null);
                x[2]= sp.getString("propStreet",null);
                x[3] = sp.getString("propCity",null);
                x[4] = sp.getString("propState",null);
                x[5] = sp.getString("propType",null);
                for(int i=0;i<5;i++)
                {
                    if(x[i]==null || x[i].equals(""))
                        return false;
                }
                return true;

            case 2: return false;

            default:    return true;
        }
    }

    private int getPage() {
        int page;
        if(pageNumber > 3 && pageNumber <= 3+numOfRooms)
            page = 4;
        else if(pageNumber>3+numOfRooms)
            page = pageNumber-numOfRooms+1;
        else
            page = pageNumber;
        return page;
    }

    private void loadFragment(Fragment fragment)
    {
        getSupportFragmentManager().beginTransaction().replace(R.id.wizard_container,
                fragment).addToBackStack(null).commit();
    }

    public class uploadPropertyData extends AsyncTask<Void, Void, String> {

        private JSONObject jsonData;
        private String url;
        ProgressDialog pd;
        int type;
        uploadPropertyData(String url,String data,int i) {
            try{
                this.url = url;
                this.type = i;
                jsonData = new JSONObject(data);
            }
            catch(JSONException e)
            {
                e.printStackTrace();
            }
        }

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(LetWizard.this);
            if(type==-1)
                pd.setMessage("Adding Property");
            else
                pd.setMessage("Adding Room "+type);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String data = "";
            try{
                Log.d("PROP", jsonData.toString());
                HttpURLConnection httpURLConnection = null;
                try {
                    httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
                    SharedPreferences sp = getSharedPreferences("login",MODE_PRIVATE);
                    httpURLConnection.addRequestProperty("cookie", sp.getString("token2", ""));
                    httpURLConnection.addRequestProperty("cookie", sp.getString("token", ""));
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);

                    DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                    wr.writeBytes(jsonData.toString());
                    wr.flush();
                    wr.close();

                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        data += line;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("ABC", e.toString());
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
        protected void onPostExecute(final String data) {
            super.onPostExecute(data);
            if (pd.isShowing()){
                pd.dismiss();
            }
            if(!data.equals("")){
                Log.e("ABCJ","data_sent");
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

    private class uploadPropertyPicture extends AsyncTask<Void, Void, Boolean> {

        private final String mProfileUrl;
        private final Uri mUri;
        ProgressDialog pd;
        uploadPropertyPicture(Uri uri, String profileUrl)
        {
            mProfileUrl = profileUrl;
            mUri = uri;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(LetWizard.this);
            pd.setMessage("Uploading photos");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try{
                HttpURLConnection httpURLConnection = null;
                DataOutputStream dataOutputStream = null;

                String twoHyphens = "--";
                String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
                String lineEnd = "\r\n";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                File sourceFile = new File(getPathFromUri(LetWizard.this, mUri));
                try {
                    FileInputStream fileInputStream = new FileInputStream(sourceFile.getPath());
                    httpURLConnection = (HttpURLConnection) new URL(mProfileUrl).openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                    SharedPreferences sp = LetWizard.this.getSharedPreferences("login",Context.MODE_PRIVATE);
                    httpURLConnection.addRequestProperty("cookie", sp.getString("token2", ""));
                    httpURLConnection.addRequestProperty("cookie", sp.getString("token", ""));
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);

                    dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
                    dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + "image" + "\"; filename=\"" + sourceFile.getName() + "\"" + lineEnd);
                    dataOutputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);

                    dataOutputStream.writeBytes(lineEnd);

                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    while (bytesRead > 0) {

                        dataOutputStream.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    }
                    dataOutputStream.writeBytes(lineEnd);
                    dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    String data ="";
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        data += line;
                    }
                    Log.e("ABC_IMG", data);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("ABC_IMG", e.toString());
                    return false;
                }
                finally {
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (pd.isShowing()){
                pd.dismiss();
            }
            onBackPressed();
        }
    }

    public static String getPathFromUri(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}

