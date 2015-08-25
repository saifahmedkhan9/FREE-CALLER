package com.example.shahjahan.freecaller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class CallProgress extends AppCompatActivity {
    SpeakerMic speakerMic = new SpeakerMic(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_progress);

        String ip_address = getIntent().getExtras().getString("IP", null);
        speakerMic.startMic(ip_address);

        speakerMic.startSpeakers();
    }


    public void disconnect(View view) {
        speakerMic.mic = false;
        speakerMic.speakers = false;

        startActivity(new Intent(this,MainActivity.class));


    }
}
