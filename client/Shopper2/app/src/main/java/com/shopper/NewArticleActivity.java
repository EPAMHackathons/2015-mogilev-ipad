package com.shopper;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.shopper.domain.Article;
import com.shopper.extra.Extra;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class NewArticleActivity extends ActionBarActivity implements View.OnClickListener {

    private String tokenLogin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_article);

        ((TextView)findViewById(R.id.newbarcode)).setText(getIntent().getStringExtra(Extra.EXTRA_NEW_BARCODE));

        ((Button)findViewById(R.id.btnSaveNewArticle)).setOnClickListener(this);
        tokenLogin = getIntent().getStringExtra(Extra.EXTRA_TOKEN);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_article, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btnSaveNewArticle){
            final Article article = new Article();
            article.setBarcode(((TextView)findViewById(R.id.newbarcode)).getText().toString());
            article.setPrice(Double.parseDouble(((EditText) findViewById(R.id.newprice)).getText().toString()));
            article.setName(((EditText) findViewById(R.id.newname)).getText().toString());
            article.setDescription(((EditText)findViewById(R.id.newdesc)).getText().toString());
            new SaveArticlesTask().execute(article);
        }
    }

    private class SaveArticlesTask extends AsyncTask<Article, String, String> {

        @Override
        protected String doInBackground(Article... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://hackathon.lyashenko.by/article");

            try {
                HttpEntity json = new StringEntity("");
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

        }



    }
}
