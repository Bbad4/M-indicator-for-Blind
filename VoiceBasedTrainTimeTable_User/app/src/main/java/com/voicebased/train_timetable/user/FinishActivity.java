package com.voicebased.train_timetable.user;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class FinishActivity extends AppCompatActivity {


    // This activity is used for closing the application of voice command
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        finish();
    }
}
