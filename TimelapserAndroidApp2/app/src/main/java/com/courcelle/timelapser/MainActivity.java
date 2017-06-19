package com.courcelle.timelapser;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends Activity {
    private final long runEvery=60*1000;
    private PictureTaker pictureTaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pictureTaker=new PictureTaker(this);

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        sleep(runEvery);
                        pictureTaker.takePicture();
                    }
                } catch (InterruptedException e) { }
            }
        };

        thread.start();
    }

    //private File lastPictureFile;
    public void onClick(View view) {
        pictureTaker.takePicture();
    }
}
