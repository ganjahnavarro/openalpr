package org.openalpr.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.openalpr.core.utils.Toaster;
import org.openalpr.core.utils.UserManager;

import java.util.Arrays;

public class LoginActivity extends Activity {

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private boolean loginSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));

        final Context context = this;

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    UserManager.getInstance().setLoggedUser(null);
                    this.stopTracking();
                }
            }
        };

        if (AccessToken.getCurrentAccessToken() == null) {
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    loginSuccess = true;

                    GraphRequest request = GraphRequest.newMeRequest(
                            loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    Log.v("LoginActivity", response.toString());

                                    try {
                                        String name = object.getString("name");
                                        Log.d(this.getClass().getSimpleName(), name);

                                        UserManager.getInstance().setLoggedUser(name);
                                        accessTokenTracker.startTracking();
                                        Toaster.show(context, "Logged on as: " + name);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id, name, email");
                    request.setParameters(parameters);
                    request.executeAsync();
                }

                @Override
                public void onCancel() {
                    Toaster.show(context, "Login cancelled.");
                    loginSuccess = false;
                }

                @Override
                public void onError(FacebookException e) {
                    Toaster.show(context, "Login error: " + e.getMessage());
                    loginSuccess = false;
                }
            });

            TextView guestSignOn = (TextView) findViewById(R.id.guest_sign_on);
            guestSignOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toaster.show(view.getContext(), "Logged on as: Guest User");
                    openMenu();
                }
            });
        } else {
            openMenu();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (loginSuccess) {
            openMenu();
        }
    }

    private void openMenu() {
        startActivity(new Intent(LoginActivity.this, MenuActivity.class));
        finish();
    }

}
