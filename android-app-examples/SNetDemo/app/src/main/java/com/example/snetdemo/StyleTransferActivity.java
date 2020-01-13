package com.example.snetdemo;

import android.Manifest;
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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;

import io.singularitynet.service.styletransfer.StyleTransferOuterClass;

import static com.example.snetdemo.ImageUtils.BitmapToJPEGBase64String;
import static com.example.snetdemo.ImageUtils.createImageFile;
import static com.example.snetdemo.ImageUtils.galleryAddPic;
import static com.example.snetdemo.ImageUtils.getPathFromUri;
import static com.example.snetdemo.ImageUtils.handleSamplingAndRotationBitmap;


public class StyleTransferActivity extends AppCompatActivity
{

    final int REQUEST_CODE_UPLOAD_INPUT_IMAGE = 10;
    final int REQUEST_CODE_UPLOAD_STYLE_IMAGE = 11;
    final int REQUEST_CODE_IMAGE_CAPTURE = 12;
    final int REQUEST_CODE_SHOW_IMAGE = 13;


    final int PROGRESS_WAITING_FOR_SERIVCE_RESPONSE = 2;
    final int PROGRESS_DECODING_SERIVCE_RESPONSE = 3;
    final int PROGRESS_LOADING_IMAGE = 4;
    final int PROGRESS_FINISHED = 5;

    private Button btn_UploadImageStyle;
    private Button btn_UploadImageInput;
    private Button btn_GrabCameraImage;
    private Button btn_RunStyleTransfer;

    private boolean isInputImageUploaded = false;
    private boolean isStyleImageUploaded = false;

    private ImageView imv_Style;
    private ImageView imv_Input;

    private Bitmap decodedBitmap = null;

    String imageInputPath = null;
    String imageStylePath = null;
    String imageResultPath = null;

    String currentPhotoPath = "";
    Uri cameraImageURI;

    private long serviceResponseTime = 0;
    private boolean isDeviceWithCamera = true;

    private TextView textViewProgress;
    private TextView textViewResponseTime;

    private boolean imgViewSizeFixed = false;

    private RelativeLayout loadingPanel;

    private String errorMessage = "";
    private boolean isExceptionCaught = false;


