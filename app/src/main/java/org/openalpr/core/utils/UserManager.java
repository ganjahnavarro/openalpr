package org.openalpr.core.utils;

import android.util.Log;

/**
 * Created by Ganjah on 1/13/2017.
 */

public class UserManager {

    private static UserManager instance = null;

    private String loggedUser = null;

    private UserManager() {}

    public static UserManager getInstance() {
        if(instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public String getLoggedUser() {
        return loggedUser;
    }

    public void setLoggedUser(String loggedUser) {
        Log.d("Open ALPR User Manager", "Logged user changed: " + loggedUser);
        this.loggedUser = loggedUser;
    }

}
