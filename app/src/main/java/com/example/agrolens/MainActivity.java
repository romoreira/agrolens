package com.example.agrolens;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {

    private ImageView myImage;
    private Button button;
    private Button check;
    private ProgressBar spinner;
    private TextView uploadedImageView;
    private TextView texto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myImage = (ImageView) findViewById(R.id.myImage);
        button = (Button) findViewById(R.id.button);
        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
        uploadedImageView = (TextView) findViewById(R.id.uploadedWebView);
        texto = (TextView) findViewById(R.id.uploadedWebView);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
    }
    private void takePicture(){
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Bitmap b = (Bitmap) data.getExtras().get("data");
            myImage.setImageBitmap(b);

            check = (Button) findViewById(R.id.check);
            check.setVisibility(View.VISIBLE);

            check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("Info", "Button Pressed to send image"+b);
                    spinner.setVisibility(View.VISIBLE);
                    UploadImageToServer uploadImageToServer = new UploadImageToServer("http://192.168.137.2:8080/aioracle",b);
                    uploadImageToServer.execute();
                    //https://androidkk.com/index.php/2021/02/28/how-to-easily-upload-image-to-the-server-programmatically-in-android/
                }
            });

        }
    }
    private class UploadImageToServer extends AsyncTask<String, String, String>{
        String requesturl = "";
        Bitmap bitmap = null;
        UploadImageToServer(String requestUrl, Bitmap bitmap){
            this.requesturl = requestUrl;
            this.bitmap = bitmap;
        }

        @Override
        protected String doInBackground(String... strings) {
            if(!this.requesturl.equals("")){
                Log.i("Info", "Creating HTTP helper to send: "+bitmap);
                HttpRequestHelper httpRequestHelper = new HttpRequestHelper(requesturl);
                httpRequestHelper.addFilePart("image_name", bitmap);
                String response = httpRequestHelper.finish();
                Log.i("Info", "RESPONSE: "+response);
                return response;
            }
            return "";
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("Info" , "PostExecute"+(result.length()));
            if(!result.equals("0") && !result.equals(null) ){
                spinner.setVisibility(View.GONE);
                texto = (TextView) findViewById(R.id.uploadedWebView);
                texto.setText(result);
                WebView uploadedImageView = (WebView) findViewById(R.id.WebView2);
                uploadedImageView.loadUrl("http://192.168.137.2:8080/aioracle"+result);
                Toast.makeText(MainActivity.this, "Image is uploaded", Toast.LENGTH_SHORT).show();

            }else{
                Log.i("Info", "Error to send");
                texto.setText("Try Again");
                Toast.makeText(MainActivity.this, "Image is not uploaded", Toast.LENGTH_SHORT).show();
            }
        }
    }
}