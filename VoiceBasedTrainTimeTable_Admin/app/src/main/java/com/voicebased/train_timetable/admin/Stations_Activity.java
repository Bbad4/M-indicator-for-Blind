package com.voicebased.train_timetable.admin;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class Stations_Activity extends AppCompatActivity {

    RelativeLayout relativeLayout;
    ListView listView;
    FloatingActionButton floatingActionButton;
    Dialog mDialog;

    ArrayList<String> SidArray, SNameArray, LidArray, PCountArray, MajorArray;
    SharedPreferences pref;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.station_layout);

        relativeLayout = (RelativeLayout) findViewById(R.id.station_screen);
        listView = (ListView) findViewById(R.id.station_list);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.addFloatButton);

        mDialog = new Dialog(Stations_Activity.this, R.style.AppTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.circular_dialog);
        mDialog.setCancelable(false);


        getSupportActionBar().setTitle("Stations");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // For opening the Add Station Screen on floating button click
                Intent intent = new Intent(Stations_Activity.this, AddStation_Activity.class);
                startActivity(intent);


            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        new getStationTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    public class getStationTask extends AsyncTask<String, JSONObject, String> {

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
                JSONObject json = api.getStations();
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


            SidArray = new ArrayList<String>();
            SNameArray = new ArrayList<String>();
            LidArray = new ArrayList<String>();
            PCountArray = new ArrayList<String>();
            MajorArray = new ArrayList<String>();

            mDialog.dismiss();

            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(Stations_Activity.this);
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

                    if (StatusValue.compareTo("no") == 0) {
                        Toast.makeText(Stations_Activity.this, "No Stations added", Toast.LENGTH_SHORT).show();
                        listView.setAdapter(null);

                    } else if (StatusValue.compareTo("ok") == 0) {

                        JSONArray result = json.getJSONArray("Data");
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject res = result.getJSONObject(i);

                            //Sid,Sname,Lid,Pcount,major
                            SidArray.add(res.getString("data0"));
                            SNameArray.add(res.getString("data1"));
                            LidArray.add(res.getString("data2"));
                            PCountArray.add(res.getString("data3"));
                            MajorArray.add(res.getString("data4"));

                        }

                        StationAdapter adapt = new StationAdapter(Stations_Activity.this, SNameArray);
                        listView.setAdapter(adapt);
                    }

                } catch (Exception e) {
                    Toast.makeText(Stations_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
    }


    public class StationAdapter extends ArrayAdapter<String> {

        Context con;
        ArrayList<String> dataset;

        public StationAdapter(Context context, ArrayList<String> data) {
            super(context, R.layout.station_list_row, data);
            con = context;
            dataset = data;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = LayoutInflater.from(con).inflate(R.layout.station_list_row, null, true);

            TextView LineName = (TextView) v.findViewById(R.id.station_name);
            LineName.setText(SNameArray.get(position));


            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Sending value from here to UpdateStation screen using Intent
//                    string id,string sname, string pcount, string major
                    Intent intent = new Intent(Stations_Activity.this, UpdateStation_Activity.class);
                    intent.putExtra("Sid", SidArray.get(position));
                    intent.putExtra("SName", SNameArray.get(position));
                    intent.putExtra("PCount", PCountArray.get(position));
                    intent.putExtra("Pmajor", MajorArray.get(position));
                    intent.putExtra("Lid", LidArray.get(position));
                    startActivity(intent);

                }
            });

            return v;
        }


    }


}
