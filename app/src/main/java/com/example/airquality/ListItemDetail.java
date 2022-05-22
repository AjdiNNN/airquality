package com.example.airquality;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.SubscriptSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_listitem);

        Intent intent = getIntent();
        FrameLayout central = findViewById(R.id.aqiLayout);
        central.setVisibility(View.INVISIBLE);
        ImageView CityImage = findViewById(R.id.imageView2);
        progressBar = findViewById(R.id.progressbar);
        int position = intent.getIntExtra("position", getResources().getStringArray(R.array.sections).length-1);
        if (position != getResources().getStringArray(R.array.sections).length-1) {
            String[] myKeys = getResources().getStringArray(R.array.sections);
            new JsonTask().execute("https://api.waqi.info/feed/" + myKeys[position] + "/?token=12c5ab71671b446ec2778d97bc3ead6efd32c0aa&keyword=");
            TypedArray imgs = getResources().obtainTypedArray(R.array.cityimages);
            CityImage.setBackgroundResource(imgs.getResourceId(position, 0));
            imgs.recycle();
        } else {
            ActivityResultLauncher<String[]> locationPermissionRequest =
                    registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                                Boolean fineLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                                Boolean coarseLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_COARSE_LOCATION, false);
                                if (fineLocationGranted != null && fineLocationGranted) {
                                    setTitle("Location finder");
                                    mContext = this;
                                    locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
                                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        finish();
                                    }
                                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                            2000,
                                            10, locationListenerGPS);
                                        CityImage.setBackgroundResource(R.drawable.bosnia);

                                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                    setTitle("Location finder");
                                    mContext=this;
                                    locationManager=(LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

                                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        finish();
                                    }

                                    locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                                            2000,
                                            10, locationListenerGPS);

                                    CityImage.setBackgroundResource(R.drawable.bosnia);
                                } else {
                                    finish();
                                }
                            }
                    );

            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }

        // Here we turn your string.xml in an array

    }
    LocationListener locationListenerGPS=new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            double latitude=location.getLatitude();
            double longitude=location.getLongitude();
            new JsonTask().execute("https://api.waqi.info/feed/geo:"+latitude+";"+longitude+"/?token=12c5ab71671b446ec2778d97bc3ead6efd32c0aa");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            finish();
        }
    };

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
                JSONObject data = jsonObject.getJSONObject("data");

                aqi = data.getInt("aqi");
                JSONObject cityData = data.getJSONObject("city");

                String city = cityData.getString("name");

                FrameLayout central = findViewById(R.id.aqiLayout);
                central.setVisibility(View.VISIBLE);

                TextView cityName = findViewById(R.id.textView2);
                cityName.setText(city);

                TextView aqivalue = findViewById(R.id.text_view_id);
                aqivalue.setText(aqi.toString());

                aqivalue.setTextColor(Color.HSVToColor(new float[]{ ((1f-((float)aqi/255f))*120f), 1f, 1f }));
                AQIView myView = findViewById(R.id.aqiDraw);
                myView.setAqi(aqi);

                JSONObject iaqi = data.getJSONObject("iaqi");
                iaqi.keys().forEachRemaining((property) -> {
                    try {
                        TextView text = new TextView(ListItemDetail.this);
                        int propertyValue = iaqi.getJSONObject(property).getInt("v");
                        String numberString = property.replaceAll("[^0-9]", "");
                        SpannableString styledString =  new SpannableString(property.replaceAll("\\d", "").toUpperCase());
                        SpannableString styledNumber = new SpannableString("");
                        SpannableStringBuilder finalString = new SpannableStringBuilder();
                        if(!numberString.isEmpty())
                        {
                            styledNumber = new SpannableString(numberString);
                            styledNumber.setSpan(new SubscriptSpan(),0,numberString.length(),0);
                        }
                        String value =  " "+Integer.toString(propertyValue);
                        SpannableString valueSpan = new SpannableString(value);
                        valueSpan.setSpan(
                                new ForegroundColorSpan(Color.HSVToColor(new float[]{ ((1f-((float)propertyValue/255f))*120f), 1f, 1f })),
                                0, // start
                                value.length(), // end
                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                        );
                        finalString.append(styledString).append(styledNumber).append(valueSpan);
                        text.setText(finalString);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,155);
                        params.setMargins(0,10,0,10);
                        text.setLayoutParams(params);
                        text.setTextSize(28);
                        text.setGravity(Gravity.CENTER);
                        text.setPadding(5,5,5,5);
                        text.setTextColor(Color.GREEN);
                        text.setBackgroundColor(Color.LTGRAY);
                        LinearLayout m_layout = findViewById(R.id.scrollLayout);
                        m_layout.addView(text);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }catch (JSONException err){
                Log.d("Error", err.toString());

            }
        }
    }
}