package io.github.umbraproject.umbra.util.gcm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by matt on 4/26/16.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = MyInstanceIDListenerService.class.getSimpleName();

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
    // [END refresh_token]

}
