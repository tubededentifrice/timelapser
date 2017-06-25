package com.courcelle.timelapser.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootInitializer extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Make sure the service is always scheduled (even if it should always be by the OS)
        TakePictureJobService.scheduleJob(context,true);
        CleanupPicturesJobService.scheduleJob(context);
        MessagingService.subscribeRelevantTopics();
    }
}
