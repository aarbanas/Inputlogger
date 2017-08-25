package com.example.okey.okeylogger;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.net.HttpURLConnection;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Sandi on 9.3.2016..
 */
public class SendTask extends AsyncTask<String, String, String> {

    String urlAddress;
    String logFile;
    String logFileName;

    private HttpURLConnection conn;
    public static final int CONNECTION_TIMEOUT = 15 * 1000;
    private NetworkOperationFinished myNetworkOpeartionListener;
    String finalResponse="";

    // Constructor
    public SendTask(String url, String logFile, String logFileName) {
        this.urlAddress = url;
        this.logFile = logFile;
        this.logFileName = logFileName;
    }


    public interface NetworkOperationFinished {
        void onNetworkOperationFinished(String response);
    }

    public void setNetworkOperationFinished(NetworkOperationFinished inputListener){
        this.myNetworkOpeartionListener = inputListener;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // do stuff before posting data
    }


    @Override
    protected String doInBackground(String... strings) {
        try {
            postData_okhttp();
            //postData();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(String lenghtOfFile) {
        // do stuff after posting data
        System.out.println("Sent from Android...");

        // tell parent activity that network operation finished!
        if (myNetworkOpeartionListener != null)
            myNetworkOpeartionListener.onNetworkOperationFinished(finalResponse);
    }

    // Method that sends data (in background)
    private void postData_okhttp() {
        finalResponse="";

        try {
            final MediaType MEDIA_TYPE_CSV = MediaType.parse("text/csv");
            final OkHttpClient client = new OkHttpClient();


            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("applicationid", "InputLogger")
                    .addFormDataPart("logFile", logFileName,
                            RequestBody.create(MEDIA_TYPE_CSV,
                                    new File(logFile)))
                    .build();

            Request request = new Request.Builder()
                    .url(urlAddress)
                    .post(requestBody)
                    .build();

            Response response = client.newCall(request).execute();
        //    Log.d("Response", response.body().string());
            if (!response.isSuccessful()) {
                finalResponse="";
                return;
            }

            String feedback = response.body().string();

            System.out.println("Server Response: " + feedback.toString());
            Thread.sleep(1000);

            if (feedback.toString().equals("200"))
            {
                finalResponse=feedback.toString();
            }


        } catch (Exception ex) {
            finalResponse="";
        }
    }

}


