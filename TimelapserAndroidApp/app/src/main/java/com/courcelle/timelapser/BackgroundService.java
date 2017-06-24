package com.courcelle.timelapser;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import java.io.File;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by tubed on 20/06/2017.
 */

public class BackgroundService extends IntentService implements Observer {
    private static int FOREGROUND_ID=1338;
    private LocalBroadcastManager broadcaster;

    public BackgroundService() {
        super("BackgroundService");
        broadcaster = LocalBroadcastManager.getInstance(this);
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(FOREGROUND_ID,buildForegroundNotification());
        onHandleIntent(intent);
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final PictureTaker pictureTaker=new PictureTaker();
        pictureTaker.addObserver(this);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                pictureTaker.takePicture();
            }
        }, 0, 60*1000);
    }

    @Override
    public void update(Observable o, Object arg) {
        if(broadcaster != null) {
            File imageFile=(File)arg;

            Intent intent = new Intent(MainActivity.PictureTakenIntentAction);
            intent.putExtra("file", imageFile.getPath());
            broadcaster.sendBroadcast(intent);
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
