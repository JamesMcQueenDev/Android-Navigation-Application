package com.example.navigationapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class ToDoListActivity extends AppCompatActivity {


    /**
     * Creates the task block viewing list
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_to_do_list);

        AllTaskFragment allTaskFragment = new AllTaskFragment();
        CompletedTaskFragment completedTaskFragment = new CompletedTaskFragment();
        PendingTaskFragment pendingTaskFragment = new PendingTaskFragment();

        loadFragment(allTaskFragment);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){

                    case R.id.page_all:
                        loadFragment(allTaskFragment);
                        return true;
                    case R.id.page_done:
                        loadFragment(completedTaskFragment);
                        return true;
                    case R.id.page_todo:
                        loadFragment(pendingTaskFragment);
                        return true;
                }
                return false;
            }
        });
    }

    public void loadFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragmentHolder, fragment);
        fragmentTransaction.commit();
    }

    /**
     * Brings up Task Creation
     * @param view
     */
    public void onNewTaskClicked(View view){
        Log.d("ToDoApp","onNewTaskClicked");
        Intent taskIntent = new Intent(ToDoListActivity.this,TaskWindow.class);
        startActivity(taskIntent);
    }

    /**
     *
     * @param view
     */
    public void returnClicked(View view){
        this.finish();
    }
}