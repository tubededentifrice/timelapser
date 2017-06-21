package com.courcelle.timelapser;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;

/**
 * Created by tubed on 19/06/2017.
 */

public class PictureTaker extends Observable {
    public void takePicture() {
        final Camera mCamera=openCamera();
        if (mCamera!=null) {
            try {
                mCamera.setPreviewTexture(new SurfaceTexture(10));
            } catch (IOException e) { }

            Camera.Parameters params = mCamera.getParameters();
            final Camera.Size biggestPictureSize = getBiggestPictureSize(params);
            params.setPictureSize(biggestPictureSize.width, biggestPictureSize.height);
            params.setPreviewSize(640,480);
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            params.setPictureFormat(ImageFormat.JPEG);
            params.setJpegQuality(80);
            params.setAutoExposureLock(true);
            params.setAutoWhiteBalanceLock(true);
            params.setExposureCompensation(params.getMaxExposureCompensation());
            params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
            params.setSceneMode(Camera.Parameters.SCENE_MODE_LANDSCAPE);
            params.setRotation(0);
            mCamera.setParameters(params);
            mCamera.startPreview(); //Commenting works in the emulator, not in actual phone

            File imageFile = getNewImageFile();
            try {
                OutputStream out = new FileOutputStream(imageFile);
                out.write(new byte[0]);
                out.close();

                setChanged();
                notifyObservers(imageFile);
            } catch (Exception e) { }

            /*mCamera.takePicture(new Camera.ShutterCallback() {
                @Override
                public void onShutter() {
                    disposeCamera();
                }
            }, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    //mCamera.stopPreview();
                    disposeCamera();
                }
            }, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    //mCamera.stopPreview();
                    disposeCamera();
                }
            }, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    //mCamera.stopPreview();
                    disposeCamera();

                    File imageFile = getNewImageFile();
                    try {
                        OutputStream out = new FileOutputStream(imageFile);
                        out.write(data);
                        out.close();

                        //
                        setChanged();
                        notifyObservers(imageFile);
                    } catch (Exception e) { }
                }
            });*/
        }
    }

    private DateFormat imageDateFormat = new SimpleDateFormat("yyyy.MM.dd-HHmmss");
    public File getNewImageFile() {
        String imageFolderPath = Environment.getExternalStorageDirectory().toString()+"/Timelapse";
        File imagesFolder = new File(imageFolderPath);
        imagesFolder.mkdirs();

        // Generating file name
        String imageName = imageDateFormat.format(new Date())+".jpg";
        return new File(imageFolderPath, imageName);
    }


    private Camera.Size getBiggestPictureSize(Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result = size;
            } else {
                int resultArea = result.width * result.height;
                int newArea = size.width * size.height;

                if (newArea > resultArea) {
                    result = size;
                }
            }
        }

        return result;
    }

    private Camera lastCamera;
    private Camera openCamera() {
        /*if (lastCamera!=null) {
            return lastCamera;
        }*/

        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    cam = Camera.open(camIdx);
                    lastCamera=cam;
                    return cam;
                } catch (RuntimeException e) {
                    Log.e("Camera","Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return null;
    }

    private synchronized void disposeCamera() {
        if (lastCamera != null) {
            lastCamera.stopPreview();
            lastCamera.release();
        }
    }
}