    private int mChannelID;
    private StyleTransferService styleTransferService;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_style_transfer);

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

        setTitle("Style Transfer Demo");

        btn_UploadImageStyle = (Button) findViewById(R.id.btn_uploadImageStyle);
        btn_UploadImageInput = (Button) findViewById(R.id.btn_uploadImageInput);
        btn_GrabCameraImage = (Button) findViewById(R.id.btn_grabCameraImage);
        btn_RunStyleTransfer = (Button) findViewById(R.id.btn_runStyleTransfer);

        textViewResponseTime = (TextView) findViewById(R.id.textViewResponseTime);
        textViewResponseTime.setVisibility(View.INVISIBLE);

        textViewProgress = (TextView) findViewById(R.id.textViewProgress);
        textViewProgress.setVisibility(View.INVISIBLE);

        btn_RunStyleTransfer.setEnabled(false);

        imv_Style = (ImageView)findViewById(R.id.imageViewStyle);
        imv_Input = (ImageView)findViewById(R.id.imageViewInput);

        loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);
        loadingPanel.setVisibility(View.INVISIBLE);

        PackageManager pm = this.getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
        {
            isDeviceWithCamera = false;
            btn_GrabCameraImage.setEnabled(false);
        }

        mChannelID = this.getResources().getInteger(R.integer.channel_id);

        AsyncTask task = new AsyncTask<Object, Object, Object>()
        {

            protected void onPreExecute()
            {
                super.onPreExecute();

                textViewProgress.setText("Opening service channel");
                textViewProgress.setVisibility(View.VISIBLE);
                loadingPanel.setVisibility(View.VISIBLE);

                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


            }

            @Override
            protected Object doInBackground(Object... param)
            {
                SnetSdk sdk = new SnetSdk(StyleTransferActivity.this);
                styleTransferService = new StyleTransferService(sdk, BigInteger.valueOf(mChannelID));
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

    @Override
    public void onWindowFocusChanged(boolean focus) {
        super.onWindowFocusChanged(focus);
        // get the imageviews width and height here

        int finalImgViewWidth;
        int finalImgViewHeight;

        if (!imgViewSizeFixed)
        {
            finalImgViewWidth = imv_Style.getWidth();
            finalImgViewHeight = imv_Style.getHeight();

            imv_Input.setLayoutParams(new LinearLayout.LayoutParams(
                    finalImgViewWidth,
                    finalImgViewHeight));

            imv_Style.setLayoutParams(new LinearLayout.LayoutParams(
                    finalImgViewWidth,
                    finalImgViewHeight));

            imgViewSizeFixed = true;
        }

    }

    public void sendUploadInputImageMessage(View view)
    {
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("*/*");
        startActivityForResult(fileIntent, REQUEST_CODE_UPLOAD_INPUT_IMAGE);
    }

    public void sendUploadStyleImageMessage(View view)
    {
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("*/*");
        startActivityForResult(fileIntent, REQUEST_CODE_UPLOAD_STYLE_IMAGE);
    }


    private void loadImageFromFileToImageView(ImageView imgView, Uri fileURI)
    {
        Glide.with(this)
                .clear(imgView);

        Glide.with(StyleTransferActivity.this)
                .load(fileURI)
                .fitCenter()
                .into(imgView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if ( requestCode == REQUEST_CODE_UPLOAD_INPUT_IMAGE)
        {
            if (resultCode == RESULT_OK)
            {
                isInputImageUploaded = true;
                loadImageFromFileToImageView(imv_Input, data.getData());
                imageInputPath = getPathFromUri(this, data.getData());
            }
        }
        if (requestCode == REQUEST_CODE_UPLOAD_STYLE_IMAGE)
        {
            if(resultCode == RESULT_OK)
            {
                isStyleImageUploaded = true;
                loadImageFromFileToImageView(imv_Style, data.getData());
                imageStylePath = getPathFromUri(this, data.getData());
            }
        }

        if (requestCode == REQUEST_CODE_IMAGE_CAPTURE)
        {
            if(resultCode==RESULT_OK)
            {
                isInputImageUploaded = true;
                galleryAddPic(this, currentPhotoPath);

                File f = new File(currentPhotoPath);
                loadImageFromFileToImageView(imv_Input, Uri.fromFile(f));
                imageInputPath = currentPhotoPath;

            }
        }

        if( isInputImageUploaded && isStyleImageUploaded)
        {
            btn_RunStyleTransfer.setEnabled(true);
        }

    }

    public void sendGrabCameraImageMessage(View view)
    {
        if(isDeviceWithCamera)
        {
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
                    Log.e("ERROR", Calendar.getInstance().getTime().toString() + " ERROR IN IMAGE FILE CREATION: " + e.toString());
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
    public void sendRunStyleTransferMessage(View view)
    {
        if(this.isInputImageUploaded && this.isStyleImageUploaded)
        {
            AsyncTask task = new AsyncTask<Object, Integer, Object>()
            {

                protected void onPreExecute()
                {
                    super.onPreExecute();
                    textViewProgress.setVisibility(View.VISIBLE);
                    loadingPanel.setVisibility(View.VISIBLE);

                    btn_UploadImageInput.setEnabled(false);
                    btn_UploadImageStyle.setEnabled(false);
                    btn_RunStyleTransfer.setEnabled(false);
                    btn_GrabCameraImage.setEnabled(false);

                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                }

                @Override
                protected Object doInBackground(Object... param)
                {
                    publishProgress(new Integer(PROGRESS_LOADING_IMAGE));
                    Bitmap bitmapInput = null;
                    Bitmap bitmapStyle = null;
                    try
                    {
                        bitmapInput = handleSamplingAndRotationBitmap(StyleTransferActivity.this, Uri.fromFile(new File(imageInputPath)));
                        bitmapStyle = handleSamplingAndRotationBitmap(StyleTransferActivity.this, Uri.fromFile(new File(imageStylePath)));
                    }
                    catch (IOException e)
                    {
                        Log.e("ERROR IN LOADING BITMAP", Calendar.getInstance().getTime().toString() + e.toString());
                        e.printStackTrace();

                        errorMessage = e.toString();
                        isExceptionCaught = true;

                        return null;
                    }

                    String inputBase64 = BitmapToJPEGBase64String(bitmapInput);
                    String styleBase64 = BitmapToJPEGBase64String(bitmapStyle);

                    publishProgress(new Integer(PROGRESS_WAITING_FOR_SERIVCE_RESPONSE));
                    StyleTransferOuterClass.TransferImageStyleRequest request = StyleTransferOuterClass.TransferImageStyleRequest.newBuilder()
                            .setContent(inputBase64)
                            .setStyle(styleBase64)
                            .setPreserveColor(true)
                            .build();

                    long startTime = System.nanoTime();
                    StyleTransferOuterClass.Image response = null;

                    try
                    {
                        response = styleTransferService.getStub().transferImageStyle(request);
                    }
                    catch (Exception e)
                    {
                        Log.e("ERROR", Calendar.getInstance().getTime().toString() + " ERROR IN SERVICE CALL: " + e.toString());
                        e.printStackTrace();

                        errorMessage = e.toString();
                        isExceptionCaught = true;
                        return null;
                    }

                    serviceResponseTime = System.nanoTime() - startTime;
                    publishProgress(new Integer(PROGRESS_DECODING_SERIVCE_RESPONSE));

                    String encodedImage = response.getData();

                    byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    StyleTransferActivity.this.decodedBitmap = decodedBitmap;
                    File fr = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), BuildConfig.APPLICATION_ID + "/segmented_image.jpg");
                    imageResultPath = fr.getAbsolutePath();
                    try (FileOutputStream out = new FileOutputStream(fr))
                    {
                        StyleTransferActivity.this.decodedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

                    } catch (IOException e)
                    {
                        Log.e("ERROR", Calendar.getInstance().getTime().toString() + " ERROR IN BITMAP SAVING: " + e.toString());
                        e.printStackTrace();
                        errorMessage = e.toString();
                        isExceptionCaught = true;
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
                    btn_UploadImageStyle.setEnabled(true);
                    btn_RunStyleTransfer.setEnabled(true);

                    serviceResponseTime /= 1e6;
                    textViewResponseTime.setText("Service response time (ms): " + String.valueOf(serviceResponseTime));
//                    textViewResponseTime.setVisibility(View.VISIBLE);

                    if(isDeviceWithCamera) {
                        btn_GrabCameraImage.setEnabled(true);
                    }


                    if (!isExceptionCaught)
                    {
                        Intent intent = new Intent(StyleTransferActivity.this, ImageShowActivity.class);

                        intent.putExtra("img_path", imageResultPath);
                        intent.putExtra("response_time", serviceResponseTime);

                        startActivityForResult(intent, REQUEST_CODE_SHOW_IMAGE);
                    }
                    else
                    {
                        isExceptionCaught = false;
                        new AlertDialog.Builder(StyleTransferActivity.this)
                                .setTitle("Error in service call")
                                .setMessage(errorMessage)

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

        }
    }


    @Override
    public void onResume()
    {
        super.onResume();

        if(this.isInputImageUploaded && this.isStyleImageUploaded)
        {
            btn_RunStyleTransfer.setEnabled(true);
        }

    }

}
