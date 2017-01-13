package org.openalpr.core.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Ganjah on 1/13/2017.
 */

public class HashGenerator {

    static String generate(PackageManager packageManager, String packageName) {
        try {
            PackageInfo info = packageManager.getPackageInfo(
                    packageName, PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.d("HashGenerator", hash);
                return hash;
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            Log.e("HashGenerator", "Error generating hash: " + e.getMessage());
        }
        return null;
    }

}
