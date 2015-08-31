package com.example.shahjahan.freecaller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Created by Saif on 29/08/2015.
 */
public class Tab1 extends Fragment implements View.OnClickListener {

    SpeakerMic speakerMic = new SpeakerMic(getContext());

    Toasting toasting = new Toasting();
//  Logging control via this class

    Broadcasting broadcasting = new Broadcasting(getActivity().getApplicationContext());

//
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
    Button device_name_button,button2;
    ProgressBar progressBarContactList;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.activity_tab1,container,false);
        editText = (EditText)v.findViewById(R.id.enter_device_name);
        device_name_button = (Button)v.findViewById(R.id.device_name_button);
        device_name_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    sharedPreferences.edit().putString(DEVICE_NAME, editText.getText().toString()).apply();
                    startActivity(new Intent(getActivity(), MainActivity.class));


            }
        });

        button2 = (Button)v.findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new ReceivePacket().execute();

            }
        });
        sharedPreferences = getActivity().getSharedPreferences(DEVICE_NAME_SHAREDPREFERENCES, getActivity().MODE_PRIVATE);
        linearLayout = (LinearLayout)v.findViewById(R.id.main_content_linear_layout);
        progressBarContactList = (ProgressBar)v.findViewById(R.id.progress_bar_contact_list);
        all_contacts = (ListView)v.findViewById(R.id.all_contacts);
//        check whether the name exists already or not?

        if ( !(sharedPreferences.getString(DEVICE_NAME,null) == null) )
        {
            editText.setVisibility(View.GONE);
            device_name_button.setVisibility(View.GONE);
            device_saved_name = sharedPreferences.getString(DEVICE_NAME,null);
            toasting.MakeText("Welcome " + device_saved_name,getActivity());

//            broadcast packets from the wifi....

            if ( loggerConfig.LOG )
                Log.d(TAG, "broadcasting packets");

            broadcasting.broadcastPackets(device_saved_name);
            broadcasting.checkForCalls();

        }
        else
        {
            if ( loggerConfig.LOG )
                Log.d(TAG,"visibility has been gone");
            linearLayout.setVisibility(View.GONE);
        }
        return v;
    }


    @Override
    public void onClick(View v) {

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

            if ((!(All_Contacts.contains(s)) )&& (!(s.equals(speakerMic.getIPAddress())))) {
                All_Contacts.add(s);
            }

            ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, All_Contacts);
            all_contacts.setAdapter(adapter);

            all_contacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    String[] parts = All_Contacts.get(i).split(":");
                    Toast.makeText(getActivity(), "The call is made to" + parts[1], Toast.LENGTH_LONG).show();
                    broadcasting.broadcastCallingPackets(parts[1]);

                    Intent intent = new Intent(getActivity(),CallProgress.class);
                    intent.putExtra("IP", parts[1]);
                    startActivity(intent);

                }
            });
            progressBarContactList.setVisibility(View.GONE);

        }
    }
}
