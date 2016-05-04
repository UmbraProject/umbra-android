package io.github.umbraproject.umbra.util.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import io.github.umbraproject.umbra.R;
import io.github.umbraproject.umbra.util.constants.GcmConstants;

/**
 * Created by matt on 4/29/16.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = RegistrationIntentService.class.getSimpleName();

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        Bundle extras = intent.getExtras();
        String token = "";

        Intent regCompleteIntent = new Intent(GcmConstants.REGISTRATION_COMPLETE);

        try {
            // Initially this call goes out to the network to retrieve the token, subsequent
            // calls are local.

            token = InstanceID.getInstance(this).getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.d(TAG, "GCM Registration Token: " + token);

            // Register token with app server.
            sendRegistrationToServer(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            regCompleteIntent.putExtra(GcmConstants.SENT_TOKEN_TO_SERVER, true);
        } catch (Exception e) {
            Log.e(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration
            // data on a third-party server, this ensures that we'll attempt the update at a later
            // time.
            regCompleteIntent.putExtra(GcmConstants.SENT_TOKEN_TO_SERVER, false);
        }

        Log.d(TAG, "Sending the broadcast");
        regCompleteIntent.putExtra(GcmConstants.EXTRA_KEY_TOKEN, token);
        LocalBroadcastManager.getInstance(this).sendBroadcast(regCompleteIntent);
    }

    /**
     * Register a GCM registration token with the app server
     * @param token Registration token to be registered
     * @return true if request succeeds
     * @throws IOException
     */
    private void sendRegistrationToServer(String token) throws IOException {
        Bundle registration = createRegistrationBundle(token);

//        GoogleCloudMessaging.getInstance(this).send(
//                GcmPlaygroundUtil.getServerUrl(getString(R.string.gcm_defaultSenderId)),
//                String.valueOf(System.currentTimeMillis()), registration);
    }

    /**
     * Creates the registration bundle and fills it with user information
     * @param token Registration token to be registered
     * @param string_identifier A human-friendly name for the client
     * @return A bundle with registration data.
     */
    private Bundle createRegistrationBundle(String token) {
        Bundle registration = new Bundle();

        // Create the bundle for registration with the server.
        registration.putString(GcmConstants.ACTION, GcmConstants.REGISTER_NEW_CLIENT);
        registration.putString(GcmConstants.REGISTRATION_TOKEN, token);
        return registration;
    }

}