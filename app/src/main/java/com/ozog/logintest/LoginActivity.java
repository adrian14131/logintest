package com.ozog.logintest;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String URL_BASIC = "http://ux.up.krakow.pl/~adrian.ozog/testlogin.php";
    private static final int PERMISSION_REQUEST_CODE = 123;

    private UserLoginTask mAuthTask = null;

    private View mLoginView;
    private EditText mLoginInput, mTokenInput;
    private Button mLoginButton;
    private ProgressBar mProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SharedPreferences sp=this.getSharedPreferences("Login", MODE_PRIVATE);

        // Set up the login form.
        mLoginView = findViewById(R.id.loginForm);
        mLoginInput = findViewById(R.id.loginInput);
        mTokenInput = findViewById(R.id.tokenInput);
        mLoginButton = findViewById(R.id.loginButton);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        mProgressBar = findViewById(R.id.loginProgress);
        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.INTERNET
        }, PERMISSION_REQUEST_CODE);
        String login = sp.getString("login", null);
        String token = sp.getString("token",null);
        if(login!=null && token!=null){
            mLoginInput.setText(login);
            mTokenInput.setText(token);
        }
    }
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mLoginInput.setError(null);
        mTokenInput.setError(null);

        String login = mLoginInput.getText().toString();
        String token = mTokenInput.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(TextUtils.isEmpty(login) || TextUtils.isEmpty(token)) {
            if (TextUtils.isEmpty(login)){
                mLoginInput.setError("Pole nie może być puste");
                focusView = mLoginInput;
                cancel = true;
            }
            if (TextUtils.isEmpty(token)){
                mTokenInput.setError("Pole nie może być puste");
                focusView = mTokenInput;
                cancel = true;
            }
        }

        if(cancel){
            focusView.requestFocus();
        }
        else {
            showProgress(true);
            mAuthTask = new UserLoginTask(login, token);
            mAuthTask.execute((Void) null);

        }
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

            mLoginView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mLogin;
        private final String mToken;

        UserLoginTask(String login, String token) {
            mLogin = login;
            mToken = token;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            JSONObject jsonObject = null;
            String urlGet = URL_BASIC+"?login="+mLogin+"&token="+mToken;
            try {
                URL url = new URL(urlGet);
                URLConnection connection = url.openConnection();
                InputStream is = connection.getInputStream();
                String response = streamToString(is);
                if(response == null || response == ""){
                    return false;
                }
                jsonObject = new JSONObject(response);
                response = jsonObject.optString("verify");
                Log.e("RESP", "doInBackground: "+response );
                if(response.equals("success")){
                    Thread.sleep(2000);
                    return true;
                }
                else
                {
                    return false;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
                SharedPreferences.Editor ed = sp.edit();
                ed.putString("login", mLogin);
                ed.putString("token", mToken);
                ed.commit();
            } else {
                mTokenInput.setError("Login lub token nie prawidłowy");
                mTokenInput.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

        protected  String streamToString(InputStream is){
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));


                String line = "";

                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return sb.toString();
        }
    }
}

