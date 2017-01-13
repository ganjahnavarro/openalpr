package org.openalpr.core.sms;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;

/**
 * Created by Ganjah on 1/9/2017.
 */

public final class SmsHelper {

    private SmsManager smsManager;

    public static final String SMS_SENT = "SMS_SENT";
    public static final String SMS_DELIVERED = "SMS_DELIVERED";

    public static final int PERMISSION_REQUEST_SMS = 123001;

    private PendingIntent sentPendingIntent;
    private PendingIntent deliveredPendingIntent;

    public SmsHelper(Activity activity) {
        requestPermission(activity);
        smsManager = SmsManager.getDefault();
        sentPendingIntent = PendingIntent.getBroadcast(activity, 0, new Intent(SMS_SENT), 0);
        deliveredPendingIntent = PendingIntent.getBroadcast(activity, 0, new Intent(SMS_DELIVERED), 0);
    }

    public void requestPermission(Activity activity) {
        String sendPermission = Manifest.permission.SEND_SMS;
        String receivePermission = Manifest.permission.RECEIVE_SMS;

        int sendPermissionCheck = ContextCompat.checkSelfPermission(activity, sendPermission);
        int receivePermissionCheck = ContextCompat.checkSelfPermission(activity, receivePermission);
        int granted = PackageManager.PERMISSION_GRANTED;

        if (sendPermissionCheck != granted || receivePermissionCheck != granted) {
            ActivityCompat.requestPermissions(activity, new String[]{sendPermission, receivePermission}, PERMISSION_REQUEST_SMS);
        }
    }

//    public BroadcastReceiver getSmsSentBroadcastReceiver() {
//        return new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                switch (getResultCode()) {
//                    case Activity.RESULT_OK:
//                        Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show();
//                        break;
//                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
//                        Toast.makeText(context, "Generic failure cause", Toast.LENGTH_SHORT).show();
//                        break;
//                    case SmsManager.RESULT_ERROR_NO_SERVICE:
//                        Toast.makeText(context, "Service is currently unavailable", Toast.LENGTH_SHORT).show();
//                        break;
//                    case SmsManager.RESULT_ERROR_NULL_PDU:
//                        Toast.makeText(context, "No pdu provided", Toast.LENGTH_SHORT).show();
//                        break;
//                    case SmsManager.RESULT_ERROR_RADIO_OFF:
//                        Toast.makeText(context, "Radio was explicitly turned off", Toast.LENGTH_SHORT).show();
//                        break;
//                }
//            }
//        };
//    }
//
//    public BroadcastReceiver getSmsDeliveredBroadcastReceiver(final Context baseContext) {
//        return new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                switch (getResultCode()) {
//                    case Activity.RESULT_OK:
//                        Toast.makeText(baseContext, "SMS delivered", Toast.LENGTH_SHORT).show();
//                        break;
//                    case Activity.RESULT_CANCELED:
//                        Toast.makeText(baseContext, "SMS not delivered", Toast.LENGTH_SHORT).show();
//                        break;
//                }
//            }
//        };
//    }

    public void sendMessage(String contactNo, String message) {
        smsManager.sendTextMessage(contactNo, null, message, sentPendingIntent, deliveredPendingIntent);
    }

}
