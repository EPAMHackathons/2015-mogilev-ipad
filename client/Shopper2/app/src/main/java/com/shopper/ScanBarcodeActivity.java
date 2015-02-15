package com.shopper;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.shopper.extra.Extra;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ScanBarcodeActivity extends ActionBarActivity implements OnClickListener{
	
	private EditText barcode;
	private Button scan;

    private String tokenLogin = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_barcode);
		
		scan = (Button) findViewById(R.id.btnScan);
		barcode = (EditText) findViewById(R.id.barcode);
		
		scan.setOnClickListener(this);

        tokenLogin = getIntent().getStringExtra(Extra.EXTRA_TOKEN);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scan_barcode, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.btnScan){
			IntentIntegrator scanIntegrator = new IntentIntegrator(this);
			scanIntegrator.initiateScan();	//scan
		}	
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanningResult != null) {
			//we have a result
		}
		else {
		    Toast toast = Toast.makeText(getApplicationContext(),
		        "No scan data received!", Toast.LENGTH_SHORT);
		    toast.show();
		}
		
		String scanContent = scanningResult.getContents();
		String scanFormat = scanningResult.getFormatName();
		
//		formatTxt.setText("FORMAT: " + scanFormat);
		barcode.setText(scanContent);

        new GetArticlesTask().execute("http://hackathon.lyashenko.by/article?barcode=" + barcode.getText().toString()
            + "&token=" + tokenLogin);
	}

    private class GetArticlesTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String urlString=params[0];
            String result = null;
            InputStream in = null;
            // HTTP Get
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream());
            } catch (Exception e ) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        e.getMessage(), Toast.LENGTH_SHORT);
                toast.show();
                return e.getMessage();
            }
            try {
                byte[] contents = new byte[1024];

                int bytesRead=0;

                while( (bytesRead = in.read(contents)) != -1){
                    result = new String(contents, 0, bytesRead);
                }
            } catch (IOException e) {
                result = e.getMessage();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }
            return result;
        }

        protected void onPostExecute(String result) {
            if ("{}".equals(result)) {
                final Intent intent = new Intent(getApplicationContext(), NewArticleActivity.class);
                intent.putExtra(Extra.EXTRA_TOKEN, tokenLogin);
                intent.putExtra(Extra.EXTRA_ARTICLES, result);
                intent.putExtra(Extra.EXTRA_NEW_BARCODE, barcode.getText().toString());
                intent.putExtra("layout", R.layout.activity_new_article);
                startActivityForResult(intent, 0);
            } else {

            }

        }
    }
}
