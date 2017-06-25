package com.courcelle.timelapser.picturetaker;

import android.hardware.Camera;
import android.hardware.camera2.CaptureRequest;
import android.util.Log;

import com.courcelle.timelapser.utils.GenericCallback;
import com.courcelle.timelapser.utils.RemoteConfigHelper;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class PictureTakerParameters {
    public String flashMode;
    public String whiteBalance;
    public String sceneMode;
    public String focusMode;
    public String exposure;
    public String isoSpeed;
    public Integer exposureTime;
    public Integer frameDuration;
    public Boolean autoExposureLock;
    public Boolean autoWhiteBalanceLock;
    public Integer exposureCompensation;
    public Integer imageFormat;
    public Integer jpegQuality;
    public Integer rotation;

    public static void retrieve(final GenericCallback<PictureTakerParameters> callback,String namespace) {
        final String namespaceDot=namespace+"_";

        RemoteConfigHelper.getRemoteConfig(new GenericCallback<FirebaseRemoteConfig>() {
            @Override
            public void onCallback(FirebaseRemoteConfig remoteConfig) {
                PictureTakerParameters parameters=new PictureTakerParameters();

                parameters.flashMode = RemoteConfigHelper.getString(remoteConfig,namespaceDot+"flashMode",null);
                parameters.whiteBalance = RemoteConfigHelper.getString(remoteConfig,namespaceDot+"whiteBalance",null);
                parameters.sceneMode = RemoteConfigHelper.getString(remoteConfig,namespaceDot+"sceneMode",null);
                parameters.focusMode = RemoteConfigHelper.getString(remoteConfig,namespaceDot+"focusMode",null);
                parameters.exposure = RemoteConfigHelper.getString(remoteConfig,namespaceDot+"exposure",null);

                parameters.exposureTime = RemoteConfigHelper.getInteger(remoteConfig,namespaceDot+"exposureTime",null); // Not in API1
                parameters.isoSpeed = RemoteConfigHelper.getString(remoteConfig,namespaceDot+"isoSpeed",null);
                parameters.frameDuration = RemoteConfigHelper.getInteger(remoteConfig,namespaceDot+"frameDuration",null);// Not in API1

                parameters.autoExposureLock = RemoteConfigHelper.getBoolean(remoteConfig,namespaceDot+"autoExposureLock",null);
                parameters.autoWhiteBalanceLock = RemoteConfigHelper.getBoolean(remoteConfig,namespaceDot+"autoWhiteBalanceLock",null);
                parameters.exposureCompensation = RemoteConfigHelper.getInteger(remoteConfig,namespaceDot+"exposureCompensation",null);
                parameters.imageFormat = RemoteConfigHelper.getInteger(remoteConfig,namespaceDot+"imageFormat",null);   // Defined at ImageReader in API2
                parameters.jpegQuality = RemoteConfigHelper.getInteger(remoteConfig,namespaceDot+"jpegQuality",null);
                parameters.rotation = RemoteConfigHelper.getInteger(remoteConfig,namespaceDot+"rotation",null);

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

        if (exposure!=null) {
            params.set("exposure", exposure);
        }

        if (isoSpeed!=null) {
            params.set("iso-speed", isoSpeed);
        }

        if (autoExposureLock!=null) {
            params.setAutoExposureLock(autoExposureLock);
        }

        if (autoWhiteBalanceLock!=null) {
            params.setAutoWhiteBalanceLock(autoWhiteBalanceLock);
        }

        if (exposureCompensation!=null) {
            params.setExposureCompensation(Math.min(Math.max(exposureCompensation,params.getMinExposureCompensation()),params.getMaxExposureCompensation()));
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
    public void apply(CaptureRequest.Builder params) {
        //Log.i("Params","FLASH_MODE_OFF = "+CaptureRequest.FLASH_MODE_OFF);
        if (flashMode!=null) {
            params.set(CaptureRequest.FLASH_MODE,Integer.parseInt(flashMode));
        }

        Log.i("Params","CONTROL_AWB_MODE_AUTO = "+CaptureRequest.CONTROL_AWB_MODE_AUTO);
        //Log.i("Params","CONTROL_AWB_MODE_AUTO = "+CaptureRequest.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT);
        if (whiteBalance!=null) {
            params.set(CaptureRequest.CONTROL_AWB_MODE,Integer.parseInt(whiteBalance));
        }

        //Log.i("Params","CONTROL_SCENE_MODE_HDR = "+CaptureRequest.CONTROL_SCENE_MODE_HDR);
        //Log.i("Params","CONTROL_SCENE_MODE_LANDSCAPE = "+CaptureRequest.CONTROL_SCENE_MODE_LANDSCAPE);
        if (sceneMode!=null) {
            params.set(CaptureRequest.CONTROL_SCENE_MODE,Integer.parseInt(sceneMode));
        }

        //Log.i("Params","CONTROL_AF_MODE_AUTO = "+CaptureRequest.CONTROL_AF_MODE_AUTO);
        //Log.i("Params","CONTROL_AF_MODE_AUTO = "+CaptureRequest.CONTROL_AF_MODE_EDOF);
        if (focusMode!=null) {
            params.set(CaptureRequest.CONTROL_AF_MODE,Integer.parseInt(focusMode));
        }

        if (exposure!=null) {
            // Ignored
        }

        if (exposureTime!=null) {
            params.set(CaptureRequest.SENSOR_EXPOSURE_TIME,new Long(exposureTime));
        }

        if (isoSpeed!=null) {
            params.set(CaptureRequest.SENSOR_SENSITIVITY,Integer.parseInt(isoSpeed));
        }

        if (frameDuration!=null) {
            params.set(CaptureRequest.SENSOR_FRAME_DURATION,new Long(frameDuration));
        }

        if (autoExposureLock!=null) {
            params.set(CaptureRequest.CONTROL_AE_LOCK,autoExposureLock);
        }

        if (autoWhiteBalanceLock!=null) {
            params.set(CaptureRequest.CONTROL_AWB_LOCK,autoWhiteBalanceLock);
        }

        if (exposureCompensation!=null) {
            params.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION,exposureCompensation);
        }

        if (imageFormat!=null) {
            // Ignored
        }

        if (jpegQuality!=null) {
            params.set(CaptureRequest.JPEG_QUALITY,(byte)(int)jpegQuality);
        }

        if (rotation!=null) {
            params.set(CaptureRequest.JPEG_ORIENTATION,rotation);
        }
    }
}
