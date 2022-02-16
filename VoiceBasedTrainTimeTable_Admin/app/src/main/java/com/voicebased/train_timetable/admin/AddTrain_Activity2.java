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
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;



public class AddTrain_Activity2 extends AppCompatActivity {


    RelativeLayout relativeLayout;
    TextView StationDestinationName;
    ListView Stations_ListView;
    Dialog mDialog;

    String SStationName, DStationName, LineId, STime, DTime, TrainType, CoachText, SPlatform, DPlatform;
    ArrayList<String> SelectedStationIdArray, SelectedStationNameArray;
    ArrayList<Integer> PlatformCountArray;

    ArrayList<String> NewPlatformTextArray, NewTimeTextArray;
    ArrayList<Integer> SpinnerPositionArray;
    SimpleDateFormat sdfd = new SimpleDateFormat("HH:mm", Locale.US);

    Button SubmitBtn;
    ArrayList<Integer> FilterStaionIDArray;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_train_layout2);

        mDialog = new Dialog(AddTrain_Activity2.this, R.style.AppTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.circular_dialog);
        mDialog.setCancelable(false);

        getSupportActionBar().setTitle("Add Train");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        relativeLayout = (RelativeLayout) findViewById(R.id.add_trains_screen2);
        StationDestinationName = (TextView) findViewById(R.id.source_destination_name);
        Stations_ListView = (ListView) findViewById(R.id.stations_listview);
        SubmitBtn = (Button) findViewById(R.id.submit_btn);


        // For gettting the value of AddTain_Activity1 to here using Intent
        Intent intent = getIntent();
        SStationName = intent.getStringExtra("SStationName");
        DStationName = intent.getStringExtra("DStationName");
        LineId = intent.getStringExtra("LineId");
        STime = intent.getStringExtra("STime");
        DTime = intent.getStringExtra("DTime");
        TrainType = intent.getStringExtra("TrainType");
        CoachText = intent.getStringExtra("CoachText");
        SelectedStationIdArray = intent.getStringArrayListExtra("SelectedStationId");
        SelectedStationNameArray = intent.getStringArrayListExtra("SelectedStationName");
        PlatformCountArray = intent.getIntegerArrayListExtra("PlatformCountArray");
        SPlatform = intent.getStringExtra("SourcePlatform");
        DPlatform = intent.getStringExtra("DestinationPlatform");

        StationDestinationName.setText(SStationName + " -> " + DStationName);

        NewPlatformTextArray = new ArrayList<String>();
        NewTimeTextArray = new ArrayList<String>();
        SpinnerPositionArray = new ArrayList<Integer>();


        Date dt = new Date();
        String date = sdfd.format(dt.getTime());

        for (int i = 0; i < SelectedStationNameArray.size(); i++) {
            SpinnerPositionArray.add(0);
            NewTimeTextArray.add(date);
            NewPlatformTextArray.add("1");
        }

        StationAdapter adapt = new StationAdapter(AddTrain_Activity2.this, SelectedStationNameArray);
        Stations_ListView.setAdapter(adapt);


        SubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (CheckStationValue()) {


                    String AllStations = SStationName + ",";
                    for (int i = 0; i < SelectedStationNameArray.size(); i++) {
                        AllStations += SelectedStationNameArray.get(FilterStaionIDArray.get(i)) + ",";
                    }
                    AllStations += DStationName;

                    String AllPlatform = SPlatform + ",";
                    for (int i = 0; i < NewPlatformTextArray.size(); i++) {
                        AllPlatform += NewPlatformTextArray.get(i) + ",";
                    }
                    AllPlatform += DPlatform;


                    String AllTime = STime + ",";
                    for (int i = 0; i < NewTimeTextArray.size(); i++) {
                        AllTime += NewTimeTextArray.get(i) + ",";
                    }
                    AllTime += DTime;


                    //                string source, string dest, string lid, string stime,string dtime,string type,
//                        string coach,string stations,string platforms,string time

                    new AddTrainTask().execute(SStationName,DStationName,LineId,STime,DTime,TrainType,CoachText,
                            AllStations,AllPlatform,AllTime);


                } else {
                    Toast.makeText(AddTrain_Activity2.this, "Check Station List,All station should be different", Toast.LENGTH_SHORT).show();
                }


            }
        });

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

    public class StationAdapter extends ArrayAdapter<String> {

        Context con;
        ArrayList<String> dataset;

        public StationAdapter(Context context, ArrayList<String> data) {
            super(context, R.layout.station_name_list, data);
            con = context;
            dataset = data;
        }


        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = LayoutInflater.from(con).inflate(R.layout.add_train_listview, null, true);

            TextView SNoText = (TextView) v.findViewById(R.id.sequence_no_text);
            final TextView TimeText = (TextView) v.findViewById(R.id.time_text);
            Spinner stationSpinner = (Spinner) v.findViewById(R.id.station_list);
            final EditText PlatformText = (EditText) v.findViewById(R.id.platform_text);
            final TextView PLimitText = (TextView) v.findViewById(R.id.plimit_text);

            SNoText.setText((position + 1) + "");


            ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddTrain_Activity2.this, android.R.layout.simple_spinner_item, SelectedStationNameArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            stationSpinner.setAdapter(adapter);

//            PLimitText.setText("1 - "+PlatformCountArray.get(position));
            TimeText.setText(NewTimeTextArray.get(position));

            PlatformText.setText(NewPlatformTextArray.get(position));

            stationSpinner.setSelection(SpinnerPositionArray.get(position));

            PlatformText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    NewPlatformTextArray.set(position, PlatformText.getText().toString().trim());
                }
            });


            stationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    SpinnerPositionArray.set(position, i);
//                    NewPlatformTextArray.set(position,"1");
                    PLimitText.setText("1 - " + PlatformCountArray.get(i));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });


            TimeText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(AddTrain_Activity2.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            TimeText.setText(selectedHour + ":" + selectedMinute);
                            NewTimeTextArray.set(position, TimeText.getText().toString());
                        }
                    }, hour, minute, true);//Yes 24 hour time
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();
                }
            });


            return v;
        }
    }


    // This funtion is used for Filter Station Id[No of stations should not be repeat for any train]
    public boolean CheckStationValue() {

        boolean CheckValue = true;
        FilterStaionIDArray = new ArrayList<Integer>();

        for (int i = 0; i < SpinnerPositionArray.size(); i++) {

            boolean result = true;
            for (int j = 0; j < SpinnerPositionArray.size(); j++) {

                if (i != j) {

                    if (result) {

                        if (SpinnerPositionArray.get(i) == SpinnerPositionArray.get(j)) {
                            return false;
                        }
                    }

                }

            }

            if (result) {
                FilterStaionIDArray.add(SpinnerPositionArray.get(i));
            }

        }

        return CheckValue;
    }


    public class AddTrainTask extends AsyncTask<String, JSONObject, String> {

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
                JSONObject json = api.AddTrains(params[0], params[1], params[2], params[3],params[4],params[5],params[6],
                        params[7],params[8],params[9]);
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

//            Toast.makeText(AddTrain_Activity2.this, s, Toast.LENGTH_SHORT).show();

            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(AddTrain_Activity2.this);
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
                        Toast.makeText(AddTrain_Activity2.this, "Train Added Successfully", Toast.LENGTH_SHORT).show();

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                Intent intent = new Intent(AddTrain_Activity2.this,Trains_Activity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();

                            }
                        }, 1000);

                    }


                } catch (Exception e) {
                    Toast.makeText(AddTrain_Activity2.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }


        }
    }



}
