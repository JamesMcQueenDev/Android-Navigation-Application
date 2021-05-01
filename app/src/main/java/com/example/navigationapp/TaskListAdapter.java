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
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder>  {

    private List<Task> tasks;
    private TasksDB db;

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

    //Sets the task list
    public void setTaskList(TasksDB db, List<Task> tasks){
        this.db = db;
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.task_layout,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.ViewHolder holder, int position)
    {
        final Task task = tasks.get(position);

        holder.titleView.setText(task.title);
        holder.descView.setText(task.description);

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

    @Override
    public int getItemCount(){
        if(tasks == null) return 0;
        return tasks.size();
    }

    //Deletes the selected held task
    public void deleteTask(int position){
        final Task task = tasks.get(position);

        Executor myExecutor = Executors.newSingleThreadExecutor();
        myExecutor.execute(new Runnable() {
            @Override
            public void run() {
                db.taskDAO().deleteTask(task);
            }
        });
    }
}