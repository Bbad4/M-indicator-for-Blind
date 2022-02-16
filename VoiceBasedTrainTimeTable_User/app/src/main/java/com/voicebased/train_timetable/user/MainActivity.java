package com.voicebased.train_timetable.user;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    boolean ChatMessage = false;
    ListView listView;
    RelativeLayout relativeLayout;
    MediaPlayer mediaPlayer;

    ArrayList<String> ChatData, ChatValueArray;
    Dialog mDialog;

    String SourceName = "", SourceLid = "", SourceLidName = "", DesName = "", DesLid = "", DesLidName = "";
    Adapter adapter;

    protected static final int RESULT_SOURCE_SPEECH = 1;
    protected static final int RESULT_DESTINATION_SPEECH = 2;
    protected static final int CHOOSE_TRAIN_SELECTION = 3;
    protected static final int LAST_OPTION = 4;

    protected static final int MANY_SOURCE = 5;
    protected static final int MANY_DESTINATION = 6;

    protected static final int CHOOSE_COMMON_STATON = 7;
    protected static final int CHOOSE_COMMON_TRAIN = 8;

    protected static final int CHOOSE_SOURCE = 9;

    ArrayList<String> SourceLidArray, SourceLidNameArray;
    ArrayList<String> DesLidArray, DesLidNameArray;

    ArrayList<String> TidArray, LidArray, SequenceArray, StationsArray, PlatformArray, TimeArray;
    ArrayList<String> SelectedStationArray, SelectedPlatformArray, SelectedTimeArray;
    ArrayList<String> FilterStationsArray;

    SimpleDateFormat sdfd = new SimpleDateFormat("HH:mm", Locale.US);
    int sPosition, dPosition;
    String NewSource = "";

    ArrayList<String> CommonStationArray, AllStationDataArray;

    ArrayList<String> CTidArray, CLidArray, CSequenceArray, CStationsArray, CPlatformArray, CTimeArray;
    String tempSourceLid = "", tempSourceLidName = "";
    String NetworkSource = "";

    String NewSTiming="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.chatlistView);
        relativeLayout = (RelativeLayout) findViewById(R.id.main_screen);

        mDialog = new Dialog(MainActivity.this, R.style.AppTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.circular_dialog);
        mDialog.setCancelable(false);

        ChatData = new ArrayList<String>();
        ChatValueArray = new ArrayList<String>();

        Intent intent = getIntent();
        String SourceStation = intent.getStringExtra("PlaceDetail");
        NetworkSource = SourceStation;

        adapter = new Adapter(this, ChatData, ChatValueArray);
        listView.setAdapter(adapter);


        // Adding  values in ArrayList first and then set to adapter
        //[dapter.notifyDataSetChanged()] this is used for refresh the adapter
        Speech_SelectSource("Say 1 for Auto Source and say 2 for User Source Location");
        ChatData.add("Say 1 for Auto Source and say 2 for User Source Location");
        ChatValueArray.add("sys");
        adapter.notifyDataSetChanged();
        listView.setSelection(ChatData.size());


    }


    @Override
    protected void onResume() {
        super.onResume();

//        Intent intent = getIntent();
//        PlaceDetail = intent.getStringExtra("PlaceDetail");
//
//        if (PlaceDetail.compareTo("Na") == 0) {
//            Speech_1("Source Location not found, say source location after a beep.");
//            //Voice to text
//        } else {
//
//            //Check with database for source [ if match then speak current source else get source from user]
//            // call  voice to text for destination
//
////            Speech_1("Your Location is " + PlaceDetail);
//        }


    }


    @Override
    protected void onPause() {
        super.onPause();

        // We used media player for text to speech , so is current screen is goes into onPause state
        // when application is minimize then we clear the media player and set null
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            // In onActivityResult all the voice to text reponse will acess here
            // Like 1 or 2 or statioName , Line ,etc.

            case RESULT_SOURCE_SPEECH:

                if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (text.size() > 0) {

                        SourceName = text.get(0).trim();
                        new CheckSourceTask().execute(SourceName);
                    }

                }

                break;

            case RESULT_DESTINATION_SPEECH:

                if (requestCode == 2 && resultCode == RESULT_OK && null != data) {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (text.size() > 0) {

                        DesName = text.get(0).trim();
                        new CheckDestination_Task().execute(DesName);
                        ChatData.add(DesName);
                        ChatValueArray.add("me");
                        adapter.notifyDataSetChanged();
                        listView.setSelection(ChatData.size());
                    }
                }
                break;

            case CHOOSE_TRAIN_SELECTION:

                if (requestCode == 3 && resultCode == RESULT_OK && null != data) {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (text.size() > 0) {

                        if (text.get(0).toLowerCase().contains("1") || text.get(0).toLowerCase().contains("one")) {


                            ChatData.add("One");
                            ChatValueArray.add("me");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());
                            new geTrainStationDetail_Task().execute(TidArray.get(0));

                        } else if (text.get(0).toLowerCase().contains("2") || text.get(0).toLowerCase().contains("two")
                                || text.get(0).toLowerCase().contains("tu") || text.get(0).toLowerCase().contains("to")) {


                            ChatData.add("Two");
                            ChatValueArray.add("me");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());
//                            Toast.makeText(this, TidArray.get(1), Toast.LENGTH_SHORT).show();
                            new geTrainStationDetail_Task().execute(TidArray.get(1));


                        } else if (text.get(0).toLowerCase().contains("3") || text.get(0).toLowerCase().contains("three")
                                || text.get(0).toLowerCase().contains("tree")) {


                            ChatData.add("Three");
                            ChatValueArray.add("me");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());
                            new geTrainStationDetail_Task().execute(TidArray.get(2));


                        } else if (text.get(0).toLowerCase().contains("4") || text.get(0).toLowerCase().contains("four")
                                || text.get(0).toLowerCase().contains("for")) {

                            ChatData.add("Four");
                            ChatValueArray.add("me");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());
                            new geTrainStationDetail_Task().execute(TidArray.get(3));

                        }


                    }
                }
                break;


            case LAST_OPTION:

                if (requestCode == 4 && resultCode == RESULT_OK && null != data) {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (text.size() > 0) {

                        if (text.get(0).toLowerCase().contains("1") || text.get(0).toLowerCase().contains("one")) {

                            ChatData.add("One");
                            ChatValueArray.add("me");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());

                            new CheckSourceTask().execute(SourceName.trim());


                        } else if (text.get(0).toLowerCase().contains("2") || text.get(0).toLowerCase().contains("two")
                                || text.get(0).toLowerCase().contains("tu")|| text.get(0).toLowerCase().contains("to")) {

                            ChatData.add("Two");
                            ChatValueArray.add("me");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());

                            ChatData.add("Thank You ");
                            ChatValueArray.add("sys");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {

                                    Intent intent = new Intent(getApplicationContext(), FinishActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();

                                }
                            }, 1500);


                        }
                    }
                }
                break;

            case MANY_SOURCE:

                if (requestCode == 5 && resultCode == RESULT_OK && null != data) {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (text.size() > 0) {

                        if (text.get(0).toLowerCase().contains("1") || text.get(0).toLowerCase().contains("one")) {

                            ChatData.add("One");
                            ChatValueArray.add("me");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());

                            ChatData.add("Your Selected Line is " + SourceLidNameArray.get(0));
                            ChatValueArray.add("sys");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());

                            SourceLid = SourceLidArray.get(0);
                            SourceLidName = SourceLidNameArray.get(0);

                            Speech_2("Source station Line is selected .Speak Destination Staion Name");
                            ChatData.add("Source station Line is selected .Speak Destination Staion Name");
                            ChatValueArray.add("sys");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());


                        } else if (text.get(0).toLowerCase().contains("2") || text.get(0).toLowerCase().contains("two")
                                || text.get(0).toLowerCase().contains("tu")|| text.get(0).toLowerCase().contains("to")) {

                            ChatData.add("Two");
                            ChatValueArray.add("me");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());

                            ChatData.add("Your Selected Line is " + SourceLidNameArray.get(1));
                            ChatValueArray.add("sys");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());

                            SourceLid = SourceLidArray.get(1);
                            SourceLidName = SourceLidNameArray.get(1);

                            Speech_2("Source station Line is selected .Speak Destination Staion Name");
                            ChatData.add("Source station Line is selected .Speak Destination Staion Name");
                            ChatValueArray.add("sys");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());


                        }
                    }
                }
                break;


            case MANY_DESTINATION:

                if (requestCode == 6 && resultCode == RESULT_OK && null != data) {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (text.size() > 0) {

                        if (text.get(0).toLowerCase().contains("1") || text.get(0).toLowerCase().contains("one")) {

                            ChatData.add("One");
                            ChatValueArray.add("me");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());

                            ChatData.add("Your Selected Line is " + DesLidNameArray.get(0));
                            ChatValueArray.add("sys");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());


                            DesLid = DesLidArray.get(0);
                            DesLidName = DesLidNameArray.get(0);

                            Speech_ResultAPI("Destination station Line is selected ");
                            ChatData.add("Destination station Line is selected ");
                            ChatValueArray.add("sys");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());


                        } else if (text.get(0).toLowerCase().contains("2") || text.get(0).toLowerCase().contains("two")
                                || text.get(0).toLowerCase().contains("tu")|| text.get(0).toLowerCase().contains("to")) {

                            ChatData.add("Two");
                            ChatValueArray.add("me");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());

                            ChatData.add("Your Selected Line is " + DesLidNameArray.get(1));
                            ChatValueArray.add("sys");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());


                            DesLid = DesLidArray.get(1);
                            DesLidName = DesLidNameArray.get(1);

                            Speech_ResultAPI("Destination station Line is selected ");
                            ChatData.add("Destination station Line is selected ");
                            ChatValueArray.add("sys");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());


                        }
                    }
                }
                break;


            case CHOOSE_COMMON_STATON:

                if (requestCode == 7 && resultCode == RESULT_OK && null != data) {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (text.size() > 0) {

                        if (text.get(0).toLowerCase().contains("1") || text.get(0).toLowerCase().contains("one")) {


                            NewSource = CommonStationArray.get(0);
                            ChatData.add("One");
                            ChatValueArray.add("me");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());

                            ChatData.add("Your Selected station is " + NewSource);
                            ChatValueArray.add("sys");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());

                            try {
                                JSONObject json = new JSONObject(AllStationDataArray.get(0));
                                String StatusValue = json.getString("status");

                                if (StatusValue.compareTo("no") == 0) {
                                    Toast.makeText(MainActivity.this, "No Destination Stations Found", Toast.LENGTH_SHORT).show();
                                    DesName = "Na";

                                    Last_SelectionOption("No trains available.If you want to search again say 1 and for exit the app say 2");
                                    ChatData.add("No trains available.If you want to search again say 1 and for exit the app say 2");
                                    ChatValueArray.add("sys");
                                    adapter.notifyDataSetChanged();
                                    listView.setSelection(ChatData.size());


                                } else if (StatusValue.compareTo("ok") == 0) {
                                    CTidArray = new ArrayList<String>();
                                    CLidArray = new ArrayList<String>();
                                    CSequenceArray = new ArrayList<String>();
                                    CStationsArray = new ArrayList<String>();
                                    CPlatformArray = new ArrayList<String>();
                                    CTimeArray = new ArrayList<String>();


                                    JSONArray result = json.getJSONArray("Data");
                                    for (int i = 0; i < result.length(); i++) {
                                        JSONObject res = result.getJSONObject(i);

//                                      tid,lid,sequence,station,platform,time
                                        CTidArray.add(res.getString("data0"));
                                        CLidArray.add(res.getString("data1"));
                                        CSequenceArray.add(res.getString("data2"));
                                        CStationsArray.add(res.getString("data3"));
                                        CPlatformArray.add(res.getString("data4"));
                                        CTimeArray.add(res.getString("data5"));
                                    }

                                    String ChooseTrain = ChooseCTrain_Funtion(CTidArray.size());
                                    Speech_CommonTrainSelection(ChooseTrain);
                                    ChatData.add(ChooseTrain);
                                    ChatValueArray.add("sys");
                                    adapter.notifyDataSetChanged();
                                    listView.setSelection(ChatData.size());

                                }

                            } catch (Exception e) {
                                Toast.makeText(this, "Common: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }


                        } else if (text.get(0).toLowerCase().contains("2") || text.get(0).toLowerCase().contains("two")
                                || text.get(0).toLowerCase().contains("tu")|| text.get(0).toLowerCase().contains("to")) {

                            NewSource = CommonStationArray.get(1);
                            ChatData.add("Two");
                            ChatValueArray.add("me");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());

                            ChatData.add("Your Selected station is " + NewSource);
                            ChatValueArray.add("sys");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());


                            try {
                                JSONObject json = new JSONObject(AllStationDataArray.get(1));
                                String StatusValue = json.getString("status");

                                if (StatusValue.compareTo("no") == 0) {
                                    Toast.makeText(MainActivity.this, "No Destination Stations Found", Toast.LENGTH_SHORT).show();
                                    DesName = "Na";

                                    Last_SelectionOption("No trains available," + "\n\n\n\n\n\n" + "If you want to search again say 1 and for exit the app say 2");
                                    ChatData.add("No trains available" + "\n" + "If you want to search again say 1 and for exit the app say 2");
                                    ChatValueArray.add("sys");
                                    adapter.notifyDataSetChanged();
                                    listView.setSelection(ChatData.size());


                                } else if (StatusValue.compareTo("ok") == 0) {
                                    CTidArray = new ArrayList<String>();
                                    CLidArray = new ArrayList<String>();
                                    CSequenceArray = new ArrayList<String>();
                                    CStationsArray = new ArrayList<String>();
                                    CPlatformArray = new ArrayList<String>();
                                    CTimeArray = new ArrayList<String>();


                                    JSONArray result = json.getJSONArray("Data");
                                    for (int i = 0; i < result.length(); i++) {
                                        JSONObject res = result.getJSONObject(i);

//                                      tid,lid,sequence,station,platform,time
                                        CTidArray.add(res.getString("data0"));
                                        CLidArray.add(res.getString("data1"));
                                        CSequenceArray.add(res.getString("data2"));
                                        CStationsArray.add(res.getString("data3"));
                                        CPlatformArray.add(res.getString("data4"));
                                        CTimeArray.add(res.getString("data5"));
                                    }

                                    String ChooseTrain = ChooseCTrain_Funtion(CTidArray.size());
                                    Speech_CommonTrainSelection(ChooseTrain);
                                    ChatData.add(ChooseTrain);
                                    ChatValueArray.add("sys");
                                    adapter.notifyDataSetChanged();
                                    listView.setSelection(ChatData.size());

                                }

                            } catch (Exception e) {
                                Toast.makeText(this, "Common: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }


                        }
                    }
                }
                break;


            case CHOOSE_COMMON_TRAIN:

                if (requestCode == 8 && resultCode == RESULT_OK && null != data) {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (text.size() > 0) {

                        if (text.get(0).toLowerCase().contains("1") || text.get(0).toLowerCase().contains("one")) {

                            ChatData.add("One");
                            ChatValueArray.add("me");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());
                            //string src, string dest, string time

//                            Toast.makeText(this,"In activity Result"+"\n"+ NewSource+"\n"+DesName+"\n"+NewSTiming, Toast.LENGTH_SHORT).show();

                            //In activity
                            new getTrainRoute_Task().execute(NewSource, DesName, NewSTiming);

                        } else if (text.get(0).toLowerCase().contains("2") || text.get(0).toLowerCase().contains("two")
                                || text.get(0).toLowerCase().contains("tu")|| text.get(0).toLowerCase().contains("to")) {


                            ChatData.add("Two");
                            ChatValueArray.add("me");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());
                            //string src, string dest, string time
                            new getTrainRoute_Task().execute(NewSource, DesName, NewSTiming);


                        } else if (text.get(0).toLowerCase().contains("3") || text.get(0).toLowerCase().contains("three")
                                || text.get(0).toLowerCase().contains("tree")) {


                            ChatData.add("Three");
                            ChatValueArray.add("me");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());
                            //string src, string dest, string time
                            new getTrainRoute_Task().execute(NewSource, DesName, NewSTiming);


                        } else if (text.get(0).toLowerCase().contains("4") || text.get(0).toLowerCase().contains("four")
                                || text.get(0).toLowerCase().contains("for")) {

                            ChatData.add("Four");
                            ChatValueArray.add("me");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());
                            //string src, string dest, string time
                            new getTrainRoute_Task().execute(NewSource, DesName, NewSTiming);


                        }


                    }
                }
                break;


            case CHOOSE_SOURCE:

                if (requestCode == 9 && resultCode == RESULT_OK && null != data) {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (text.size() > 0) {

                        if (text.get(0).toLowerCase().contains("1") || text.get(0).toLowerCase().contains("one")) {

                            ChatData.add("One");
                            ChatValueArray.add("me");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());

                            try {
                                String temp[] = NetworkSource.split(" ");
                                String FinalSource = temp[0];

                                if (NetworkSource.compareTo("Na") == 0) {
                                    Speech_1("Source Location not found, say source station name after a beep.");

                                } else {
                                    SourceName = FinalSource;
                                    new CheckSourceTask().execute(SourceName.trim());
                                }

                            } catch (Exception e) {
                                Speech_1("Source Location not found, say source station name after a beep.");
                            }


                        } else if (text.get(0).toLowerCase().contains("2") || text.get(0).toLowerCase().contains("two")
                                || text.get(0).toLowerCase().contains("tu")|| text.get(0).toLowerCase().contains("to")) {


                            ChatData.add("Two");
                            ChatValueArray.add("me");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());

                            Speech_1("speak Source Station Name");
                            ChatData.add("speak Source Station Name");
                            ChatValueArray.add("sys");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());


                        }
                    }
                }
                break;


        }

    }

    public void Speech_SelectSource(String text) {

        new Speech_SelectSource().execute(text);
    }

    public class Speech_SelectSource extends AsyncTask<String, Void, MediaPlayer> {

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
                mediaPlayer.setDataSource(MainActivity.this, uri);
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

                mediaPlayer = null;
            }

            return mediaPlayer;
        }

        @Override
        protected void onPostExecute(final MediaPlayer s) {
            super.onPostExecute(s);

            if (s != null) {
                s.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        s.stop();
                        s.reset();

                        VoiceToTextSearch_SelectSource();

                    }
                });
            }
        }
    }


    public void VoiceToTextSearch_SelectSource() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

        try {
            startActivityForResult(intent, CHOOSE_SOURCE);

        } catch (ActivityNotFoundException a) {
            Toast t = Toast.makeText(getApplicationContext(),
                    "Opps! Your device doesn't support Speech to Text",
                    Toast.LENGTH_SHORT);
            t.show();
        }
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
                mediaPlayer.setDataSource(MainActivity.this, uri);
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

                mediaPlayer = null;
            }

            return mediaPlayer;
        }

        @Override
        protected void onPostExecute(final MediaPlayer s) {
            super.onPostExecute(s);

            if (s != null) {
                s.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        s.stop();
                        s.reset();

                        VoiceToTextSearch_Source();

                    }
                });
            }
        }
    }

    // this funtion is used for calling the speech to text dialog
    public void VoiceToTextSearch_Source() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

        try {
            startActivityForResult(intent, RESULT_SOURCE_SPEECH);

        } catch (ActivityNotFoundException a) {
            Toast t = Toast.makeText(getApplicationContext(),
                    "Opps! Your device doesn't support Speech to Text",
                    Toast.LENGTH_SHORT);
            t.show();
        }
    }


