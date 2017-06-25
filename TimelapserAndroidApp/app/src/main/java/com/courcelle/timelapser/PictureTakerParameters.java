package com.courcelle.timelapser;

import android.hardware.Camera;

import com.courcelle.timelapser.utils.GenericCallback;
import com.courcelle.timelapser.utils.RemoteConfigHelper;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

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
        RemoteConfigHelper.getRemoteConfig(new GenericCallback<FirebaseRemoteConfig>() {
            @Override
            public void onCallback(FirebaseRemoteConfig remoteConfig) {
                PictureTakerParameters parameters=new PictureTakerParameters();

                parameters.flashMode = RemoteConfigHelper.getString(remoteConfig,"flashMode",null);
                parameters.whiteBalance = RemoteConfigHelper.getString(remoteConfig,"whiteBalance",null);
                parameters.sceneMode = RemoteConfigHelper.getString(remoteConfig,"sceneMode",null);
                parameters.focusMode = RemoteConfigHelper.getString(remoteConfig,"focusMode",null);
                parameters.autoExposureLock = RemoteConfigHelper.getBoolean(remoteConfig,"autoExposureLock",null);
                parameters.autoWhiteBalanceLock = RemoteConfigHelper.getBoolean(remoteConfig,"autoWhiteBalanceLock",null);
                parameters.exposureCompensation = RemoteConfigHelper.getInteger(remoteConfig,"exposureCompensation",null);
                parameters.imageFormat = RemoteConfigHelper.getInteger(remoteConfig,"imageFormat",null);
                parameters.jpegQuality = RemoteConfigHelper.getInteger(remoteConfig,"jpegQuality",null);
                parameters.rotation = RemoteConfigHelper.getInteger(remoteConfig,"rotation",null);

                callback.onCallback(parameters);
            }
        });
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
