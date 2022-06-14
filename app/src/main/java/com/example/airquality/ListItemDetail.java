package com.example.airquality;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ListItemDetail extends MainActivity {
    int aqi = 0;
    ProgressBar progressBar;
    AirQuality airQuality = null;
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
            airQuality = AppDatabase.getInsance(ListItemDetail.this).airqualityDao().getByCityName(myKeys[position]);
            new JsonTask().execute("https://api.waqi.info/feed/" + myKeys[position] + "/?token=12c5ab71671b446ec2778d97bc3ead6efd32c0aa&keyword=");

            TypedArray imgs = getResources().obtainTypedArray(R.array.cityimages);
            CityImage.setBackgroundResource(imgs.getResourceId(position, 0));
            imgs.recycle();
        } else {
            airQuality = AppDatabase.getInsance(ListItemDetail.this).airqualityDao().getLastEntry();
            ActivityResultLauncher<String[]> locationPermissionRequest =
                    registerForActivityResult(new ActivityResultContracts
                                    .RequestMultiplePermissions(), result -> {
                                Boolean fineLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_FINE_LOCATION, false);
                                Boolean coarseLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_COARSE_LOCATION, false);
                                if (coarseLocationGranted != null && coarseLocationGranted || fineLocationGranted != null && fineLocationGranted ) {
                                    setTitle("Location finder");
                                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        finish();
                                    }
                                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
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

        Intent notificationIntent = new Intent(getApplicationContext(), ShowNotification.class);
        PendingIntent contentIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT + PendingIntent.FLAG_MUTABLE);

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(contentIntent);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                + AlarmManager.INTERVAL_HALF_DAY , AlarmManager.INTERVAL_HALF_DAY, contentIntent);
    }
    LocationListener locationListenerGPS=new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            double latitude=location.getLatitude();
            double longitude=location.getLongitude();
            new JsonTask().execute("https://api.waqi.info/feed/geo:"+latitude+";"+longitude+"/?token=12c5ab71671b446ec2778d97bc3ead6efd32c0aa");
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

            /*
             * Setup HTTP connection to JSON api
             */
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
            String city = "";
            JSONObject iaqi = new JSONObject();
            String dateTime = "";
            /*
             * If there is internet connection get data
             */
            if(isConnected(ListItemDetail.this))
            {
                /*
                 * Get base JSON OBJECT
                 */
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /*
                 * Get station data
                 */
                JSONObject data = null;
                try {
                    assert jsonObject != null;
                    data = jsonObject.getJSONObject("data");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /*
                 * Get air quality index data
                 */
                try {
                    assert data != null;
                    aqi = data.getInt("aqi");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /*
                 * Get city/station information
                 */
                JSONObject cityData = null;
                try {
                    cityData = data.getJSONObject("city");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /*
                 * Get  city/station name
                 */
                try {
                    assert cityData != null;
                    city = cityData.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /*
                 * Get station gps location
                 */
                JSONArray cityPosition = null;
                try {
                    cityPosition = cityData.getJSONArray("geo");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /*
                 * Get station measuring
                 */
                try {
                    iaqi = data.getJSONObject("iaqi");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    dateTime = data.getJSONObject("time").getString("s");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /*
                 * Save current data into room database
                 */
                AirQuality todo = null;
                try {
                    assert cityPosition != null;
                    todo = new AirQuality(aqi,city.split(" ")[0],cityPosition.getDouble(0),cityPosition.getDouble(1), iaqi.toString(), dateTime);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                AppDatabase.getInsance(ListItemDetail.this).airqualityDao().add(todo);

            }
            /*
             * If there is not internet connection get data from room
             */
            else
            {

                if(airQuality==null)
                {
                    city = "No internet";
                }
                else
                {
                    /*
                     * Get data from room and put them into variables
                     */
                    aqi = airQuality.getAQIndex();
                    city = airQuality.getCityName();
                    dateTime = airQuality.getDateTaken();
                    try {
                        iaqi = new JSONObject(airQuality.getIaqi());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            /*
             * Set all values on UI
             */

            /*
             * Get top most frame layout
             */
            FrameLayout central = findViewById(R.id.aqiLayout);
            central.setVisibility(View.VISIBLE);

            /*
             * Set name of station/city on frame layout
             */
            TextView cityName = findViewById(R.id.cityName);
            cityName.setText(city);

            /*
             * Set air quality index value and give it color according to hazard
             */
            TextView aqiValue = findViewById(R.id.aqiValue);
            aqiValue.setText(Integer.toString(aqi));
            aqiValue.setTextColor(Color.HSVToColor(new float[]{ ((1f-((float)aqi/255f))*120f), 1f, 1f }));

            /*
             * Set when time measurements are taken
             */
            TextView dateTimeText = findViewById(R.id.dateTime);
            dateTimeText.setText(dateTime);

            /*
             * Set value into drawable custom view
             */
            AQIView myView = findViewById(R.id.aqiDraw);

            myView.setAqi(aqi);

            /* Air quality description */
            String airQualityInfo;
            if(aqi<50)
            {
                airQualityInfo = "Air quality is satisfactory, and air pollution poses little or no risk.";
            }
            else if(aqi<100)
            {
                airQualityInfo = "Active children and adults, and people with respiratory disease, such as asthma, should limit prolonged outdoor exertion.";
            }
            else if(aqi<150)
            {
                airQualityInfo = "Active children and adults, and people with respiratory disease, such as asthma, should limit prolonged outdoor exertion.";
            }
            else if(aqi<200)
            {
                airQualityInfo = "Active children and adults, and people with respiratory disease, such as asthma, should avoid prolonged outdoor exertion; everyone else, especially children, should limit prolonged outdoor exertion";
            }
            else if(aqi<300)
            {
                airQualityInfo = "Active children and adults, and people with respiratory disease, such as asthma, should avoid all outdoor exertion; everyone else, especially children, should limit outdoor exertion.";
            }
            else
            {
                airQualityInfo = "Everyone should avoid all outdoor exertion";
            }
            TextView airQualityInfoText = findViewById(R.id.air_quality_info);
            airQualityInfoText.setText(airQualityInfo);
            airQualityInfoText.setTextColor(Color.HSVToColor(new float[]{ ((1f-((float)aqi/255f))*120f), 1f, 1f }));


            TableLayout m_layout = findViewById(R.id.table);
            m_layout.removeAllViews();
            /*
             * List all measurements
             */
            JSONObject finalIaqi = iaqi;
            iaqi.keys().forEachRemaining((property) -> {
                try {
                    TextView propertyTextView = new TextView(ListItemDetail.this);
                    TextView propertyValueTextView = new TextView(ListItemDetail.this);


                    String addon = "";
                    float propertyValue = finalIaqi.getJSONObject(property).getInt("v");
                    int valueColor = Color.HSVToColor(new float[]{ ((1f-(propertyValue%300f/300f))*120f), 1f, 1f});
                    if(property.equals("p"))
                        valueColor = propertyValue<950f || propertyValue>1100f ? Color.RED : Color.GREEN;

                    switch(property) {
                        case "p":
                            property = "Pressure";
                            addon = "mb";
                            break;
                        case "t":
                            property = "Temperature";
                            addon = "°C";
                            break;
                        case "w":
                            property = "Wind";
                            addon = "km/h";
                            break;
                        case "h":
                            property = "Humidity";
                            addon = "%";
                            break;
                        case "dew":
                            property = "Dew P.";
                            addon = "°C";
                            break;

                        default:
                    }



                    String numberString = property.replaceAll("[^0-9]", "");

                    SpannableString styledString =  new SpannableString(property.replaceAll("\\d", "").toUpperCase());
                    SpannableString styledNumber = new SpannableString("");
                    SpannableStringBuilder finalString = new SpannableStringBuilder();

                    if(!numberString.isEmpty())
                    {
                        styledNumber = new SpannableString(numberString);
                        styledNumber.setSpan(new RelativeSizeSpan(0.5f),0,numberString.length(),0);
                    }

                    String formatted = propertyValue == (int)propertyValue ? Integer.toString((int)propertyValue) : Float.toString(propertyValue);
                    String value =  " "+formatted + addon;
                    SpannableString valueSpan = new SpannableString(value);



                    valueSpan.setSpan(
                            new ForegroundColorSpan(valueColor),
                            0, // start
                            value.length(), // end
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    );

                    finalString.append(styledString).append(styledNumber);
                    propertyTextView.setText(finalString);
                    propertyValueTextView.setText(valueSpan);

                    propertyTextView.setTextSize(32);
                    propertyValueTextView.setTextSize(32);

                    propertyTextView.setGravity(Gravity.CENTER);
                    propertyValueTextView.setGravity(Gravity.CENTER);

                    propertyTextView.setTextColor(Color.DKGRAY);

                    propertyTextView.setBackgroundColor(Color.LTGRAY);
                    propertyValueTextView.setBackgroundColor(Color.GRAY);


                    TableRow row = new TableRow(ListItemDetail.this);
                    TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                    row.setLayoutParams(lp);

                    TableLayout.LayoutParams tableRowParams=
                            new TableLayout.LayoutParams
                                    (TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);

                    int leftMargin=10;
                    int topMargin=3;
                    int rightMargin=10;
                    int bottomMargin=3;

                    tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);

                    row.setLayoutParams(tableRowParams);

                    row.addView(propertyTextView);
                    row.addView(propertyValueTextView);
                    m_layout.addView(row);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    public static boolean isConnected(@NonNull Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}