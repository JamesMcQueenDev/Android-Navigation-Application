package com.example.navigationapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link com.example.navigationapp.AllTaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllTaskFragment extends Fragment {

    RecyclerView recyclerView;
    TaskListAdapter taskListAdapter;
    TasksDB db;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AllTaskFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AllTaskFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static com.example.navigationapp.AllTaskFragment newInstance(String param1, String param2) {
        com.example.navigationapp.AllTaskFragment fragment = new com.example.navigationapp.AllTaskFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_task, container, false);
    }

    //My Code
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        db = TasksDB.getInstance(view.getContext());

        recyclerView = view.findViewById(R.id.taskListRecycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        taskListAdapter = new TaskListAdapter();
        recyclerView.setAdapter(taskListAdapter);

        ItemTouchHelper.SimpleCallback touchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target){
                return false;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction){
                int swipedPosition = viewHolder.getAdapterPosition();

                taskListAdapter.deleteTask(swipedPosition);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onResume(){
        super.onResume();

        LiveData<List<Task>> tasks = db.taskDAO().getAll();
        tasks.observe(this, new androidx.lifecycle.Observer<List<Task>>(){
            @Override
            public void onChanged(List<Task> tasks){
                Log.d("ToDoApp","onResume - data Changed: " + tasks.size());
                taskListAdapter.setTaskList(db,tasks);
            }
        });
    }
}