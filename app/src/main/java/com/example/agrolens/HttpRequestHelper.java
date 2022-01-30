package com.example.agrolens;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequestHelper {
    private HttpURLConnection httpConn;
    private DataOutputStream request;
    private final String boundary =  "*****";
    private final String crlf = "\r\n";
    private final String twoHyphens = "--";

    public HttpRequestHelper(String requestURL){
        try {
            // creates a unique boundary based on time stamp
            URL url = new URL(requestURL);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);

            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("Cache-Control", "no-cache");
            httpConn.setRequestProperty(
                    "Content-Type", "multipart/form-data;boundary=" + this.boundary);

            request = new DataOutputStream(httpConn.getOutputStream());
        }catch (Exception e){
            Log.e("Exception",e.toString());
        }
    }

    public void addFormField(String name, String value) {
        try {
            request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + this.crlf);
            request.writeBytes("Content-Type: text/plain; charset=UTF-8" + this.crlf);
            request.writeBytes(this.crlf);
            request.writeBytes(value + this.crlf);
            request.flush();
        }
        catch (Exception e){
            Log.e(getClass()+"Exception",e.toString());
        }
    }

    public void addFilePart(String fieldName, Bitmap inImage)
    {
        try {
            String fileName = "temporary.jpg";
            request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"" +
                    fieldName + "\";filename=\"" +
                    fileName + "\"" + this.crlf);
            request.writeBytes(this.crlf);
            ByteArrayOutputStream bytes1 = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes1);
            byte[] bytes = bytes1.toByteArray();
            request.write(bytes);
        }
        catch (Exception e){
            Log.e(getClass()+"Exception",e.toString());
        }
    }

    public String finish() {
        Log.i("Info", "Sending....");
        String response = "";
        try {
            request.writeBytes(this.crlf);
            request.writeBytes(this.twoHyphens + this.boundary +
                    this.twoHyphens + this.crlf);
            request.flush();
            request.close();
            int status = httpConn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                InputStream responseStream = new
                        BufferedInputStream(httpConn.getInputStream());
                BufferedReader responseStreamReader =
                        new BufferedReader(new InputStreamReader(responseStream));

                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                responseStreamReader.close();
                response = stringBuilder.toString().trim();
                httpConn.disconnect();
            } else {
                throw new IOException("Server returned non-OK status: " + status);
            }
        }
        catch(Exception e){
            Log.e(getClass()+"Exception",e.toString());
        }
        return response;
    }

}