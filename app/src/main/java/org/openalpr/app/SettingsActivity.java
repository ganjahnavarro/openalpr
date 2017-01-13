package org.openalpr.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    private static final int PERMISSION_REQUEST_CALL = 123002;

//    private DatabaseHelper databaseHelper;
//    private BroadcastReceiver smsSentBroadcastReceiver;
//    private BroadcastReceiver smsDeliveredBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

//        smsSetup();
        callSetup();
//        databaseSetup();
    }

//    private void smsSetup() {
//        final SmsHelper helper = new SmsHelper(this);
//        smsSentBroadcastReceiver = helper.getSmsSentBroadcastReceiver();
//        smsDeliveredBroadcastReceiver = helper.getSmsDeliveredBroadcastReceiver(getBaseContext());
//        registerReceiver(smsSentBroadcastReceiver, new IntentFilter(SmsHelper.SMS_SENT));
//        registerReceiver(smsDeliveredBroadcastReceiver, new IntentFilter(SmsHelper.SMS_DELIVERED));
//
//        TextView smsLink = (TextView) findViewById(R.id.smsTest);
//        smsLink.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(view.getContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
//                helper.sendMessage("09433427345", "Test Message");
//            }
//        });
//    }
//
//    @Override
//    protected void onDestroy() {
//        unregisterReceiver(smsSentBroadcastReceiver);
//        unregisterReceiver(smsDeliveredBroadcastReceiver);
//        super.onDestroy();
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        switch (requestCode) {
//            case PERMISSION_REQUEST_SMS: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Log.i(this.getClass().getName(), "Send/Receive SMS Permission granted");
//                } else {
//                    Toast.makeText(this, "This feature won't function properly if you don't accept Send/Receive SMS permission", Toast.LENGTH_SHORT).show();
//                }
//            }
//            break;
//            case PERMISSION_REQUEST_CALL: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Log.i(this.getClass().getName(), "Call Permission granted");
//                } else {
//                    Toast.makeText(this, "This feature won't function properly if you don't accept Call permission", Toast.LENGTH_SHORT).show();
//                }
//            }
//            break;
//        }
//    }
//
    private void callSetup() {
        requestCallPermission();

        ImageView callLink = (ImageView) findViewById(R.id.call911);
        callLink.setOnClickListener(callOnClickListener());

        TextView callLinkMessage = (TextView) findViewById(R.id.call911message);
        callLinkMessage.setOnClickListener(callOnClickListener());
    }

    @NonNull
    private View.OnClickListener callOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:911"));

                if (!isPermitted(Manifest.permission.CALL_PHONE)) {
                    startActivity(callIntent);
                }
            }
        };
    }

    private boolean isPermitted(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED;
    }

    private void requestCallPermission() {
        String callPermission = Manifest.permission.CALL_PHONE;
        if (isPermitted(callPermission)) {
            ActivityCompat.requestPermissions(this, new String[]{callPermission}, PERMISSION_REQUEST_CALL);
        }
    }
//
//    private void databaseSetup() {
//        databaseHelper = new DatabaseHelper(this);
//        writeDataSetup(databaseHelper);
//        readDataSetup(databaseHelper);
//    }
//
//    private void writeDataSetup(final DatabaseHelper databaseHelper) {
//        TextView writeLink = (TextView) findViewById(R.id.databaseWrite);
//        writeLink.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Long newRowId = databaseHelper.insertSnapshot("test123.png", "ABC1234");
//                Toast.makeText(view.getContext(), "New row ID: " + newRowId, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void readDataSetup(final DatabaseHelper databaseHelper) {
//        TextView readLink = (TextView) findViewById(R.id.databaseRead);
//        readLink.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(view.getContext(), databaseHelper.findSnapshots().size(), Toast.LENGTH_LONG).show();
//            }
//        });
//    }

}
