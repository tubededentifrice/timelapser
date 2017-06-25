package com.courcelle.timelapser.picturetaker;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import java.io.IOException;

public class PictureTaker extends APictureTaker {
    @Override
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

                        savePictureAndNotifyObservers(data);
                    }
                });
            } catch(Exception e) {
                Log.e("PictureTaker","Error setting parameters",e);
            }
        }
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
                    Log.e("Camera","Camera failed to open: " + e.getLocalizedMessage(),e);
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
