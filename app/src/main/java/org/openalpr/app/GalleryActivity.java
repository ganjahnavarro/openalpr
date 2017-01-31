package org.openalpr.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import org.openalpr.core.database.DatabaseContract;
import org.openalpr.core.database.DatabaseHelper;
import org.openalpr.core.database.VerificationStatus;
import org.openalpr.core.utils.Toaster;
import org.openalpr.core.utils.UserManager;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GalleryActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        ListView snapshotList = (ListView) findViewById(R.id.list_snapshot);
        snapshotList.setAdapter(new SnapshotAdapter(this, new DatabaseHelper(this)));
    }

    private class SnapshotAdapter extends BaseAdapter {

        private Activity activity;
        private List<DatabaseContract.Snapshot> snapshots;
        private DatabaseHelper databaseHelper;
        private Format dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());

        private ShareDialog shareDialog;
        private CallbackManager callbackManager;
        private boolean doneSetupFacebookSharing;

        public SnapshotAdapter(Activity activity, DatabaseHelper databaseHelper) {
            this.activity = activity;
            this.databaseHelper = databaseHelper;
            this.snapshots = databaseHelper.findSnapshots();

            doneSetupFacebookSharing = false;
        }

        @Override
        public int getCount() {
            return snapshots.size();
        }

        @Override
        public Object getItem(int position) {
            return snapshots.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            final View view;

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.gallery_item, parent, false);
            } else {
                view = convertView;
            }

            final DatabaseContract.Snapshot snapshot = (DatabaseContract.Snapshot) getItem(position);
            final TableLayout tableLayout = (TableLayout) view.findViewById(R.id.optionsLayout);
            final TableRow recognizedRowOnly = (TableRow) view.findViewById(R.id.recognizedRowOnly);

            Log.d(this.getClass().getSimpleName(), "Image: " + snapshot.getImageFileName());

            setImageCaption(view, snapshot);
            setSnapshotPreview(view, snapshot);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tableLayout.getVisibility() == View.GONE) {
                        tableLayout.setVisibility(View.VISIBLE);

                        Boolean notRecognized = snapshot.getStatus().equals(VerificationStatus.NOT_RECOGNIZED);
                        recognizedRowOnly.setVisibility(notRecognized ? View.GONE : View.VISIBLE);

                        setViewDetailsListener(view, snapshot);
                        setFacebookShareListener(view, snapshot);
                        setSendEmailListener(view, snapshot);
                        setDeleteListener(view, snapshot);
                    } else {
                        tableLayout.setVisibility(View.GONE);
                    }
                }
            });

            return view;
        }

        private void setImageCaption(View view, DatabaseContract.Snapshot snapshot) {
            Boolean notRecognized = snapshot.getStatus().equals(VerificationStatus.NOT_RECOGNIZED);

            TextView label = (TextView) view.findViewById(R.id.snapshot_label);
            String capturedBy = snapshot.getCapturedBy() != null ? snapshot.getCapturedBy() : "Guest User";
            String capturedDate = dateTimeFormat.format(snapshot.getCapturedDate());
            String plateNumber = notRecognized ? "N/A" : snapshot.getPlateNumber();

            label.setText("Plate: " + plateNumber + " \n"
                    + "Status: " + snapshot.getStatus() + " \n"
                    + "Captured Date: \n"
                    + capturedDate + " \n"
                    + "Captured By: \n"
                    + capturedBy);
        }

        private void setViewDetailsListener(View view, final DatabaseContract.Snapshot snapshot) {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String message = "No response from 2600";
                    if (snapshot.getStatus() == VerificationStatus.PROCESSED) {
                        message = "Request Result for Plate (" + snapshot.getPlateNumber() + ") \n" + snapshot.getResult();
                    }
                    Toaster.show(activity, message);
                }
            };

            Button button = (Button) view.findViewById(R.id.btnViewDetails);
            button.setOnClickListener(listener);
        }

        private void setFacebookShareListener(View view, final DatabaseContract.Snapshot snapshot) {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!UserManager.getInstance().isGuestUser()) {
                        if (!doneSetupFacebookSharing) {
                            callbackManager = CallbackManager.Factory.create();
                            askPostPermission();
                            setupShareDialog();
                        }

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeFile(snapshot.getImageFileName(), options);

                        SharePhoto photo = new SharePhoto.Builder()
                                .setBitmap(bitmap)
                                .setCaption("From license plate recognition application")
                                .build();

                        SharePhotoContent content = new SharePhotoContent.Builder()
                                .addPhoto(photo)
                                .build();

                        if (ShareDialog.canShow(ShareLinkContent.class)) {
                            shareDialog.show(content);
                        }
                    } else {
                        Toaster.show(activity, "Guest user can't share to facebook");
                    }
                }
            };

            Button button = (Button) view.findViewById(R.id.btnShareFacebook);
            button.setOnClickListener(listener);
        }

        private void setSendEmailListener(View view, final DatabaseContract.Snapshot snapshot) {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String emailBody = "This is a stolen vehicle according to 2600 LTO hotline please verify. \n"
                            + "Plate No: " + snapshot.getPlateNumber();

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("message/rfc822");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ltombox@lto.gov.ph"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Stolen Vehicle Verification");
                    intent.putExtra(Intent.EXTRA_TEXT, emailBody);
                    try {
                        startActivity(Intent.createChooser(intent, "Send mail.."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(view.getContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            Button button = (Button) view.findViewById(R.id.btnSendEmail);
            button.setOnClickListener(listener);
        }

        private void setDeleteListener(View view, final DatabaseContract.Snapshot snapshot) {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    databaseHelper.getWritableDatabase()
                                            .delete(DatabaseContract.Snapshot.TABLE_NAME,
                                                    "_id=?", new String[]{String.valueOf(snapshot.getId())});

                                    Toaster.show(view.getContext(), "Delete successful");
                                    snapshots = databaseHelper.findSnapshots();
                                    notifyDataSetChanged();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setMessage("Are you sure you want to delete this item?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("Cancel", dialogClickListener).show();
                }
            };

            Button button = (Button) view.findViewById(R.id.btnDeleteItem);
            button.setOnClickListener(listener);
        }

        private void setSnapshotPreview(View view, DatabaseContract.Snapshot snapshot) {
            ImageView image = (ImageView) view.findViewById(R.id.snapshot_preview);
            int targetW = image.getWidth();
            int targetH = image.getHeight();

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(snapshot.getImageFileName(), bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            int scaleFactor = 1;
            if ((targetW > 0) || (targetH > 0)) {
                scaleFactor = Math.min(photoW / targetW, photoH / targetH);
            }

            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(snapshot.getImageFileName(), bmOptions);

            image.setImageBitmap(bitmap);
            image.setVisibility(View.VISIBLE);
        }

        private void setupShareDialog() {
            shareDialog = new ShareDialog(activity);
            shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                @Override
                public void onSuccess(Sharer.Result result) {
                    Log.i("Share Dialog: ", "Share success, " + result.getPostId());
                }

                @Override
                public void onCancel() {
                    Log.e("Share Dialog: ", "User cancelled.");
                    Toaster.show(activity, "User cancelled.");
                }

                @Override
                public void onError(FacebookException error) {
                    Log.e("Share Dialog: ", error.getMessage());
                    Toaster.show(activity, "Share error: " + error.getMessage());
                }
            });
        }

        private void askPostPermission() {
            LoginManager.getInstance().logInWithPublishPermissions(
                    activity, Arrays.asList("publish_actions"));
        }

    }

}
