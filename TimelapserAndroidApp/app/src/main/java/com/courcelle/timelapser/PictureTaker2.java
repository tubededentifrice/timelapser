package com.courcelle.timelapser;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class PictureTaker2 extends APictureTaker {
    private Context context;
    private CameraManager cameraManager;

    public PictureTaker2(Context context) {
        this.context = context;
        this.cameraManager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
    }

    @Override
    public void takePicture(final PictureTakerParameters pictureTakerParameters) {
        if (cameraManager!=null) {
            final String cameraId = getBackCameraId();

            try {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Size biggestPicture = getBiggestPictureSize(characteristics);

                final ImageReader imageReader = ImageReader.newInstance(biggestPicture.getWidth(), biggestPicture.getHeight(), ImageFormat.JPEG, 2); //fps * 5s?
                imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader){
                        Image image = reader.acquireNextImage();
                        try {
                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[buffer.remaining()];
                            buffer.get(bytes);

                            savePictureAndNotifyObservers(bytes);
                        } finally {
                            image.close();
                        }
                    }
                }, null);

                cameraManager.openCamera(cameraId,new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull final CameraDevice cameraDevice) {
                        // This method is called when the camera is opened.  We start camera preview here.
                        //mCameraDevice = cameraDevice;
                        //createCameraPreviewSession();
                        try {
                            cameraDevice.createCaptureSession(Arrays.asList(imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                                @Override
                                public void onConfigured(CameraCaptureSession session) {
                                    try {
                                        CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                                        builder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
                                        pictureTakerParameters.apply(builder);
                                        builder.addTarget(imageReader.getSurface());

                                        session.capture(builder.build(), new CameraCaptureSession.CaptureCallback() {
                                            @Override
                                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                                super.onCaptureCompleted(session, request, result);

                                                cameraDevice.close();
                                            }

                                            @Override
                                            public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                                                super.onCaptureFailed(session, request, failure);

                                                cameraDevice.close();
                                            }
                                        }, null);
                                    } catch (CameraAccessException e){
                                        Log.e("PictureTaker2", "Failed to interact with camera "+cameraId, e);
                                    }
                                }

                                @Override
                                public void onConfigureFailed(CameraCaptureSession session) { }
                            }, null);
                        } catch (CameraAccessException e) {
                            Log.e("PictureTaker2", "Failed to interact with camera "+cameraId, e);
                        }
                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                        cameraDevice.close();
                    }

                    @Override
                    public void onError(@NonNull CameraDevice cameraDevice, int error) {
                        cameraDevice.close();
                    }
                },null);
            } catch(SecurityException e) {
                Log.e("PictureTaker2", "Failed to interact with camera "+cameraId, e);
            } catch (CameraAccessException e) {
                Log.e("PictureTaker2", "Failed to interact with camera "+cameraId, e);
            }
        }
    }

    private String getBackCameraId() {
        try {
            for (String cameraId: cameraManager.getCameraIdList()) {
                Integer facing = cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.LENS_FACING);
                if (facing!=null && facing==CameraCharacteristics.LENS_FACING_BACK) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            Log.e("PictureTaker2", "Failed to interact with camera", e);
        }

        return null;
    }

    private Size getBiggestPictureSize(CameraCharacteristics characteristics) {
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        return Collections.max(
            Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
            new CompareSizesByArea()
        );
    }

    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum(
                (long)lhs.getWidth()*lhs.getHeight() -
                (long)rhs.getWidth()*rhs.getHeight()
            );
        }
    }
}
