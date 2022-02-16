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
import android.support.design.widget.Snackbar;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;



public class Lines_Activity extends AppCompatActivity {

    RelativeLayout relativeLayout;
    ListView listView;
    FloatingActionButton floatingActionButton;

    Dialog mDialog, AddLineDialog;
    ArrayList<String> data;
    SharedPreferences pref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.station_layout);

        mDialog = new Dialog(Lines_Activity.this, R.style.AppTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.circular_dialog);
        mDialog.setCancelable(false);

        pref = getSharedPreferences("VoiceBasedTT", Context.MODE_PRIVATE);
        String userId_pref = pref.getString("UserId", "");


        getSupportActionBar().setTitle("Lines");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        relativeLayout = (RelativeLayout) findViewById(R.id.station_screen);
        listView = (ListView) findViewById(R.id.station_list);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.addFloatButton);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // On Floating Action button click, it will show dialog for adding line
                shwoDialog();
            }
        });


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


    @Override
    protected void onResume() {
        super.onResume();

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
                AlertDialog.Builder ad = new AlertDialog.Builder(Lines_Activity.this);
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
                        Toast.makeText(Lines_Activity.this, "No lines added", Toast.LENGTH_SHORT).show();
                        listView.setAdapter(null);

                    } else if (StatusValue.compareTo("ok") == 0) {

                        JSONArray result = json.getJSONArray("Data");
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject res = result.getJSONObject(i);

                            String FinalData = res.getString("data0") + "*" + res.get("data1");
                            data.add(FinalData);
                        }

                        LineAdapter adapt = new LineAdapter(Lines_Activity.this, data);
                        listView.setAdapter(adapt);
                    }

                } catch (Exception e) {
                    Toast.makeText(Lines_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
    }


    public class LineAdapter extends ArrayAdapter<String> {

        Context con;
        ArrayList<String> dataset;

        public LineAdapter(Context context, ArrayList<String> data) {
            super(context, R.layout.line_list_row, data);
            con = context;
            dataset = data;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = LayoutInflater.from(con).inflate(R.layout.line_list_row, null, true);

            final String[] temp = dataset.get(position).split("\\*");

            TextView LineName = (TextView) v.findViewById(R.id.line_name);
            LineName.setText(temp[1]);

            return v;
        }


    }

    public void shwoDialog() {

        AddLineDialog = new Dialog(Lines_Activity.this);
        AddLineDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        AddLineDialog.setContentView(R.layout.add_line_dialog);

        final RelativeLayout relativeLayout = (RelativeLayout)AddLineDialog. findViewById(R.id.add_line_screen);
        final EditText LineText = (EditText)AddLineDialog. findViewById(R.id.LineName_Text);
        Button SubmitBtn = (Button)AddLineDialog. findViewById(R.id.addLineBtn);


        SubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (LineText.getText().toString().compareTo("") == 0) {
                    Snackbar.make(relativeLayout, "Line Name is required", Snackbar.LENGTH_SHORT).show();
                } else {
                   new AddLineTask().execute(LineText.getText().toString());

                }

            }
        });

        AddLineDialog.show();


    }


    public class AddLineTask extends AsyncTask<String, JSONObject, String> {

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
                JSONObject json = api.AddLine(params[0]);
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
                AlertDialog.Builder ad = new AlertDialog.Builder(Lines_Activity.this);
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
                        AddLineDialog.dismiss();
                        new getLinesTask().execute();

                    } else if (StatusValue.compareTo("already") == 0) {
                        Snackbar.make(relativeLayout,"Line Name already exist",Snackbar.LENGTH_SHORT).show();
                    }


                } catch (Exception e) {
                    Toast.makeText(Lines_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }


        }
    }


}
