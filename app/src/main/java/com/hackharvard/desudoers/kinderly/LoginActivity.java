package com.hackharvard.desudoers.kinderly;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via number/password.
 */
public class LoginActivity extends AppCompatActivity{

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mNumberView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private SharedPreferences sp_login, sp_profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sp_login = getSharedPreferences("login", MODE_PRIVATE);
        sp_profile = getSharedPreferences("profile", MODE_PRIVATE);

        if(sp_login.getBoolean("logged", false)) {
            goToMainActivity();
        }

        // Set up the login form.
        mNumberView = (AutoCompleteTextView) findViewById(R.id.number);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mNumberSignInButton = (Button) findViewById(R.id.number_sign_in_button);
        mNumberSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegisterActivity();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        this.finishAffinity();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid number, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mNumberView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        long number = 0;
        // Check for a valid number address.
        try {
            number = Long.parseLong(mNumberView.getText().toString());
            // Check for a valid number.
            if (TextUtils.isEmpty(number + "")) {
                mNumberView.setError(getString(R.string.error_field_required));
                focusView = mNumberView;
                cancel = true;
            } else if (!isNumberValid(number + "")) {
                mNumberView.setError(getString(R.string.error_invalid_number));
                focusView = mNumberView;
                cancel = true;
            }
        }
        catch (NumberFormatException nfe){
            nfe.printStackTrace();
            mNumberView.setError(getString(R.string.error_invalid_number));
            focusView = mNumberView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(number, password, getString(R.string.url)+"login");
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isNumberValid(String number) {
        return number.length()==10;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final long mNumber;
        private final String mPassword;
        private final String mUrl;

        UserLoginTask(long number, String password, String url) {
            mNumber = number;
            mPassword = password;
            mUrl = url;
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String data = "An Error Occurred. Please Try Again.";
            JSONObject postData = new JSONObject();
            try{
                postData.put("mobile", mNumber);
                postData.put("password", mPassword);
                HttpURLConnection httpURLConnection = null;
                try {
                    httpURLConnection = (HttpURLConnection) new URL(mUrl).openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                    wr.writeBytes(postData.toString());
                    wr.flush();
                    wr.close();
                    String cookie3 = httpURLConnection.getHeaderField(3);
                    String cookie = httpURLConnection.getHeaderField("Set-Cookie");
                    sp_login.edit().putString("token", cookie).apply();
                    sp_login.edit().putString("token2", cookie3).apply();

                    int response = httpURLConnection.getResponseCode();
                    if(response == HttpURLConnection.HTTP_OK){
                        data = "Login Successful.";
                    }
                    else if(response == HttpURLConnection.HTTP_BAD_REQUEST){
                        data = "Login Unsuccessful.";
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
        protected void onPostExecute(final String success) {
            mAuthTask = null;
            showProgress(false);
            Log.e("login", success);
            switch (success) {
                case "Login Successful.":
                    goToMainActivity();
                    break;
                default:
                    Snackbar.make(getWindow().getDecorView().getRootView(), success, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void goToMainActivity(){
        String result;
        try {
            result = new JsonTask(getString(R.string.url) + "profile").execute((Void) null).get();
            if(!result.equals("Error")){
                sp_profile.edit().putString("data",result).apply();
                JSONObject json = new JSONObject(result);
                sp_login.edit().putBoolean("logged", true).apply();
                finish();
                if (json.getJSONObject("user").getInt("age") <= 35) {
                    goToRentActivity();
                } else {
                    goToLetActivity();
                }
            }
        }
        catch (Exception e){

        }
    }

    private void goToRentActivity(){
        Intent i = new Intent(this, RentActivity.class);
        startActivity(i);
    }

    private void goToLetActivity(){
        Intent i = new Intent(this, LetActivity.class);
        startActivity(i);
    }

    private void goToRegisterActivity(){
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }

    private class JsonTask extends AsyncTask<Void, Void, String> {

        private final String profileUrl;

        JsonTask(String s)
        {
            profileUrl = s;
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
                    httpURLConnection.addRequestProperty("cookie", sp_login.getString("token2", ""));
                    httpURLConnection.addRequestProperty("cookie", sp_login.getString("token", ""));
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
    }
}

