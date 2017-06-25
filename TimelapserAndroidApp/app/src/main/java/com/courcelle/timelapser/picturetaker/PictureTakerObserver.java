package com.courcelle.timelapser.picturetaker;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.courcelle.timelapser.MainActivity;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

public class PictureTakerObserver implements Observer {
    private Context context;
    private LocalBroadcastManager broadcaster;

    public PictureTakerObserver(Context context) {
        this.context = context;
        broadcaster = LocalBroadcastManager.getInstance(context);
    }

    @Override
    public void update(Observable o, Object arg) {
        File imageFile=(File)arg;

        Intent intent = new Intent(MainActivity.PictureTakenIntentAction);
        intent.putExtra("file", imageFile.getPath());

        if(broadcaster != null) {
            broadcaster.sendBroadcast(intent);
        }

        try {
            MediaScannerConnection.scanFile(
                context,
                new String[] { imageFile.getPath() },
                new String[] { "image/jpeg" },
                null
            );

            context.sendBroadcast(
                new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(imageFile)
                )
            );
        } catch(Exception e) {
            Log.e("PictureTakerObserver","Exception: "+e.getMessage());
        }
    }
}
