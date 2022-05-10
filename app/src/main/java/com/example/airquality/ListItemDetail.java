package com.example.airquality;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ProgressBar;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ListItemDetail extends MainActivity {
    int height;
    int width;
    Integer aqi;
    ProgressBar progressBar;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listitem);

        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);

        // Here we turn your string.xml in an array
        String[] myKeys = getResources().getStringArray(R.array.sections);

        //txtJson = (TextView) findViewById(R.id.tvJsonItem);
        progressBar = findViewById(R.id.progressbar);
        new JsonTask().execute("https://api.waqi.info/feed/"+myKeys[position]+"/?token=12c5ab71671b446ec2778d97bc3ead6efd32c0aa");
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

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
                    if (reader != null) {
                        reader.close();
                    }
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
                setContentView(new AQIView(ListItemDetail.this));
            }catch (JSONException err){
                Log.d("Error", err.toString());
            }
        }
    }
    class AQIView extends View
    {
        Point center;
        RectF outer_rect;
        RectF inner_rect;
        Path path;
        Paint fill;
        Paint border;
        int inner_radius = 100;
        int outer_radius = 150;
        int arc_sweep = -180;
        int arc_ofset;
        int centerW = width/2;
        int centerH = height/4;
        public AQIView(Context context) {
            super(context);
            this.init();
        }
        private void init()
        {
            arc_ofset = aqi%360;
            center = new Point(centerW,centerH);
            outer_rect = new RectF(center.x-outer_radius, center.y-outer_radius, center.x+outer_radius, center.y+outer_radius);
            inner_rect = new RectF(center.x-inner_radius, center.y-inner_radius, center.x+inner_radius, center.y+inner_radius);
            path = new Path();
            fill = new Paint();
            fill.setColor(Color.HSVToColor(new float[]{ ((1f-((float)aqi/255f))*120f), 1f, 1f }));
            border = new Paint();
        }
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            path.arcTo(outer_rect, arc_ofset, arc_sweep);
            path.arcTo(inner_rect, arc_ofset + arc_sweep, -arc_sweep);
            path.close();

            canvas.drawPath(path, fill);

            border.setStyle(Paint.Style.STROKE);
            border.setStrokeWidth(2);
            canvas.drawPath(path, border);
        }

    }
}