//    public class Adapter extends ArrayAdapter<String> {
//
//        Context con;
//        ArrayList<String> dataset, cSet;
//
//        public Adapter(Context context, int resource, ArrayList<String> data, ArrayList<String> Cvalue) {
//            super(context, resource, data);
//            con = context;
//            dataset = data;
//            cSet = Cvalue;
//        }
//
//        @NonNull
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ViewHolder holder;
//            LayoutInflater inflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
//
//            int layoutResource = 0;
//            int viewType = getItemViewType(position);
//
//            if (ChatValueArray.get(position).compareTo("me") == 0) {
//                layoutResource = R.layout.item_chat_right;
//
//            } else if (ChatValueArray.get(position).compareTo("sys") == 0) {
//                layoutResource = R.layout.item_chat_left;
//            }
//
//            if (convertView != null) {
//                holder = (ViewHolder) convertView.getTag();
//            } else {
//                convertView = inflater.inflate(layoutResource, parent, false);
//                holder = new ViewHolder(convertView);
//                convertView.setTag(holder);
//            }
//
//            holder.msg.setText(ChatData.get(position));
//
//
//            return convertView;
//        }
//
//        @Override
//        public int getViewTypeCount() {
//            // return the total number of view types. this value should never change
//            // at runtime
//            return 2;
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            // return a value between 0 and (getViewTypeCount - 1)
//            return position % 2;
//        }
//
//        private class ViewHolder {
//            private TextView msg;
//
//            public ViewHolder(View v) {
//                msg = (TextView) v.findViewById(R.id.txt_msg);
//            }
//        }
//
//
//    }

    public class Adapter extends ArrayAdapter<String> {

        Context con;
        ArrayList<String> dataset;
        ArrayList<String> dataset1;

        public Adapter(Context context, ArrayList<String> data, ArrayList<String> Cvalue) {
            super(context, R.layout.main_chat_layout, data);
            con = context;
            dataset = data;
            dataset1 = Cvalue;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = LayoutInflater.from(con).inflate(R.layout.main_chat_layout, null, true);

            LinearLayout LeftLayout = (LinearLayout) v.findViewById(R.id.leftchat_layout);
            LinearLayout RightLayout = (LinearLayout) v.findViewById(R.id.rightchat_layout);
            TextView LeftText = (TextView) v.findViewById(R.id.left_text);
            TextView RightText = (TextView) v.findViewById(R.id.right_text);


            if (ChatValueArray.get(position).compareTo("me") == 0) {
                LeftLayout.setVisibility(View.GONE);
                RightLayout.setVisibility(View.VISIBLE);
                RightText.setText(ChatData.get(position));

            } else if (ChatValueArray.get(position).compareTo("sys") == 0) {
                LeftLayout.setVisibility(View.VISIBLE);
                RightLayout.setVisibility(View.GONE);
                LeftText.setText(ChatData.get(position));
            }


            return v;
        }

    }


    public class CheckSourceTask extends AsyncTask<String, JSONObject, String> {

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
                JSONObject json = api.checkSource(params[0]);
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

                    if (StatusValue.compareTo("no") == 0) {
                        Toast.makeText(MainActivity.this, "No Stations Found", Toast.LENGTH_SHORT).show();
                        SourceName = "Na";

                        Speech_1("Source Station not found, speak the source station Name again");
                        ChatData.add("Source Station not found, speak the source station Name again");
                        ChatValueArray.add("sys");
                        adapter.notifyDataSetChanged();

                    } else if (StatusValue.compareTo("ok") == 0) {

                        SourceLidArray = new ArrayList<String>();
                        SourceLidNameArray = new ArrayList<String>();

                        JSONArray result = json.getJSONArray("Data");
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject res = result.getJSONObject(i);

                            //lid,lname
                            SourceLid = res.getString("data0");
                            SourceLidName = res.getString("data1");

                            SourceLidArray.add(res.getString("data0"));
                            SourceLidNameArray.add(res.getString("data1"));

                            if (SourceLidNameArray.size() > 1) {

                                String Statement = SourceName + " comes in " + SourceLidNameArray.size() + " lines. say 1 for " +
                                        SourceLidNameArray.get(0) + "and say 2 for " + SourceLidNameArray.get(1) + ".";

                                Speak_NewSourceLid(Statement);
                                ChatData.add(Statement);
                                ChatValueArray.add("sys");
                                adapter.notifyDataSetChanged();
                                listView.setSelection(ChatData.size());

                            } else {

                                tempSourceLid = SourceLid;
                                Speech_2("Source Location is " + SourceName + " .Speak Destination station Name");
                                ChatData.add("Source Location is " + SourceName);
                                ChatValueArray.add("sys");
                                adapter.notifyDataSetChanged();
                                listView.setSelection(ChatData.size());

                            }


                        }


                    }

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
    }


    public class CheckDestination_Task extends AsyncTask<String, JSONObject, String> {

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
                JSONObject json = api.checkSource(params[0]);
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

                    if (StatusValue.compareTo("no") == 0) {
                        DesName = "Na";

                        Speech_2("Destination Station not found, speak the Destination station Name again");
                        ChatData.add("Destination Station not found, Try again !");
                        ChatValueArray.add("sys");
                        adapter.notifyDataSetChanged();
                        listView.setSelection(ChatData.size());

                    } else if (StatusValue.compareTo("ok") == 0) {

                        DesLidArray = new ArrayList<String>();
                        DesLidNameArray = new ArrayList<String>();

                        JSONArray result = json.getJSONArray("Data");
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject res = result.getJSONObject(i);

                            //lid,lname
                            DesLid = res.getString("data0");
                            DesLidName = res.getString("data1");

                            DesLidArray.add(res.getString("data0"));
                            DesLidNameArray.add(res.getString("data1"));

                            boolean checkres = false;
                            boolean rvalue = false;

                            if (tempSourceLid.compareTo("") != 0) {

                                for (int a = 0; a < DesLidArray.size(); a++) {

                                    if (DesLidArray.get(a).compareTo(tempSourceLid) == 0) {
                                        DesLid = DesLidArray.get(a);
                                        DesLidName = DesLidNameArray.get(a);
//                                        Toast.makeText(MainActivity.this, DesLid + "\n" + DesLidName, Toast.LENGTH_SHORT).show();

                                        checkres = true;
                                        if (!rvalue) {
                                            Speech_ResultAPI("Destination Location is " + DesName);
                                            ChatData.add("Destination Location is " + DesName);
                                            ChatValueArray.add("sys");
                                            adapter.notifyDataSetChanged();
                                            listView.setSelection(ChatData.size());
                                            rvalue = true;
                                        }

                                        break;
                                    }
                                }


                            }


                            if (!checkres) {

                                if (DesLidNameArray.size() > 1) {

                                    String Statement = DesName + " comes in " + DesLidNameArray.size() + " lines. say 1 for " +
                                            DesLidNameArray.get(0) + "and say 2 for " + DesLidNameArray.get(1) + ".";

                                    Speak_NewDesLid(Statement);
                                    ChatData.add(Statement);
                                    ChatValueArray.add("sys");
                                    adapter.notifyDataSetChanged();
                                    listView.setSelection(ChatData.size());

                                } else {

//                                    Toast.makeText(MainActivity.this, DesLidNameArray.size()+"\n"+"In else", Toast.LENGTH_SHORT).show();
                                    Speech_ResultAPI("Destination Location is " + DesName);
                                    ChatData.add("Destination Location is " + DesName);
                                    ChatValueArray.add("sys");
                                    adapter.notifyDataSetChanged();
                                    listView.setSelection(ChatData.size());

                                }

                            }


                        }


                    }

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
    }


    public void Speak_NewSourceLid(String text) {

        new Speak_NewSourceLid().execute(text);
    }

    public class Speak_NewSourceLid extends AsyncTask<String, Void, MediaPlayer> {

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
                mediaPlayer.setDataSource(MainActivity.this, uri);
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

                mediaPlayer = null;
            }

            return mediaPlayer;
        }

        @Override
        protected void onPostExecute(final MediaPlayer s) {
            super.onPostExecute(s);

            if (s != null) {
                s.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        s.stop();
                        s.reset();

                        VoiceToText_NewSourceLid();

                    }
                });
            }
        }
    }

    public void VoiceToText_NewSourceLid() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

        try {
            startActivityForResult(intent, MANY_SOURCE);

        } catch (ActivityNotFoundException a) {
            Toast t = Toast.makeText(getApplicationContext(),
                    "Opps! Your device doesn't support Speech to Text",
                    Toast.LENGTH_SHORT);
            t.show();
        }
    }


    public void Speak_NewDesLid(String text) {

        new Speak_NewDesLid().execute(text);
    }

    public class Speak_NewDesLid extends AsyncTask<String, Void, MediaPlayer> {

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
                mediaPlayer.setDataSource(MainActivity.this, uri);
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

                mediaPlayer = null;
            }

            return mediaPlayer;
        }

        @Override
        protected void onPostExecute(final MediaPlayer s) {
            super.onPostExecute(s);

            if (s != null) {
                s.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        s.stop();
                        s.reset();

                        VoiceToText_NewDesLid();

                    }
                });
            }
        }
    }

    public void VoiceToText_NewDesLid() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

        try {
            startActivityForResult(intent, MANY_DESTINATION);

        } catch (ActivityNotFoundException a) {
            Toast t = Toast.makeText(getApplicationContext(),
                    "Opps! Your device doesn't support Speech to Text",
                    Toast.LENGTH_SHORT);
            t.show();
        }
    }


    public void Speech_2(String text) {

        new Speech2().execute(text);
    }

    public class Speech2 extends AsyncTask<String, Void, MediaPlayer> {

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
                mediaPlayer.setDataSource(MainActivity.this, uri);
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

                mediaPlayer = null;
            }

            return mediaPlayer;
        }

        @Override
        protected void onPostExecute(final MediaPlayer s) {
            super.onPostExecute(s);

            if (s != null) {
                s.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        s.stop();
                        s.reset();

                        VoiceToTextSearch_Destination();

                    }
                });
            }
        }
    }


    public void Speech_Information(String text) {

        new SpeechInformation().execute(text);
    }

    public class SpeechInformation extends AsyncTask<String, Void, MediaPlayer> {

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
                mediaPlayer.setDataSource(MainActivity.this, uri);
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

                mediaPlayer = null;
            }

            return mediaPlayer;
        }

        @Override
        protected void onPostExecute(final MediaPlayer s) {
            super.onPostExecute(s);

            if (s != null) {
                s.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        s.stop();
                        s.reset();

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                Last_SelectionOption("If you want to search again say 1 and for exit the app say 2");
                                ChatData.add("If you want to search again say 1 and for exit the app say 2");
                                ChatValueArray.add("sys");
                                adapter.notifyDataSetChanged();
                                listView.setSelection(ChatData.size());

                            }
                        }, 3000);


                    }
                });
            }
        }
    }


    public void Speech_3(String text) {

        new Speech3().execute(text);
    }

    public class Speech3 extends AsyncTask<String, Void, MediaPlayer> {

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
                mediaPlayer.setDataSource(MainActivity.this, uri);
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

                mediaPlayer = null;
            }

            return mediaPlayer;
        }

        @Override
        protected void onPostExecute(final MediaPlayer s) {
            super.onPostExecute(s);

            if (s != null) {
                s.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        s.stop();
                        s.reset();

                        VoiceToTextSearch_ChooseTrain();
                    }
                });
            }
        }
    }

    public void VoiceToTextSearch_ChooseTrain() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

        try {
            startActivityForResult(intent, CHOOSE_TRAIN_SELECTION);

        } catch (ActivityNotFoundException a) {
            Toast t = Toast.makeText(getApplicationContext(),
                    "Opps! Your device doesn't support Speech to Text",
                    Toast.LENGTH_SHORT);
            t.show();
        }
    }

    public void VoiceToTextSearch_Destination() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

        try {
            startActivityForResult(intent, RESULT_DESTINATION_SPEECH);

        } catch (ActivityNotFoundException a) {
            Toast t = Toast.makeText(getApplicationContext(),
                    "Opps! Your device doesn't support Speech to Text",
                    Toast.LENGTH_SHORT);
            t.show();
        }
    }

    public void Speech_ResultAPI(String text) {

        new Speech_ResultAPI().execute(text);
    }


