package org.openalpr.core.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.openalpr.core.utils.UserManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openalpr.core.database.DatabaseContract.Snapshot;

/**
 * Created by Ganjah on 1/10/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "openalpr.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + Snapshot.TABLE_NAME + " (" +
                    Snapshot._ID + " INTEGER PRIMARY KEY," +
                    Snapshot.COLUMN_NAME_IMAGE_FILE_NAME + " TEXT," +
                    Snapshot.COLUMN_NAME_PLATE_NUMBER + " TEXT," +
                    Snapshot.COLUMN_NAME_STATUS + " TEXT," +
                    Snapshot.COLUMN_NAME_RESULT + " TEXT," +
                    Snapshot.COLUMN_NAME_CAPTURED_BY + " TEXT," +
                    Snapshot.COLUMN_NAME_CAPTURED_DATE + " LONG)";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + Snapshot.TABLE_NAME;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public List<Snapshot> findSnapshots() {
        String[] projection = {
                Snapshot._ID,
                Snapshot.COLUMN_NAME_IMAGE_FILE_NAME,
                Snapshot.COLUMN_NAME_PLATE_NUMBER,
                Snapshot.COLUMN_NAME_STATUS,
                Snapshot.COLUMN_NAME_RESULT,
                Snapshot.COLUMN_NAME_CAPTURED_BY,
                Snapshot.COLUMN_NAME_CAPTURED_DATE
        };

        String sortOrder = Snapshot.COLUMN_NAME_CAPTURED_DATE + " DESC";

        Cursor cursor = getWritableDatabase()
                .query(Snapshot.TABLE_NAME, projection, null, null, null, null, sortOrder);

        List<Snapshot> snapshots = new ArrayList<>();
        while (cursor.moveToNext()) {
            snapshots.add(createSnapshot(cursor));
        }

        cursor.close();
        return snapshots;
    }

    public void updateLatestRecord(VerificationStatus status, String result) {
        SQLiteDatabase database = getWritableDatabase();
        String updateScript = "UPDATE " + Snapshot.TABLE_NAME
                + " SET " + Snapshot.COLUMN_NAME_STATUS + " = '" + status.toString() + "'"
                + (result != null ? ", " + Snapshot.COLUMN_NAME_RESULT + " = '" + result + "'" : "")
                + " WHERE " + Snapshot._ID + " = ("
                    + " SELECT MAX(" + Snapshot._ID + ") FROM " + Snapshot.TABLE_NAME
                    + " WHERE " + Snapshot.COLUMN_NAME_STATUS + " != '" + VerificationStatus.PROCESSED.toString() + "'"
                + ")";

        try {
            Log.d(this.getClass().getSimpleName(), updateScript);
            database.execSQL(updateScript);
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }
    }

    public Long insertSnapshot(String fileName, String plateNumber, VerificationStatus status) {
        ContentValues values = new ContentValues();
        values.put(Snapshot.COLUMN_NAME_IMAGE_FILE_NAME, fileName);
        values.put(Snapshot.COLUMN_NAME_PLATE_NUMBER, plateNumber);
        values.put(Snapshot.COLUMN_NAME_CAPTURED_BY, UserManager.getInstance().getLoggedUser());
        values.put(Snapshot.COLUMN_NAME_CAPTURED_DATE, new Date().getTime());
        values.put(Snapshot.COLUMN_NAME_STATUS, status.toString());
        return getWritableDatabase().insert(Snapshot.TABLE_NAME, null, values);
    }

    private Snapshot createSnapshot(Cursor cursor) {
        Snapshot snapshot = new Snapshot();
        snapshot.setId(cursor.getLong(cursor.getColumnIndex(Snapshot._ID)));
        snapshot.setImageFileName(cursor.getString(cursor.getColumnIndex(Snapshot.COLUMN_NAME_IMAGE_FILE_NAME)));
        snapshot.setPlateNumber(cursor.getString(cursor.getColumnIndex(Snapshot.COLUMN_NAME_PLATE_NUMBER)));
        snapshot.setStatus(VerificationStatus.valueOf(cursor.getString(cursor.getColumnIndex(Snapshot.COLUMN_NAME_STATUS))));
        snapshot.setResult(cursor.getString(cursor.getColumnIndex(Snapshot.COLUMN_NAME_RESULT)));
        snapshot.setCapturedBy(cursor.getString(cursor.getColumnIndex(Snapshot.COLUMN_NAME_CAPTURED_BY)));
        snapshot.setCapturedDate(new Date(cursor.getLong(cursor.getColumnIndex(Snapshot.COLUMN_NAME_CAPTURED_DATE))));
        return snapshot;
    }

}
