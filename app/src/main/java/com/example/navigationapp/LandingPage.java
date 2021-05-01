package com.example.navigationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LandingPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
/*        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();*/
    }

    public void onTaskButtonClick(View view){
        Intent intent = new Intent(this,ToDoListActivity.class);
        startActivity(intent);
    }

    public void onMapButtonClick(View view){
        Intent intent = new Intent(this,MapViewer.class);
        startActivity(intent);
    }

    public void onSignOutClick(View view){
        SignOut();
    }

    public void SignOut(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
    }
}