//    public void Speech_CommonStaton(String text) {
//
//        new Speech_CommonStaton().execute(text);
//    }
//
//    public class Speech_CommonStaton extends AsyncTask<String, Void, MediaPlayer> {
//
//        String a = "back";
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected MediaPlayer doInBackground(String... params) {
//
//            try {
//                String Url = "https://translate.google.com/translate_tts?ie=UTF-8";
//                String pronouce = "&q=" + params[0].replaceAll(" ", "%20");
//                String language = "&tl=hi";
//                String web = "&client=tw-ob";
//
//                String fullUrl = Url + pronouce + language + web;
//                Uri uri = Uri.parse(fullUrl);
//                mediaPlayer = new MediaPlayer();
//                mediaPlayer.setDataSource(MainActivity.this, uri);
////                mediaPlayer.prepare();
////                mediaPlayer.start();
//                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                    @Override
//                    public void onPrepared(MediaPlayer mp) {
//
//                        mediaPlayer.start();
//                    }
//                });
//                mediaPlayer.prepareAsync();
//
//            } catch (Exception e) {
//
//                mediaPlayer = null;
//            }
//
//            return mediaPlayer;
//        }
//
//        @Override
//        protected void onPostExecute(final MediaPlayer s) {
//            super.onPostExecute(s);
//
//            if (s != null) {
//                s.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mp) {
//
//                        s.stop();
//                        s.reset();
//
//                        VoiceToTextSearch_CommonStaton();
//                    }
//                });
//            }
//        }
//    }
//
//    public void VoiceToTextSearch_CommonStaton() {
//
//        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
//
//        try {
//            startActivityForResult(intent, CHOOSE_COMMON_STATON);
//
//        } catch (ActivityNotFoundException a) {
//            Toast t = Toast.makeText(getApplicationContext(),
//                    "Opps! Your device doesn't support Speech to Text",
//                    Toast.LENGTH_SHORT);
//            t.show();
//        }
//    }


    public class Speech_ResultAPI extends AsyncTask<String, Void, MediaPlayer> {

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
                mediaPlayer.setDataSource(MainActivity.this, uri);
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

                mediaPlayer = null;
            }

            return mediaPlayer;
        }

        @Override
        protected void onPostExecute(final MediaPlayer s) {
            super.onPostExecute(s);

            if (s != null) {
                s.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        s.stop();
                        s.reset();

                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.MINUTE, 5);
                        String time = sdfd.format(cal.getTime());

//                        Toast.makeText(MainActivity.this, "Get TrainTask"+"\n"+SourceName+"\n"+SourceLid+"\n"+
//                                DesName+"\n"+DesLid, Toast.LENGTH_SHORT).show();
                        //string src,string slid, string dest,string dlid,string time
                        new getTrain_Task().execute(SourceName, SourceLid, DesName, DesLid, time);


                    }
                });
            }
        }
    }

    public class getTrain_Task extends AsyncTask<String, JSONObject, String> {

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
                JSONObject json = api.getTrains(params[0], params[1], params[2], params[3], params[4]);
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

//            Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();

            TidArray = new ArrayList<String>();
            LidArray = new ArrayList<String>();
            SequenceArray = new ArrayList<String>();
            StationsArray = new ArrayList<String>();
            PlatformArray = new ArrayList<String>();
            TimeArray = new ArrayList<String>();


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

                    ArrayList<String> AllDataArray = new ArrayList<String>();

                    if (s.contains("#")) {
                        CommonStationArray = new ArrayList<String>();
                        AllStationDataArray = new ArrayList<String>();

                        String temp[] = s.split("\\#");
                        for (int i = 0; i < temp.length; i++) {
                            AllDataArray.add(temp[i]);
                        }

                        String temps[] = null;
                        for (int i = 0; i < AllDataArray.size(); i++) {

                            temps = AllDataArray.get(i).split("\\*");
                            CommonStationArray.add(temps[0]);
                            AllStationDataArray.add(temps[1]);

                            NewSource = CommonStationArray.get(0);
                        }

//                        Toast.makeText(MainActivity.this, "New Source-  "+NewSource, Toast.LENGTH_SHORT).show();
                        try {
                            JSONObject json = new JSONObject(AllStationDataArray.get(0));
                            String StatusValue = json.getString("status");

                            if (StatusValue.compareTo("no") == 0) {
                                Toast.makeText(MainActivity.this, "No Destination Stations Found", Toast.LENGTH_SHORT).show();
                                DesName = "Na";

                                Last_SelectionOption("No trains available.If you want to search again say 1 and for exit the app say 2");
                                ChatData.add("No trains available.If you want to search again say 1 and for exit the app say 2");
                                ChatValueArray.add("sys");
                                adapter.notifyDataSetChanged();


                            } else if (StatusValue.compareTo("ok") == 0) {
                                CTidArray = new ArrayList<String>();
                                CLidArray = new ArrayList<String>();
                                CSequenceArray = new ArrayList<String>();
                                CStationsArray = new ArrayList<String>();
                                CPlatformArray = new ArrayList<String>();
                                CTimeArray = new ArrayList<String>();


                                JSONArray result = json.getJSONArray("Data");
                                for (int i = 0; i < result.length(); i++) {
                                    JSONObject res = result.getJSONObject(i);

//                                      tid,lid,sequence,station,platform,time
                                    CTidArray.add(res.getString("data0"));
                                    CLidArray.add(res.getString("data1"));
                                    CSequenceArray.add(res.getString("data2"));
                                    CStationsArray.add(res.getString("data3"));
                                    CPlatformArray.add(res.getString("data4"));
                                    CTimeArray.add(res.getString("data5"));
                                }

                                new getCommonTrainTiming_Task().execute(CTidArray.get(0));



                            }

                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Common: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }


//                        String ChooseCommonStation = ChooseCommonStation_Funtion(TidArray.size());
//                        Speech_CommonStaton(ChooseCommonStation);
//
//                        ChatData.add(ChooseCommonStation);
//                        ChatValueArray.add("sys");
//                        adapter.notifyDataSetChanged();


                    } else {

//                        Toast.makeText(MainActivity.this, "In else", Toast.LENGTH_SHORT).show();

                        JSONObject json = new JSONObject(s);
                        String StatusValue = json.getString("status");

                        if (StatusValue.compareTo("no") == 0) {
                            Toast.makeText(MainActivity.this, "No Destination Stations Found", Toast.LENGTH_SHORT).show();
                            DesName = "Na";

                            Last_SelectionOption("No trains available.If you want to search again say 1 and for exit the app say 2");
                            ChatData.add("No trains available. If you want to search again say 1 and for exit the app say 2");
                            ChatValueArray.add("sys");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());


                        } else if (StatusValue.compareTo("ok") == 0) {
                            JSONArray result = json.getJSONArray("Data");
                            for (int i = 0; i < result.length(); i++) {
                                JSONObject res = result.getJSONObject(i);

//                            tid,lid,sequence,station,platform,time
                                TidArray.add(res.getString("data0"));
                                LidArray.add(res.getString("data1"));
                                SequenceArray.add(res.getString("data2"));
                                StationsArray.add(res.getString("data3"));
                                PlatformArray.add(res.getString("data4"));
                                TimeArray.add(res.getString("data5"));
                            }

                            String ChooseTrain = ChooseTrain_Funtion(TidArray.size());
                            Speech_3(ChooseTrain);

                            ChatData.add(ChooseTrain);
                            ChatValueArray.add("sys");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());

                        }

                    }


                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

            SelectedStationArray = new ArrayList<String>();
            SelectedPlatformArray = new ArrayList<String>();
            SelectedTimeArray = new ArrayList<String>();
            FilterStationsArray = new ArrayList<String>();

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

                    if (StatusValue.compareTo("no") == 0) {

                        Last_SelectionOption("No trains available.If you want to search again say 1 and for exit the app say 2");
                        ChatData.add("No trains available.If you want to search again say 1 and for exit the app say 2");
                        ChatValueArray.add("sys");
                        adapter.notifyDataSetChanged();
                        listView.setSelection(ChatData.size());


                    } else if (StatusValue.compareTo("ok") == 0) {

                        JSONArray result = json.getJSONArray("Data");
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject res = result.getJSONObject(i);

                            //stations,platforms,time
                            SelectedStationArray.add(res.getString("data0"));
                            SelectedPlatformArray.add(res.getString("data1"));
                            SelectedTimeArray.add(res.getString("data2"));
                        }


                        boolean ans = false;
                        for (int i = 0; i < SelectedStationArray.size(); i++) {
                            if (!ans) {
                                if (SourceName.compareTo(SelectedStationArray.get(i)) == 0) {
                                    FilterStationsArray.add(SelectedStationArray.get(i));
                                    ans = true;
                                    sPosition = i;
                                }
                            }
                            if (ans) {
                                FilterStationsArray.add(SelectedStationArray.get(i));
                                if (DesName.compareTo(SelectedStationArray.get(i)) == 0) {
                                    ans = false;
                                    dPosition = i;
                                }
                            }
                        }


                        String stations = "";
                        for (int i = 2; i < FilterStationsArray.size() - 1; i++) {
                            stations += FilterStationsArray.get(i) + ",";
                        }

                        Speech_Information("Train Departure from " + FilterStationsArray.get(0) + " at " + SelectedTimeArray.get(sPosition) + " and it will " +
                                "reach " + FilterStationsArray.get(FilterStationsArray.size() - 1) + " at " + SelectedTimeArray.get(dPosition) + ". The following stations coming between this " +
                                stations);

                        ChatData.add("Train Departure from " + FilterStationsArray.get(0) + " at " + SelectedTimeArray.get(sPosition) + " and it will " +
                                "reach " + FilterStationsArray.get(FilterStationsArray.size() - 1) + " at " + SelectedTimeArray.get(dPosition) + ". The following stations coming between this " +
                                stations);
                        ChatValueArray.add("sys");
                        adapter.notifyDataSetChanged();
                        listView.setSelection(ChatData.size());


                    }

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
    }

    public void Last_SelectionOption(String text) {

        new Last_SelectionOption().execute(text);
    }

    public class Last_SelectionOption extends AsyncTask<String, Void, MediaPlayer> {

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
                mediaPlayer.setDataSource(MainActivity.this, uri);
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

                mediaPlayer = null;
            }

            return mediaPlayer;
        }

        @Override
        protected void onPostExecute(final MediaPlayer s) {
            super.onPostExecute(s);

            if (s != null) {
                s.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        s.stop();
                        s.reset();


                        Last_SelectionOption();

                    }
                });
            }
        }

        ;
    }

    public void Last_SelectionOption() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

        try {
            startActivityForResult(intent, LAST_OPTION);

        } catch (ActivityNotFoundException a) {
            Toast t = Toast.makeText(getApplicationContext(),
                    "Opps! Your device doesn't support Speech to Text",
                    Toast.LENGTH_SHORT);
            t.show();
        }
    }

    public String ChooseTrain_Funtion(int count) {

        String Statement = "";

        if (count == 1) {
            Statement = "one train is available at " + TimeArray.get(0) + ". Say 1 to get train details";

        } else if (count == 2) {
            Statement = "Two trains are available at " + TimeArray.get(0) + " and " + TimeArray.get(1)
                    + ". Say 1 to get first train details,Say 2 to get second second train details";

        } else if (count == 3) {
            Statement = "Three trains are available at " + TimeArray.get(0) + "," + TimeArray.get(1) + " and " + TimeArray.get(2) +
                    ". Say 1 to get first train details,Say 2 to get second train details" +
                    " ,Say 3 to get third train details";

        } else if (count >= 4) {
            Statement = "Four trains are available at " + TimeArray.get(0) + "," + TimeArray.get(1) + "," + TimeArray.get(2) + " and " +
                    TimeArray.get(3) + ". Say 1 to get first train details,Say 2 to get second train details" +
                    " ,Say 3 to get third train details, Say 4 to get fourth train detail";
        }

        return Statement;
    }

    public String ChooseCTrain_Funtion(int count) {

        String Statement = "";

        if (count == 1) {
            Statement = "one train is available at " + CTimeArray.get(0) + ". Say 1 to get train details";

        } else if (count == 2) {
            Statement = "Two trains are available at " + CTimeArray.get(0) + " and " + CTimeArray.get(1)
                    + ". Say 1 to get first train details,Say 2 to get second second train details";

        } else if (count == 3) {
            Statement = "Three trains are available at " + CTimeArray.get(0) + "," + CTimeArray.get(1) + " and " + CTimeArray.get(2) +
                    ". Say 1 to get first train details,Say 2 to get second train details" +
                    " ,Say 3 to get third train details";

        } else if (count >= 4) {
            Statement = "Four trains are available at " + CTimeArray.get(0) + "," + CTimeArray.get(1) + "," + CTimeArray.get(2) + " and " +
                    CTimeArray.get(3) + ". Say 1 to get first train details,Say 2 to get second train details" +
                    " ,Say 3 to get third train details, Say 4 to get fourth train detail";
        }

        return Statement;
    }

    public String ChooseCommonStation_Funtion(int count) {

        String Statement = "";

        if (count == 1) {
            Statement = "one route is available via " + CommonStationArray.get(0) + ". Say 1 to select route";

        } else if (count >= 2) {
            Statement = "two routes are available via " + CommonStationArray.get(0) + " and " + CommonStationArray.get(1)
                    + ". Say 1 to select first route, Say 2 to select second route";
        }

        return Statement;
    }

    public void Speech_CommonTrainSelection(String text) {

        new Speech_CommonTrainSelection().execute(text);
    }

    public class Speech_CommonTrainSelection extends AsyncTask<String, Void, MediaPlayer> {

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
                mediaPlayer.setDataSource(MainActivity.this, uri);
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

                mediaPlayer = null;
            }

            return mediaPlayer;
        }

        @Override
        protected void onPostExecute(final MediaPlayer s) {
            super.onPostExecute(s);

            if (s != null) {
                s.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        s.stop();
                        s.reset();

                        VoiceToTextSearch_ChooseCommonTrain();
                    }
                });
            }
        }
    }

    public void VoiceToTextSearch_ChooseCommonTrain() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

        try {
            startActivityForResult(intent, CHOOSE_COMMON_TRAIN);

        } catch (ActivityNotFoundException a) {
            Toast t = Toast.makeText(getApplicationContext(),
                    "Opps! Your device doesn't support Speech to Text",
                    Toast.LENGTH_SHORT);
            t.show();
        }
    }


    public class getTrainRoute_Task extends AsyncTask<String, JSONObject, String> {

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
                JSONObject json = api.getTrainRoute(params[0], params[1], params[2]);
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

//            Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();

            SelectedStationArray = new ArrayList<String>();
            SelectedPlatformArray = new ArrayList<String>();
            SelectedTimeArray = new ArrayList<String>();
            FilterStationsArray = new ArrayList<String>();

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

                    if (StatusValue.compareTo("no") == 0) {

                        Last_SelectionOption("No trains available.If you want to search again say 1 and for exit the app say 2");
                        ChatData.add("No trains available.If you want to search again say 1 and for exit the app say 2");
                        ChatValueArray.add("sys");
                        adapter.notifyDataSetChanged();
                        listView.setSelection(ChatData.size());


                    } else if (StatusValue.compareTo("ok") == 0) {

                        JSONArray result = json.getJSONArray("Data");
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject res = result.getJSONObject(i);

                            //stations,platforms,time
                            SelectedStationArray.add(res.getString("data0"));
                            SelectedPlatformArray.add(res.getString("data1"));
                            SelectedTimeArray.add(res.getString("data2"));
                        }

                        new geCommonTrainStationDetail_Task().execute(SelectedStationArray.get(0));


                    }

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Route" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
    }


    public class geCommonTrainStationDetail_Task extends AsyncTask<String, JSONObject, String> {

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

            SelectedStationArray = new ArrayList<String>();
            SelectedPlatformArray = new ArrayList<String>();
            SelectedTimeArray = new ArrayList<String>();
            FilterStationsArray = new ArrayList<String>();

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

                    if (StatusValue.compareTo("no") == 0) {

                        Last_SelectionOption("No trains available.If you want to search again say 1 and for exit the app say 2");
                        ChatData.add("No trains available.If you want to search again say 1 and for exit the app say 2");
                        ChatValueArray.add("sys");
                        adapter.notifyDataSetChanged();
                        listView.setSelection(ChatData.size());


                    } else if (StatusValue.compareTo("ok") == 0) {

                        JSONArray result = json.getJSONArray("Data");
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject res = result.getJSONObject(i);

                            //stations,platforms,time
                            SelectedStationArray.add(res.getString("data0"));
                            SelectedPlatformArray.add(res.getString("data1"));
                            SelectedTimeArray.add(res.getString("data2"));
                        }


                        boolean ans = false;
                        for (int i = 0; i < SelectedStationArray.size(); i++) {
                            if (!ans) {
                                if (NewSource.compareTo(SelectedStationArray.get(i)) == 0) {
                                    FilterStationsArray.add(SelectedStationArray.get(i));
                                    ans = true;
                                    sPosition = i;
                                }
                            }
                            if (ans) {
                                FilterStationsArray.add(SelectedStationArray.get(i));
                                if (DesName.compareTo(SelectedStationArray.get(i)) == 0) {
                                    ans = false;
                                    dPosition = i;
                                }
                            }
                        }


                        String stations = "";
                        for (int i = 2; i < FilterStationsArray.size() - 1; i++) {
                            stations += FilterStationsArray.get(i) + ",";
                        }


                        if (FilterStationsArray.size() > 1) {
                            Speech_Information("Train Departure from "+SourceName +" and it will reach "+FilterStationsArray.get(0) +" at "+
                                    NewSTiming+" and it will Arrive "+ FilterStationsArray.get(FilterStationsArray.size() - 1) +" at "+ SelectedTimeArray.get(dPosition));

//                            ChatData.add("Train Departure from " + FilterStationsArray.get(0) + " at " + SelectedTimeArray.get(sPosition) + " and it will " +
//                                    "reach " + FilterStationsArray.get(FilterStationsArray.size() - 1) + " at " + SelectedTimeArray.get(dPosition) + ". The following stations coming between this " +
//                                    stations);

                            ChatData.add("Train Departure from "+SourceName +" and it will reach "+FilterStationsArray.get(0) +" at "+
                            NewSTiming+" and it will Arrive "+ FilterStationsArray.get(FilterStationsArray.size() - 1) +" at "+ SelectedTimeArray.get(dPosition));
                            ChatValueArray.add("sys");
                            adapter.notifyDataSetChanged();
                            listView.setSelection(ChatData.size());
                        } else {

                            Toast.makeText(MainActivity.this, "There is no train between this", Toast.LENGTH_SHORT).show();
                        }


                    }

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
    }


    public class getCommonTrainTiming_Task extends AsyncTask<String, JSONObject, String> {

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
//            Toast.makeText(MainActivity.this, "For Common Train"+"\n"+s, Toast.LENGTH_SHORT).show();

            ArrayList<String> cStation = new ArrayList<String>();
            ArrayList<String> cPlatform = new ArrayList<String>();
            ArrayList<String> cTiming  = new ArrayList<String>();

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

                    if (StatusValue.compareTo("no") == 0) {

                        Last_SelectionOption("No trains available" + "\n" + "If you want to search again say 1 and for exit the app say 2");
                        ChatData.add("No trains available" + "\n" + "If you want to search again say 1 and for exit the app say 2");
                        ChatValueArray.add("sys");
                        adapter.notifyDataSetChanged();
                        listView.setSelection(ChatData.size());


                    } else if (StatusValue.compareTo("ok") == 0) {

                        JSONArray result = json.getJSONArray("Data");
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject res = result.getJSONObject(i);

                            //stations,platforms,time
                            cStation.add(res.getString("data0"));
                            cPlatform.add(res.getString("data1"));
                            cTiming.add(res.getString("data2"));
                        }

                        for (int i=0;i<cPlatform.size();i++){

                            if (NewSource.compareTo(cStation.get(i))==0){
                                NewSTiming = cTiming.get(i);
                                break;
                            }
                        }

                        String ChooseTrain = ChooseCTrain_Funtion(CTidArray.size());
                        Speech_CommonTrainSelection(ChooseTrain);
                        ChatData.add(ChooseTrain);
                        ChatValueArray.add("sys");
                        adapter.notifyDataSetChanged();
                        listView.setSelection(ChatData.size());


                    }

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

        }
    }


}
