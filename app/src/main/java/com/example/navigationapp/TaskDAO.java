package com.example.navigationapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDAO {

    @Query("SELECT * FROM task")
    LiveData<List<Task>> getAll();

    @Query("SELECT * FROM task WHERE done = '1'")
    LiveData<List<Task>> getCompleted();

    @Query("SELECT * FROM task WHERE done = '0'")
    LiveData<List<Task>> getPending();

    @Insert
    void insert(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);
}
