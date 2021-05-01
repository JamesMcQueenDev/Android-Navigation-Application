package com.example.navigationapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.firebase.ui.auth.AuthUI;

import java.util.List;

public class ToDoListActivity extends AppCompatActivity {

    TasksDB db;
    TaskListAdapter taskListAdapter;

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

        db = Room.databaseBuilder(getApplicationContext(),
                TasksDB.class,
                "tasks_database_name").build();


        RecyclerView recyclerView = findViewById(R.id.taskListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskListAdapter = new TaskListAdapter();
        recyclerView.setAdapter(taskListAdapter);

        //Allows user to delete task by swiping the task card to the right
        ItemTouchHelper.SimpleCallback touchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            /**
             *
             * @param recyclerView
             * @param viewHolder
             * @param target
             * @return
             */
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            /**
             *
             * @param viewHolder
             * @param direction
             */
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction){
                int swipedPosition = viewHolder.getAdapterPosition();

                taskListAdapter.deleteTask(swipedPosition);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Updates the landing page to display the tasks taken in
     */
    @Override
    protected void onResume() {
        super.onResume();

        LiveData<List<Task>> tasks = db.taskDAO().getAll();

        tasks.observe(this, new Observer<List<Task>>(){
            @Override
            public void onChanged(List<Task> tasks){
                for(Task task: tasks){
                    taskListAdapter.setTaskList(db,tasks);
                }
            }
        });
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