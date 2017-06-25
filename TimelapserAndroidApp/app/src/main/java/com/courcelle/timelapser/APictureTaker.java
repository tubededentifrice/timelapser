package com.courcelle.timelapser;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.courcelle.timelapser.utils.FileUtils;
import com.courcelle.timelapser.utils.GenericCallback;
import com.courcelle.timelapser.utils.RemoteConfigHelper;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;

public abstract class APictureTaker extends Observable {
    public static APictureTaker getInstance(Context context) {
        //return new PictureTaker();
        return new PictureTaker2(context);
    }

    public void takePicture() {
        PictureTakerParameters.retrieve(new GenericCallback<PictureTakerParameters>() {
            @Override
            public void onCallback(PictureTakerParameters pictureTakerParameters) {
                takePicture(pictureTakerParameters);
            }
        },"camera2");
    }

    public abstract void takePicture(PictureTakerParameters pictureTakerParameters);


    public static void cleanupPictures() {
        RemoteConfigHelper.getRemoteConfig(new GenericCallback<FirebaseRemoteConfig>() {
            @Override
            public void onCallback(FirebaseRemoteConfig remoteConfig) {
                Long removePicturesAfterSeconds = RemoteConfigHelper.getLong(remoteConfig,"removePicturesAfterSeconds",new Long(30*24*60*60));
                cleanupPictures(new Date().getTime()-removePicturesAfterSeconds*1000);
            }
        });
    }
    public static int cleanupPictures(long olderThan) {
        return FileUtils.removeOldFiles(getImageFolder(),olderThan);
    }

    protected File savePictureAndNotifyObservers(byte[] imageBytes) {
        File imageFile = getNewImageFile();
        try {
            OutputStream out = new FileOutputStream(imageFile);
            try {
                out.write(imageBytes);
            } finally {
                out.close();
            }

            setChanged();
            notifyObservers(imageFile);
        } catch (Exception e) {
            Log.e("APictureTaker","Error writing picture file",e);
        }

        return imageFile;
    }

    private DateFormat imageDateFormat = new SimpleDateFormat("yyyy-MM-dd.HHmmss", Locale.US);
    protected File getNewImageFile() {
        File imagesFolder = getImageFolder();

        // Generating file name
        String imageName = imageDateFormat.format(new Date())+".jpg";
        return new File(imagesFolder.getPath(), imageName);
    }

    protected static File getImageFolder() {
        String imageFolderPath = Environment.getExternalStorageDirectory().toString()+"/Timelapse/";
        File imagesFolder = new File(imageFolderPath);
        imagesFolder.mkdirs();

        return imagesFolder;
    }
}
