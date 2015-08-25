package com.example.shahjahan.freecaller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
//    Class for speaker and mic control

    SpeakerMic speakerMic = new SpeakerMic(this);

//    Class used for broadcasting packets

    Broadcasting broadcasting = new Broadcasting(this);
//    class used for toasting purpose
    Toasting toasting = new Toasting();

//  Logging control via this class

    LoggerConfig loggerConfig = new LoggerConfig();

//    String name for shared preferences

    String DEVICE_NAME_SHAREDPREFERENCES = "DEVICE_NAME_SHAREDPREFERENCES";
    String DEVICE_NAME = "DEVICE_NAME";
    String device_saved_name;
    String TAG = "FREECALLER_MAIN";
    Integer DISCOVERY_PORT = 50005;
    SharedPreferences sharedPreferences;
    EditText editText;
    LinearLayout linearLayout;
    ListView all_contacts;
    ArrayList<String> All_Contacts = new ArrayList<>();
    Button device_name_button;
    ProgressBar progressBarContactList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.enter_device_name);
        device_name_button = (Button)findViewById(R.id.device_name_button);
        sharedPreferences = getSharedPreferences(DEVICE_NAME_SHAREDPREFERENCES, this.MODE_PRIVATE);
        linearLayout = (LinearLayout) findViewById(R.id.main_content_linear_layout);
        progressBarContactList = (ProgressBar)findViewById(R.id.progress_bar_contact_list);
        all_contacts = (ListView)findViewById(R.id.all_contacts);
//        check whether the name exists already or not?

        if ( !(sharedPreferences.getString(DEVICE_NAME,null) == null) )
        {
            editText.setVisibility(View.GONE);
            device_name_button.setVisibility(View.GONE);
            device_saved_name = sharedPreferences.getString(DEVICE_NAME,null);
            toasting.MakeText("Welcome " + device_saved_name,this);

//            broadcast packets from the wifi....

            if ( loggerConfig.LOG )
                Log.d(TAG,"broadcasting packets");

            broadcasting.broadcastPackets(device_saved_name);
            broadcasting.checkForCalls();

        }
        else
        {
            if ( loggerConfig.LOG )
                Log.d(TAG,"visibility has been gone");
                linearLayout.setVisibility(View.GONE);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void setDeviceName(View view) {

        sharedPreferences.edit().putString(DEVICE_NAME, editText.getText().toString()).apply();
        startActivity(new Intent(this,MainActivity.class));
    }

    public void updateList(View view) {

        new ReceivePacket().execute();
    }

    class ReceivePacket extends AsyncTask<Void, Void, String> {
        DatagramPacket packet;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarContactList.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT);
                socket.setBroadcast(true);
                byte[] buf = new byte[1024];
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                socket.disconnect();
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            String device_name = new String(packet.getData());
            String ip_address = new String(packet.getAddress().getHostAddress());

            return device_name + " ip:" + ip_address;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (!(All_Contacts.contains(s)) && (!(s.equals(speakerMic.getIPAddress())))) {
                All_Contacts.add(s);
            }

            ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, All_Contacts);
            all_contacts.setAdapter(adapter);

            all_contacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    String[] parts = All_Contacts.get(i).split(":");
                        Toast.makeText(getApplicationContext(), "The call is made to" + parts[1], Toast.LENGTH_LONG).show();
                    broadcasting.broadcastCallingPackets(parts[1]);

                    Intent intent = new Intent(getApplicationContext(),CallProgress.class);
                    intent.putExtra("IP", parts[1]);
                    startActivity(intent);

                }
            });
            progressBarContactList.setVisibility(View.GONE);

        }
    }

}

//The asynctask simply updates the contact list
