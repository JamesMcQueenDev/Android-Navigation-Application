package com.example.navigationapp;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.FirebaseFirestore;

@Entity
public class Task {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "date")
    public String duedate;

    @ColumnInfo(name = "time")
    public String duetime;

    @ColumnInfo(name = "image")
    public String image;

    @ColumnInfo(name = "done")
    public boolean done;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "locationName")
    public String locationName;
}
