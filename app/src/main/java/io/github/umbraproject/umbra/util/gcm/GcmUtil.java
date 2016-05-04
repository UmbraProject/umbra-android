package io.github.umbraproject.umbra.util.gcm;

/**
 * Created by matt on 5/3/16.
 */
public class GcmUtil {

    public static String getServerUrl(String senderId) {
        return senderId + "@gcm.googleapis.com";
    }
}
