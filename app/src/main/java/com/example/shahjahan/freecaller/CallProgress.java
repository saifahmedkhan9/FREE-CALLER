package com.example.shahjahan.freecaller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class CallProgress extends AppCompatActivity {

    String device_saved_name="";
    SpeakerMic speakerMic = new SpeakerMic(this);
    TextView txt;
    public int seconds = 00;
    public int minutes = 00;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_progress);
//        txt=(TextView)findViewById(R.id.textView);
        String ip_address = getIntent().getExtras().getString("IP", null);
        speakerMic.startMic(ip_address);
//        device_saved_name=getIntent().getExtras().getString("name");
        speakerMic.startSpeakers();
//        txt.setText("Dailing via Lan Network..\n\n"+device_saved_name);
        Timer t = new Timer();
        //Set the schedule function and rate
        t.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        TextView tv = (TextView) findViewById(R.id.textView);
                        tv.setText(String.valueOf(minutes) + ":" + String.valueOf(seconds));
                        seconds += 1;

                        if (seconds == 60) {
                            tv.setText(String.valueOf(minutes) + ":" + String.valueOf(seconds));

                            seconds = 0;
                            minutes = minutes + 1;

                        }
                    }

                });
            }

        }, 0, 1000);
    }


    public void disconnect(View view) {
        speakerMic.mic = false;
        speakerMic.speakers = false;

        startActivity(new Intent(this,MainActivity.class));


    }
}
