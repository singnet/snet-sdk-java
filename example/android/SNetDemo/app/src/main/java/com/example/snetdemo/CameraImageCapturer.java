package com.example.snetdemo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import static com.example.snetdemo.ImageUtils.createImageFile;
import static com.example.snetdemo.ImageUtils.galleryAddPic;

class CameraImageCapturer
{
    private final String TAG = "CameraImageCapturer";

    private static final int REQUEST_CODE_IMAGE_CAPTURE = 12;

    private final Activity activity;
    private final boolean isDeviceWithCamera;

    private String currentPhotoPath = "";
    private Uri cameraImageURI;

    public CameraImageCapturer(Activity activity) {
        this.activity = activity;
        isDeviceWithCamera = this.activity.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    public boolean hasCamera()
    {
        return isDeviceWithCamera;
    }

    public void grabImage() {

        if(isDeviceWithCamera)
        {
            File photoFile = null;

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null)
            {
                try
                {
                    photoFile = createImageFile("input_image_");
                    currentPhotoPath = photoFile.getAbsolutePath();
                }
                catch (IOException e)
                {
                    Log.e(TAG, "Exception on image file creation", e);

                    new AlertDialog.Builder(activity)
                            .setTitle("ERROR")
                            .setMessage(e.toString())
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();
                                }
                            })

                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }

                if (photoFile != null)
                {
                    cameraImageURI = FileProvider.getUriForFile(activity,
                            BuildConfig.APPLICATION_ID,
                            photoFile);

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageURI);
                    activity.startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_CAPTURE);
                }
            }

        }

    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data, Consumer<String> consumer) {
        if ( requestCode == REQUEST_CODE_IMAGE_CAPTURE)
        {
            if(resultCode==activity.RESULT_OK)
            {
                galleryAddPic(activity, currentPhotoPath);
                consumer.accept(currentPhotoPath);
            }
            else if(resultCode==activity.RESULT_CANCELED)
            {
                File f = new File(currentPhotoPath);
                if (f.exists())
                {
                    f.delete();
                }
            }
        }
    }

}
