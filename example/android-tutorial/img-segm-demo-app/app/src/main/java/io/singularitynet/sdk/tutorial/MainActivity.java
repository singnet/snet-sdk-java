package io.singularitynet.sdk.tutorial;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private Button btnOpenImage;
    private Button btnCallService;

    private ImageView imageView;

    private ProgressBar progressBar;

    private boolean isInputImageUploaded = false;

    private HandlerMainActivity handler;

    private SNETServiceHelper serviceHelper;

    private Uri imageUri;

    private String[] appPermissions;

    private static final int REQUEST_CODE_OPEN_IMAGE = 0;
    private static final int REQUEST_CODE_PERMISSIONS = 1234;

    private final int MAX_IMAGE_HEIGHT = 1024;
    private final int MAX_IMAGE_WIDTH = 1024;

    public static AlertDialog.Builder newAlertDialogBuilder(Context context) {
        return new AlertDialog.Builder(context)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert);
    }

    public void msgShowException(Exception e)
    {
        msgShowError(e.getMessage());
    }

    public void msgShowError(String message)
    {
        handler.sendMessage(handler.obtainMessage(
                HandlerMainActivity.MSG_SHOW_ERROR, message));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpenImage = findViewById(R.id.btnOpenImage);
        btnCallService= findViewById(R.id.btnCallService);
        btnCallService.setEnabled(false);

        imageView = findViewById(R.id.imageView);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        handler = new HandlerMainActivity(this);
        serviceHelper = new SNETServiceHelper(handler);

        appPermissions = new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        initialize();
    }

    public void enableActivityGUI()
    {
        progressBar.setVisibility(View.INVISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void disableActivityGUI()
    {
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
    public void setImageBitmap(Bitmap bm)
    {
        imageView.setImageBitmap(bm);
    }

    private List<String> getRequiredPermissions()
    {
        List<String> permissionsRequired = new ArrayList<>();

        for(String p : appPermissions)
        {
            if(ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED)
            {
                permissionsRequired.add(p);
            }
        }

        return permissionsRequired;
    }
    public void checkAndRequestPermissions()
    {
        List<String> permissionsRequired = getRequiredPermissions();
        if(permissionsRequired.isEmpty())
        {
            return;
        }

        requestPermissions(permissionsRequired.toArray(
                new String[permissionsRequired.size()]),
                REQUEST_CODE_PERMISSIONS);
    }
    public void initialize()
    {
        checkAndRequestPermissions();
        openProxyServiceChannelAsync();
    }

    public void openProxyServiceChannelAsync()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)
        {
            serviceHelper.openProxyServiceChannelAsync();
        }
    }

    public void runImageSegmentationService(View view)
    {
        List<String> permissionsRequired = getRequiredPermissions();
        if(!permissionsRequired.isEmpty())
        {
            if(serviceHelper != null)
                serviceHelper.callImageSegmentationServiceAsync(imageUri, maxImageHeight, maxImageWidth);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            isInputImageUploaded = true;
            assert data != null;
            imageUri = data.getData();
            loadImageFromFileToImageView(imageView, imageUri);

            btnCallService.setEnabled(true);
        }
    }

    public void onOpenImage(View view)
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED)
        {
            Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
            fileIntent.setType("image/*");
            startActivityForResult(fileIntent, REQUEST_CODE_OPEN_IMAGE);
        }
    }

    private void loadImageFromFileToImageView(ImageView imgView, Uri fileURI)
    {
        Glide.with(this)
                .clear(imgView);

        Glide.with(this)
                .load(fileURI)
                .fitCenter()
                .into(imgView);
    }

    public Uri getImageUri()
    {
        return imageUri;
    }

    @Override
    protected void onDestroy()
    {
        serviceHelper.closeServiceChannel();
        super.onDestroy();
    }
}
