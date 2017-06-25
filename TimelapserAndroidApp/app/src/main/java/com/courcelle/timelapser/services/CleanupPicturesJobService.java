package com.courcelle.timelapser.services;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

import com.courcelle.timelapser.picturetaker.APictureTaker;

public class CleanupPicturesJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        APictureTaker.cleanupPictures();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    public static void scheduleJob(Context context) {
        JobScheduler mJobScheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        mJobScheduler.schedule(
            new JobInfo.Builder(2,new ComponentName(context.getPackageName(),CleanupPicturesJobService.class.getName()))
                .setPeriodic(24*60*60*1000)
                .setPersisted(true)
                .build()
        );
    }
}
