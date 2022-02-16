package com.voicebased.train_timetable.admin;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;



public class AddStation_Activity extends AppCompatActivity {

    RelativeLayout relativeLayout;
    EditText StationNameText, TotalPlatformText;
    Spinner LineTypeSpinner;
    RadioGroup radioGroup;
    RadioButton RadioYes, Radiono;
    Button AddStationBtn;
    Dialog mDialog;

    String SelectedLine = "", MajorTypeText = "Na";
    ArrayList<String> LineIdArray, LineNameArray;
    ArrayList<String> StationIdArray,StationNameArray;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_station_layout);

        mDialog = new Dialog(AddStation_Activity.this, R.style.AppTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.circular_dialog);
        mDialog.setCancelable(false);


        // For Adding the title in action bar and display the back button
        getSupportActionBar().setTitle("Add Station");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        init();
    }


    @Override
    protected void onResume() {
        super.onResume();
        new getLinesTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // On back button click finish current activity
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    public void init() {

        relativeLayout = (RelativeLayout) findViewById(R.id.add_station_screen);
        StationNameText = (EditText) findViewById(R.id.stationName_Text);
        TotalPlatformText = (EditText) findViewById(R.id.totalPlatform_Text);
        LineTypeSpinner = (Spinner) findViewById(R.id.line_type_spinner);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        RadioYes = (RadioButton) findViewById(R.id.radio_yes);
        Radiono = (RadioButton) findViewById(R.id.radio_no);
        AddStationBtn = (Button) findViewById(R.id.addStationBtn);


        LineTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get selected Line Id [which are selected in spinner]
                SelectedLine = LineIdArray.get(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        AddStationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (checkCriteria()) {

                    if (StationNameText.getText().toString().equals("")) {
                        Snackbar.make(relativeLayout, "Station Name is required", Snackbar.LENGTH_SHORT).show();
                        StationNameText.requestFocus();

                    } else if (SelectedLine.compareTo("Select Lines") == 0) {
                        Snackbar.make(relativeLayout, "Line is required", Snackbar.LENGTH_SHORT).show();

                    } else if (TotalPlatformText.getText().toString().equals("")) {
                        Snackbar.make(relativeLayout, "Number of Platform is required", Snackbar.LENGTH_SHORT).show();
                        TotalPlatformText.requestFocus();

                    } else {

                        if (radioGroup.getCheckedRadioButtonId() == R.id.radio_yes) {
                            MajorTypeText = "yes";

                        } else if (radioGroup.getCheckedRadioButtonId() == R.id.radio_no) {
                            MajorTypeText = "no";

                        }
//                        string sname,string lid,string pcount,string major
                        new AddStationTask().execute(StationNameText.getText().toString().trim(),
                                SelectedLine, TotalPlatformText.getText().toString().trim(), MajorTypeText);

                    }
                } else {
                    new AlertDialog.Builder(AddStation_Activity.this)
                            .setMessage("All fields are mandatary. Please enter all details")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }


            }
        });

    }


    protected boolean checkCriteria() {
        boolean b = true;
        if ((StationNameText.getText().toString()).equals("")) {
            b = false;
        }
        return b;
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

            LineNameArray = new ArrayList<String>();
            LineIdArray = new ArrayList<String>();
            mDialog.dismiss();

            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(AddStation_Activity.this);
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
                        Toast.makeText(AddStation_Activity.this, "No lines added", Toast.LENGTH_SHORT).show();


                    } else if (StatusValue.compareTo("ok") == 0) {

                        JSONArray result = json.getJSONArray("Data");
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject res = result.getJSONObject(i);

                            LineIdArray.add(res.getString("data0"));
                            LineNameArray.add(res.getString("data1"));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddStation_Activity.this, R.layout.spinner_textview, LineNameArray);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        LineTypeSpinner.setAdapter(adapter);


                    }

                } catch (Exception e) {
                    Toast.makeText(AddStation_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
    }


    public class AddStationTask extends AsyncTask<String, JSONObject, String> {

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
                JSONObject json = api.AddStations(params[0], params[1], params[2], params[3]);
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
                AlertDialog.Builder ad = new AlertDialog.Builder(AddStation_Activity.this);
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
                        Toast.makeText(AddStation_Activity.this, "Station Added Successfully", Toast.LENGTH_SHORT).show();

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 1000);

                    } else if (StatusValue.compareTo("already") == 0) {
                        Toast.makeText(AddStation_Activity.this, "Staion Name already exist", Toast.LENGTH_SHORT).show();

                    }


                } catch (Exception e) {
                    Toast.makeText(AddStation_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }


        }
    }


}
