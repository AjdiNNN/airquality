package com.example.airquality;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ShowNotification extends BroadcastReceiver {
    Context mContext;
    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        Log.i("Notify", "Notification created");
        AirQuality airQuality = AppDatabase.getInsance(context).airqualityDao().getLastEntry();
        if(airQuality!=null)
            new JsonTask().execute("https://api.waqi.info/feed/geo:"+airQuality.getLatitude()+";"+airQuality.getLongitude()+"/?token=12c5ab71671b446ec2778d97bc3ead6efd32c0aa");
    }


    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {

            /*
              Setup HTTP connection to JSON api
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
            String city = "";
            int aqi = 0;
            /*
              If there is internet connection get data
             */
            if(isConnected(mContext))
            {
                /*
                  Get base JSON OBJECT
                 */
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /*
                  Get station data
                 */
                JSONObject data = null;
                try {
                    assert jsonObject != null;
                    data = jsonObject.getJSONObject("data");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /*
                  Get air quality index data
                 */
                try {
                    assert data != null;
                    aqi = data.getInt("aqi");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /*
                  Get city/station information
                 */
                JSONObject cityData = null;
                try {
                    cityData = data.getJSONObject("city");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /*
                  Get  city/station name
                 */
                try {
                    assert cityData != null;
                    city = cityData.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

                Intent repeating_intent = new Intent();
                repeating_intent.setClass(mContext, ListItemDetail.class);
                repeating_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                repeating_intent.putExtra("position", 2);
                repeating_intent.putExtra("id", 2);
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext,0,repeating_intent,PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_MUTABLE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);
                    // Configure the notification channel.
                    notificationChannel.setDescription("Air info");
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.RED);
                    notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                    notificationChannel.enableVibration(true);
                    notificationManager.createNotificationChannel(notificationChannel);
                }


                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext.getApplicationContext(), NOTIFICATION_CHANNEL_ID);

                notificationBuilder.setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.gas_icon)
                        .setTicker("Air quality update")
                        .setContentTitle("Air Quality")
                        .setContentText("Current air quality index in "+city+": " + aqi)
                        .setContentInfo("Update")
                        .setContentIntent(pendingIntent);

                notificationManager.notify(/*notification id*/1, notificationBuilder.build());
            }
        }
    }
    public static boolean isConnected(@NonNull Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
