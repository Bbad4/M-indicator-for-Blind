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
import android.os.Handler;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;



public class Trains_Activity extends AppCompatActivity {

    RelativeLayout relativeLayout;
    ListView listView;
    FloatingActionButton floatingActionButton;
    Dialog mDialog;

    ArrayList<String> TidArray, SourceArray, DestinationArray, LidArray, STimeArray, DTimeArray, TypeArray, CoachArray;
    SharedPreferences pref;
    int Trainposition = -1;

    ArrayList<String> SelectedStationNameArray,SelectedPlatformArray,SelectedTimeArray;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.station_layout);

        relativeLayout = (RelativeLayout) findViewById(R.id.station_screen);
        listView = (ListView) findViewById(R.id.station_list);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.addFloatButton);

        mDialog = new Dialog(Trains_Activity.this, R.style.AppTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.circular_dialog);
        mDialog.setCancelable(false);

        getSupportActionBar().setTitle("Trains");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // For calling Add Train screen
                Intent intent = new Intent(Trains_Activity.this, AddTrain_Activity1.class);
                startActivity(intent);

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        new getTrainsTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // On back button click on header , close current screen
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    public class getTrainsTask extends AsyncTask<String, JSONObject, String> {

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
                JSONObject json = api.getATrains();
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


            TidArray = new ArrayList<String>();
            SourceArray = new ArrayList<String>();
            DestinationArray = new ArrayList<String>();
            LidArray = new ArrayList<String>();
            STimeArray = new ArrayList<String>();
            DTimeArray = new ArrayList<String>();
            TypeArray = new ArrayList<String>();
            CoachArray = new ArrayList<String>();

            mDialog.dismiss();

            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(Trains_Activity.this);
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
                        Toast.makeText(Trains_Activity.this, "No Trains added", Toast.LENGTH_SHORT).show();
                        listView.setAdapter(null);

                    } else if (StatusValue.compareTo("ok") == 0) {

                        JSONArray result = json.getJSONArray("Data");
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject res = result.getJSONObject(i);

                            //tid,source,dest,lid,stime,dtime,type,coach
                            TidArray.add(res.getString("data0"));
                            SourceArray.add(res.getString("data1"));
                            DestinationArray.add(res.getString("data2"));
                            LidArray.add(res.getString("data3"));
                            STimeArray.add(res.getString("data4"));
                            DTimeArray.add(res.getString("data5"));
                            TypeArray.add(res.getString("data6"));
                            CoachArray.add(res.getString("data7"));

                        }

                        TrainAdapter adapt = new TrainAdapter(Trains_Activity.this, SourceArray);
                        listView.setAdapter(adapt);
                    }

                } catch (Exception e) {
                    Toast.makeText(Trains_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
    }


    public class TrainAdapter extends ArrayAdapter<String> {

        Context con;
        ArrayList<String> dataset;

        public TrainAdapter(Context context, ArrayList<String> data) {
            super(context, R.layout.train_list_row, data);
            con = context;
            dataset = data;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = LayoutInflater.from(con).inflate(R.layout.train_list_row, null, true);

            TextView SourceText = (TextView) v.findViewById(R.id.source_station);
            TextView DestinationText = (TextView) v.findViewById(R.id.destination_station);

            Button EditBtn = (Button) v.findViewById(R.id.edit_btn);
            Button DeleteBtn = (Button) v.findViewById(R.id.delete_btn);


            DeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Confirmation before train deletion
                    new AlertDialog.Builder(Trains_Activity.this).setTitle("Delete")
                            .setMessage("Are you sure to Delete ?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

//                                   string Trainid
                                    new DeleteTrainTask().execute(TidArray.get(position));
                                }

                            })
                            .setNegativeButton(android.R.string.no, null).show();
                }
            });

            //tid,source,dest,lid,stime,dtime,type,coach
            SourceText.setText(SourceArray.get(position));
            DestinationText.setText(DestinationArray.get(position));

           EditBtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {

                   Trainposition = position;
                   new geTrainStationDetail_Task().execute(TidArray.get(position));
               }
           });

            return v;
        }

    }


    public class DeleteTrainTask extends AsyncTask<String, JSONObject, String> {

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
                JSONObject json = api.DeleteTrains(params[0]);
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
            mDialog.dismiss();


            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(Trains_Activity.this);
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

                    if (StatusValue.compareTo("true") == 0) {
                        Toast.makeText(Trains_Activity.this, "Train Deleted Successfully", Toast.LENGTH_SHORT).show();

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                new getTrainsTask().execute();

                            }
                        }, 1000);

                    }


                } catch (Exception e) {
                    Toast.makeText(Trains_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }


        }
    }


    public class geTrainStationDetail_Task extends AsyncTask<String, JSONObject, String> {

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
                JSONObject json = api.getTrainsStationsInfo(params[0]);
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

            SelectedStationNameArray = new ArrayList<String>();
            SelectedPlatformArray = new ArrayList<String>();
            SelectedTimeArray = new ArrayList<String>();

            mDialog.dismiss();

            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(Trains_Activity.this);
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
                        Toast.makeText(Trains_Activity.this, "No Stations Found", Toast.LENGTH_SHORT).show();
                        listView.setAdapter(null);

                    } else if (StatusValue.compareTo("ok") == 0) {

                        JSONArray result = json.getJSONArray("Data");
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject res = result.getJSONObject(i);

                            //stations,platforms,time
                            SelectedStationNameArray.add(res.getString("data0"));
                            SelectedPlatformArray.add(res.getString("data1"));
                            SelectedTimeArray.add(res.getString("data2"));
                        }

                        //tid,source,dest,lid,stime,dtime,type,coach
                        Intent  intent = new Intent(Trains_Activity.this,UpdateTrain_Activity1.class);
                        intent.putExtra("TrainId",TidArray.get(Trainposition));
                        intent.putExtra("SourceName",SourceArray.get(Trainposition));
                        intent.putExtra("DestinationName",DestinationArray.get(Trainposition));
                        intent.putExtra("LineId",LidArray.get(Trainposition));
                        intent.putExtra("STime",STimeArray.get(Trainposition));
                        intent.putExtra("DTime",DTimeArray.get(Trainposition));
                        intent.putExtra("TrainType",TypeArray.get(Trainposition));
                        intent.putExtra("Coach",CoachArray.get(Trainposition));

                        intent.putStringArrayListExtra("SelectedStationNameArray",SelectedStationNameArray);
                        intent.putStringArrayListExtra("SelectedPlatformNameArray",SelectedPlatformArray);
                        intent.putStringArrayListExtra("SelectedTimeArray",SelectedTimeArray);
                        startActivity(intent);

                    }

                } catch (Exception e) {
                    Toast.makeText(Trains_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
    }

}
