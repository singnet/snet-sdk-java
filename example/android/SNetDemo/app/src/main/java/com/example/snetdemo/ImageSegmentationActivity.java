package com.example.snetdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.singularitynet.sdk.client.OnDemandPaymentChannelPaymentStrategy;
import io.singularitynet.sdk.client.PaymentStrategy;
import io.singularitynet.sdk.client.ServiceClient;
import io.singularitynet.service.semanticsegmentation.Segmentation;
import io.singularitynet.service.semanticsegmentation.SemanticSegmentationGrpc;

import static com.example.snetdemo.ImageUtils.BitmapToJPEGByteArray;
import static com.example.snetdemo.ImageUtils.createImageFile;
import static com.example.snetdemo.ImageUtils.galleryAddPic;
import static com.example.snetdemo.ImageUtils.getPathFromUri;
import static com.example.snetdemo.ImageUtils.handleSamplingAndRotationBitmap;


public class ImageSegmentationActivity extends AppCompatActivity
{
    private final String TAG = "ImageSegmentationActivity";

    final int REQUEST_CODE_UPLOAD_INPUT_IMAGE = 10;
    final int REQUEST_CODE_IMAGE_CAPTURE = 11;
    final int REQUEST_CODE_PERMISSIONS = 1234;

    final int PROGRESS_WAITING_FOR_SERIVCE_RESPONSE = 2;
    final int PROGRESS_DECODING_SERIVCE_RESPONSE = 3;
    final int PROGRESS_LOADING_IMAGE = 4;
    final int PROGRESS_FINISHED = 5;

    private boolean isInputImageUploaded = false;

    private Button btn_UploadImageInput;
    private Button btn_RunImageSegmentation;
    private Button btn_GrabCameraImage;

    int maxImageHeight = 1024;
    int maxImageWidth = 1024;

    private ImageView imv_Input;

    private TextView textViewProgress;
    private TextView textViewResponseTime;

    private Bitmap decodedBitmap = null;

    private RelativeLayout loadingPanel;

    private long serviceResponseTime = 0;
    private boolean isDeviceWithCamera = true;

    String currentPhotoPath = "";
    Uri cameraImageURI;

    String imageInputPath = null;
    String imageSegmentedPath = null;

    private String errorMessage = "";
    private boolean isExceptionCaught = false;

    private SnetSdk sdk;
    private ServiceClient serviceClient;

