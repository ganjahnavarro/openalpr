package org.openalpr.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.openalpr.core.database.DatabaseHelper;
import org.openalpr.core.database.VerificationStatus;
import org.openalpr.core.sms.SmsHelper;
import org.openalpr.core.utils.Toaster;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import static org.openalpr.app.AppConstants.ALPR_ARGS;
import static org.openalpr.app.AppConstants.ALPR_FRAGMENT_TAG;
import static org.openalpr.app.AppConstants.ANDROID_DATA_DIR;
import static org.openalpr.app.AppConstants.FORMAT_PLATE_NUMBER_INQUIRY;
import static org.openalpr.app.AppConstants.FORMAT_PLATE_NUMBER_KEY;
import static org.openalpr.app.AppConstants.JPEG_FILE_PREFIX;
import static org.openalpr.app.AppConstants.JPEG_FILE_SUFFIX;
import static org.openalpr.app.AppConstants.LTO_NUMBER;
import static org.openalpr.app.AppConstants.OPENALPR_CONF_FILE;
import static org.openalpr.app.AppConstants.PREF_INSTALLED_KEY;
import static org.openalpr.app.AppConstants.REQUEST_IMAGE_CAPTURE;
import static org.openalpr.app.AppConstants.RUNTIME_DATA_DIR_ASSET;


public class MainActivity extends Activity implements AsyncListener<AlprResult> {

    private DatabaseHelper databaseHelper;

    private String mCurrentPhotoPath;
    private ImageView capturedImage;

    private EditText plate;
    private TextView duration;

    private Button openCameraButton;
    private TextView errorText;
    private ProgressDialog progressDialog;

    private Button sendSmsButton;
    private BroadcastReceiver smsSentBroadcastReceiver;
    private BroadcastReceiver smsDeliveredBroadcastReceiver;

    Button.OnClickListener takePhotoBtnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            openCamera();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        copyAssets();

        setContentView(R.layout.activity_main);

        capturedImage = (ImageView) findViewById(R.id.capture);
        plate = (EditText) findViewById(R.id.plate);
        duration = (TextView) findViewById(R.id.duration);
        errorText = (TextView) findViewById(R.id.errorMessage);
        openCameraButton = (Button) findViewById(R.id.camera);
        setOpenCameraListener(openCameraButton, takePhotoBtnClickListener,
                MediaStore.ACTION_IMAGE_CAPTURE);

