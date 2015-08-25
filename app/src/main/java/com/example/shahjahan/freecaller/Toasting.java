package com.example.shahjahan.freecaller;

import android.content.Context;
import android.widget.Toast;


/**
 * Created by shahjahan on 25/8/15.
 */
public class Toasting {
    String TAG = "FREECALLER_TOAST";

    public void MakeText(String message,Context context)
    {
        Toast.makeText(context,message,Toast.LENGTH_LONG).show();

    }


}
