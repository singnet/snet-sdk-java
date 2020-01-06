package com.example.snetdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Calendar;

import io.singularitynet.service.semanticsegmentation.Segmentation;

import static com.example.snetdemo.ImageUtils.*;


public class ImageSegmentationActivity extends AppCompatActivity
{

    final int REQUEST_CODE_UPLOAD_INPUT_IMAGE = 10;
    final int REQUEST_CODE_IMAGE_CAPTURE = 11;

    final int PROGRESS_WAITING_FOR_SERIVCE_RESPONSE = 2;
    final int PROGRESS_DECODING_SERIVCE_RESPONSE = 3;
    final int PROGRESS_LOADING_IMAGE = 4;
    final int PROGRESS_FINISHED = 5;

    private boolean mIsInputImageUploaded = false;

    private Button btn_UploadImageInput;
    private Button btn_RunImageSegmentation;
    private Button btn_GrabCameraImage;

    private ImageView imv_Input;

    private TextView textViewProgress;
    private TextView textViewResponseTime;

    private Bitmap mDecodedBitmap = null;

    private RelativeLayout loadingPanel;

    private long mServiceResponseTime = 0;
    private boolean mIsDeviceWithCamera = true;

    String mCurrentPhotoPath = "";
    Uri mCameraImageURI;

    byte[] mBytesInput = null;

    String mImageInputPath = null;
    String mImageSegmentedPath = null;

    private String mErrorMessage = "";
    private boolean isExceptionCaught = false;

