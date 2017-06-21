package com.courcelle.timelapser;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by tubed on 20/06/2017.
 */

public class BackgroundService extends IntentService implements Observer {
    private static int FOREGROUND_ID=1338;
    private Messenger messenger = null;

    public BackgroundService() {
        super("BackgroundService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BackgroundService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        startForeground(FOREGROUND_ID,buildForegroundNotification());

        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            messenger = (Messenger) bundle.get("messenger");
        }

        PictureTaker pictureTaker=new PictureTaker();
        pictureTaker.addObserver(this);

        while(true) {
            try {
                while(true) {
                    Thread.sleep(10*1000);
                    pictureTaker.takePicture();
                }
            } catch (InterruptedException e) { }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if(messenger != null) {
            File imageFile=(File)arg;
            Message msg = Message.obtain();

            Bundle dataBundle=new Bundle();
            dataBundle.putString("file",imageFile.getPath());
            msg.setData(dataBundle);

            try {
                messenger.send(msg);
            } catch (RemoteException e) { }
        }
    }

    private Notification buildForegroundNotification() {
        NotificationCompat.Builder b=new NotificationCompat.Builder(this);

        b.setOngoing(true)
            .setContentTitle("PictureTaker")
            .setContentText("Picture taker running")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setTicker("Running");

        return(b.build());
    }
}
