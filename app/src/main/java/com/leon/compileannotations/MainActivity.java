package com.leon.compileannotations;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.annotations.InjectInt;
import com.example.annotations.InjectString;


public class MainActivity extends AppCompatActivity {

    @InjectString(R.string.hello)
    public String hello;

    @InjectString(R.string.world)
    public String world;

    @InjectInt(R.integer.one)
    public int one;

    @InjectInt(R.integer.two)
    public int two;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
