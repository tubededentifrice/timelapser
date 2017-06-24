package com.courcelle.timelapser;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.io.File;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class MessagingService extends FirebaseMessagingService {
    private LocalBroadcastManager broadcaster;
    private PictureTaker pictureTaker;

    public MessagingService() { }

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);

        pictureTaker=new PictureTaker();
        pictureTaker.addObserver(new PictureTakerObserver(this));
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String,String> data=remoteMessage.getData();
        if (data!=null) {
            switch(data.get("action")) {
                case "TakePicture":
                    takePicture(data);
                    break;
            }
        }



//        // TODO(developer): Handle FCM messages here.
//        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
//        Log.d("Firebase", "From: " + remoteMessage.getFrom());
//
//        // Check if message contains a data payload.
//        if (remoteMessage.getData().size() > 0) {
//            Log.d("Firebase", "Message data payload: " + remoteMessage.getData());
//        }
//
//        // Check if message contains a notification payload.
//        if (remoteMessage.getNotification() != null) {
//            Log.d("Firebase", "Message Notification Body: " + remoteMessage.getNotification().getBody());
//        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void takePicture( Map<String,String> data) {
        String cameraParameters=data.get("cameraParameters");

        pictureTaker.takePicture();
    }
}
