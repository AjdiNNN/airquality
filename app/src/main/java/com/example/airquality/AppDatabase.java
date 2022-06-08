package com.example.airquality;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {AirQuality.class},version = 1,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AirQualityDao airqualityDao();
    private static AppDatabase INSTANCE;

    public static AppDatabase getInsance(Context context){
        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context,AppDatabase.class,"app_database").allowMainThreadQueries().build();
        }
        return INSTANCE;
    }
}