        openCamera();
        databaseHelper = new DatabaseHelper(this);
        setupSmsHelper();
    }

    private void setupSmsHelper() {
        final SmsHelper helper = new SmsHelper(this);
        smsSentBroadcastReceiver = getSmsSentBroadcastReceiver();
        registerReceiver(smsSentBroadcastReceiver, new IntentFilter(SmsHelper.SMS_SENT));
        registerReceiver(smsDeliveredBroadcastReceiver, new IntentFilter(SmsHelper.SMS_DELIVERED));

        sendSmsButton = (Button) findViewById(R.id.sendSms);
        sendSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (plate.getText() == null || String.valueOf(plate.getText()).isEmpty()) {
                    Toaster.show(view.getContext(), "No plate number present, please take another picture.");
                    return;
                }

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                String message = FORMAT_PLATE_NUMBER_INQUIRY;
                                helper.sendMessage(LTO_NUMBER, message.replace(FORMAT_PLATE_NUMBER_KEY, plate.getText()));
                                insertSnapshot();
                                Toaster.show(view.getContext(), "Vehicle details request was sent. Please check back after few minutes.");
                                sendSmsButton.setVisibility(View.INVISIBLE);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage("This actions requires P2.00 load. Continue?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("Cancel", dialogClickListener).show();
            }
        });
    }

    public BroadcastReceiver getSmsSentBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(this.getClass().getSimpleName(), "Sms Send Result: " + getResultCode());

                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        databaseHelper.updateLatestRecord(VerificationStatus.REQUESTED, "SMS sent successfully");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        databaseHelper.updateLatestRecord(VerificationStatus.REQUEST_FAILURE, "Generic failure cause");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        databaseHelper.updateLatestRecord(VerificationStatus.REQUEST_FAILURE, "Service is currently unavailable");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        databaseHelper.updateLatestRecord(VerificationStatus.REQUEST_FAILURE, "No pdu provided");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        databaseHelper.updateLatestRecord(VerificationStatus.REQUEST_FAILURE, "Radio was explicitly turned off");
                        break;
                }
            }
        };
    }

    private void insertSnapshot() {
        databaseHelper.insertSnapshot(mCurrentPhotoPath, String.valueOf(plate.getText()));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(smsSentBroadcastReceiver);
        super.onDestroy();
    }

    private void copyAssets() {
        if (!PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext())
                .getBoolean(PREF_INSTALLED_KEY, false)) {

            PreferenceManager.getDefaultSharedPreferences(
                    getApplicationContext())
                    .edit().putBoolean(PREF_INSTALLED_KEY, true).apply();

            copyAssetFolder(getAssets(), RUNTIME_DATA_DIR_ASSET,
                    ANDROID_DATA_DIR + File.separatorChar + RUNTIME_DATA_DIR_ASSET);
        }
    }

    private static boolean copyAssetFolder(AssetManager assetManager,
                                           String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files)
                if (file.contains("."))
                    res &= copyAsset(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
                else
                    res &= copyAssetFolder(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean copyAsset(AssetManager assetManager,
                                     String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private File getStorageDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = getExternalFilesDir(null);
        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    private File createImageFile() throws IOException {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File storageDir = getStorageDir();
        File imageFile = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, storageDir);
        return imageFile;
    }

    private File setupPhotoFile() throws IOException {
        File file = createImageFile();
        mCurrentPhotoPath = file.getAbsolutePath();
        return file;
    }

    private void resizeImage() {
        if (mCurrentPhotoPath != null) {
            setCapturedImage();
        }
    }

    private void setCapturedImage() {
		/* There isn't enough memory to open up more than a couple camera photos */
        /* So pre-scale the target bitmap into which the file is decoded */
		/* Get the size of the ImageView */
        int targetW = capturedImage.getWidth();
        int targetH = capturedImage.getHeight();

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

		/* Associate the Bitmap to the ImageView */
        capturedImage.setImageBitmap(bitmap);
        capturedImage.setVisibility(View.VISIBLE);
    }

    private void openCamera() {
        setErrorText("");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file;

        try {
            file = setupPhotoFile();
            mCurrentPhotoPath = file.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        } catch (IOException e) {
            e.printStackTrace();
            mCurrentPhotoPath = null;
        }

        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            String openAlprConfFile = ANDROID_DATA_DIR + File.separatorChar +
                    RUNTIME_DATA_DIR_ASSET + File.separatorChar + OPENALPR_CONF_FILE;
            resizeImage();
            String parameters[] = {"eu", "", this.mCurrentPhotoPath, openAlprConfFile, "1"};
            Bundle args = new Bundle();
            args.putStringArray(ALPR_ARGS, parameters);
            AlprFragment alprFragment = (AlprFragment) getFragmentManager()
                    .findFragmentByTag(ALPR_FRAGMENT_TAG);

            if (alprFragment == null) {
                alprFragment = new AlprFragment();
                alprFragment.setArguments(args);

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.add(alprFragment, ALPR_FRAGMENT_TAG);
                transaction.commitAllowingStateLoss();
            }
        }
    }


    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private void setOpenCameraListener(Button btn, Button.OnClickListener onClickListener, String intentName) {
        if (isIntentAvailable(this, intentName)) {
            btn.setOnClickListener(onClickListener);
        }
    }

    public void setPlate(String plate) {
        this.plate.setText(plate);
    }

    public void setProcessingTime(long processingTime) {
        this.duration.setText(String.format(Locale.ENGLISH, "Duration: %d %s", processingTime, "ms"));
    }

    private void setErrorText(String text) {
        errorText.setText(text);
    }

    @Override
    public void onPreExecute() {
        onProgressUpdate();
    }

    @Override
    public void onProgressUpdate() {
        if (progressDialog == null) {
            prepareProgressDialog();
        }
    }

    @Override
    public void onPostExecute(AlprResult alprResult) {
        if (alprResult.isRecognized()) {
            List<AlprResultItem> resultItems = alprResult.getResultItems();
            if (resultItems.size() > 0) {
                AlprResultItem resultItem = resultItems.get(0);
                setPlate(resultItem.getPlate());
                setProcessingTime(alprResult.getProcessingTime());
                sendSmsButton.setVisibility(View.VISIBLE);
            }
            cleanUp();
        } else {
            sendSmsButton.setVisibility(View.INVISIBLE);
            setErrorText(getString(R.string.recognition_error));
            cleanUp();
        }
    }

    private void cleanUp() {
        progressDialog.dismiss();
        progressDialog = null;
        FragmentManager fm = getFragmentManager();
        AlprFragment alprFragment = (AlprFragment) fm.findFragmentByTag(ALPR_FRAGMENT_TAG);
        fm.beginTransaction().remove(alprFragment).commitAllowingStateLoss();
    }

    private void prepareProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Processing Image data");
        progressDialog.show();
    }
}
