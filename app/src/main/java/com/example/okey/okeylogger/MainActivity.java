package com.example.okey.okeylogger;

import android.Manifest;
import android.app.DialogFragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import in.gauriinfotech.commons.Commons;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener, MyDialogFragment.NoticeDialogListener{// SingleChoiceDialogFragment.ChoiceDialogListener {

    private Intent loadIntent;

    private String testName = "";
    private String keyboardType = "";
    private String tempKeyboard = "";
    private long phraseCount = 0;
    private long currentPhraseCount = 0;
    private boolean showTime = false;
    private boolean showResults = false;
    private boolean repeatPhrase = false;
    private boolean moveCursor = false;
    private Handler handler;
    private int isPhrasewritten = 0;
    AlertDialog sendDialog;

    private String filePatha;
    private String filePathb;
    private String row;
    private String interStyle, orientation, phraseSrc, phraseID;
    private String mUsername = "";
    private String  inStream = "";

    private Context ctx;

    private SqlDatabase phraseDB;
    private SqlDatabase sourceDB;

    private TextView readPhrase;
    private EditText writePhrase;
    private TextView usernameView;
    private TextView showTimeView;
    private TextView phraseCountView;
    private TextView showResultsView;

    private  FileWriter writer;

    private long startTime;
    //private long endTime;
    private double diffTime;
    private int phraseLenght;

    private char current;

    private boolean timerStart = false;

    private File logFile;

    public String givenPhrase="";
    public String submittedPhrase="";
    public int bckspccnt=0;
    public int msd=0;
    public int incfix=0;
    public int correct=0;
    public float ter=0;
    public float cer=0;
    public float ncer=0;
    public int IFc=0;
    public int IFe=0;
    public double wpmcorrect =0;
    public double wpmtrans=0;
    public float cbrer=0;
    public float cawer=0;
    //public int subPhraseLength=0;

    public int distance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        // i == 0
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ctx = getApplicationContext();

        //instance of SqlDatabase class
        // Using constructor with context argument for default
        // database (if no source file is loaded)
        phraseDB = new SqlDatabase(ctx);
        SQLiteDatabase db = phraseDB.getWritableDatabase();

        checkFile();

        // write phrase edit text initialization
        // and read phrase edit text initialization
        writePhrase = (EditText) findViewById(R.id.writePhraseBox);
        readPhrase = (TextView) findViewById(R.id.readPhraseBox);
        usernameView = (TextView) findViewById(R.id.username);
        showTimeView = (TextView) findViewById(R.id.showTime);
        phraseCountView = (TextView) findViewById(R.id.phraseCount);
        showResultsView = (TextView) findViewById(R.id.textViewShowRes);
        handler = new Handler() ;
        readPhrase.setText("Tap here to get a random phrase.");

        // setting some flags to false
        // so the user isn't able to edit the phrase
        readPhrase.setActivated(false);
        readPhrase.setFocusable(false);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        // setting touch listener to handle touch events
        readPhrase.setOnTouchListener(this);
        // onTextChangedListener used to catch events before, during and after editing
        writePhrase.addTextChangedListener(new TextWatcher() {

            //reads the phrase lenght before text change
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //capture phrase lenght
                phraseLenght = writePhrase.length();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //if return button is pressed
                if (s.length()>0 && s.subSequence(s.length()-1, s.length()).toString().equalsIgnoreCase("\n")) {
                    //check if empty string is in edittextbox
                    if(inStream.length() == 0){
                        Toast.makeText(MainActivity.this, "Can't submit empty string", Toast.LENGTH_SHORT).show();
                        dummyDetected();
                    }
                    else {

                            //clear the edit text and finalize input
                            //before submitting we get the final phrase in textbox and length
                            givenPhrase = readPhrase.getText().toString();
                            Log.d("zadana", givenPhrase);
                            submittedPhrase = writePhrase.getText().toString();
                            submittedPhrase = submittedPhrase.replace("\n", "");
                            //subPhraseLength = submittedPhrase.length();
                            Log.d("zavrsna", submittedPhrase);
                            //Log.d("zavrsna duljina", Integer.toString(subPhraseLength));

                            //levenstein MSD, INF metric
                            msd = distance(givenPhrase, submittedPhrase);
                            Log.d("leven", Integer.toString(msd));
                            //C metric
                            correct = Math.max(givenPhrase.length(), submittedPhrase.length()) - msd;
                            Log.d("Correct", Integer.toString(correct));
                            //IF metric = incfix = IFc+IFe
                            int IFIFeIFc[] = calculateIF(givenPhrase, inStream);
                            incfix = IFIFeIFc[0];
                            IFe = IFIFeIFc[1];
                            IFc = IFIFeIFc[2];
                            Log.d("IncFIXED", Integer.toString(incfix));
                            //corrected but right error rate
                            cbrer = (((float) IFc) / (correct + msd + incfix)) * 100;
                            Log.d("CBRER", Float.toString(cbrer));
                            //corrected and wrong error rate
                            cawer = (((float) IFe) / (correct + msd + incfix)) * 100;
                            Log.d("CAWER", Float.toString(cawer));
                            //total error rate
                            ter = (((float) msd + incfix) / (correct + msd + incfix)) * 100;
                            Log.d("TER", Float.toString(ter));
                            //corrected error rate
                            cer = (((float) incfix) / (correct + msd + incfix)) * 100;
                            Log.d("CER", Float.toString(cer));
                            //not corrected error rate
                            ncer = (((float) msd) / (correct + msd + incfix)) * 100;
                            Log.d("NCER", Float.toString(ncer));

                            //clean the box and call the finishInput function
                            finishInput();
                            currentPhraseCount++;
                            phraseCountView.setText("Phrase count: " + currentPhraseCount + "/" + phraseCount);
                            writePhrase.getText().clear();

                    }
                } else if (phraseLenght > writePhrase.length() && timerStart) {

                    //if user hits backspace, log it
                    inStream = inStream + "[backspace]";
                    Log.d("tag", inStream);
                    //F metric
                    bckspccnt=bckspccnt+1;
                    Log.d("bckspcounter", Integer.toString(bckspccnt));
                }
                else {
                    if (phraseLenght != 0 && writePhrase.length() == 0 && inStream.length() == 0) {
                        Log.d("End","Finished!!");
                        current = '\0';

                    } else if (!timerStart || inStream.length() == 0 ) {
                        startTime = SystemClock.elapsedRealtime();
                        timerStart = true;
                        handler.postDelayed(runable,0);
                        Log.d("tag", "time started");
                    }
                    //filling in the char instream
                    inStream = inStream + current;
                    Log.d("tag", inStream);
                    isPhraseOver();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (readPhrase.getText() == "Tap here to get a random phrase."){
                    //check if user is a dummy
                    // don't allow to submit placeholder text, and timer to start
                    //remind the dummy he has to tap to get a phrase
                    Toast.makeText(MainActivity.this, "Tap to get a phrase!", Toast.LENGTH_SHORT).show();
                    dummyDetected();
                }else {
                    if (mUsername.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please, enter your username.", Toast.LENGTH_SHORT).show();
                        dummyDetected();
                    } else {
                        if (testName == ""){
                            Toast.makeText(MainActivity.this, "Please select number of phrases for input", Toast.LENGTH_SHORT).show();
                            dummyDetected();
                        }
                        else if (currentPhraseCount == phraseCount){
                            Toast.makeText(MainActivity.this, "You have written all phrases for this test", Toast.LENGTH_LONG).show();
                            dummyDetected();
                        }
                        else if (isPhrasewritten == 1 && writePhrase.length() !=0){
                            Toast.makeText(MainActivity.this, "You have allready written this phrase, click to get new one", Toast.LENGTH_SHORT).show();
                            dummyDetected();
                        }
                        if (s.length() > start) {
                            current = s.charAt(start);
                        }

                    }

                }

            }
        });

        writePhrase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (moveCursor == true){
                    writePhrase.setSelection(writePhrase.getText().length());
                }

            }
        });

    }

    @NonNull
    private int [] calculateIF(String prvi, String drugi){

        int j=0;
        int counterIF=0;
        int counterCF=0;

        //check if last one was backspace
        boolean prosli=false;

        //change [backspace] with * in inStream for easier usage
        drugi=drugi.replace("[backspace]","*");
        //Log.d("promjena",drugi);

        //string to array, givenstring & instream
        char[] first  = prvi.toLowerCase().toCharArray();
        char[] second = drugi.toLowerCase().toCharArray(); //instream

        //lenght of first string
        int firstlen= first.length;
        //zapis tocnosti u polje velicine zadanog stringa (za svaki char)
        boolean[] tocnost=new boolean[firstlen];

        //lenght of second string
        int secondlen=second.length;

        for(int i = 1; i <= firstlen; i++){
            //if the second string is shorter (ends sooner) than the first -> break the loop
            if(j==secondlen) break;

            //if not backspace check for equality of chars
            //first cant be backspace
            if(second[j]!='*') {
                prosli=false;
                //check if chars match , i changes in for, j starts at 0
                if (first[i-1] == second[j]) {
                    tocnost[i-1] = true;
                    j++;
                    if(j==secondlen) break;
                } else {
                    tocnost[i-1] = false;
                    j++;
                    if(j==secondlen) break;
                }
            }
            //check if next char instream is bckspace ->*

            if(second[j]=='*'){
                //if it is backspace, keep the i value in the next iteration on the same spot
                //go to next spot in instream and count that (in)correct sign was deleted
                if(!prosli) {
                    if (tocnost[i-1]) counterCF++;
                    else if (!tocnost[i-1]) counterIF++;
                    i--;
                    j++;
                    if(j==secondlen) break;
                }
                if(prosli) {
                    i--;
                    if (tocnost[i-1]) counterCF++;
                    else if (!tocnost[i-1]) counterIF++;
                    i--;
                    j++;
                    if(j==secondlen) break;
                }
                prosli=true;
            }
        }
        //if second string has more text than first,for loop ends before we check every char
        //EX: first: DOG second:DOF*GE   ,foor loop wont get the E
        //check if j is smaller than lenght of string, if true go through the rest of string
        //assume that every character after is incorrect so every deletion is IF
        if(j<secondlen){
            for(;j<secondlen;j++){
                if(second[j]=='*') counterIF++;
            }
        }

        //return IF , IFc (incorrect fixed - correct) and IFe(incorrect fixed - wrong)
        Log.d("IF",Integer.toString((counterIF+counterCF)));
        Log.d("IFe",Integer.toString(counterIF));
        Log.d("IFc",Integer.toString(counterCF));
        return new int[] {(counterCF+counterIF),counterIF, counterCF};
    }

    private void finishInput() {

        if (inStream.length() != 0) {

            //capture time
            handler.removeCallbacks(runable);
            if (showTime == false) diffTime = (SystemClock.elapsedRealtime() - startTime) / 1000.0;
            //   Toast.makeText(MainActivity.this, "Finished in " + diffTime + " s.", Toast.LENGTH_SHORT).show();

            //WPM counting only correct characters
            //-1 because timer starts after the first input
            Log.d("time",Double.toString(diffTime));
            wpmcorrect =((correct-1)/diffTime)*12;
            Log.d("WPMcorrect",Double.toString(wpmcorrect));
            //WPM counting transcribed characters
            //-1 because timer starts after the first input
            wpmtrans =((submittedPhrase.length()-1)/diffTime)*12;
            Log.d("WPMtrans",Double.toString(wpmtrans));


            final long time = System.currentTimeMillis();
            final java.sql.Timestamp tstmp = new java.sql.Timestamp(time);
            final String timestamp = tstmp.toString();

            //get the phrase id
            phraseID = phraseDB.getPhraseID();
            Log.d("tag", "time = " + phraseID + "s");

            // write everything to log.csv file
            try {
                FileWriter phraseWriter = new FileWriter(logFile, true);
                BufferedWriter br = new BufferedWriter(phraseWriter);
                String str = timestamp + "," + testName + "," + keyboardType + "," + mUsername + "," + orientation + "," + interStyle + ","
                        + phraseID + "," + row + "," + String.valueOf(row.length()) + "," + inStream + "," + String.valueOf(diffTime) + ","
                        + String.format(Locale.US,"%.2f", wpmcorrect) + "," + String.format(Locale.US,"%.2f", wpmtrans) + ","
                        + String.format(Locale.US,"%.2f", ter) + "%," + String.format(Locale.US,"%.2f", cer) + "%,"
                        + String.format(Locale.US,"%.2f", ncer) + "%," + String.format(Locale.US,"%.2f", cbrer) + "%,"
                        + String.format(Locale.US,"%.2f", cawer) + "%";
                //gore mozda staviti String.format(Locale.US,"0.2f",wpmcorrect...)
                //Log.d("rerer",submittedPhrase);
                br.append(str);
                br.newLine();
                phraseWriter.flush();
                br.close();
                phraseWriter.close();
                //flush input stream and stop the timer reset counter
                inStream = "";
                timerStart = false;
                bckspccnt=0;
                tempKeyboard = keyboardType;
                Log.d("tag", "file updated");
                if (showResults == true){
                    showResultsView.setText("Time: " + String.valueOf(diffTime) + "\t\t\t" + "TER: " + String.format(Locale.US,"%.2f", ter) + "\n"
                                          + "WPM correct: " + String.format(Locale.US,"%.2f", wpmcorrect) + "\n" + "WPM transcribed: "
                                          + String.format(Locale.US,"%.2f", wpmtrans));
                }

                if (repeatPhrase == true) isPhrasewritten = 1;

            } catch (IOException e) {

                e.printStackTrace();
            }
        }

        // set used phrase column to 1
        if (filePatha != null) {
            sourceDB.upgradePhraseUsage(phraseID);
        } else {
            phraseDB.upgradePhraseUsage(phraseID);
        }
    }

    private void dummyDetected(){

        writePhrase.getText().clear();
        inStream = "";

    }

    // check if file exists
    private void checkFile() {

        final String logFilepath = Environment.getExternalStorageDirectory() + "/log.csv";
        logFile = new File(logFilepath);

        if(!logFile.exists()){

            try{
                writer = new FileWriter(logFile);
                writer.append("\"sep=,\"\n");
                writer.append("timestamp,test_name,keyboard_type,username,orientation,inter_style,phrase_ID,phrase,phrase_lenght,input_stream," +
                        "time,wpmcorrect,wpmtrans,ter,cer,ncer,cbrer,cawer\n");
                writer.flush();
                writer.close();
                Log.d("tag", "First line written!");

            }catch(IOException e){

                e.printStackTrace();
            }
        }
    }

    public void resetLogFile(){
        new AlertDialog.Builder(this)
        .setTitle("Reset LOG file")
        .setMessage("Are you really sure you want to reset the log file?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                final String logFilepath = Environment.getExternalStorageDirectory() + "/log.csv";
                logFile = new File(logFilepath);
                logFile.delete();
                Toast.makeText(MainActivity.this, "Log file reseted!", Toast.LENGTH_SHORT).show();
                checkFile();
            }

        })
        .setNegativeButton("No", null)
        .show();
    }

    // when user clicks on the phrase it generates a random query
    // conditions if there is a default db or loaded one
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (writePhrase.getText().length() == 0) {

            if (filePatha != null) {
                row = sourceDB.getRow();
            } else {
                row = phraseDB.getRow();
            }

            phraseLenght = row.length();
            readPhrase.setText(row);
            showResultsView.setText("");
            inStream = "";
            isPhrasewritten = 0;
            if (showTime == true){
                showTimeView.setText("Current time: 0.0");
            }
            else {
                showTimeView.setText("");
            }


        } else {

            Toast.makeText(MainActivity.this, "A little late to change now!", Toast.LENGTH_SHORT).show();
        }

        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
            new AlertDialog.Builder(this)
            .setTitle("Reset database")
            .setMessage("Are you really sure you want to reset the database?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (filePatha != null) {
                        sourceDB.deleteDatabase();
                    } else {
                        phraseDB.deleteDatabase();
                    }

                    readPhrase.setText("Tap here to get a random phrase.");
                    Toast.makeText(MainActivity.this, "Database reseted!", Toast.LENGTH_SHORT).show();
                }

            })
            .setNegativeButton("No", null)
            .show();

            //Loading action, using file explorer
        } else if (id == R.id.action_load) {

            loadIntent = new Intent(Intent.ACTION_GET_CONTENT);
            String [] mimeTypes = {"text/*","text/plain","text/rtf"};
            loadIntent.setType("*/*");
            loadIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

            try {

                startActivityForResult(Intent.createChooser(loadIntent, "Select your phrases .txt document"), 0);

            } catch (android.content.ActivityNotFoundException e) {

                //if the user doesen't have file explorer installed
                Toast.makeText(MainActivity.this, "Please install a file manager!", Toast.LENGTH_SHORT).show();

            }

            //username
        }  else if (id == R.id.action_user) {

            //starting username dialog
            MyDialogFragment frag = new MyDialogFragment();
            Bundle args = new Bundle();
            args.putString("currentName", mUsername);
            frag.setArguments(args);
            frag.show(getFragmentManager(), "User Info");

        } else if (id == R.id.action_delete_file){

            resetLogFile();

            //phrase visibility
        } else if (id == R.id.action_toggle_visibility){
            if (readPhrase.getVisibility() == View.VISIBLE) {

                readPhrase.setVisibility(View.GONE);

            } else {

                readPhrase.setVisibility(View.VISIBLE);
            }
        } else if (id == R.id.action_test_settings){
            Intent intent = new Intent(MainActivity.this, TestSettings.class);
            if (testName != "" && keyboardType != "" && phraseCount != 0){
                intent.putExtra("TEST_NAME", testName);
                intent.putExtra("KEYBOARD_TYPE", keyboardType);
                intent.putExtra("PHRASE_COUNT", phraseCount);
                intent.putExtra("INTER_STYLE", interStyle);
                intent.putExtra("ORIENTATION", orientation);
                intent.putExtra("TIME", showTime);
                intent.putExtra("RESULTS", showResults);
                intent.putExtra("PHRASE_REPEAT", repeatPhrase);
                intent.putExtra("CURSOR", moveCursor);
            }
            startActivityForResult(intent, 1);
        } else if (id == R.id.action_upload_log){
            if (testName.equals("")){
                Toast.makeText(MainActivity.this, "Please enter test name for file upload", Toast.LENGTH_SHORT).show();
            }
            else {
                File dir = Environment.getExternalStorageDirectory();
                String finalName = testName + ".csv";
                if(dir.exists()){
                    File from = new File(dir,logFile.getName());
                    File to = new File(dir,finalName);
                    if(from.exists())
                        from.renameTo(to);
                    logFile = to;
                }
                dialog_for_sending("http://207.154.235.97/logapk/sendfile-logger.php",
                        "Do you want to upload " + testName + ".csv to server?",
                        "Send log file to server",
                        "Log file successfully sent.",
                        "Error sending log file via network.");
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case 0:

                if (resultCode == RESULT_OK) {

                    Uri uri = data.getData();
                    String filePath = uri.toString();
                    File myFile = new File(filePath);
                    if (filePath.startsWith("content://")) {
                        Cursor cursor = null;
                        try {
                            cursor = this.getContentResolver().query(uri, null, null, null, null);
                            if (cursor != null && cursor.moveToFirst()) {
                                String fullPath = Commons.getPath(uri, this);
                                filePatha = fullPath;
                                sourceDB = new SqlDatabase(ctx, filePatha);
                                Log.d("tag", "File path: " + filePathb);
                            }

                        } finally {
                            cursor.close();
                        }
                    } else if (filePath.startsWith("file://")) {
                        String tmpfilepath = myFile.getAbsolutePath();
                        tmpfilepath = tmpfilepath.replaceAll("file:/", "");
                        filePatha = tmpfilepath;
                        sourceDB = new SqlDatabase(ctx, filePatha);
                        Log.d("tag", "File path: " + filePathb);
                    }

                    //other instance of SqlDatabase class
                    //using constructor with ctx and filepath arguments
                    // when we have a defined file path to read from
                    FileReader file = null;
                    try {
                        file = new FileReader(filePatha);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    BufferedReader buffer = new BufferedReader(file);
                    SQLiteDatabase db2 = sourceDB.getWritableDatabase();
                    String line = "";
                    sourceDB.deleteDatabase();
                    db2.beginTransaction();
                    try {
                        while ((line = buffer.readLine()) != null) {
                            ContentValues cv = new ContentValues();
                            cv.put("phrase", line.trim());
                            db2.insert("phrases", null, cv);

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    db2.setTransactionSuccessful();
                    db2.endTransaction();
                }
                break;


            case 1:
                if (resultCode == RESULT_OK) {
                    TextView testNameView = (TextView) findViewById(R.id.testName);
                    TextView keyboardView = (TextView) findViewById(R.id.keyboardType);
                    testName = data.getStringExtra("TEST_NAME");
                    keyboardType = data.getStringExtra("KEYBOARD_TYPE");
                    interStyle = data.getStringExtra("INTER_STYLE");
                    orientation = data.getStringExtra("ORIENTATION");
                    phraseCount = data.getLongExtra("PHRASE_COUNT", 0);
                    showTime = data.getBooleanExtra("TIME", false);
                    showResults = data.getBooleanExtra("RESULTS", false);
                    repeatPhrase = data.getBooleanExtra("PHRASE_REPEAT", false);
                    moveCursor = data.getBooleanExtra("CURSOR", false);

                    testNameView.setText("Test name: " + testName);
                    keyboardView.setText("Keyboard type: " + keyboardType);
                    phraseCountView.setText("Phrase count: " + currentPhraseCount + "/" + phraseCount);
                    if (showTime == true){
                        showTimeView.setText("Current time: " + diffTime);
                    }
                    if (!tempKeyboard.equals(keyboardType) ){
                        currentPhraseCount = 0;
                        phraseCountView.setText("Phrase count: " + currentPhraseCount + "/" + phraseCount);
                    }
                 /*   if (toggleVisability == true){
                        readPhrase.setVisibility(View.GONE);
                    }
                    else readPhrase.setVisibility(View.VISIBLE);*/

                }
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String username) {

        //log username
        Log.d("tag", username);
        mUsername = username;
        usernameView.setText("Username: " + mUsername);
        if (phraseCount != 0){
            currentPhraseCount = 0;
            phraseCountView.setText("Phrase count: " + currentPhraseCount + "/" + phraseCount);
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        //cancels dialog alert
    }


    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {

            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {

                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {

            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public Runnable runable = new Runnable() {
        @Override
        public void run() {
            if (showTime == true){
                diffTime = (SystemClock.elapsedRealtime() - startTime) / 1000.0;
                showTimeView.setText("Current time: " + diffTime);
            }
            handler.postDelayed(this, 0);
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void dialog_for_sending(final String urladdress,
                                    String dlgMessage, String dlgTitle,
                                    final String dlgResultOK, final String dlgResultNOTok){

        // Let's design dialog programatically:
        // It will contain title, text, editbox, and progressbar. And 2 buttons, of course.
        // Progress bar will be shown only when network operation lasts longer.
        final LinearLayout myDialogLayout = new LinearLayout(this);
        myDialogLayout.setOrientation(LinearLayout.VERTICAL);
        final ProgressBar myBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);

        final AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);
        alertDlg.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDlg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        sendDialog = alertDlg.create();
        sendDialog.setMessage(dlgMessage);
        sendDialog.setTitle(dlgTitle);
        sendDialog.setView(myDialogLayout);

        /*
        Add an OnShowListener to change the OnClickListener on the first time the alert is shown.
        Calling getButton() before the alert is shown will return null.
        Then use a regular View.OnClickListener for the button, which will not dismiss
        the AlertDialog after it has been called.
        */

        sendDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button button = sendDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                //        File renamed = new File(logFile.toURI());
                        //renamed.renameTo(new File(testName));

                            myDialogLayout.addView(myBar);
                            button.setEnabled(false);
                            sendDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);

                            SendTask mySendTask = new SendTask(
                                    urladdress,
                                    logFile.getAbsolutePath(),
                                    logFile.getName());
                            mySendTask.setNetworkOperationFinished(new SendTask.NetworkOperationFinished() {
                                @Override
                                public void onNetworkOperationFinished(String response) {
                                    myBar.setVisibility(View.INVISIBLE);
                                    sendDialog.cancel();
                                    if (response!="") {
                                        showToastFromDialog(dlgResultOK);
                                    } else {
                                        showToastFromDialog(dlgResultNOTok);
                                    }
                                }
                            });
                            mySendTask.execute();
                    }
                });
            }
        });

        sendDialog.show();
    }

    private void showToastFromDialog(String message){
        Toast.makeText(this, message , Toast.LENGTH_SHORT).show();
    }

    private void isPhraseOver(){
        if (phraseLenght != 0 && writePhrase.length() == 0) {
            inStream = "";
        }
    }

}


