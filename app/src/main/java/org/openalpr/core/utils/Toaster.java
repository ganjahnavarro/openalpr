package org.openalpr.core.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Ganjah on 1/13/2017.
 */

public class Toaster {

    public static void show(Context context, String value) {
        Toast.makeText(context, value, Toast.LENGTH_SHORT).show();
    }

}
