package com.leon.compileannotations;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.annotations.Inject;


public class MainActivity extends AppCompatActivity {

    @Inject
    public String mString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }
}
