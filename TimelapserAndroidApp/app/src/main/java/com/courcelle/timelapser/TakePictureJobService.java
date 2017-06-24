package com.courcelle.timelapser;

import android.app.job.JobParameters;
import android.app.job.JobService;
import java.util.Observable;
import java.util.Observer;

public class TakePictureJobService extends JobService {
    private PictureTaker pictureTaker;

    @Override
    public boolean onStartJob(final JobParameters params) {
        PictureTaker pictureTaker=new PictureTaker();
        pictureTaker.addObserver(new PictureTakerObserver(this));

        pictureTaker.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                jobFinished(params,false);
            }
        });

        pictureTaker.takePicture();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
