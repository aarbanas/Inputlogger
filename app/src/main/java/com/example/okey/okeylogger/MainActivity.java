package com.example.okey.okeylogger;

import android.app.DialogFragment;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.annotation.NonNull;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, MyDialogFragment.NoticeDialogListener{// SingleChoiceDialogFragment.ChoiceDialogListener {

    private Intent loadIntent;

    String testName = "";
    String keyboardType = "";
    String tempKeyboard = "";
    long phraseCount = 100;
    long currentPhraseCount = 0;
    boolean showTime = false;
    boolean showResults = false;
    Handler handler;

    private String filePatha;
    private String filePathb;
    private String row;
    private String interStyle, orientation, phraseSrc, phraseID;
    private String mUsername = "";
    private String  inStream = "";

    private String[] array;

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
                    if (phraseLenght != 0 && writePhrase.length() == 0) {
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
                            Toast.makeText(MainActivity.this, "You have written all phrases for this test", Toast.LENGTH_SHORT).show();
                            dummyDetected();
                        }
                        if (s.length() > start) {
                            current = s.charAt(start);
                        }

                    }

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
            diffTime = (SystemClock.elapsedRealtime() - startTime) / 1000.0;
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
                Log.d("tag", "file updated");
                if (showResults == true){
                    showResultsView.setText("WPM correct: " + wpmcorrect + "\n" +
                                            "WPM transcribed: " + wpmtrans + "\n" +
                                            "TER: " + ter);
                }

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
            tempKeyboard = keyboardType;
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
            loadIntent.setType("*/*");
            loadIntent.addCategory(Intent.CATEGORY_OPENABLE);

            try {

                startActivityForResult(Intent.createChooser(loadIntent, "Select your phrases .txt document"), 0);

            } catch (android.content.ActivityNotFoundException e) {

                //if the user doesen't have file explorer installed
                Toast.makeText(MainActivity.this, "Please install a file manager!", Toast.LENGTH_SHORT).show();

            }

            //phrase visibility
        } else if (id == R.id.action_visibility) {

            if (readPhrase.getVisibility() == View.VISIBLE) {

                readPhrase.setVisibility(View.GONE);

            } else {

                readPhrase.setVisibility(View.VISIBLE);
            }

        } else if (id == R.id.action_user) {

            //starting username dialog
            MyDialogFragment frag = new MyDialogFragment();
            Bundle args = new Bundle();
            args.putString("currentName", mUsername);
            frag.setArguments(args);
            frag.show(getFragmentManager(), "User Info");

        } else if (id == R.id.action_delete_file){

            resetLogFile();
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
            }
            startActivityForResult(intent, 1);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        final String isExt = "com.android.externalstorage.documents";
        final String isDown = "com.android.providers.downloads.documents";



        switch (requestCode) {

            case 0:

                if (resultCode == RESULT_OK) {

                    Uri fileUri = data.getData();
                    Log.d("tag", "File uri: " + fileUri.toString());

                    filePatha = fileUri.getPath();
                    Log.d("tag", "File path: " + filePatha);

                    //check if the file selected comes from external storage
                    if(isExt.equals(fileUri.getAuthority())){

                        filePathb = Environment.getExternalStorageDirectory() + "/" + filePatha.split(":")[1];
                        Log.d("tag", "File path: " + filePathb);
                        phraseSrc = filePatha.split(":")[1];

                        //check if the file selected comes from Downloads directory
                    }else if(isDown.equals(fileUri.getAuthority())){

                        final String id;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                            id = DocumentsContract.getDocumentId(fileUri);

                            final Uri contentUri = ContentUris.withAppendedId(
                                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));


                            filePathb = getDataColumn(ctx, contentUri, null, null);
                            array = filePathb.split("/");
                            phraseSrc = array[array.length - 1];
                        }
                        Log.d("tag", "File path: " + filePathb);
                        Log.d("tag", phraseSrc);
                    }


                    //other instance of SqlDatabase class
                    //using constructor with ctx and filepath arguments
                    // when we have a defined file path to read from
                    int currentApiVersion = Build.VERSION.SDK_INT;
                    if (currentApiVersion <= Build.VERSION_CODES.KITKAT){
                        if (Build.VERSION.RELEASE == "4.4.4"){
                            sourceDB = new SqlDatabase(ctx, filePathb);
                        }else {
                            sourceDB = new SqlDatabase(ctx, filePatha);
                        }
                    }else {
                        sourceDB = new SqlDatabase(ctx, filePathb);
                    }
                    SQLiteDatabase db2 = sourceDB.getWritableDatabase();

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

                    testNameView.setText("Test name: " + testName);
                    keyboardView.setText("Keyboard type: " + keyboardType);
                    if (showTime == true){
                        showTimeView.setText("Current time: " + diffTime);
                    }
                    if (tempKeyboard != keyboardType){
                        currentPhraseCount = 0;
                        phraseCountView.setText("Phrase count: " + currentPhraseCount + "/" + phraseCount);
                    }

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
        currentPhraseCount = 0;
        phraseCountView.setText("Phrase count: " + currentPhraseCount + "/" + phraseCount);

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




}


