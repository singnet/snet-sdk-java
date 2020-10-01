package io.singularitynet.sdk.tutorial;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class HandlerMainActivity extends Handler
{
    private MainActivity mainActivity;
    static final int MSG_DISABLE_ACTIVITY_GUI = 0;
    static final int MSG_ENABLE_ACTIVITY_GUI = 1;
    static final int MSG_SET_IMAGE_BITMAP = 2;
    static final int MSG_SHOW_ERROR = 3;

    HandlerMainActivity(MainActivity mainActivity)
    {
        super(Looper.getMainLooper());

        this.mainActivity = mainActivity;
    }

    public Context getContext()
    {
        return mainActivity;
    }

    @Override
    public void handleMessage(Message msg)
    {
        super.handleMessage(msg);

        switch (msg.what)
        {
            case(MSG_SET_IMAGE_BITMAP):
            {
                Bitmap bm = (Bitmap)msg.obj;
                if (bm != null)
                    mainActivity.setImageBitmap(bm);

                break;
            }
            case(MSG_ENABLE_ACTIVITY_GUI):
            {
                mainActivity.enableActivityGUI();
                break;
            }
            case(MSG_DISABLE_ACTIVITY_GUI):
            {
                mainActivity.disableActivityGUI();
                break;
            }

            case(MSG_SHOW_ERROR):
            {
                if (msg.obj != null && !mainActivity.isFinishing())
                    MainActivity.newAlertDialogBuilder(mainActivity)
                            .setTitle("ERROR")
                            .setMessage(msg.obj.toString())
                            .show();

                mainActivity.enableActivityGUI();
                break;
            }
        }
    }


}
