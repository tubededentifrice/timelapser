package com.courcelle.timelapser.services;

import com.courcelle.timelapser.picturetaker.APictureTaker;
import com.courcelle.timelapser.picturetaker.PictureTakerObserver;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MessagingService extends FirebaseMessagingService {
    private APictureTaker pictureTaker;

    public MessagingService() { }

    @Override
    public void onCreate() {
        pictureTaker = APictureTaker.getInstance(this);
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
                    APictureTaker.cleanupPictures();
                    break;
            }
        }
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
