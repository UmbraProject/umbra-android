package io.github.umbraproject.umbra.util.gcm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by matt on 4/26/16.
 */
public class MyGcmListenerService extends Service{
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
