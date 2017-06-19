package com.courcelle.timelapser.timelapserandroidapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Intent cameraIntent = new Intent(this, HelloService.class);
        //cameraIntent.putExtra("Front_Request", false);
        //cameraIntent.putExtra("Quality_Mode", 0);
        //startService(cameraIntent);
    }
}
