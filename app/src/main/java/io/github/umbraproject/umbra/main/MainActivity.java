package io.github.umbraproject.umbra.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.scottyab.aescrypt.AESCrypt;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import io.github.umbraproject.umbra.R;
import io.github.umbraproject.umbra.main.model.Message;
import io.github.umbraproject.umbra.util.constants.GcmConstants;
import io.github.umbraproject.umbra.util.gcm.GcmUtil;
import io.github.umbraproject.umbra.util.gcm.RegistrationIntentService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TOPIC_PREFIX = "/topics/";
    private GoogleCloudMessaging gcm;
    private GcmPubSub pubSub;

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private BroadcastReceiver mDownstreamBroadcastReceiver;
    private String token;

    private RecyclerView messageRecyclerView;
    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter messageAdapter;

    private EditText messageEditText;
    private Button sendButton;

    private int messageId = 0;
    String password = "test_password";

    // 5X ID
    private String recipient = "c9W7lVoiYTw:APA91bEml1MbMvRPYX5HsydYeKXHXkzFS4rVDSJWHHztFPt1dJNXbfE5ZtkwgVaDZVxLhwf8hnTeYXhJcK40MyyoTD11QdRBJ2P3cWlYZaOcpocyyghTHjuVD50hZJbz9WEZUpp6QHWq";

    // AVD 5
//    private String recipient = "dSxsYIi8Rfw:APA91bFAnRfmhP5G9y1ij2EtgmVzG_W6q61fBtnfBUfCWgi-DFsyYyRRVCF7w7XWvJaM9o4wEuU4lbHjhxpA0TqgOmojX2asJckSIqtD3HZ5vJ-TMRmRH3Z2azKNHPX0Yc7EfV1AfiUB";

    // My Nexus 5
