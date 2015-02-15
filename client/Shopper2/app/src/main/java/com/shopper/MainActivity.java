package com.shopper;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.shopper.domain.Store;
import com.shopper.extra.Extra;
import com.shopper.util.HttpUtils;

public class MainActivity extends ActionBarActivity {

    private static final String SCOPE =
            "oauth2:https://www.googleapis.com/auth/userinfo.profile";
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;
    private String userEmail;
    private String userName;
    private String accessToken;

    private FrameLayout loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loader = (FrameLayout) findViewById(R.id.loader);
        pickUserAccount();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            Log.d("onActivityResult", "REQUEST_CODE_PICK_ACCOUNT");
            // Receiving a result from the AccountPicker
            TextView textView = (TextView) findViewById(R.id.user_email_field);
            if (resultCode == RESULT_OK) {
                userEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                textView.setText(userEmail);

                AsyncTask<Void, Void, String> task = new OAuthTask();
                task.execute();

            } else if (resultCode == RESULT_CANCELED) {

                Toast.makeText(this, R.string.account_not_selected, Toast.LENGTH_SHORT).show();
                pickUserAccount();
            }
        } else if (requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR) {
            Log.d("onActivityResult", "REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR");
            AsyncTask<Void, Void, String> task = new OAuthTask();
            task.execute();
        }
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    private class OAuthTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {

            loader.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            String token = null;

            try {
                token = GoogleAuthUtil.getToken(MainActivity.this,
                        userEmail,
                        SCOPE);
            } catch (IOException transientEx) {
                transientEx.printStackTrace();
            } catch (UserRecoverableAuthException e) {

                Intent intent = e.getIntent();
                startActivityForResult(intent,
                        REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
            } catch (GoogleAuthException authEx) {
                authEx.printStackTrace();
            }

            return token;
        }

        @Override
        protected void onPostExecute(String token) {
            System.out.println("Access token retrieved:" + token);
            accessToken = token;
            loader.setVisibility(View.GONE);
            AsyncTask<Void, Void, String> task = new UserAccountTask();
            task.execute();
        }
    }

    private class UserAccountTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            loader.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                StringBuilder profile = HttpUtils.connectionResult("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + accessToken);
//                JSONObject profile = HttpUtils.getJSON("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + accessToken);
                if (profile == null) {
                    return null;
                }
                return profile.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return userName;
        }

        @Override
        protected void onPostExecute(String profile) {

            new UserLoginTask().execute(profile);


            userName = profile;
            Log.d("onPostExecute", "user profile is " + userName);
            TextView textView = (TextView) findViewById(R.id.user_name_field);
            textView.setText(userName);
            loader.setVisibility(View.GONE);
        }
    }

    private class UserLoginTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            String token = "";

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://hackathon.lyashenko.by/user");

            try {
                HttpEntity json = new StringEntity(params[0]);
                httppost.setEntity(json);
                HttpResponse response = httpclient.execute(httppost);

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                StringBuilder builder = new StringBuilder();
                for (String line = null; (line = reader.readLine()) != null;) {
                    builder.append(line).append("\n");
                }
                JSONObject jsonResult = new JSONObject(builder.toString());
                token = jsonResult.getString("uuid");

            } catch (Exception e) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Error!!!", Toast.LENGTH_SHORT);
                toast.show();
            }
            return token;
        }

        protected void onPostExecute(String result) {
//            final Intent intent = new Intent(getApplicationContext(), MapActivity.class);
//            intent.putExtra(EXTRA_MESSAGE, result);
//            startActivity(intent);
//            TextView textView = (TextView) findViewById(R.id.user_name_field);
//            textView.setText(result);
//            loader.setVisibility(View.GONE);
            new CheckinTask().execute(result);
        }
    }

    private class CheckinTask extends AsyncTask<String, String, Store> {

        @Override
        protected Store doInBackground(String... params) {
            return new Store();
        }

        protected void onPostExecute(Store result) {
            final Intent intent = new Intent(getApplicationContext(), ScanBarcodeActivity.class);
            intent.putExtra(Extra.EXTRA_STORE, result);

            intent.putExtra("layout", R.layout.activity_scan_barcode);
            startActivityForResult(intent, 0);
//            final Intent intent = new Intent(getApplicationContext(), NewArticleActivity.class);
//            intent.putExtra(Extra.EXTRA_STORE, result);
//
//            intent.putExtra("layout", R.layout.activity_new_article);
//            startActivityForResult(intent, 0);
        }
    }
} 