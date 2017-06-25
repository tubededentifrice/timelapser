package com.courcelle.timelapser.services;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

import com.courcelle.timelapser.picturetaker.APictureTaker;
import com.courcelle.timelapser.picturetaker.PictureTakerObserver;
import com.courcelle.timelapser.utils.GenericCallback;
import com.courcelle.timelapser.utils.RemoteConfigHelper;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

public class TakePictureJobService extends JobService {
    private static final long runEveryDefault = 60*60*1000;
    private static final long minDelayBetweenPicturesDefault = 30*60*1000; // Avoid having multiple executions too close since setPeriodic doesn't take a deadline in API 23 (it does in API 24)

    private static final String preferencesName = "takePictureJobService";
    private static final String lastPictureDatePreferenceKey = "lastPictureDate";
    private static final String runEveryConfigKey = "takePictureJobServiceRunEvery";
    private static final String minDelayBetweenPicturesConfigKey = "takePictureJobServiceMinDelayBetweenPictures";
    private static final DateFormat lastPictureDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    @Override
    public boolean onStartJob(final JobParameters params) {
        RemoteConfigHelper.getRemoteConfig(new GenericCallback<FirebaseRemoteConfig>() {
            @Override
            public void onCallback(FirebaseRemoteConfig remoteConfig) {
                final SharedPreferences preferences = getSharedPreferences(preferencesName,0);
                String lastPictureDateString=preferences.getString(lastPictureDatePreferenceKey,null);

                // Check if the last picture isn't too recent
                try {
                    if (lastPictureDateString!=null) {
                        Date lastPictureDate = lastPictureDateFormatter.parse(lastPictureDateString);
                        long minDelayBetweenPictures = RemoteConfigHelper.getLong(remoteConfig,minDelayBetweenPicturesConfigKey,minDelayBetweenPicturesDefault);
                        if (lastPictureDate.getTime()+minDelayBetweenPictures>new Date().getTime()) {
                            jobFinished(params,false);
                            return;
                        }
                    }
                } catch(Exception e) { }


                APictureTaker pictureTaker = APictureTaker.getInstance(TakePictureJobService.this);
                pictureTaker.addObserver(new PictureTakerObserver(TakePictureJobService.this));

                pictureTaker.addObserver(new Observer() {
                    @Override
                    public void update(Observable o, Object arg) {
                        // Save the new last picture date
                        SharedPreferences.Editor preferencesEditor = preferences.edit();
                        preferencesEditor.putString(lastPictureDatePreferenceKey,lastPictureDateFormatter.format(new Date()));
                        preferencesEditor.commit();

                        jobFinished(params,false);
                    }
                });

                pictureTaker.takePicture();
            }
        });

        // Check if config changed and reschedule the JobService if it did
        scheduleJob(this,false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    public static void scheduleJob(final Context context,final boolean forceReschedule) {
        RemoteConfigHelper.getRemoteConfig(new GenericCallback<FirebaseRemoteConfig>() {
            @Override
            public void onCallback(FirebaseRemoteConfig remoteConfig) {
                final SharedPreferences preferences = context.getSharedPreferences(preferencesName,0);
                long currentRunEvery = preferences.getLong(runEveryConfigKey,runEveryDefault);
                long currentMinDelayBetweenPictures = preferences.getLong(minDelayBetweenPicturesConfigKey,minDelayBetweenPicturesDefault);

                long runEvery = RemoteConfigHelper.getLong(remoteConfig,runEveryConfigKey,runEveryDefault);
                long minDelayBetweenPictures = RemoteConfigHelper.getLong(remoteConfig,minDelayBetweenPicturesConfigKey,minDelayBetweenPicturesDefault);

                if (forceReschedule || currentRunEvery!=runEvery || currentMinDelayBetweenPictures!=minDelayBetweenPictures) {
                    SharedPreferences.Editor preferencesEditor = preferences.edit();
                    preferencesEditor.putLong(runEveryConfigKey,runEvery);
                    preferencesEditor.putLong(minDelayBetweenPicturesConfigKey,minDelayBetweenPictures);
                    preferencesEditor.commit();

                    scheduleJob(context,runEvery);
                }
            }
        });
    }

    public static void scheduleJob(Context context,long runEvery) {
        JobScheduler mJobScheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        mJobScheduler.schedule(
            new JobInfo.Builder(1,new ComponentName(context.getPackageName(),TakePictureJobService.class.getName()))
                .setPeriodic(runEvery)
                .setPersisted(true)
                .build()
        );
    }
}
