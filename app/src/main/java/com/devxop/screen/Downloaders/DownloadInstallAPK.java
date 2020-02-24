package com.devxop.screen.Downloaders;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.devxop.screen.Helper.StorageManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadInstallApk extends AsyncTask<String, String, File> {

    /**
     * Before starting background thread Show Progress Bar Dialog
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //showDialog(progress_bar_type);
    }

    /**
     * Downloading file in background thread
     */
    @Override
    protected File doInBackground(String... f_url) {

        int count = 0;
        //Toast.makeText(getApplicationContext(),"Download video...",Toast.LENGTH_LONG).show();
        try {
            URL url = new URL(f_url[0]);
            URLConnection conection = url.openConnection();
            conection.connect();


            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);

            File dir = Environment.getExternalStorageDirectory();
            String path = dir.getAbsolutePath();

            Log.d("PATH FILE: ", path);

            if (dir.exists()) {
                File from = new File(dir, "devxop.apk");
                Log.d("FILE CHECK", from.getAbsolutePath());
                if (from.exists()) {
                    from.delete();
                }

            }

            // Output stream
            OutputStream output = new FileOutputStream(path.toString()
                    + "/devxop.apk");

            byte data[] = new byte[1024];

            long total = 0;
            int increment = 10;
            while ((count = input.read(data)) != -1) {
                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();


            File file = new File(dir, "devxop.apk");
            return file;


        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }
        return null;
    }

    /**
     * Updating progress bar
     */
    protected void onProgressUpdate(String... progress) {
        // setting progress percentage
        //pDialog.setProgress(Integer.parseInt(progress[0]));
    }

    /**
     * After completing background task Dismiss the progress dialog
     **/
    @Override
    protected void onPostExecute(File file) {
        // dismiss the dialog after the file was downloaded
        //dismissDialog(progress_bar_type);

        /*Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, 0);*/

    }

}