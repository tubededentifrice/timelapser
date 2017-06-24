package com.courcelle.timelapser;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.courcelle.timelapser.utils.StringUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue;

public class PictureTakerParameters {
    public String flashMode;
    public String whiteBalance;
    public String sceneMode;
    public String focusMode;
    public Boolean autoExposureLock;
    public Boolean autoWhiteBalanceLock;
    public Integer exposureCompensation;
    public Integer imageFormat;
    public Integer jpegQuality;
    public Integer rotation;

    public static void retrieve(final GenericCallback<PictureTakerParameters> callback) {
        final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setConfigSettings(
            new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        );

        remoteConfig
            .fetch(getInteger(remoteConfig,"remoteConfigCacheDuration",12*60*60))
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        remoteConfig.activateFetched();

                        PictureTakerParameters parameters=new PictureTakerParameters();

                        parameters.flashMode=getString(remoteConfig,"flashMode",null);
                        parameters.whiteBalance=getString(remoteConfig,"whiteBalance",null);
                        parameters.sceneMode=getString(remoteConfig,"sceneMode",null);
                        parameters.focusMode=getString(remoteConfig,"focusMode",null);
                        parameters.autoExposureLock=getBoolean(remoteConfig,"autoExposureLock",null);
                        parameters.autoWhiteBalanceLock=getBoolean(remoteConfig,"autoWhiteBalanceLock",null);
                        parameters.exposureCompensation=getInteger(remoteConfig,"exposureCompensation",null);
                        parameters.imageFormat=getInteger(remoteConfig,"imageFormat",null);
                        parameters.jpegQuality=getInteger(remoteConfig,"jpegQuality",null);
                        parameters.rotation=getInteger(remoteConfig,"rotation",null);

                        callback.onCallback(parameters);
                    }
                }
            });
    }

    private static String getString(FirebaseRemoteConfig remoteConfig,String key,String defaultValue) {
        FirebaseRemoteConfigValue value = remoteConfig.getValue(key);
        if(value!=null && !StringUtils.isNullOrEmpty(value.asString())) {
            return value.asString();
        }
        return defaultValue;
    }
    private static Integer getInteger(FirebaseRemoteConfig remoteConfig,String key,Integer defaultValue) {
        FirebaseRemoteConfigValue value = remoteConfig.getValue(key);
        if(value!=null && !StringUtils.isNullOrEmpty(value.asString())) {
            return (int)value.asLong();
        }
        return defaultValue;
    }
    private static Boolean getBoolean(FirebaseRemoteConfig remoteConfig,String key,Boolean defaultValue) {
        FirebaseRemoteConfigValue value = remoteConfig.getValue(key);
        if(value!=null && !StringUtils.isNullOrEmpty(value.asString())) {
            return value.asBoolean();
        }
        return defaultValue;
    }

    public void apply(Camera.Parameters params) {
        if (flashMode!=null) {
            params.setFlashMode(flashMode);
        }

        if (whiteBalance!=null) {
            params.setWhiteBalance(whiteBalance);
        }

        if (sceneMode!=null) {
            params.setSceneMode(sceneMode);
        }

        if (focusMode!=null) {
            params.setFocusMode(focusMode);
        }

        if (autoExposureLock!=null) {
            params.setAutoExposureLock(autoExposureLock);
        }

        if (autoWhiteBalanceLock!=null) {
            params.setAutoWhiteBalanceLock(autoWhiteBalanceLock);
        }

        if (exposureCompensation!=null) {
            params.setExposureCompensation(exposureCompensation);
        }

        if (imageFormat!=null) {
            params.setPictureFormat(imageFormat);
        }

        if (jpegQuality!=null) {
            params.setJpegQuality(jpegQuality);
        }

        if (rotation!=null) {
            params.setRotation(rotation);
        }
    }
}
