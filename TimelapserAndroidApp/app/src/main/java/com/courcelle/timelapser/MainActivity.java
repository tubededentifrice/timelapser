package com.courcelle.timelapser;

import android.Manifest;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends Activity implements Observer {
    public final static String PictureTakenIntentAction="PictureTaken";

    private PictureTaker pictureTaker;
    private TextView counterTextView;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()) {
                case PictureTakenIntentAction:
                    Bundle data=intent.getExtras();
                    String filePath=data.getString("file");

                    update(null,new File(filePath));
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver,
                new IntentFilter(PictureTakenIntentAction)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        askPermissions(new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });

        pictureTaker=new PictureTaker();
        pictureTaker.addObserver(this);
        this.counterTextView=(TextView)findViewById(R.id.counter);

//        Intent intent=new Intent(this,BackgroundService.class);
//        intent.setAction("PictureTaker");
//        startService(intent);
        JobScheduler mJobScheduler = (JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(1,new ComponentName(getPackageName(),TakePictureJobService.class.getName()))
                .setPeriodic(60*60*1000)
                .setPersisted(true);
        if(mJobScheduler.schedule(builder.build())<=0) {
            Toast.makeText(this, "Failed to register the schedule job", Toast.LENGTH_LONG).show();
        }
    }

    public void onClickForceTakePicture(View view) {
        pictureTaker.takePicture();
    }

    private void askPermissions(String[] permissions) {
        List<String> permissionsNeeded=new ArrayList<String>();
        for(String permission: permissions) {
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        if (permissionsNeeded.size()>0) {
            String[] permissionsNeededArray = new String[permissionsNeeded.size()];
            permissionsNeededArray = permissionsNeeded.toArray(permissionsNeededArray);

            ActivityCompat.requestPermissions(this,permissionsNeededArray,0);
        }

    }

    private int counter=0;
    @Override
    public void update(Observable o, Object arg) {
        counter++;
        counterTextView.setText(""+counter);

        if (arg!=null) {
            File imageFile=(File)arg;

            MediaScannerConnection.scanFile(
                this,
                new String[] { imageFile.getPath() },
                new String[] { "image/jpeg" },
                null
            );
            sendBroadcast(
                new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(imageFile)
                )
            );
            Toast.makeText(this, "Picture "+imageFile.getName()+" taken, size "+imageFile.length(), Toast.LENGTH_LONG).show();
        }
    }
}
