package com.example.snetdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity
{

    private Button btn_StyleTransfer;
    private Button btn_ImageSegmentation;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        btn_StyleTransfer = findViewById(R.id.btn_styleTransfer);
        btn_ImageSegmentation = findViewById(R.id.btn_imageSegmentation);


    }

    private void requestStoragePermission()
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
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