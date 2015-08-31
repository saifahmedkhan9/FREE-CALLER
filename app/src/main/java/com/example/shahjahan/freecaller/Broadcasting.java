package com.example.shahjahan.freecaller;

import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by shahjahan on 25/8/15.
 */

public class Broadcasting
{
    private boolean calling_broadcast = true;
    private static final int CALLING_PORT = 5010;
    Context context;
    Toasting toasting = new Toasting();

    Broadcasting(Context context) {
        this.context = context;
    }

    //    this method will broadcast packets for device discovery
    String TAG = "FREECALLER_BROADCAST";
    Integer DISCOVERY_PORT = 50005;

    public void broadcastPackets(final String device_name) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        sleep(10000);
//                        Toast.makeText(getApplicationContext(), "I am after the thread", Toast.LENGTH_LONG).show();
                        try {
                            byte[] data = device_name.getBytes();
                            DatagramSocket socket = new DatagramSocket();
                            DatagramPacket packet = new DatagramPacket(data, data.length,
                                    getBroadcastAddress(), DISCOVERY_PORT);
                            socket.setBroadcast(true);
                            socket.send(packet);
                            Log.d(TAG, "Sent successfully.");
                            socket.disconnect();
                            socket.close();
                        } catch (SocketException e) {
                            e.printStackTrace();
                        } catch (IOException ie) {
                            ie.printStackTrace();
                        }


                        Log.d(TAG, "I am in thread.");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }


    public void broadcastCallingPackets(final String IP_Address)
    {
        calling_broadcast = true;
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (calling_broadcast) {
                        sleep(10000);
//                        Toast.makeText(getApplicationContext(), "I am after the thread", Toast.LENGTH_LONG).show();
                        try {
                            byte[] data = IP_Address.getBytes();
                            DatagramSocket socket = new DatagramSocket();
                            DatagramPacket packet = new DatagramPacket(data, data.length,
                                    InetAddress.getByName(IP_Address), CALLING_PORT);
                            socket.send(packet);
                            Log.d(TAG, "Sent successfully.");
                            socket.disconnect();
                            socket.close();
                            calling_broadcast = false;
                        } catch (SocketException e) {
                            e.printStackTrace();
                        } catch (IOException ie) {
                            ie.printStackTrace();
                        }

                        Log.d(TAG, "I am in thread.");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }


    public void checkForCalls()
    {
        Thread thread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                try {
                   while (true)
                   {
                       try {
                           sleep(10000);
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }
                       DatagramSocket socket = new DatagramSocket(CALLING_PORT);
                       socket.setBroadcast(true);
                       byte[] buf = new byte[1024];
                       DatagramPacket packet = new DatagramPacket(buf, buf.length);
                       socket.receive(packet);
                       String call_packet = new String(packet.getAddress().getHostAddress());
                       Log.d(TAG, "I am getting call from." + call_packet);
                       Intent intent = new Intent(context,CallReceive.class);
                       intent.putExtra("IP",call_packet);
                       context.startActivity(intent);
                       socket.disconnect();
                       socket.close();
                   }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        };

        thread.start();

    }




    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

}