    String[] appPermissions={
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private class OpenServiceChannelTask extends AsyncTask<Object, Object, Object>
    {
        protected void onPreExecute()
        {
            super.onPreExecute();
            textViewProgress.setText("Opening service channel");

            disableActivityGUI();
        }

        protected Object doInBackground(Object... param)
        {
            try
            {
                sdk = new SnetSdk(ImageSegmentationActivity.this);
                PaymentStrategy paymentStrategy = new OnDemandPaymentChannelPaymentStrategy(sdk.getSdk());
                serviceClient = sdk.getSdk().newServiceClient("snet", "semantic-segmentation",
                        "default_group", paymentStrategy);
            }
            catch (Exception e)
            {
                Log.e(TAG, "Client connection error", e);

                errorMessage = e.toString();
                isExceptionCaught = true;
            }
            return null;
        }

        protected void onPostExecute(Object obj)
        {
            if (isExceptionCaught)
            {
                isExceptionCaught = false;
                new AlertDialog.Builder(ImageSegmentationActivity.this)
                        .setTitle("ERROR")
                        .setMessage(errorMessage)

                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                                ImageSegmentationActivity.this.finish();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
            else {
                enableActivityGUI();
            }
        }
    }

    private class CloseServiceChannelTask extends AsyncTask<Object, Object, Object>
    {
        protected Object doInBackground(Object... objects)
        {
            if (serviceClient != null)
            {
                serviceClient.shutdownNow();
            }
            if( sdk != null )
            {
                sdk.close();
            }

            return null;
        }
    }

    private void initApp()
    {
        new OpenServiceChannelTask().execute();
    }

    @Override
    public void onResume(){
        super.onResume();

    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_segmentation);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setTitle("Image Segmentation Demo");

        btn_UploadImageInput = findViewById(R.id.btn_uploadImageForSegmentation);
        btn_RunImageSegmentation = findViewById(R.id.btn_runImageSegmentation);
        btn_RunImageSegmentation.setEnabled(false);

        btn_GrabCameraImage = findViewById(R.id.btn_grabCameraImage);

        imv_Input = findViewById(R.id.imageViewInput);

        loadingPanel = findViewById(R.id.loadingPanel);
        loadingPanel.setVisibility(View.INVISIBLE);

        textViewProgress = findViewById(R.id.textViewProgress);
        textViewProgress.setVisibility(View.INVISIBLE);
        textViewProgress.setText("");

        textViewResponseTime = findViewById(R.id.textViewResponseTime);
        textViewResponseTime.setText("Service response time (ms):");
        textViewResponseTime.setVisibility(View.INVISIBLE);

        PackageManager pm = this.getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
        {
            isDeviceWithCamera = false;
            btn_GrabCameraImage.setEnabled(false);
        }

        disableActivityGUI();

        if( checkAndRequestPermissions() )
        {
            initApp();
        }

    }
    public boolean checkAndRequestPermissions()
    {
        List<String> permissionsRequired = new ArrayList<>();
        for(String p : appPermissions)
        {
            if(ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED)
            {
                permissionsRequired.add(p);
            }
        }
        if( !permissionsRequired.isEmpty())
        {
            requestPermissions(permissionsRequired.toArray(
                    new String[permissionsRequired.size()]),
                    REQUEST_CODE_PERMISSIONS);
            return false;
        }

        return true;
    }

    @Override
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
                initApp();
            }
            else
            {
                for (Map.Entry<String, Integer> entry : permissionResults.entrySet())
                {
                    String permName = entry.getKey();

                    if (shouldShowRequestPermissionRationale(permName))
                    {
                        String msg = "This app needs all of the requested permissions to work without any issues. Grant permissions?";
                        new AlertDialog.Builder(ImageSegmentationActivity.this)
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
                                        finish();
                                    }
                                })

                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();

                    }
                    else
                    {
                        String msg = "Please allow all of the required permissions. Open Settings?";
                        new AlertDialog.Builder(ImageSegmentationActivity.this)
                                .setTitle("Attention!")
                                .setMessage(msg)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", getPackageName(), null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);

                                        finish();

                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                        finish();
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

    @Override
    protected void onDestroy()
    {
        new CloseServiceChannelTask().execute();
        super.onDestroy();
    }

    private void disableActivityGUI()
    {
        textViewProgress.setVisibility(View.VISIBLE);
        loadingPanel.setVisibility(View.VISIBLE);

        btn_UploadImageInput.setEnabled(false);
        btn_RunImageSegmentation.setEnabled(false);

        btn_GrabCameraImage.setEnabled(false);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void enableActivityGUI()
    {
        loadingPanel.setVisibility(View.INVISIBLE);
        textViewProgress.setVisibility(View.INVISIBLE);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        btn_UploadImageInput.setEnabled(true);

        if (isInputImageUploaded) {
            btn_RunImageSegmentation.setEnabled(true);
        }

        if(isDeviceWithCamera) {
            btn_GrabCameraImage.setEnabled(true);
        }

    }


    private class CallingServiceTask extends AsyncTask<Object, Integer, Object>
    {
        protected void onPreExecute()
        {
            super.onPreExecute();

            disableActivityGUI();

        }

        protected Void doInBackground(Object... param)
        {
            publishProgress(new Integer(PROGRESS_LOADING_IMAGE));
            Bitmap bitmap = null;
            try
            {
                bitmap = handleSamplingAndRotationBitmap(ImageSegmentationActivity.this,
                        Uri.fromFile(new File(imageInputPath)),
                        maxImageWidth, maxImageHeight);
            }
            catch (IOException e)
            {
                Log.e(TAG, "Exception on loading bitmap", e);

                errorMessage = e.toString();
                isExceptionCaught = true;

                return null;
            }

            byte[] bytesInput = BitmapToJPEGByteArray(bitmap);
            publishProgress(new Integer(PROGRESS_WAITING_FOR_SERIVCE_RESPONSE));
            Segmentation.Request request = Segmentation.Request.newBuilder()
                    .setImg(Segmentation.Image.newBuilder()
                            .setContent(ByteString.copyFrom(bytesInput))
                            .setMimetype("image/jpeg")
                            .build()
                    )
                    .setVisualise(true)
                    .build();

            long startTime = System.nanoTime();
            Segmentation.Result response = null;

            try
            {
                response = serviceClient.getGrpcStub(SemanticSegmentationGrpc::newBlockingStub).segment(request);
            }
            catch (Exception e)
            {
                Log.e(TAG, "Exception on service call", e);

                errorMessage = e.toString();
                isExceptionCaught = true;

                return null;
            }

            serviceResponseTime = System.nanoTime() - startTime;

            publishProgress(new Integer(PROGRESS_DECODING_SERIVCE_RESPONSE));

            Segmentation.Image dbgImage = response.getDebugImg();
            byte[] decodedBytes = dbgImage.getContent().toByteArray();
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            ImageSegmentationActivity.this.decodedBitmap = decodedBitmap;
            File fr = null;
            try {
                fr = createImageFile("segmented_image_");
            }
            catch (IOException e)
            {
                Log.e(TAG, "Exception on file creation", e);
                errorMessage = e.toString();
                isExceptionCaught = true;

                return null;
            }
            imageSegmentedPath = fr.getAbsolutePath();
            try (FileOutputStream out = new FileOutputStream(fr))
            {
                ImageSegmentationActivity.this.decodedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            } catch (IOException e)
            {
                Log.e(TAG, "Exception on saving bitmap", e);
                errorMessage = e.toString();
                isExceptionCaught = true;

                return null;
            }

            publishProgress(PROGRESS_FINISHED);

            return null;
        }

        protected void onProgressUpdate(Integer... progress)
        {
            int v = progress[0].intValue();
            switch (v)
            {
                case PROGRESS_WAITING_FOR_SERIVCE_RESPONSE:
                    textViewProgress.setVisibility(View.VISIBLE);
                    textViewProgress.setText("Waiting for response");
                    break;
                case PROGRESS_DECODING_SERIVCE_RESPONSE:
                    textViewProgress.setText("Decoding response");
                    break;
                case PROGRESS_LOADING_IMAGE:
                    textViewProgress.setText("Loading Image");
                    break;
                case PROGRESS_FINISHED:
                    textViewProgress.setVisibility(View.INVISIBLE);
                    break;
            }

        }

        protected void onPostExecute(Object obj)
        {
            if (isExceptionCaught)
            {
                isExceptionCaught = false;
                new AlertDialog.Builder(ImageSegmentationActivity.this)
                        .setTitle("ERROR")
                        .setMessage(errorMessage)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })

                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            else
            {
                imv_Input.setImageBitmap(decodedBitmap);
                galleryAddPic(ImageSegmentationActivity.this, imageSegmentedPath);

                serviceResponseTime /= 1e6;
                textViewResponseTime.setText("Service response time (ms): " + String.valueOf(serviceResponseTime));
                textViewResponseTime.setVisibility(View.VISIBLE);

                isInputImageUploaded = false;

            }

            enableActivityGUI();

        }
    }

    public void sendRunImageSegmentationMessage(View view)
    {
        if(this.isInputImageUploaded)
        {
            new CallingServiceTask().execute();
            isInputImageUploaded = false;
        }
    }

    public void sendUploadInputImageMessage(View view)
    {
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("image/*");
        startActivityForResult(fileIntent, REQUEST_CODE_UPLOAD_INPUT_IMAGE);

        textViewResponseTime.setVisibility(View.INVISIBLE);
    }

    private void loadImageFromFileToImageView(ImageView imgView, Uri fileURI)
    {
        Glide.with(this)
                .clear(imgView);

        Glide.with(ImageSegmentationActivity.this)
                .load(fileURI)
                .fitCenter()
                .into(imgView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == REQUEST_CODE_UPLOAD_INPUT_IMAGE)
        {
            if(resultCode==RESULT_OK)
            {
                isInputImageUploaded = true;
                loadImageFromFileToImageView(imv_Input, data.getData());
                imageInputPath = getPathFromUri(this, data.getData());

                btn_RunImageSegmentation.setEnabled(true);

            }
        }

        if ( requestCode == REQUEST_CODE_IMAGE_CAPTURE)
        {
            if(resultCode==RESULT_OK)
            {
                isInputImageUploaded = true;
                galleryAddPic(this, currentPhotoPath);

                File f = new File(currentPhotoPath);
                loadImageFromFileToImageView(imv_Input, Uri.fromFile(f));
                imageInputPath = currentPhotoPath;
                btn_RunImageSegmentation.setEnabled(true);
            }
            else if(resultCode==RESULT_CANCELED)
            {
                File f = new File(currentPhotoPath);
                if (f.exists())
                {
                    f.delete();
                }
            }
        }
    }

    public void sendGrabCameraImageMessage(View view)
    {
        if(isDeviceWithCamera)
        {
            textViewResponseTime.setVisibility(View.INVISIBLE);
            File photoFile = null;

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null)
            {
                try
                {
                    photoFile = createImageFile("input_image_");
                    currentPhotoPath = photoFile.getAbsolutePath();
                }
                catch (IOException e)
                {
                    Log.e(TAG, "Exception on image file creation", e);

                    new AlertDialog.Builder(ImageSegmentationActivity.this)
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
                    cameraImageURI = FileProvider.getUriForFile(this,
                            BuildConfig.APPLICATION_ID,
                            photoFile);

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageURI);
                    startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_CAPTURE);
                }
            }

        }
    }

}





