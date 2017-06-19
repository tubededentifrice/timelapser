package com.courcelle.timelapser;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import com.frosquivel.magicalcamera.MagicalPermissions;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tubed on 19/06/2017.
 */

public class PictureTaker {
    private MagicalPermissions magicalPermissions;
    private final Activity activity;

    public PictureTaker(final Activity activity) {
        this.activity=activity;
        magicalPermissions=new MagicalPermissions(activity, new String[] {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, "Thanks for granting permissions", Toast.LENGTH_SHORT).show();
            }
        };
        magicalPermissions.askPermissions(runnable);
    }

    public void takePicture() {
        final Camera mCamera=openCamera();
        if (mCamera!=null) {
            try {
                mCamera.setPreviewTexture(new SurfaceTexture(10));
            } catch (IOException e1) {

            }

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

            mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    Camera.Size size=mCamera.getParameters().getPictureSize();
                    Toast.makeText(activity, "Picture taken size "+data.length+", resolution "+size.width+"x"+size.height, Toast.LENGTH_LONG).show();
                    //mCamera.stopPreview();
                    disposeCamera();

                    File imageFile=getNewImageFile();
                    try {
                        OutputStream out=new FileOutputStream(imageFile);
                        out.write(data);
                        out.close();

                        MediaScannerConnection.scanFile(activity, new String[]{imageFile.getPath()}, new String[]{"image/jpeg"}, null);
                        activity.sendBroadcast(new Intent(
                                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                Uri.fromFile(imageFile)));
                    } catch(Exception e) {
                        logException(e);
                    }
                }
            });
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


    private void logException(Exception e) {
        Toast.makeText(activity, "Exception: "+e.getMessage(), Toast.LENGTH_LONG).show();
    }

    private synchronized void disposeCamera() {
        if (lastCamera != null) {
            lastCamera.stopPreview();
            lastCamera.release();
        }
    }
}
