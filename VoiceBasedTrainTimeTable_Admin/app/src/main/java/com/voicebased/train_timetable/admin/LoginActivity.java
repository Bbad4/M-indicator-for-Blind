package com.voicebased.train_timetable.admin;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Locale;




public class LoginActivity extends AppCompatActivity {

    SharedPreferences pref;
    protected EditText EmailID, Password;
    protected Button SignIn;
    protected RelativeLayout relativeLayout;
    Dialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDialog = new Dialog(LoginActivity.this, R.style.AppTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.circular_dialog);
        mDialog.setCancelable(false);

        getSupportActionBar().hide();

        // For Calling Runtime Permission
        Boolean ans = weHavePermission();
        if (!ans) {
            requestforPermissionFirst();
        }



        pref = getSharedPreferences("VoiceBasedTT", Context.MODE_PRIVATE);
        String userId_pref = pref.getString("UserId", "");

        // For Checking User prefrence[If user id is empty then login otherwise redirect to home page]
        if (userId_pref.compareTo("") !=0){

            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
            finish();

        }else {
            setContentView(R.layout.login_activity);
            init();

        }



    }


    protected void init() {

        EmailID = (EditText) findViewById(R.id.loginUserName);
        Password = (EditText) findViewById(R.id.loginPassword);
        SignIn = (Button) findViewById(R.id.loginButton);
        relativeLayout = (RelativeLayout) findViewById(R.id.activity_login);


        SignIn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (EmailID.getText().toString().equals("")) {
                            Snackbar.make(relativeLayout, "User Name is required", Snackbar.LENGTH_SHORT).show();
                            EmailID.requestFocus();

                        } else if (Password.getText().toString().equals("")) {
                            Snackbar.make(relativeLayout, "Password is required", Snackbar.LENGTH_SHORT).show();
                            Password.requestFocus();
                        } else {

                            new logintask().execute(EmailID.getText().toString().trim(), Password.getText().toString().trim());

                        }


                    }
                }

        );


    }


    public class logintask extends AsyncTask<String, JSONObject, String> {

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
                JSONObject json = api.ALogin(params[0],params[1]);
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
                AlertDialog.Builder ad = new AlertDialog.Builder(LoginActivity.this);
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
                    
                    if (StatusValue.compareTo("true")==0){
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("UserId", EmailID.getText().toString());
                        editor.apply();
                        editor.commit();

                        Intent intent  = new Intent(LoginActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                        
                    }else if (StatusValue.compareTo("false")==0){

                        Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                        EmailID.setText("");
                        Password.setText("");
                        
                    }


                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }


        

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



