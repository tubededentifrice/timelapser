package com.courcelle.timelapser;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import com.courcelle.timelapser.utils.FileUtils;
import com.courcelle.timelapser.utils.GenericCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;

public class PictureTaker extends Observable {
    public void takePicture() {
        PictureTakerParameters.retrieve(new GenericCallback<PictureTakerParameters>() {
            @Override
            public void onCallback(PictureTakerParameters pictureTakerParameters) {
                takePicture(pictureTakerParameters);
            }
        });
    }

    public void takePicture(PictureTakerParameters pictureTakerParameters) {
        final Camera mCamera=openCamera();
        if (mCamera!=null) {
            try {
                mCamera.setPreviewTexture(new SurfaceTexture(10));
            } catch (IOException e) {
                Log.e("PictureTaker","Error setting preview texture",e);
            }

            final Camera.Parameters params = mCamera.getParameters();
            final Camera.Size biggestPictureSize = getBiggestPictureSize(params);
            params.setPictureSize(biggestPictureSize.width, biggestPictureSize.height);
            params.setPreviewSize(640,480);

            try {
                pictureTakerParameters.apply(params);
                mCamera.setParameters(params);
                mCamera.startPreview(); //Commenting works in the emulator, not in actual phone

                mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        //mCamera.stopPreview();
                        disposeCamera();

                        File imageFile = getNewImageFile();
                        try {
                            OutputStream out = new FileOutputStream(imageFile);
                            out.write(data);
                            out.close();

                            setChanged();
                            notifyObservers(imageFile);
                        } catch (Exception e) {
                            Log.e("PictureTaker","Error writing picture file",e);
                        }
                    }
                });
            } catch(Exception e) {
                Log.e("PictureTaker","Error setting parameters",e);
            }
        }
    }

    private DateFormat imageDateFormat = new SimpleDateFormat("yyyy-MM-dd.HHmmss", Locale.US);
    private File getNewImageFile() {
        File imagesFolder = getImageFolder();

        // Generating file name
        String imageName = imageDateFormat.format(new Date())+".jpg";
        return new File(imagesFolder.getPath(), imageName);
    }

    public static int cleanupPictures() {
        return FileUtils.remoteOldFiles(getImageFolder(),new Date().getTime()-30*24*60*60*1000);
    }

    private static File getImageFolder() {
        String imageFolderPath = Environment.getExternalStorageDirectory().toString()+"/Timelapse/";
        File imagesFolder = new File(imageFolderPath);
        imagesFolder.mkdirs();

        return imagesFolder;
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
