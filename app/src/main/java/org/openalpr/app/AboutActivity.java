package org.openalpr.app;

import android.app.Activity;
import android.os.Bundle;

public class AboutActivity extends Activity{

//    private ShareDialog shareDialog;
//    private CallbackManager callbackManager;
//    private String photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
//        TextView textView = (TextView) findViewById(R.id.aboutMessage);
//        textView.setText("De Castro, Mark Anthony \n Elbambo, Lev Luague \n Villamor, Erwin");

//        callbackManager = CallbackManager.Factory.create();
//        setupSharePhoto();
//        loadPhotoPath();
    }

//    private void setupSharePhoto() {
//        askPostPermission();
//        setupShareDialog();
//
//        ImageView aboutComingSoon = (ImageView) findViewById(R.id.aboutComingSoon);
//        aboutComingSoon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//                Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
//
//                SharePhoto photo = new SharePhoto.Builder()
//                        .setBitmap(bitmap)
//                        .setCaption("From license plate recognition application")
//                        .build();
//
//                SharePhotoContent content = new SharePhotoContent.Builder()
//                        .addPhoto(photo)
//                        .build();
//
//                if (ShareDialog.canShow(ShareLinkContent.class)) {
//                    shareDialog.show(content);
//                }
//            }
//        });
//    }
//
//    private void setupShareDialog() {
//        shareDialog = new ShareDialog(this);
//        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
//            @Override
//            public void onSuccess(Sharer.Result result) {
//                Log.i("Share Dialog: ", "Share success, " + result.getPostId());
//            }
//
//            @Override
//            public void onCancel() {
//                Log.e("Share Dialog: ", "User cancelled.");
//            }
//
//            @Override
//            public void onError(FacebookException error) {
//                Log.e("Share Dialog: ", error.getMessage());
//            }
//        });
//    }
//
//    private void askPostPermission() {
//        LoginManager.getInstance().logInWithPublishPermissions(
//                this, Arrays.asList("publish_actions"));
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        callbackManager.onActivityResult(requestCode, resultCode, data);
//    }
//
//    private void loadPhotoPath() {
//        DatabaseHelper databaseHelper = new DatabaseHelper(this);
//        List<DatabaseContract.Snapshot> snapshots = databaseHelper.findSnapshots();
//
//        for (DatabaseContract.Snapshot snapshot : snapshots) {
//            photoPath = snapshot.getImageFileName();
//        }
//    }

}