//    private String recipient = "dSxsYIi8Rfw:APA91bFAnRfmhP5G9y1ij2EtgmVzG_W6q61fBtnfBUfCWgi-DFsyYyRRVCF7w7XWvJaM9o4wEuU4lbHjhxpA0TqgOmojX2asJckSIqtD3HZ5vJ-TMRmRH3Z2azKNHPX0Yc7EfV1AfiUB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gcm = GoogleCloudMessaging.getInstance(this);
        pubSub = GcmPubSub.getInstance(this);

        // If Play Services is not up to date, quit the app.
        checkPlayServices();

        registerClient();

        // Restore from saved instance state
        if (savedInstanceState != null) {
            token = savedInstanceState.getString(GcmConstants.EXTRA_KEY_TOKEN, "");
            if (!("".equals(token))) {
//                updateUI("Registration SUCCEEDED", true);
            }
        }

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean sentToken = intent.getBooleanExtra(
                        GcmConstants.SENT_TOKEN_TO_SERVER, false);

                token = intent.getStringExtra(GcmConstants.EXTRA_KEY_TOKEN);
                if (!sentToken) {
//                    updateUI("Registration FAILED", false);
                }
            }
        };

        mDownstreamBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String from = intent.getStringExtra(GcmConstants.SENDER_ID);
                Bundle data = intent.getBundleExtra(GcmConstants.EXTRA_KEY_BUNDLE);
                String messageString = data.getString(GcmConstants.EXTRA_KEY_MESSAGE);

                Log.d(TAG, "Received from >" + from + "< with >" + data.toString() + "<");
                Log.d(TAG, "Message: " + messageString);

                String decryptedMessageString = null;
                try {
                    decryptedMessageString = AESCrypt.decrypt(password, messageString);
                    Log.d(TAG, "Decrypted Message: " + decryptedMessageString);
                } catch (GeneralSecurityException e) {
                    Toast.makeText(getApplicationContext(), "Encryption Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                Message message = new Message(from, decryptedMessageString);
                messageList.add(message);
                messageAdapter.notifyDataSetChanged();

                messageRecyclerView.scrollToPosition(messageList.size() - 1);

                String action = data.getString(GcmConstants.ACTION);
                String status = data.getString(GcmConstants.STATUS);

                if (GcmConstants.REGISTER_NEW_CLIENT.equals(action) &&
                        GcmConstants.STATUS_REGISTERED.equals(status)) {
//                    updateUI("Registration SUCCEEDED", true);
                } else if (GcmConstants.UNREGISTER_CLIENT.equals(action) &&
                        GcmConstants.STATUS_UNREGISTERED.equals(status)) {
                    token = "";
//                    updateUI("Unregistration SUCCEEDED", false);
                    showToast("Unregistered!");
                } else {
                    // TODO
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GcmConstants.REGISTRATION_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mDownstreamBroadcastReceiver,
                new IntentFilter(GcmConstants.NEW_DOWNSTREAM_MESSAGE));


        messageRecyclerView = (RecyclerView) this.findViewById(R.id.messagesRecyclerView);

        messageAdapter = new MessageAdapter(messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Useful for chat interface items are added to the bottom
        messageRecyclerView.setLayoutManager(layoutManager);
        messageRecyclerView.setItemAnimator(new DefaultItemAnimator());
        messageRecyclerView.setAdapter(messageAdapter);

        messageEditText = (EditText) this.findViewById(R.id.message_editText);
        sendButton = (Button) this.findViewById(R.id.send_button);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle data = new Bundle();

                try {
                    // Encrypt the message
                    String encryptedMessage = AESCrypt.encrypt(password, messageEditText.getText().toString());

                    data.putString("message_to", recipient);
                    data.putString("message_text", encryptedMessage);

                    String senderId = GcmUtil.getServerUrl(getString(R.string.gcm_defaultSenderId));
                    Log.d(TAG, "Sending to senderID: " + senderId);

                    gcm.send(senderId, Integer.toString(++messageId), data);

                    Toast.makeText(getApplicationContext(), "Sent Message", Toast.LENGTH_LONG).show();

                    messageList.add(new Message("Me", messageEditText.getText().toString()));

                    // Clear the edit text
                    messageEditText.setText("");

                    // Notify view needs to be updated
                    messageAdapter.notifyDataSetChanged();

                    messageRecyclerView.scrollToPosition(messageList.size() - 1);
                } catch (GeneralSecurityException e) {
                    Toast.makeText(getApplicationContext(), "Encryption Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(GcmConstants.EXTRA_KEY_TOKEN, token);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GcmConstants.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onStop();
    }

    /**
     * Calls the GCM API to register this client if not already registered.
     *
     * @throws IOException
     */
    public void registerClient() {
        Log.d(TAG, "registerClient");
        // Get the sender ID
        String senderId = getString(R.string.gcm_defaultSenderId);

        Log.d(TAG, "Registering with SenderID: " + senderId);
        if (!("".equals(senderId))) {

            // Register with GCM
            Intent intent = new Intent(this, RegistrationIntentService.class);
            intent.putExtra(GcmConstants.SENDER_ID, senderId);
            startService(intent);
        }
    }

    /**
     * Calls the GCM API to unregister this client
     */
    public void unregisterClient() {
        String senderId = getString(R.string.gcm_defaultSenderId);
        if (!("".equals(senderId))) {
            // Create the bundle for registration with the server.
            Bundle registration = new Bundle();
            registration.putString(GcmConstants.ACTION, GcmConstants.UNREGISTER_CLIENT);
            registration.putString(GcmConstants.REGISTRATION_TOKEN, token);

            try {
                gcm.send(GcmUtil.getServerUrl(senderId),
                        String.valueOf(System.currentTimeMillis()), registration);
            } catch (IOException e) {
                Log.e(TAG, "Message failed", e);
            }
        }
    }

    /**
     * Sends an upstream message.
     */
    public void sendMessage() {
        String senderId = getString(R.string.gcm_defaultSenderId);
        if (!("".equals(senderId))) {
//            String text = upstreamMessageField.getText().toString();
//            if (text == "") {
//                showToast("Please enter a message to send");
//                return;
//            }

            // Create the bundle for sending the message.
            Bundle message = new Bundle();
            message.putString(GcmConstants.ACTION, GcmConstants.UPSTREAM_MESSAGE);
//            message.putString(GcmConstants.EXTRA_KEY_MESSAGE, text);

            try {
                gcm.send(GcmUtil.getServerUrl(senderId),
                        String.valueOf(System.currentTimeMillis()), message);
                showToast("Message sent successfully");
            } catch (IOException e) {
                Log.e(TAG, "Message failed", e);
                showToast("Upstream FAILED");
            }
        }
    }

    /**
     * Subscribes client to the entered topic.
     */
    public void subscribeToTopic() {
        String senderId = getString(R.string.gcm_defaultSenderId);
        if (!("".equals(senderId))) {
//            String topic = topicField.getText().toString().trim();
//            if (topic == "" || !topic.startsWith(TOPIC_PREFIX) ||
//                    topic.length() <= TOPIC_PREFIX.length()) {
//                showToast("Make sure topic is in format \"/topics/topicName\"");
//                return;
//            }


//            new SubscribeToTopicTask().execute(topic);
        }
    }

    /**
     * Subscribe the client to the passed topic.
     */
    private class SubscribeToTopicTask extends AsyncTask<String, Void, Boolean> {

        private String topic;

        @Override
        protected Boolean doInBackground(String... params) {
            if (params.length > 0) {
                topic = params[0];
                try {
                    pubSub.subscribe(token, topic, null);
                    return true;
                } catch (IOException e) {
                    Log.e(TAG, "Subscribe to topic failed", e);
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean succeed) {
            if (succeed) {
//                updateUI("Subscribed to topic: " + topic, true);
            } else {
//                updateUI("Subscription to topic failed: " + topic, false);
            }
        }
    }

    /**
     * Show a toast with passed text
     *
     * @param text to be used as toast message
     */
    private void showToast(CharSequence text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private void checkPlayServices() {
        Log.d(TAG, "checkPlayServices");

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            Log.d(TAG, "Play Services NOT Available");
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST,
                        new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                            }
                        }).show();
            } else {
                Log.w(TAG, "Google Play Services is required and not supported on this device.");
            }
        } else {
            Log.d(TAG, "Play Services Available");
        }
    }

}