package com.example.android.githubdemoapp.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import com.example.android.githubdemoapp.model.SimpleRepoModel;
import com.example.android.githubdemoapp.service.RepoDao;

@Database(entities = {SimpleRepoModel.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase{

    public abstract RepoDao repoDao();

    private static AppDatabase sInstance;

    public static AppDatabase getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                if (sInstance == null) {
                    sInstance = buildDatabase(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }


    private static AppDatabase buildDatabase(final Context context) {
        return Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
    }

}
