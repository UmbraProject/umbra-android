package io.github.umbraproject.umbra.util.gcm;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by matt on 4/26/16.
 */
public class MyGcmListenerService extends GcmListenerService {
    public static final String TAG = MyGcmListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
    }
}
