package org.openalpr.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.openalpr.core.database.DatabaseContract;
import org.openalpr.core.database.DatabaseHelper;
import org.openalpr.core.utils.Toaster;

import java.text.Format;
import java.text.SimpleDateFormat;
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

        private Context context;
        private DatabaseHelper databaseHelper;
        private List<DatabaseContract.Snapshot> snapshots;
        private Format dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());

        public SnapshotAdapter(Context context, DatabaseHelper databaseHelper) {
            this.context = context;
            this.databaseHelper = databaseHelper;
            this.snapshots = databaseHelper.findSnapshots();
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            final View view;

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.gallery_item, parent, false);
            } else {
                view = convertView;
            }

            final DatabaseContract.Snapshot snapshot = (DatabaseContract.Snapshot) getItem(position);

            Log.d(this.getClass().getSimpleName(), "Image: " + snapshot.getImageFileName());

            TextView label = (TextView) view.findViewById(R.id.snapshot_label);

            String capturedBy = snapshot.getCapturedBy() != null ? snapshot.getCapturedBy() : "Guest User";
            String capturedDate = dateTimeFormat.format(snapshot.getCapturedDate());

            label.setText("Plate: " + snapshot.getPlateNumber() + " \n"
                    + "Status: " + snapshot.getStatus() + " \n"
                    + "Captured Date: \n"
                    + capturedDate + " \n"
                    + "Captured By: \n"
                    + capturedBy);

            setSnapshotPreview(view, snapshot);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toaster.show(context, "Request Result for Plate (" + snapshot.getPlateNumber() + ") \n" + snapshot.getResult());
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {
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

                    return true;
                }
            });

            return view;
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

    }

}
