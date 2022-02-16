package com.voicebased.train_timetable.admin;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;



public class AddTrain_Activity1 extends AppCompatActivity {

    RelativeLayout relativeLayout;
    TextView DepartureTimingText, ArrivalTimingText;
    ImageView DepartureTimeBtn, ArrivalTimeBtn;

    Spinner LineSpinner, CoachSpinner;
    RadioGroup RadioGroup;
    RadioButton RadioFastTrain, RadioSlowTrain;

    Dialog mDialog;
    String SelectedLine = "", TrainTypeText = "Na", SelectedCoach = "";
    ArrayList<String> LineIdArray, LineNameArray;

    Button AddTrainBtn;
    ListView StationList;
    ArrayList<String>  CheckBoxValue, StationNameArray, StationIdArray;
    Spinner SourceStationSpinner, DestinationStationSpinner;
    String SourceStationId, DestinationStationId;

    ArrayList<String> FilterStationIdArray, FilterStationNameArray;
    ArrayList<String> SelectedStationNameArray,SelectedStationIdArray;
    ArrayList<Integer> PlatformCountArray;

    EditText SourcePlatform,DestinationPlatform;
    TextView SourceLimit,DestinationLimit;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_train_layout1);

        mDialog = new Dialog(AddTrain_Activity1.this, R.style.AppTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.circular_dialog);
        mDialog.setCancelable(false);

        getSupportActionBar().setTitle("Add Train");
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
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    public void init() {

        SourcePlatform = (EditText) findViewById(R.id.source_platform_text);
        DestinationPlatform = (EditText) findViewById(R.id.destination_platform_text);
        SourceLimit = (TextView) findViewById(R.id.source_limit);
        DestinationLimit = (TextView) findViewById(R.id.destination_limit);

        relativeLayout = (RelativeLayout) findViewById(R.id.add_train_screen);
        SourceStationSpinner = (Spinner) findViewById(R.id.source_station_spinner);
        DestinationStationSpinner = (Spinner) findViewById(R.id.destination_station_spinner);

        DepartureTimingText = (TextView) findViewById(R.id.Departure_timing);
        ArrivalTimingText = (TextView) findViewById(R.id.Arrival_timing);
        DepartureTimeBtn = (ImageView) findViewById(R.id.Departure_timing_btn);
        ArrivalTimeBtn = (ImageView) findViewById(R.id.Arrival_timing_btn);

        LineSpinner = (Spinner) findViewById(R.id.line_type_spinner);
        CoachSpinner = (Spinner) findViewById(R.id.coach_spinner);

        RadioGroup = (RadioGroup) findViewById(R.id.radio_group);
        RadioFastTrain = (RadioButton) findViewById(R.id.radio_fast);
        RadioSlowTrain = (RadioButton) findViewById(R.id.radio_slow);
        AddTrainBtn = (Button) findViewById(R.id.addTrainBtn);
        StationList = (ListView) findViewById(R.id.station_list);




        LineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Based on line selection call the station listing API
                SelectedLine = LineIdArray.get(position);
                new getStationTask().execute(LineIdArray.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        SourceStationId = SourceStationSpinner.getSelectedItemId() + "";
        DestinationStationId = DestinationStationSpinner.getSelectedItemId() + "";

        SourceStationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SourceStationId = StationIdArray.get(i);
                FilterStationIdArray = new ArrayList<String>();
                FilterStationNameArray = new ArrayList<String>();
                CheckBoxValue = new ArrayList<String>();

                SourceLimit.setText("Between 1 to "+PlatformCountArray.get(i));

                for (int j = 0; j < StationIdArray.size(); j++) {

                    if ((StationIdArray.get(j).compareTo(SourceStationId)) != 0 && (StationIdArray.get(j).compareTo(DestinationStationId)) != 0) {
                        FilterStationIdArray.add(StationIdArray.get(j));
                        FilterStationNameArray.add(StationNameArray.get(j));
                    }

                }

                if (FilterStationIdArray.size() > 0) {
                    for (int k = 0; k < FilterStationIdArray.size(); k++) {
                        CheckBoxValue.add("no");
                    }
                    StationAdapter adapt = new StationAdapter(AddTrain_Activity1.this, FilterStationNameArray, CheckBoxValue);
                    StationList.setAdapter(adapt);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        DestinationStationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DestinationStationId = StationIdArray.get(i);
                FilterStationIdArray = new ArrayList<String>();
                FilterStationNameArray = new ArrayList<String>();
                CheckBoxValue = new ArrayList<String>();

                DestinationLimit.setText("Between 1 to "+PlatformCountArray.get(i));

                for (int j = 0; j < StationIdArray.size(); j++) {

                    if ((StationIdArray.get(j).compareTo(SourceStationId)) != 0 && (StationIdArray.get(j).compareTo(DestinationStationId)) != 0) {
                        FilterStationIdArray.add(StationIdArray.get(j));
                        FilterStationNameArray.add(StationNameArray.get(j));
                    }

                }

                if (FilterStationIdArray.size() > 0) {
                    for (int k = 0; k < FilterStationIdArray.size(); k++) {
                        CheckBoxValue.add("no");
                    }
                    StationAdapter adapt = new StationAdapter(AddTrain_Activity1.this, FilterStationNameArray, CheckBoxValue);
                    StationList.setAdapter(adapt);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final String Coaches[] = {"Select Coach", "9", "12", "15"};
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Coaches);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        CoachSpinner.setAdapter(spinnerArrayAdapter);


        DepartureTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddTrain_Activity1.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        DepartureTimingText.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Departure Time");
                mTimePicker.show();

            }
        });


        DepartureTimingText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddTrain_Activity1.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        DepartureTimingText.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Departure Time");
                mTimePicker.show();


            }
        });


        ArrivalTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Time Picker calling,it will shows Timer option for time selection

                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddTrain_Activity1.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        ArrivalTimingText.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Arrival Time");
                mTimePicker.show();

            }
        });


        ArrivalTimingText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddTrain_Activity1.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        ArrivalTimingText.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Arrival Time");
                mTimePicker.show();
            }
        });


        AddTrainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SelectedCoach = CoachSpinner.getSelectedItem().toString();

                if (checkCriteria()) {

//                    if (SourceStationText.getText().toString().equals("")) {
//                        Snackbar.make(relativeLayout, "Source Station Name is required", Snackbar.LENGTH_SHORT).show();
//                        SourceStationText.requestFocus();
//
//                    } else if (DestinationStation.getText().toString().equals("")) {
//                        Snackbar.make(relativeLayout, "Destination Station Name is required", Snackbar.LENGTH_SHORT).show();
//                        DestinationStation.requestFocus();
//
//                    } else
                    if (DepartureTimingText.getText().toString().equals("")) {
                        Snackbar.make(relativeLayout, "Departure Time is required", Snackbar.LENGTH_SHORT).show();

                    } else if (SourcePlatform.getText().toString().compareTo("")==0){
                        Snackbar.make(relativeLayout, "Source Platform is required", Snackbar.LENGTH_SHORT).show();

                    }else if (DestinationPlatform.getText().toString().compareTo("")==0){
                        Snackbar.make(relativeLayout, "Source Platform is required", Snackbar.LENGTH_SHORT).show();
                    }
                    else if (ArrivalTimingText.getText().toString().equals("")) {
                        Snackbar.make(relativeLayout, "Arrival Time is required", Snackbar.LENGTH_SHORT).show();

                    } else if (SelectedLine.compareTo("Select Lines") == 0) {
                        Snackbar.make(relativeLayout, "Line is required", Snackbar.LENGTH_SHORT).show();

                    } else if (SelectedCoach.compareTo("Select Coach") == 0) {
                        Snackbar.make(relativeLayout, "Coach is required", Snackbar.LENGTH_SHORT).show();

                    } else {

                        if (RadioGroup.getCheckedRadioButtonId() == R.id.radio_fast) {
                            TrainTypeText = "fast";
                        } else if (RadioGroup.getCheckedRadioButtonId() == R.id.radio_slow) {
                            TrainTypeText = "slow";
                        }

                        SelectedStationNameArray = new ArrayList<String>();
                        SelectedStationIdArray = new ArrayList<String>();

                        for (int i = 0; i < CheckBoxValue.size(); i++) {

                            if (CheckBoxValue.get(i).compareTo("yes") == 0) {
                                SelectedStationIdArray.add(FilterStationIdArray.get(i));
                                SelectedStationNameArray.add(FilterStationNameArray.get(i));
                            }
                        }

                        if (SourceStationSpinner.getSelectedItemId() != DestinationStationSpinner.getSelectedItemId()) {

                            if (SelectedStationNameArray.size() > 0) {
//                            string source, string dest, string lid, string stime,string dtime,
//                                    string type,string coach,string stations

                                // sending parameter for one screen to another screen using intent
                                Intent intent = new Intent(AddTrain_Activity1.this, AddTrain_Activity2.class);
                                intent.putExtra("SStationName", SourceStationSpinner.getSelectedItem().toString());
                                intent.putExtra("DStationName", DestinationStationSpinner.getSelectedItem().toString());
                                intent.putExtra("LineId", SelectedLine);
                                intent.putExtra("STime", DepartureTimingText.getText().toString().trim());
                                intent.putExtra("DTime", ArrivalTimingText.getText().toString().trim());
                                intent.putExtra("TrainType", TrainTypeText);
                                intent.putExtra("CoachText", SelectedCoach);
                                intent.putStringArrayListExtra("SelectedStationId", SelectedStationIdArray);
                                intent.putStringArrayListExtra("SelectedStationName", SelectedStationNameArray);
                                intent.putIntegerArrayListExtra("PlatformCountArray", PlatformCountArray);
                                intent.putExtra("SourcePlatform",SourcePlatform.getText().toString().trim());
                                intent.putExtra("DestinationPlatform",DestinationPlatform.getText().toString().trim());
                                startActivity(intent);


                            } else {
                                Toast.makeText(AddTrain_Activity1.this, "Station Not Selected", Toast.LENGTH_SHORT).show();
                            }

                        }else {
                            Toast.makeText(AddTrain_Activity1.this, "Source and Destination station must be Different", Toast.LENGTH_SHORT).show();
                        }


                    }
                } else {
                    new AlertDialog.Builder(AddTrain_Activity1.this)
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
        if ((DepartureTimingText.getText().toString()).equals("")) {
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
                AlertDialog.Builder ad = new AlertDialog.Builder(AddTrain_Activity1.this);
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
                        Toast.makeText(AddTrain_Activity1.this, "No lines added", Toast.LENGTH_SHORT).show();


                    } else if (StatusValue.compareTo("ok") == 0) {

                        JSONArray result = json.getJSONArray("Data");
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject res = result.getJSONObject(i);

                            LineIdArray.add(res.getString("data0"));
                            LineNameArray.add(res.getString("data1"));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddTrain_Activity1.this, R.layout.spinner_textview, LineNameArray);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        LineSpinner.setAdapter(adapter);


                    }

                } catch (Exception e) {
                    Toast.makeText(AddTrain_Activity1.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
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
                JSONObject json = api.getStations_Lines(params[0]);
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

//            Toast.makeText(AddTrain_Activity1.this, s, Toast.LENGTH_SHORT).show();

            StationIdArray = new ArrayList<String>();
            StationNameArray = new ArrayList<String>();
            PlatformCountArray = new ArrayList<Integer>();
            mDialog.dismiss();

            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(AddTrain_Activity1.this);
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
                        Toast.makeText(AddTrain_Activity1.this, "No Stations added", Toast.LENGTH_SHORT).show();
                        StationList.setAdapter(null);

                    } else if (StatusValue.compareTo("ok") == 0) {

                        JSONArray result = json.getJSONArray("Data");
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject res = result.getJSONObject(i);

                            //Sid,Sname,Lid,Pcount,major
                            StationIdArray.add(res.getString("data0"));
                            StationNameArray.add(res.getString("data1"));
                            PlatformCountArray.add(Integer.parseInt(res.getString("data3")));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddTrain_Activity1.this, android.R.layout.simple_spinner_item, StationNameArray);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        SourceStationSpinner.setAdapter(adapter);

                        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(AddTrain_Activity1.this, android.R.layout.simple_spinner_item, StationNameArray);
                        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        DestinationStationSpinner.setAdapter(adapter1);

                    }

                } catch (Exception e) {
                    Toast.makeText(AddTrain_Activity1.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
    }


    public class StationAdapter extends ArrayAdapter<String> {

        Context con;
        ArrayList<String> dataset;
        ArrayList<String> CheckBoxValue;


        public StationAdapter(Context context, ArrayList<String> data, ArrayList<String> checkBoxValue) {
            super(context, R.layout.station_name_list, data);
            con = context;
            dataset = data;
            CheckBoxValue = checkBoxValue;
        }


        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = LayoutInflater.from(con).inflate(R.layout.station_name_list, null, true);

            final CheckBox StationCheckBox = (CheckBox) v.findViewById(R.id.station_checkbox);
            TextView StationName = (TextView) v.findViewById(R.id.station_name);

            final String temp[] = dataset.get(position).split("\\*");


            StationName.setText(FilterStationNameArray.get(position));

            if (CheckBoxValue.get(position).compareTo("no") == 0) {
                StationCheckBox.setChecked(false);
            } else if (CheckBoxValue.get(position).compareTo("yes") == 0) {
                StationCheckBox.setChecked(true);
            }


            StationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        CheckBoxValue.set(position, "yes");
                    } else {
                        CheckBoxValue.set(position, "no");
                    }
                }
            });


            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (CheckBoxValue.get(position).compareTo("no") == 0) {
                        CheckBoxValue.set(position, "yes");
                        StationCheckBox.setChecked(true);
                    } else {
                        CheckBoxValue.set(position, "no");
                        StationCheckBox.setChecked(false);
                    }
                }
            });


            return v;
        }
    }


}
