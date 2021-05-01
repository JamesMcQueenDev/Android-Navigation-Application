package com.example.navigationapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskWindow extends AppCompatActivity{

    TasksDB db;

    FirebaseFirestore firestoreDatabase;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    ListenerRegistration listenerRegistration;

    public String title, time, date, description, locationName;

    static Map<String, StoredLocation> storedLocations = new HashMap<String, StoredLocation>();

    List<String> locationSpinner = new ArrayList<>();

    /**
     * Creates the XML page
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_taskwindow);

        db = Room.databaseBuilder(getApplicationContext(),
                TasksDB.class,
                "tasks_database_name").build();

        firestoreDatabase = FirebaseFirestore.getInstance();
        Location();
    }

    /**
     * Logs when Save Button is pressed
     * @param view
     */
    public void onSaveClick(View view) {
        Log.d("ToDoApp","onSaveClick");

        EditText titleView = findViewById(R.id.taskTitleView);
        EditText descView = findViewById(R.id.taskDescriptionView);
        EditText dateView = findViewById(R.id.taskDueDateView);
        EditText timeView = findViewById(R.id.taskDueTimeView);

        title = titleView.getText().toString();
        description = descView.getText().toString();
        date = dateView.getText().toString();
        time = timeView.getText().toString();

        final Task task = new Task();
        task.title = title;
        task.description = description;
        task.image = imageUri.toString();
        task.done = false;

        Executor myExecutor = Executors.newSingleThreadExecutor();
        myExecutor.execute(new Runnable() {
            @Override
            public void run() {
                db.taskDAO().insert(task);
            }
        });

        LiveData<List<Task>> tasks = db.taskDAO().getAll();

        tasks.observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                for(Task task: tasks){
                    Log.d("ToDoApp",task.title + ":" + task.description);
                }
            }
        });

        //Stores Task to Firebase
        StoredTask newTask = new StoredTask(
                task.title,
                task.duedate,
                task.description,
                task.image,
                user.getUid());

        firestoreDatabase.collection("tasks")
                .document(newTask.taskName)
                .set(newTask);

        this.finish();
    }

    /**
     *
     */
    @Override
    public void onPause(){
        super.onPause();
        listenerRegistration.remove();
    }

    /**
     *
     */
    @Override
    public void onResume(){
        super.onResume();
        CollectionReference collection = firestoreDatabase.collection("tasks");

        listenerRegistration = collection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                Log.d("MyTask","Collection Changed");

                if(error != null){
                    Log.d("MyTask","Cannot access server");
                }

                for(QueryDocumentSnapshot doc: value){

                    Task task1 = new Task();
                    StoredTask storedTask = doc.toObject(StoredTask.class);

                    db = TasksDB.getInstance(getApplicationContext());

                    Executor myExecutor = Executors.newSingleThreadExecutor();
                    myExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if(db.taskDAO().equals(storedTask.taskName)){

                            }
                            else{
                                task1.title = storedTask.taskName;
                                task1.description = storedTask.taskDescription;
                                task1.duedate = storedTask.taskDate;
                                task1.duetime = storedTask.taskTime;
                                task1.image = storedTask.taskImage;

                                db.taskDAO().insert(task1);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Updates the contents of the Spinner
     */
    public void SpinnerUpdate(){

        Spinner spinner = findViewById(R.id.spinLocation);
        for(String locationName : storedLocations.keySet()){
            locationSpinner.add(locationName);
        }

        ArrayAdapter<String> spinAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, locationSpinner);

        spinner.setAdapter(spinAdapter);
    }

    /**
     * Gets the location name and stores it to the Hash Map
     */
    public void Location(){
        CollectionReference collectionReference = firestoreDatabase.collection("locations");
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot documentSnapshot: task.getResult()){
                        StoredLocation location = documentSnapshot.toObject(StoredLocation.class);

                        //Adds only the logged locations by the user
                        if(user.getUid() == location.uid){
                            storedLocations.put(location.locationName,location);
                        }
                    }
                    SpinnerUpdate();
                }
            }
        });
    }

    /**
     * Calls the Map
     * @param view
     */
    public void onMapClick(View view){
        Intent intent = new Intent(this, MapViewer.class);
        startActivity(intent);
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
    Uri imageUri;

    /**
     * Method launches the camera feature of the task creation
     * @param view
     */
    public void onCameraClick(View view){
        Log.d("ToDoApp","onCameraClick");

        String imageFilename = "JPEG_" + System.currentTimeMillis() + ".jpg";

        File imageFile = new File(getFilesDir(),imageFilename);

        imageUri = FileProvider.getUriForFile(this,".pictureprovider",imageFile);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);
    }

    /**
     * Updates the Date of the Widget
     * @param view
     */
    public void onDateClick(View view){
        Log.d("ToDoApp","onDateClick");

        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                EditText dateView = findViewById(R.id.taskDueDateView);
                dateView.setText(dayOfMonth + "/" + month + "/" + year);
            }
        };

        DatePickerDialog dialog = new DatePickerDialog(this, listener,2021,1,1);
        dialog.show();
    }

    /**
     * Updates the Time of the Widget
     * @param view
     */
    public void onTimeClick(View view){
        Log.d("ToDoApp","onDateClick");

        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                EditText timeView = findViewById(R.id.taskDueTimeView);
                timeView.setText(hourOfDay + ":" + minute);
            }
        };

        TimePickerDialog dialog = new TimePickerDialog(this, listener, 0,0,true);
        dialog.show();
    }

    /**
     * Updates the image view to display the most recent image taken
     * @param requestCode
     * @param resultCode
     * @param resultData
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData){
        super.onActivityResult(requestCode,resultCode,resultData);

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            ImageView taskImage = findViewById(R.id.taskImage);
            taskImage.setImageURI(imageUri);
        }
    }
}