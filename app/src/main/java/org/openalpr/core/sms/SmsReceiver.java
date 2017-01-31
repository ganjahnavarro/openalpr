package org.openalpr.core.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import org.openalpr.core.database.DatabaseHelper;
import org.openalpr.core.database.VerificationStatus;

/**
 * Created by Ganjah on 1/9/2017.
 */

public class SmsReceiver extends BroadcastReceiver {

    private String TAG = SmsReceiver.class.getSimpleName();
    private DatabaseHelper databaseHelper;

    public SmsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(context);
        }

        Bundle bundle = intent.getExtras();
        SmsMessage[] messages;

        String message = null;
        String phoneNumber = null;

        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                phoneNumber = messages[i].getDisplayOriginatingAddress();
                message = messages[i].getMessageBody();
            }

            Log.d(TAG, "Message: " + message);
            Log.d(TAG, "Phone Number: " + phoneNumber);

            if (message != null && phoneNumber != null && phoneNumber.equals("2600")) {
                databaseHelper.updateLatestRecord(VerificationStatus.PROCESSED, message);
            }
        }
    }

}
