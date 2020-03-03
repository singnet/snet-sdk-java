package com.example.snetdemo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PermissionRequester
{
    private static final int REQUEST_CODE_PERMISSIONS = 1234;

    private final Activity activity;
    private final String[] appPermissions;
    private final Runnable initApp;

    public PermissionRequester(Activity activity, String[] appPermissions, Runnable initApp) {
        this.activity = activity;
        this.appPermissions = appPermissions;
        this.initApp = initApp;
    }

    public void checkAndRequestPermissions()
    {
        List<String> permissionsRequired = new ArrayList<>();
        for(String p : appPermissions)
        {
            if(ContextCompat.checkSelfPermission(activity, p) != PackageManager.PERMISSION_GRANTED)
            {
                permissionsRequired.add(p);
            }
        }

        if(permissionsRequired.isEmpty()) {
            initApp.run();
            return;
        }

        activity.requestPermissions(permissionsRequired.toArray(
                new String[permissionsRequired.size()]),
                REQUEST_CODE_PERMISSIONS);
    }

    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    )
    {
        if (requestCode == REQUEST_CODE_PERMISSIONS)
        {
            HashMap<String, Integer> permissionResults = new HashMap<>();
            int deniedCount = 0;
            for (int i = 0; i < grantResults.length; i++)
            {
                if(grantResults[i] == PackageManager.PERMISSION_DENIED)
                {
                    permissionResults.put(permissions[i], grantResults[i]);
                    deniedCount++;
                }
            }

            if(deniedCount == 0)
            {
                initApp.run();
            }
            else
            {
                for (Map.Entry<String, Integer> entry : permissionResults.entrySet())
                {
                    String permName = entry.getKey();

                    if (activity.shouldShowRequestPermissionRationale(permName))
                    {
                        String msg = "This app needs all of the requested permissions to work without any issues. Grant permissions?";
                        new AlertDialog.Builder(activity)
                                .setTitle("Attention!")
                                .setMessage(msg)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                        checkAndRequestPermissions();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                        activity.finish();
                                    }
                                })

                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();

                    }
                    else
                    {
                        String msg = "Please allow all of the required permissions. Open Settings?";
                        new AlertDialog.Builder(activity)
                                .setTitle("Attention!")
                                .setMessage(msg)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", activity.getPackageName(), null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        activity.startActivity(intent);

                                        activity.finish();

                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                        activity.finish();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();

                        break;
                    }
                }
            }

        }
    }

}
