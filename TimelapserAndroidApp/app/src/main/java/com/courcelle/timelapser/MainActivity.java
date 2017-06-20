package com.courcelle.timelapser;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.frosquivel.magicalcamera.MagicalPermissions;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends Activity implements Observer {
    private final long runEvery=60*1000;
    private PictureTaker pictureTaker;
    private TextView counterTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MagicalPermissions magicalPermissions=new MagicalPermissions(this, new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });

        magicalPermissions.askPermissions(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Thanks for granting permissions", Toast.LENGTH_SHORT).show();
            }
        });


        pictureTaker=new PictureTaker();
        pictureTaker.addObserver(this);
        this.counterTextView=(TextView)findViewById(R.id.counter);

        Intent intent=new Intent(this,BackgroundService.class);
        intent.putExtra("messenger", new Messenger(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle data=msg.getData();
                String filePath=data.getString("file");

                update(null,new File(filePath));
            }
        }));
        intent.setAction("PictureTaker");
        startService(intent);
    }

    public void onClickForceTakePicture(View view) {
        pictureTaker.takePicture();
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
