package com.example.snetdemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
{

    private Button btn_StyleTransfer;
    private Button btn_ImageSegmentation;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        btn_StyleTransfer = findViewById(R.id.btn_styleTransfer);
        btn_ImageSegmentation = findViewById(R.id.btn_imageSegmentation);
    }

    public void sendStyleTransferMessage(View view)
    {
        Intent intent = new Intent(this, StyleTransferActivity.class);
        startActivity(intent);
    }

    public void sendImageSegmentationMessage(View view)
    {
        Intent intent = new Intent(this, ImageSegmentationActivity.class);
        startActivity(intent);
    }

}