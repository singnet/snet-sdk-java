package com.example.snetdemo;

import android.Manifest;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

abstract class SnetDemoActivity extends AppCompatActivity
{
    private final String TAG = "SnetDemoActivity";

    private final PermissionRequester permissionRequester;

    SnetDemoActivity()
    {
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

    private final Map<Integer, ActivityResultCallback> callbackByRequestCode = new HashMap<>();
    private int nextRequestCode = 0;

    public int nextRequestCode()
    {
        int result = 10000 + nextRequestCode;
        nextRequestCode = (nextRequestCode + 1) % 10000;
        return result;
    }

    public static interface ActivityResultCallback
    {
        void onActivityResult(int requestCode, int resultCode, @Nullable Intent data);
    }

    public void startActivityForResult(Intent intent, ActivityResultCallback callback)
    {
        int requestCode = nextRequestCode();
        callbackByRequestCode.put(requestCode, callback);
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        Log.i(TAG, "onActivityResult: requestCode: " + requestCode + ", resultCode: " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        ActivityResultCallback callback = callbackByRequestCode.remove(requestCode);
        if (callback != null) {
            callback.onActivityResult(requestCode, resultCode, data);
        }
    }

}
