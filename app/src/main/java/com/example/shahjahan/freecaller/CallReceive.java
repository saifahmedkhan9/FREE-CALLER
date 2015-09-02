package com.example.shahjahan.freecaller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;


public class CallReceive extends AppCompatActivity {

    Toasting toasting = new Toasting();

    TextView ip_address_text;
    String ip_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_receive);
        ip_address_text = (TextView)findViewById(R.id.dialerName);


         ip_address = getIntent().getExtras().getString("IP", null);

        if ( ip_address != null )
        {
            toasting.MakeText("The ip_address is: " + ip_address, this);
        }
        ip_address_text.setText("Getting call from " + ip_address);




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_call_receive, menu);
        return true;
    }

    public void acceptCall(View view)
    {

        Intent intent = new Intent(this,CallProgress.class);
        intent.putExtra("IP",ip_address);
        this.startActivity(intent);


    }

    public void rejectCall(View view)
    {
        startActivity(new Intent(this,MainActivity.class));
    }
}
