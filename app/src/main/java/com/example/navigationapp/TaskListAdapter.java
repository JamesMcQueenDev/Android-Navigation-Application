package com.example.navigationapp;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder>  {

    private List<Task> tasks;
    private TasksDB db;

    String taskTitle;

    //Assigns components to held variables.
    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView titleView;
        TextView descView;
        TextView longitudeView;
        TextView latitudeView;
        ImageView imageView;
        CheckBox doneCheckBox;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            titleView = itemView.findViewById(R.id.taskListTitle);
            descView = itemView.findViewById(R.id.taskListDescription);
            longitudeView = itemView.findViewById(R.id.taskListLongitude);
            latitudeView = itemView.findViewById(R.id.taskListLatitude);
            imageView = itemView.findViewById(R.id.taskListImage);
            doneCheckBox = itemView.findViewById(R.id.taskListDone);
        }
    }

    /**
     * Sets the task list
     * @param db
     * @param tasks
     */
    public void setTaskList(TasksDB db, List<Task> tasks){
        this.db = db;
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    /**
     *
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public TaskListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.task_layout,parent,false);

        return new ViewHolder(view);
    }

    /**
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position)
    {
        final Task task = tasks.get(position);

        taskTitle = task.title;

        holder.titleView.setText(task.title);
        holder.descView.setText(task.description);

/*        holder.longitudeView.setText((int)task.longitude);
        holder.latitudeView.setText((int)task.latitude);*/

        holder.imageView.setImageURI(Uri.parse(task.image));

        holder.doneCheckBox.setChecked(task.done);

        holder.doneCheckBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        task.done = isChecked;

                        Executor myExecutor = Executors.newSingleThreadExecutor();
                        myExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                db.taskDAO().updateTask(task);
                            }
                        });
                    }
                }
        );
    }

    /**
     *
     * @return
     */
    @Override
    public int getItemCount(){
        if(tasks == null) return 0;
        return tasks.size();
    }


    Map<String,StoredTask> fieldToDelete = new HashMap();
    /**
     * Deletes the selected held task
     * @param position
     */
    public void deleteTask(int position){
        final Task task = tasks.get(position);

        FirebaseFirestore.getInstance().collection("tasks")
                .document(taskTitle)
                .delete();

        Executor myExecutor = Executors.newSingleThreadExecutor();
        myExecutor.execute(new Runnable() {
            @Override
            public void run() {
                db.taskDAO().deleteTask(task);
            }
        });

    }
}