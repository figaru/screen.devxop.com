package com.devxop.screen.Helper;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by admin on 19/05/2017.
 */

public class StorageManager {

    public StorageManager(){

    }

    public static void Set(Context context, String key, String data){
        try{
            FileOutputStream fOut = context.openFileOutput(key ,MODE_PRIVATE);
            fOut.write(data.getBytes());
            fOut.close();
            //Toast.makeText(getBaseContext(),"file saved",Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String Get(Context context, String key){
        try {
            FileInputStream fin = context.openFileInput(key);
            int c;
            String temp="";
            while( (c = fin.read()) != -1){
                temp = temp + Character.toString((char)c);
            }
            //tv.setText(temp);
            return  temp;
            //Toast.makeText(getBaseContext(),"file read",Toast.LENGTH_SHORT).show();
        }
        catch(Exception e){

        }

        return "";
    }

}
