package com.voicebased.train_timetable.admin;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ButtonBarLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;



public class UpdateStation_Activity extends AppCompatActivity {

    RelativeLayout relativeLayout;
    EditText StationNameText, TotalPlatformText;
    Spinner LineTypeSpinner;
    RadioGroup radioGroup;
    RadioButton RadioYes, Radiono;
    Button EditStationBtn;
    Dialog mDialog;

    String SelectedLine = "", MajorTypeText = "Na";
    ArrayList<String> LineIdArray, LineNameArray;
    String sid, sName, pCount, pMajor, lid;

    LinearLayout ButtonLayout;
    Button CancelBtn, UpdateBtn;
    boolean isEditable = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_station_layout);

        mDialog = new Dialog(UpdateStation_Activity.this, R.style.AppTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.circular_dialog);
        mDialog.setCancelable(false);

        getSupportActionBar().setTitle("Update Station");
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
        MenuInflater mi = new MenuInflater(UpdateStation_Activity.this);
        mi.inflate(R.menu.delete_option, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Confimation for station deletion in menu inflator
        if (item.getItemId() == R.id.delete_option) {

            AlertDialog.Builder ad = new AlertDialog.Builder(UpdateStation_Activity.this);
            ad.setTitle("Delete!");
            ad.setMessage("Are You Sure You Want to Delete?");
            ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    new DeleteStationTask().execute(sid);
                    dialog.cancel();
                }
            });
            ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            ad.show();

        }


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
        EditStationBtn = (Button) findViewById(R.id.addStationBtn);

        ButtonLayout = (LinearLayout) findViewById(R.id.button_layout);
        CancelBtn = (Button) findViewById(R.id.cancel_btn);
        UpdateBtn = (Button) findViewById(R.id.UpdateBtn);

        // receiving value from Intent
        // and based on values it will automatically fill Text filed,select spinner,select radio button
        Intent intent = getIntent();
        sid = intent.getStringExtra("Sid");
        sName = intent.getStringExtra("SName");
        pCount = intent.getStringExtra("PCount");
        pMajor = intent.getStringExtra("Pmajor");
        lid = intent.getStringExtra("Lid");

        StationNameText.setText(sName);
        TotalPlatformText.setText(pCount);

        if (pMajor.compareTo("yes") == 0) {
            RadioYes.setChecked(true);
        } else {
            Radiono.setChecked(true);
        }


        EditStationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isEditable = true;
                isEditable();
            }
        });

        if (isEditable) {
            isEditable();
        } else {
            isNotEditable();
        }

        CancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isEditable = false;
                isNotEditable();
            }
        });


        LineTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SelectedLine = LineIdArray.get(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        UpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (checkCriteria()) {

                    if (StationNameText.getText().toString().equals("")) {
                        Snackbar.make(relativeLayout, "Station Name is required", Snackbar.LENGTH_SHORT).show();
                        StationNameText.requestFocus();

                    } else if (TotalPlatformText.getText().toString().equals("")) {
                        Snackbar.make(relativeLayout, "Number of Platform is required", Snackbar.LENGTH_SHORT).show();
                        TotalPlatformText.requestFocus();

                    } else {

                        if (radioGroup.getCheckedRadioButtonId() == R.id.radio_yes) {
                            MajorTypeText = "yes";

                        } else if (radioGroup.getCheckedRadioButtonId() == R.id.radio_no) {
                            MajorTypeText = "no";

                        }
//                        string id,string sname, string pcount, string major

                        new UpdateStationTask().execute(sid, StationNameText.getText().toString().trim(),
                                TotalPlatformText.getText().toString().trim(), MajorTypeText);

                    }
                } else {
                    new AlertDialog.Builder(UpdateStation_Activity.this)
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
                AlertDialog.Builder ad = new AlertDialog.Builder(UpdateStation_Activity.this);
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
                        Toast.makeText(UpdateStation_Activity.this, "No lines added", Toast.LENGTH_SHORT).show();


                    } else if (StatusValue.compareTo("ok") == 0) {

                        JSONArray result = json.getJSONArray("Data");
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject res = result.getJSONObject(i);

                            LineIdArray.add(res.getString("data0"));
                            LineNameArray.add(res.getString("data1"));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(UpdateStation_Activity.this, R.layout.spinner_textview, LineNameArray);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        LineTypeSpinner.setAdapter(adapter);

                        for (int i = 0; i < LineIdArray.size(); i++) {

                            if (lid.compareTo(LineIdArray.get(i)) == 0) {
                                LineTypeSpinner.setSelection(i);
                            }
                        }

                    }

                } catch (Exception e) {
                    Toast.makeText(UpdateStation_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
    }


    public class UpdateStationTask extends AsyncTask<String, JSONObject, String> {

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
                JSONObject json = api.updateStations(params[0], params[1], params[2], params[3]);
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

//            Toast.makeText(UpdateStation_Activity.this, s, Toast.LENGTH_SHORT).show();

            if (s.contains("Unable to resolve host")) {
                // If internet is not there, it will show Alert Dialog
                AlertDialog.Builder ad = new AlertDialog.Builder(UpdateStation_Activity.this);
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
                        Toast.makeText(UpdateStation_Activity.this, "Station Detail Updated Successfully", Toast.LENGTH_SHORT).show();

                        // After station update it will wait for 1 second, then automatically close
                        // current page
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 1000);

                    } else if (StatusValue.compareTo("already") == 0) {
                        Toast.makeText(UpdateStation_Activity.this, "Staion Name already exist", Toast.LENGTH_SHORT).show();

                    }


                } catch (Exception e) {
                    Toast.makeText(UpdateStation_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }


        }
    }


    public class DeleteStationTask extends AsyncTask<String, JSONObject, String> {

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
                JSONObject json = api.DeleteStation(params[0]);
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
                AlertDialog.Builder ad = new AlertDialog.Builder(UpdateStation_Activity.this);
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
                        Toast.makeText(UpdateStation_Activity.this, "Station Deleted Successfully", Toast.LENGTH_SHORT).show();

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 1000);

                    }


                } catch (Exception e) {
                    Toast.makeText(UpdateStation_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }


        }
    }


    // If user not click edit button so, all the fileds are disable and after click edit button
    // options are enable for editing
    public void isEditable() {

        StationNameText.setEnabled(true);
        TotalPlatformText.setEnabled(true);
        RadioYes.setEnabled(true);
        Radiono.setEnabled(true);
        LineTypeSpinner.setEnabled(false);

        ButtonLayout.setVisibility(View.VISIBLE);
        EditStationBtn.setVisibility(View.GONE);
    }

    public void isNotEditable() {

        StationNameText.setEnabled(false);
        TotalPlatformText.setEnabled(false);
        RadioYes.setEnabled(false);
        Radiono.setEnabled(false);
        LineTypeSpinner.setEnabled(false);

        ButtonLayout.setVisibility(View.GONE);
        EditStationBtn.setVisibility(View.VISIBLE);

    }


}
