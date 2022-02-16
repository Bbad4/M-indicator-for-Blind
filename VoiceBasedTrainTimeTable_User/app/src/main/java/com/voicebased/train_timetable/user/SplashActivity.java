package com.voicebased.train_timetable.user;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    GPS_Tracker gps_tracker;
    RelativeLayout relativeLayout;
    MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);

        relativeLayout = (RelativeLayout) findViewById(R.id.splash_screen);
        gps_tracker = new GPS_Tracker(SplashActivity.this, SplashActivity.this);

        // this is used for runtime permission
        Boolean ans = weHavePermission();
        if (!ans) {
            requestforPermissionFirst();
        }

        // caling the text to speech funtion
        Speech_1("Welcome to I-CAN");


        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                CheckLocation();
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    public void Speech_1(String text) {

        new Speech1().execute(text);
    }

    public class Speech1 extends AsyncTask<String, Void, MediaPlayer> {

        String a = "back";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected MediaPlayer doInBackground(String... params) {

            try {
                String Url = "https://translate.google.com/translate_tts?ie=UTF-8";
                String pronouce = "&q=" + params[0].replaceAll(" ", "%20");
                String language = "&tl=hi";
                String web = "&client=tw-ob";

                String fullUrl = Url + pronouce + language + web;
                Uri uri = Uri.parse(fullUrl);
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(SplashActivity.this, uri);
//                mediaPlayer.prepare();
//                mediaPlayer.start();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {

                        mediaPlayer.start();
                    }
                });
                mediaPlayer.prepareAsync();

            } catch (Exception e) {

                mediaPlayer=null;
            }

            return mediaPlayer;
        }

        @Override
        protected void onPostExecute(final MediaPlayer s) {
            super.onPostExecute(s);

            if(s!=null)
            {
                s.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        s.stop();
                        s.reset();

                        CheckLocation();

                    }
                });
            }
        }
    }


    // this is used for checking the internet is connected or not

    public void CheckLocation(){

        if (gps_tracker.canGetLocation()) {

            double Lat = gps_tracker.getLatitude();
            double Lng = gps_tracker.getLongitude();

            if (Lat != 0 && Lng != 0) {

                getAddress(Lat,Lng);

            } else {
//                Speech_1("Determining Your cordinates,Tap on the screen for retry");
                Toast.makeText(this, "Determining Your cordinates,Tap on the screen for retry", Toast.LENGTH_SHORT).show();
            }
        } else {
//            Speech_1("Enable Your GPS(Location)");
            Toast.makeText(this, "Enable Your GPS(Location)", Toast.LENGTH_SHORT).show();
        }

    }

   // this funtion is used for getting the station name based on Location[Lat,Lng]
    public void getAddress(double Lat,double Lng) {

        Geocoder geocoder = new Geocoder(SplashActivity.this, Locale.getDefault());
        String result = null;
        try {
            List<Address> addressList = geocoder.getFromLocation(Lat, Lng, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append("\n");
                }

                sb.append(address.getAdminArea()).append("\n");
                sb.append(address.getSubAdminArea()).append("\n");
                sb.append(address.getLocality()).append("\n");
                sb.append(address.getSubLocality()).append("\n");
                sb.append(address.getPostalCode()).append("\n");
                sb.append(address.getCountryName());
                result = sb.toString();

                String PlaceDetail = address.getSubLocality();

                try {
                    if (PlaceDetail.compareTo("") == 0 ) {
                        PlaceDetail = "Na";
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        intent.putExtra("PlaceDetail", PlaceDetail);
                        startActivity(intent);

                    } else {

                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        intent.putExtra("PlaceDetail", PlaceDetail);
                        startActivity(intent);
                    }

                }catch(Exception e){

                    PlaceDetail = "Na";
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.putExtra("PlaceDetail", PlaceDetail);
                    startActivity(intent);
                }



            }
        } catch (Exception e) {
            Toast.makeText(SplashActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //Android Runtime Permission
    private boolean weHavePermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestforPermissionFirst() {
        if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) || (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))) {
            requestForResultContactsPermission();
        } else {
            requestForResultContactsPermission();
        }
    }

    private void requestForResultContactsPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 111);
    }


}
