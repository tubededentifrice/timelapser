package com.courcelle.timelapser;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MessagingService extends FirebaseMessagingService {
    private PictureTaker pictureTaker;

    public MessagingService() { }

    @Override
    public void onCreate() {
        pictureTaker = new PictureTaker();
        pictureTaker.addObserver(new PictureTakerObserver(this));
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String,String> data=remoteMessage.getData();
        if (data!=null) {
            switch(data.get("action")) {
                case "TakePicture":
                    pictureTaker.takePicture();
                    break;
                case "CleanupPictures":
                    PictureTaker.cleanupPictures();
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

    public static void subscribeRelevantTopics() {
        for(String relevantTopic: getRelevantTopics()) {
            FirebaseMessaging.getInstance().subscribeToTopic(relevantTopic);
        }
    }
    public static String[] getRelevantTopics() {
        String[] relevantTopics=new String[1];
        relevantTopics[0]="PictureTaker";

        return relevantTopics;
    }
}
