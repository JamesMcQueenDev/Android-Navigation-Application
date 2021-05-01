package com.example.navigationapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Task.class}, version = 1)
public abstract class TasksDB extends RoomDatabase {

    public abstract TaskDAO taskDAO();

    private static final String DB_NAME = "task_database_name";
    private static TasksDB db;


    public static TasksDB getInstance(Context context){
        if(db == null) db= buildDatabaseInstance(context);
        return db;
    }

    private static TasksDB buildDatabaseInstance(Context context) {
        return Room.databaseBuilder(context, TasksDB.class, DB_NAME).build();
    }
}
