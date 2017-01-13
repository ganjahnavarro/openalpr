package org.openalpr.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.facebook.login.LoginManager;

public class MenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        generateNavigationButton(R.id.camera, MainActivity.class);
        generateNavigationButton(R.id.about, AboutActivity.class);
        generateNavigationButton(R.id.settings, SettingsActivity.class);
        generateNavigationButton(R.id.gallery, GalleryActivity.class);
        generateNavigationButton(R.id.logout, LoginActivity.class, true);
    }

    private Button generateNavigationButton(int id, final Class pathClass) {
        return generateNavigationButton(id, pathClass, false);
    }

    private Button generateNavigationButton(int id, final Class pathClass, final Boolean finish) {
        Button button = (Button) findViewById(id);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuActivity.this, pathClass));

                if (finish) {
                    LoginManager.getInstance().logOut();
                    finish();
                }
            }
        });
        return button;
    }



}
