package io.github.umbraproject.umbra.util.gcm;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import io.github.umbraproject.umbra.R;
import io.github.umbraproject.umbra.util.constants.GcmConstants;

/**
 * Created by matt on 4/26/16.
 */
public class MyGcmListenerService extends GcmListenerService {
    private static final String TAG = MyGcmListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "Data: " + data.toString());

        String messageTo = data.getString("message_to");
        String messageText = data.getString("message_text");

        createNotification("Umbra", messageText);

        Intent downstreamMessageIntent = new Intent(GcmConstants.NEW_DOWNSTREAM_MESSAGE);
        downstreamMessageIntent.putExtra(GcmConstants.SENDER_ID, from);
        downstreamMessageIntent.putExtra(GcmConstants.EXTRA_KEY_BUNDLE, data);
        LocalBroadcastManager.getInstance(this).sendBroadcast(downstreamMessageIntent);

        // TODO Call main activity presenter to update recycler view data source
    }

    // Creates notification based on title and body received
    private void createNotification(String title, String body) {
        Log.d(TAG, "Title: " + title + " Body: " + body);

        Context context = getBaseContext();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher).setContentTitle(title)
                .setContentText(body);
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());

    }
}
