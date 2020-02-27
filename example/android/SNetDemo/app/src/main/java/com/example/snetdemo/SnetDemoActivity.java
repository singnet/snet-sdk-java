package com.example.snetdemo;

import android.Manifest;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

abstract class SnetDemoActivity extends AppCompatActivity
{
    private final PermissionRequester permissionRequester;

    SnetDemoActivity() {
        this.permissionRequester = new PermissionRequester(this, new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        }, () -> initApp());
    }

    protected abstract void initApp();

    public void checkPermissionsAndInitApp()
    {
        permissionRequester.checkAndRequestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    )
    {
        permissionRequester.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
