package org.openalpr.core.database;

import android.provider.BaseColumns;

import java.util.Date;

/**
 * Created by Ganjah on 1/10/2017.
 */

public final class DatabaseContract {

    private DatabaseContract() {}

    public static class Snapshot implements BaseColumns {

        public static final String TABLE_NAME = "snapshot";
        public static final String COLUMN_NAME_IMAGE_FILE_NAME = "imageFileName";
        public static final String COLUMN_NAME_PLATE_NUMBER = "plateNumber";
        public static final String COLUMN_NAME_CAPTURED_DATE = "capturedDate";
        public static final String COLUMN_NAME_CAPTURED_BY = "capturedBy";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_RESULT = "result";

        private long _id;
        private String imageFileName;
        private String plateNumber;
        private Date capturedDate;
        private String capturedBy;
        private VerificationStatus status;
        private String result;

        public long getId() {
            return _id;
        }

        public void setId(long _id) {
            this._id = _id;
        }

        public String getImageFileName() {
            return imageFileName;
        }

        public void setImageFileName(String imageFileName) {
            this.imageFileName = imageFileName;
        }

        public String getPlateNumber() {
            return plateNumber;
        }

        public void setPlateNumber(String plateNumber) {
            this.plateNumber = plateNumber;
        }

        public Date getCapturedDate() {
            return capturedDate;
        }

        public void setCapturedDate(Date capturedDate) {
            this.capturedDate = capturedDate;
        }

        public String getCapturedBy() {
            return capturedBy;
        }

        public void setCapturedBy(String capturedBy) {
            this.capturedBy = capturedBy;
        }

        public VerificationStatus getStatus() {
            return status;
        }

        public void setStatus(VerificationStatus status) {
            this.status = status;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }

}
