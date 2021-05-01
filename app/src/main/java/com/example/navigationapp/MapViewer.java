package com.example.navigationapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.HashMap;
import java.util.Map;

public class MapViewer extends AppCompatActivity {

    FirebaseFirestore db;
    ListenerRegistration listenerRegistration;

    MapView mapView;

    long minimumTimeBetweenUpdates = 10000;
    float minimumDistanceBetweenUpdates = 0.5f;

    LocationListener locationListener;
    LocationManager locationManager;

    int LOCATION_REQUEST_CODE = 1;
    boolean trackingEnabled = true;

    static Map<String, StoredLocation> storedLocations = new HashMap<String, StoredLocation>();

    double triggerDistance = 100;

    NotificationManagerCompat notificationManager;

    /**
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MyLocation","onResume");
        mapView.onResume();
        startTracking();
        setListenerRegistration();
    }

    /**
     *
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        locationManager.removeUpdates(locationListener);
        listenerRegistration.remove();
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_mapviewer);

        mapView = findViewById(R.id.map);

        mapView.setTileSource(TileSourceFactory.MAPNIK);

        mapView.setMultiTouchControls(true);

        IMapController mapController = mapView.getController();

        mapController.setZoom(6.0);

        GeoPoint startPoint = new GeoPoint(52.8583, -2.2944);

        mapController.setCenter(startPoint);

        notificationManager = NotificationManagerCompat.from(getApplicationContext());

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                GeoPoint currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

                Marker currentLocationMarker = new Marker(mapView);
                currentLocationMarker.setPosition(currentLocation);

                mapView.getOverlays().add(currentLocationMarker);

                mapView.invalidate();

                //Tracks stored location
                for (StoredLocation storedLocation : storedLocations.values()) {
                    GeoPoint geoPoint = new GeoPoint(storedLocation.latitude, storedLocation.longitude);
                    double distance = currentLocation.distanceToAsDouble(geoPoint);

                    if (distance < triggerDistance) {
                        if(!storedLocation.notificationActive && storedLocation.notificationRequired){
                            int notificationID = storedLocation.locationName.hashCode();

                            Notification notification = createNotification(storedLocation, distance);

                            notificationManager.notify(notificationID, notification);

                            storedLocation.notificationActive = true;
                        }
                    }
                    else{
                        storedLocation.notificationActive = false;
                    }
                }
            }
        };

        //FireBase
        db = FirebaseFirestore.getInstance();


        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            /**
             *
             * @param p
             * @return
             */
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                Log.d("MyLocation","Top at " +p.toString());
                return false;
            }

            /**
             *
             * @param p
             * @return
             */
            @Override
            public boolean longPressHelper(GeoPoint p) {
                Log.d("MyLocation","Press at " + p.toString());
                addNewLocationDialog(p);
                return true;
            }
        });

        mapView.getOverlays().add(mapEventsOverlay);
    }

    /**
     *
     * @param view
     */
    public void onHomeClick(View view){
        this.finish();
    }

    /**
     *
     */
    public void startTracking() {
        if (trackingEnabled) {

            boolean permission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;

            if (permission) {
                Log.d("MyLocation", "Permission Already Granted");
                updateLocation();
            } else {
                Log.d("MyLocation", "Permission Requesting...");
                String[] arrayOfPermissions = {Manifest.permission.ACCESS_FINE_LOCATION};
                requestPermissions(arrayOfPermissions, LOCATION_REQUEST_CODE);
            }
        }
    }

    /**
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MyLocation", "Permission Granted");
            } else {
                Log.d("MyLocation", "Permission Denied");
            }
        }
    }

    /**
     *
     */
    @SuppressLint("MissingPermission")
    public void updateLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    minimumTimeBetweenUpdates,
                    minimumDistanceBetweenUpdates,
                    locationListener);
        }
    }

    static final String NOTIFICATION_KEY = "MyLocation";
    static final int NOTIFICATION_INTENT_CODE = 0;

    /**
     *
     * @param storedLocation
     * @param distance
     * @return
     */
    private Notification createNotification(StoredLocation storedLocation, double distance) {

        Intent intent = getIntent();

        intent.putExtra(NOTIFICATION_KEY, storedLocation.locationName);

        PendingIntent pendingIntent = PendingIntent.getActivity(
          this, NOTIFICATION_INTENT_CODE,intent,PendingIntent.FLAG_UPDATE_CURRENT
        );
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_KEY)
                    .setSmallIcon(R.drawable.current_location)
                    .setContentTitle("MyLocation Update: " + storedLocation.locationName)
                    .setContentText("You are " + distance + " metres from " + storedLocation.locationName)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

        return notification;

    }

    /**
     *
     * @param intent
     */
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);

        String locationName = intent.getStringExtra(NOTIFICATION_KEY);

        for(StoredLocation storedLocation: storedLocations.values()){
            if(storedLocation.locationName.equals(locationName)){
                showNotificationDialog(storedLocation);
                return;
            }
        }
    }

    /**
     *
     * @param storedLocation
     */
    private void showNotificationDialog(StoredLocation storedLocation){

        storedLocation.notificationActive = false;

        new AlertDialog.Builder(this)
                .setTitle(storedLocation.locationName)
                .setMessage("You are within proximity of this location! Do you want to receive notifications from this location in the future?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    /**
                     *
                     * @param dialog
                     * @param i
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        storedLocation.notificationActive = false;
                        storedLocation.notificationRequired = true;

                        db.collection("locations")
                                .document(storedLocation.locationName)
                                .set(storedLocation);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    /**
                     *
                     * @param dialog
                     * @param i
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        storedLocation.notificationActive = false;
                        storedLocation.notificationRequired = false;

                        db.collection("locations")
                                .document(storedLocation.locationName)
                                .set(storedLocation);
                    }
                })
                .create()
                .show();
    }

    /**
     *
     * @param geoPoint
     */
    private void addNewLocationDialog(GeoPoint geoPoint){
        Log.d("MyLocation","Long at " + geoPoint);

        EditText locationEditText = new EditText(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        new AlertDialog.Builder(this)
                .setTitle("Create New Location")
                .setView(locationEditText)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    /**
                     *
                     * @param dialog
                     * @param i
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        String locationName = locationEditText.getText().toString();
                        StoredLocation newLocation = new StoredLocation(
                                locationName,
                                geoPoint.getLatitude(),
                                geoPoint.getLongitude(),
                                user.getUid());

                        db.collection("locations")
                                .document(newLocation.locationName)
                                .set(newLocation);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    /**
                     *
                     * @param dialog
                     * @param i
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int i) {

                    }
                })
                .create()
                .show();
    }

    /**
     *
     */
    private void setListenerRegistration(){
        //This block of code bricks the app
        CollectionReference collection = db.collection("locations");
        listenerRegistration = collection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            /**
             *
             * @param value
             * @param error
             */
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                Log.d("MyLocation","Collection Changed");

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(error != null){
                    Log.d("MyLocation","Listener for changers on server not working");
                    return;
                }

                for(QueryDocumentSnapshot doc : value){
                    StoredLocation location = doc.toObject(StoredLocation.class);

                    if(!storedLocations.containsKey(location.locationName))
                        storedLocations.put(location.locationName,location);
                }

                for(StoredLocation storedLoc : storedLocations.values()){
                    GeoPoint geoPoint = new GeoPoint(storedLoc.latitude,storedLoc.longitude);
                    Marker marker = new Marker(mapView);
                    marker.setPosition(geoPoint);

                    if(user.getUid() == storedLoc.uid){
                        marker.setIcon(getDrawable(R.drawable.user_pointer_24));
                    }
                    else{
                        marker.setIcon(getDrawable(R.drawable.non_user_pointer_24));
                    }
                    mapView.getOverlays().add(marker);
                    mapView.invalidate();
                }
            }
        });
    }
}