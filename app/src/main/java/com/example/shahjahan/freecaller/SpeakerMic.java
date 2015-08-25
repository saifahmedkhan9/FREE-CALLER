package com.example.shahjahan.freecaller;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by shahjahan on 25/8/15.
 */
public class SpeakerMic {
    private static final int SAMPLE_RATE = 8000; // Hertz
    private static final int SAMPLE_INTERVAL = 20; // Milliseconds
    private static final int SAMPLE_SIZE = 2; // Bytes
    private static final int BUF_SIZE = SAMPLE_INTERVAL * SAMPLE_INTERVAL * SAMPLE_SIZE * 2; //Bytes
    private InetAddress address; // Address to call
    //    private int port = 50000; // Port the packets are addressed to
    public boolean mic = false; // Enable mic?
    public boolean speakers = false; // Enable speakers?

    String TAG = "FREECALLER_SPEAKER";
    private int port=50015;
    Context context;
    SpeakerMic(Context context)
    {

        this.context = context;
    }


    public void startSpeakers() {
        // Creates the thread for receiving and playing back audio
        if(!speakers) {

            speakers = true;
            Thread receiveThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    // Create an instance of AudioTrack, used for playing back audio
                    Log.i(TAG, "Receive thread started. Thread id: " + Thread.currentThread().getId());
                    AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT, BUF_SIZE, AudioTrack.MODE_STREAM);
                    track.play();
                    try {
                        // Define a socket to receive the audio
                        DatagramSocket socket = new DatagramSocket(port);
                        byte[] buf = new byte[BUF_SIZE];
                        while(speakers) {
                            // Play back the audio received from packets
                            DatagramPacket packet = new DatagramPacket(buf, BUF_SIZE);
                            socket.receive(packet);
                            Log.i(TAG, "Packet received: " + packet.getLength());
                            track.write(packet.getData(), 0, BUF_SIZE);
                        }
                        // Stop playing back and release resources
                        socket.disconnect();
                        socket.close();
                        track.stop();
                        track.flush();
                        track.release();
                        speakers = false;
                        return;
                    }
                    catch(SocketException e) {

                        Log.e(TAG, "SocketException: " + e.toString());
                        speakers = false;
                    }
                    catch(IOException e) {

                        Log.e(TAG, "IOException: " + e.toString());
                        speakers = false;
                    }
                }
            });
            receiveThread.start();
        }
    }

    public String getIPAddress()
    {
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
        Log.d(TAG,"the ip address is : " + ip);
        return ip;
    }


    public void startMic(final String IP_Address) {
        // Creates the thread for capturing and transmitting audio
        mic = true;
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                // Create an instance of the AudioRecord class
                Log.i(TAG, "Send thread started. Thread id: " + Thread.currentThread().getId());
                AudioRecord audioRecorder = new AudioRecord (MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)*10);
                int bytes_read = 0;
                int bytes_sent = 0;
                byte[] buf = new byte[BUF_SIZE];
                try {
                    // Create a socket and start recording
//                    Log.i(TAG, "Packet destination: " + address.toString());
                    DatagramSocket socket = new DatagramSocket();
                    audioRecorder.startRecording();
                    while(mic) {
                        // Capture audio from the mic and transmit it

                        if ( IP_Address.contains(getIPAddress()))
                        {
                            Log.i(TAG,"Same ip here.");
                            continue;
                        }
                        bytes_read = audioRecorder.read(buf, 0, BUF_SIZE);
                        DatagramPacket packet = new DatagramPacket(buf, bytes_read, InetAddress.getByName(IP_Address), port);
                        socket.send(packet);
                        bytes_sent += bytes_read;
                        Log.i(TAG, "Total bytes sent: " + bytes_sent);
                        Thread.sleep(SAMPLE_INTERVAL, 0);
                    }
                    // Stop recording and release resources
                    audioRecorder.stop();
                    audioRecorder.release();
                    socket.disconnect();
                    socket.close();
                    mic = false;
                    return;
                }
                catch(InterruptedException e) {

                    Log.e(TAG, "InterruptedException: " + e.toString());
                    mic = false;
                }
                catch(SocketException e) {

                    Log.e(TAG, "SocketException: " + e.toString());
                    mic = false;
                }
                catch(UnknownHostException e) {

                    Log.e(TAG, "UnknownHostException: " + e.toString());
                    mic = false;
                }
                catch(IOException e) {

                    Log.e(TAG, "IOException: " + e.toString());
                    mic = false;
                }
            }
        });
        thread.start();
    }



}
