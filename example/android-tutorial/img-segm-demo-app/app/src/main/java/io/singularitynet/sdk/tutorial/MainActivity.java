package io.singularitynet.sdk.tutorial;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
{
    private ImageView imageView;

    private ProgressBar progressBar;

    private HandlerMainActivity handler;

    private SNETServiceHelper serviceHelper;

    private Bitmap inputBitmap = null;

    private static final int REQUEST_CODE_PERMISSIONS = 0;

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


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

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

    public void initialize()
    {
        handler = new HandlerMainActivity(this);
        serviceHelper = new SNETServiceHelper(handler);

        requestPermissions(new String[]{ Manifest.permission.INTERNET}, REQUEST_CODE_PERMISSIONS);

        loadDemoImage();

        openProxyServiceChannelAsync();
    }

    private void loadDemoImage()
    {
        try
        {
            inputBitmap = BitmapFactory.decodeStream( getAssets().open("img_demo.jpg") );
            setImageBitmap(inputBitmap);
        }
        catch (IOException e)
        {
            handler.sendMessage(handler.obtainMessage(HandlerMainActivity.MSG_SHOW_ERROR, e.getMessage()));
        }
    }

    public void openProxyServiceChannelAsync()
    {
        serviceHelper.openProxyServiceChannelAsync();
    }

    public void runImageSegmentationService(View view)
    {
        if(inputBitmap != null)
                serviceHelper.callImageSegmentationServiceAsync(inputBitmap);
    }

    @Override
    protected void onDestroy()
    {
        serviceHelper.closeServiceChannel();
        super.onDestroy();
    }
}