    private int channelID;
    private SemanticSegmentationService semanticSegmentationService;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_segmentation);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.INTERNET}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
        }

        setTitle("Image Segmentation Demo");

        btn_UploadImageInput = (Button) findViewById(R.id.btn_uploadImageForSegmentation);
        btn_RunImageSegmentation = (Button) findViewById(R.id.btn_runImageSegmentation);
        btn_RunImageSegmentation.setEnabled(false);

        btn_GrabCameraImage = (Button) findViewById(R.id.btn_grabCameraImage);

        imv_Input = (ImageView)findViewById(R.id.imageViewInput);

        loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);
        loadingPanel.setVisibility(View.INVISIBLE);

        textViewProgress = findViewById(R.id.textViewProgress);
        textViewProgress.setVisibility(View.INVISIBLE);

        textViewResponseTime = findViewById(R.id.textViewResponseTime);
        textViewResponseTime.setText("Service response time (ms):");
        textViewResponseTime.setVisibility(View.INVISIBLE);


        PackageManager pm = this.getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
        {
            mIsDeviceWithCamera = false;
            btn_GrabCameraImage.setEnabled(false);
        }

        channelID = this.getResources().getInteger(R.integer.channel_id);

        AsyncTask task = new AsyncTask<Object, Object, Object>()
        {

            protected void onPreExecute()
            {
                super.onPreExecute();

                textViewProgress.setVisibility(View.VISIBLE);
                textViewProgress.setText("Opening service channel");
                loadingPanel.setVisibility(View.VISIBLE);

                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


            }

            @Override
            protected Object doInBackground(Object... param)
            {
                SnetSdk sdk = new SnetSdk(ImageSegmentationActivity.this);
                semanticSegmentationService = new SemanticSegmentationService(sdk, BigInteger.valueOf(channelID));
                return null;
            }

            protected void onPostExecute(Object obj)
            {
                textViewProgress.setVisibility(View.INVISIBLE);
                loadingPanel.setVisibility(View.INVISIBLE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }


        };
        task.execute();

    }

    public void sendRunImageSegmentationMessage(View view)
    {
        if(this.mIsInputImageUploaded)
        {
            AsyncTask task = new AsyncTask<Object, Integer, Object>()
            {
                protected void onPreExecute()
                {
                    super.onPreExecute();
                    textViewProgress.setVisibility(View.VISIBLE);
                    loadingPanel.setVisibility(View.VISIBLE);

                    btn_UploadImageInput.setEnabled(false);
                    btn_RunImageSegmentation.setEnabled(false);

                    btn_GrabCameraImage.setEnabled(false);

                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


                }

                protected Void doInBackground(Object... param)
                {
                    publishProgress(new Integer(PROGRESS_LOADING_IMAGE));
                    Bitmap bitmap = null;
                    try
                    {
                        bitmap = handleSamplingAndRotationBitmap(ImageSegmentationActivity.this, Uri.fromFile(new File(mImageInputPath)));
                    }
                    catch (IOException e)
                    {
                        Log.e("ERROR IN LOADING BITMAP ", Calendar.getInstance().getTime().toString() + e.toString());
                        e.printStackTrace();

                        mErrorMessage = e.toString();
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
                        response = semanticSegmentationService.getStub().segment(request);
                    }
                    catch (Exception e)
                    {
                        Log.e("ERROR", Calendar.getInstance().getTime().toString() + " ERROR IN SERVICE CALL: " + e.toString());
                        e.printStackTrace();

                        mErrorMessage = e.toString();
                        isExceptionCaught = true;

                        return null;
                    }

                    mServiceResponseTime = System.nanoTime() - startTime;

                    publishProgress(new Integer(PROGRESS_DECODING_SERIVCE_RESPONSE));

                    Segmentation.Image dbgImage = response.getDebugImg();
                    byte[] decodedBytes = dbgImage.getContent().toByteArray();
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                    mDecodedBitmap = decodedBitmap;
                    File fr = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), BuildConfig.APPLICATION_ID + "/segmented_image.jpg");
                    mImageSegmentedPath = fr.getAbsolutePath();
                    try (FileOutputStream out = new FileOutputStream(fr))
                    {
                        mDecodedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

                    } catch (IOException e)
                    {
                        Log.e("ERROR", Calendar.getInstance().getTime().toString() + " ERROR IN BITMAP SAVING: " + e.toString());
                        e.printStackTrace();

                        mErrorMessage = e.toString();
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
                    loadingPanel.setVisibility(View.INVISIBLE);
                    textViewProgress.setVisibility(View.INVISIBLE);

                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    btn_UploadImageInput.setEnabled(true);

                    imv_Input.setImageBitmap(mDecodedBitmap);

                    mServiceResponseTime /= 1e6;
                    textViewResponseTime.setText("Service response time (ms): " + String.valueOf(mServiceResponseTime));
                    textViewResponseTime.setVisibility(View.VISIBLE);

                    btn_RunImageSegmentation.setEnabled(false);

                    if(mIsDeviceWithCamera) {
                        btn_GrabCameraImage.setEnabled(true);
                    }

                    if (isExceptionCaught)
                    {
                        isExceptionCaught = false;
                        new AlertDialog.Builder(ImageSegmentationActivity.this)
                                .setTitle("Error in service call")
                                .setMessage(mErrorMessage)

                                // Specifying a listener allows you to take an action before dismissing the dialog.
                                // The dialog is automatically dismissed when a dialog button is clicked.
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Continue with delete operation
                                    }
                                })

//                                // A null listener allows the button to dismiss the dialog and take no further action.
//                                .setNegativeButton(android.R.string.no, null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                }
            };

            task.execute();
            mIsInputImageUploaded = false;
        }
    }

    public void sendUploadInputImageMessage(View view)
    {
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("*/*");
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
        if ( requestCode == REQUEST_CODE_UPLOAD_INPUT_IMAGE)
        {
            if(resultCode==RESULT_OK)
            {
                mIsInputImageUploaded = true;
                loadImageFromFileToImageView(imv_Input, data.getData());
                mImageInputPath = getPathFromUri(this, data.getData());

                btn_RunImageSegmentation.setEnabled(true);

            }
        }

        if ( requestCode == REQUEST_CODE_IMAGE_CAPTURE)
        {
            if(resultCode==RESULT_OK)
            {
                mIsInputImageUploaded = true;
                galleryAddPic(this, mCurrentPhotoPath);

                File f = new File(mCurrentPhotoPath);
                loadImageFromFileToImageView(imv_Input, Uri.fromFile(f));
                mImageInputPath = mCurrentPhotoPath;
                btn_RunImageSegmentation.setEnabled(true);
            }
        }
    }

    public void sendGrabCameraImageMessage(View view)
    {
        if(mIsDeviceWithCamera)
        {
            textViewResponseTime.setVisibility(View.INVISIBLE);
            File photoFile = null;

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null)
            {
                try
                {
                    photoFile = createImageFile("input_image_");
                    mCurrentPhotoPath = photoFile.getAbsolutePath();
                }
                catch (IOException e)
                {
                    Log.e("ERROR", Calendar.getInstance().getTime().toString() + " ERROR IN IMAGE FILE CREATION: " + e.toString());
                }

                if (photoFile != null)
                {
                    mCameraImageURI = FileProvider.getUriForFile(this,
                            BuildConfig.APPLICATION_ID,
                            photoFile);

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraImageURI);
                    startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_CAPTURE);
                }
            }

        }
    }

}






