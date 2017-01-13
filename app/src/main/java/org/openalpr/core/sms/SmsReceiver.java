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

        String message = "";

        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                message = messages[i].getMessageBody();
            }

            Log.d(TAG, message);

            if (message != null) {
                databaseHelper.updateLatestRecord(VerificationStatus.PROCESSED, message);
            }
        }
    }

}
