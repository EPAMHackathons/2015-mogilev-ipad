package com.shopper;

import android.accounts.AccountManager;
import android.content.Intent;
import android.location.Location;
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
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.shopper.domain.Store;
import com.shopper.extra.Extra;
import com.shopper.util.HttpUtils;

public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String SCOPE =
            "oauth2:https://www.googleapis.com/auth/userinfo.profile";
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;
    private String userEmail;
    private String userName;
    private String accessToken;
    private GoogleApiClient mGoogleApiClient;
    private Location lastLocation;
    private FrameLayout loader;

    private String tokenLogin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loader = (FrameLayout) findViewById(R.id.loader);
        pickUserAccount();
        buildGoogleApiClient();
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

    protected synchronized void buildGoogleApiClient() {
        Log.d("build google api client", "start");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        Log.d("build google api client", "stop");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("get location", "connected to service. start get location");
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null) {
            Log.i("get location", "Latitude is " + lastLocation.getLatitude());
            Log.i("get location", "Longitude is " + lastLocation.getLongitude());
//            final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            String token = intent.getStringExtra(Extra.EXTRA_TOKEN);
            String token = Session.TOKEN;
            new CheckinTask().execute(token);
        } else {
            Log.e("get location", "last location is null");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("get location", "connection suspended: " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("get location", connectionResult.toString());
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
                tokenLogin = jsonResult.getString("uuid");

            } catch (Exception e) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Error!!!", Toast.LENGTH_SHORT);
                toast.show();
            }
            return tokenLogin;
        }

        protected void onPostExecute(String result) {
//            final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            intent.putExtra(Extra.EXTRA_TOKEN, result);
            Session.TOKEN = result;

            mGoogleApiClient.connect();
        }
    }

    private class CheckinTask extends AsyncTask<String, String, Store> {

        @Override
        protected Store doInBackground(String... params) {
//            HttpClient httpclient = new DefaultHttpClient();
//            HttpPost httppost = new HttpPost("http://hackathon.lyashenko.by/store");

            try {
//                HttpParams httpParams = new BasicHttpParams();
//                httpParams.setParameter("token", params[0]);
//                httpParams.setParameter("lt", lastLocation.getLatitude());
//                httpParams.setParameter("lg", lastLocation.getLongitude());
//                httppost.setParams(httpParams);
//                HttpEntity json = new StringEntity(params[0]);
//                httppost.setEntity(json);
//                HttpResponse response = httpclient.execute(httppost);
//
//                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
//
//                StringBuilder builder = new StringBuilder();
//                for (String line = null; (line = reader.readLine()) != null;) {
//                    builder.append(line).append("\n");
//                }
                StringBuilder builder = HttpUtils.connectionResult("http://hackathon.lyashenko.by/store?token=" + params[0] +
                "&lt=" + lastLocation.getLatitude() + "&lg=" + lastLocation.getLongitude());
                String json = builder.toString();
                JSONArray jsonResult = new JSONArray(json);

                if (jsonResult.length() > 0) {
                    final Long id = jsonResult.getJSONObject(0).getLong("id");
                    final String name = jsonResult.getJSONObject(0).getString("name");
                    final String description = jsonResult.getJSONObject(0).getString("description");

                    Store store = new Store() {{
                        setDescription(description);
                        setId(id);
                        setName(name);
                    }};

                    Log.d("CheckinTask", "id: " + id + " name: " + name);
                    return store;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return new Store();
        }

        protected void onPostExecute(Store result) {
            final Intent intent = new Intent(getApplicationContext(), ScanBarcodeActivity.class);
            intent.putExtra(Extra.EXTRA_STORE, result);
            intent.putExtra(Extra.EXTRA_TOKEN, tokenLogin);

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