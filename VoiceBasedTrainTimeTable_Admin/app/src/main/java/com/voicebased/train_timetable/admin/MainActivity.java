package com.voicebased.train_timetable.admin;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    CardView LineOption,StationOption,TrainOption,LogoutOption;
    RelativeLayout relativeLayout;
    Dialog mDialog;

    ArrayList<String> data;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDialog = new Dialog(MainActivity.this, R.style.AppTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.circular_dialog);
        mDialog.setCancelable(false);

        getSupportActionBar().hide();


        LineOption = (CardView) findViewById(R.id.line_option);
        StationOption = (CardView) findViewById(R.id.station_option);
        TrainOption = (CardView) findViewById(R.id.train_option);
        LogoutOption = (CardView) findViewById(R.id.logout_option);

        relativeLayout = (RelativeLayout) findViewById(R.id.main_screen);

        pref = getSharedPreferences("VoiceBasedTT", Context.MODE_PRIVATE);
        String userId_pref = pref.getString("UserId", "");



        LineOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this,Lines_Activity.class);
                startActivity(intent);

            }
        });

        StationOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (data.size()>0){

                    Intent intent = new Intent(MainActivity.this,Stations_Activity.class);
                    startActivity(intent);

                }else {
                    Toast.makeText(MainActivity.this, "Lines not added", Toast.LENGTH_SHORT).show();
                }

            }
        });

        TrainOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // If line is not added, so admin can't add train
                if (data.size()>0){

                    Intent intent = new Intent(MainActivity.this,Trains_Activity.class);
                    startActivity(intent);

                }else {
                    Toast.makeText(MainActivity.this, "Lines not added", Toast.LENGTH_SHORT).show();
                }

            }
        });


        LogoutOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Clear the shared prefrence value and redirect to Login page
                SharedPreferences pref = getSharedPreferences("VoiceBasedTT", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        //calling the Line listing API using Async Task
        new getLinesTask().execute();
    }


    public class getLinesTask extends AsyncTask<String, JSONObject, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            RestAPI api = new RestAPI();
            try {
                JSONObject json = api.getLine();
                JSONPARSE jp = new JSONPARSE();
                a = jp.parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            data = new ArrayList<String>();
            mDialog.dismiss();

            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
                ad.setTitle("Unable to Connect!");
                ad.setMessage("Check your Internet Connection,Unable to connect the Server");
                ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                ad.show();

            } else {

                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");

                    if (StatusValue.compareTo("no")==0){
                        Toast.makeText(MainActivity.this, "No lines added", Toast.LENGTH_SHORT).show();

                    }else if (StatusValue.compareTo("ok")==0){

                        JSONArray result = json.getJSONArray("Data");
                        for (int i=0;i<result.length();i++){
                            JSONObject res = result.getJSONObject(i);

                            String FinalData = res.getString("data0")+"*"+res.get("data1");
                            data.add(FinalData);
                        }

                    }

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
    }




}
