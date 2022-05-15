package com.example.airquality;

import android.content.Intent;

import android.graphics.Color;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ListItemDetail extends MainActivity {
    Integer aqi;
    ProgressBar progressBar;
    private String[] myKeys;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listitem);

        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);

        // Here we turn your string.xml in an array
        String[] myKeys = getResources().getStringArray(R.array.sections);
        FrameLayout central = (FrameLayout)findViewById(R.id.aqiLayout);
        central.setVisibility(View.INVISIBLE);
        progressBar = findViewById(R.id.progressbar);
        new JsonTask().execute("https://api.waqi.info/feed/"+ myKeys[position]+"/?token=12c5ab71671b446ec2778d97bc3ead6efd32c0aa");
        TextView cityName = (TextView)findViewById(R.id.textView2);
        cityName.setText(myKeys[position]);
    }


    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder buffer = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    line = line+"\n";
                    buffer.append(line);
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject getSth = jsonObject.getJSONObject("data");
                aqi = getSth.getInt("aqi");
                // globally
                FrameLayout central = (FrameLayout)findViewById(R.id.aqiLayout);
                central.setVisibility(View.VISIBLE);
 
                TextView aqivalue = (TextView)findViewById(R.id.text_view_id);
                aqivalue.setText(aqi.toString());
                aqivalue.setTextColor(Color.HSVToColor(new float[]{ ((1f-((float)aqi/255f))*120f), 1f, 1f }));
                AQIView myView = (AQIView) findViewById(R.id.aqiDraw);
                myView.setAqi(aqi);
            }catch (JSONException err){
                Log.d("Error", err.toString());
            }
        }
    }
}