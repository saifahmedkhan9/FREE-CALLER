package com.example.shahjahan.freecaller;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
/**
 * Created by Saif on 29/08/2015.
 */

public class SplashActivity extends Activity {

    private static final long DELAY = 3000;
    private boolean scheduled = false;
    private Timer splashTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashTimer = new Timer();
        splashTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                SplashActivity.this.finish();
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
        }, DELAY);
        scheduled = true;
    }


}