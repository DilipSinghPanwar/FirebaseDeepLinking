package com.androiddevs;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SuccessActivity extends AppCompatActivity{

    private static final String TAG = SuccessActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Intent intent = getIntent();
        String deeplinkUrl = intent.getStringExtra("key");
        Log.e(TAG, "deeplinkUrl: >>"+deeplinkUrl);
    }
}