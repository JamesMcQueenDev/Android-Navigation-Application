package com.example.navigationapp;

import java.io.Serializable;

public class StoredLocation implements Serializable {

    public String locationName;
    public boolean notificationActive;
    public boolean notificationRequired;
    public double latitude;
    public double longitude;
    public String uid;

    public StoredLocation(){

    }

    /**
     *
     * @param pLocationName
     * @param pLatitude
     * @param pLongitude
     * @param pUid
     */
    public StoredLocation(String pLocationName, double pLatitude, double pLongitude, String pUid){
        locationName = pLocationName;
        latitude = pLatitude;
        longitude = pLongitude;
        uid = pUid; // UID of the user
        notificationActive = false;
        notificationRequired = true;
    }
}