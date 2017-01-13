package org.openalpr.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class AboutActivity extends Activity{

    private int tapCount = 0;
    private int tapsNeeded = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        tapCount = 0;

        ImageView aboutComingSoon = (ImageView) findViewById(R.id.aboutComingSoon);
        aboutComingSoon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tapCount++;
                if (tapCount > tapsNeeded) {
                    Toast.makeText(v.getContext(), "Contact developer thru: ganjahnavarro@gmail.com", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
