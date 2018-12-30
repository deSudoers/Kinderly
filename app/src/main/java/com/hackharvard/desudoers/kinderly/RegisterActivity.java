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

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via number/password.
 */
public class RegisterActivity extends AppCompatActivity {
    
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mFNameView;
    private EditText mLNameView;
    private EditText mAgeView;
    private EditText mNumberView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private SharedPreferences sp_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sp_login = getSharedPreferences("login", MODE_PRIVATE);

        setupActionBar();
        // Set up the login form.
        mFNameView = (EditText) findViewById(R.id.firstname);
        mLNameView = (EditText) findViewById(R.id.lastname);
        mAgeView = (EditText) findViewById(R.id.age);
        mNumberView = (EditText) findViewById(R.id.number);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        Button mSignUpButton = (Button) findViewById(R.id.sign_up_button);
        mSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid number, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister() {
        String response = "Invalid Attempt";

        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mFNameView.setError(null);
        mLNameView.setError(null);
        mAgeView.setError(null);
        mNumberView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String firstname = mFNameView.getText().toString();
        String lastname = mLNameView.getText().toString();
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

        int age = 0;
        try {
            age = Integer.parseInt(mAgeView.getText().toString());
            if (TextUtils.isEmpty(age + "")) {
                mAgeView.setError(getString(R.string.error_field_required));
                focusView = mAgeView;
                cancel = true;
            }
            if (age > 35 && age < 60){
                mAgeView.setError("People between age 35-60 cannot be part of Kinderly");
                focusView = mAgeView;
                cancel = true;
            }
        }
        catch (NumberFormatException nfe){
            nfe.printStackTrace();
            mAgeView.setError(getString(R.string.error_invalid_age));
            focusView = mAgeView;
            cancel = true;
        }

        if(TextUtils.isEmpty(lastname)){
            mLNameView.setError(getString(R.string.error_field_required));
            focusView = mLNameView;
            cancel = true;
        }

        if(TextUtils.isEmpty(firstname)){
            mFNameView.setError(getString(R.string.error_field_required));
            focusView = mFNameView;
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
            mAuthTask = new UserLoginTask(firstname, lastname, age, number, password, getString(R.string.url)+"register");
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

        private final String mFirstName;
        private final String mLastName;
        private final int mAge;
        private final long mNumber;
        private final String mPassword;
        private final String mUrl;

        UserLoginTask(String firstName, String lastName, int age, long number, String password, String url) {
            mFirstName = firstName;
            mLastName = lastName;
            mAge = age;
            mNumber = number;
            mPassword = password;
            mUrl = url;
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String data = "";
            JSONObject postData = new JSONObject();
            try{
                postData.put("first_name", mFirstName);
                postData.put("last_name", mLastName);
                postData.put("age", mAge);
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
                        data = "Register Successful.";
                    }
                    else if(response == HttpURLConnection.HTTP_BAD_REQUEST){
                        data = "Mobile Number Already Exists.";
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

        @Override
        protected void onPostExecute(final String success) {
            mAuthTask = null;
            showProgress(false);
            switch (success) {
                case "Register Successful.":
                    finish();
                    if(mAge <= 35)
                        goToRentActivity();
                    else
                        goToLetActivity();
                    sp_login.edit().putBoolean("logged", true).apply();
                    break;
                case "Mobile Number Already Exists.":
                    mNumberView.setError(success);
                    mNumberView.requestFocus();
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

    private void goToRentActivity(){
        Intent i = new Intent(this, RentActivity.class);
        startActivity(i);
    }

    private void goToLetActivity(){
        Intent i = new Intent(this, LetActivity.class);
        startActivity(i);
    }
}

