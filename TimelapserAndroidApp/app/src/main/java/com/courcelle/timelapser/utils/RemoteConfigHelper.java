package com.courcelle.timelapser.utils;

import android.support.annotation.NonNull;
import com.courcelle.timelapser.BuildConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue;

public class RemoteConfigHelper {
    public static void getRemoteConfig(@NonNull final GenericCallback<FirebaseRemoteConfig> callback) {
        final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setConfigSettings(
            new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        );

        remoteConfig
            .fetch(getInteger(remoteConfig,"remoteConfigCacheDuration",60))
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        remoteConfig.activateFetched();
                        callback.onCallback(remoteConfig);
                    }
                }
            });
    }

    public static String getString(FirebaseRemoteConfig remoteConfig,String key,String defaultValue) {
        FirebaseRemoteConfigValue value = remoteConfig.getValue(key);
        if(value!=null && !StringUtils.isNullOrEmpty(value.asString())) {
            return value.asString();
        }
        return defaultValue;
    }
    public static Integer getInteger(FirebaseRemoteConfig remoteConfig,String key,Integer defaultValue) {
        Long defaultValueLong=null;
        if(defaultValue!=null) {
            defaultValueLong = defaultValue.longValue();
        }
        Long longResult = getLong(remoteConfig,key,defaultValueLong);
        if (longResult!=null) {
            return longResult.intValue();
        }
        return defaultValue;
    }
    public static Long getLong(FirebaseRemoteConfig remoteConfig,String key,Long defaultValue) {
        FirebaseRemoteConfigValue value = remoteConfig.getValue(key);
        if(value!=null && !StringUtils.isNullOrEmpty(value.asString())) {
            return value.asLong();
        }
        return defaultValue;
    }
    public static Boolean getBoolean(FirebaseRemoteConfig remoteConfig,String key,Boolean defaultValue) {
        FirebaseRemoteConfigValue value = remoteConfig.getValue(key);
        if(value!=null && !StringUtils.isNullOrEmpty(value.asString())) {
            return value.asBoolean();
        }
        return defaultValue;
    }
}
