package com.example.okey.okeylogger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class TestSettings extends AppCompatActivity {

    String testNameVariable = "";
    String keyboardTypeVariable = "";
    String interactionStyleVariable = "";
    String orientationVariable = "";
    long phraseCountVariable = 0;
    boolean showTimeVariable = false;
    boolean showResultsVariable = false;
    boolean phraseRepeatVariable = false;
    boolean cursorMovementVariable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_settings);
        Bundle extras = getIntent().getExtras();

        final EditText testName = (EditText) findViewById(R.id.editTextTestName);
        final EditText keyboardType = (EditText) findViewById(R.id.editTextKeyboardType);
        final EditText phraseCount = (EditText) findViewById(R.id.editTextPhraseCount);
        final RadioGroup interactionStyle = (RadioGroup) findViewById(R.id.radioGroupInterStyle);
        final RadioGroup orientation = (RadioGroup) findViewById(R.id.radioGroupOrientation);
        final RadioButton buttonTT = (RadioButton) findViewById(R.id.radioButtonTT);
        final RadioButton buttonOH = (RadioButton) findViewById(R.id.radioButtonOH);
        final RadioButton buttonC = (RadioButton) findViewById(R.id.radioButtonC);
        final RadioButton buttonP = (RadioButton) findViewById(R.id.radioButtonP);
        final RadioButton buttonL = (RadioButton) findViewById(R.id.radioButtonL);
        final Switch showTime = (Switch) findViewById(R.id.switchTime);
        final Switch showResults = (Switch) findViewById(R.id.switchResults);
        final Switch phraseRepeat = (Switch) findViewById(R.id.switchPhrases);
        final Switch cursorMovement = (Switch) findViewById(R.id.switchCursor);
        final Button buttonOK = (Button) findViewById(R.id.buttonOk);
        final Button buttonCancel = (Button) findViewById(R.id.buttonCancel);
        final TextView test123123 = (TextView) findViewById(R.id.textView);

        if (extras != null){
            testNameVariable = extras.getString("TEST_NAME");
            keyboardTypeVariable = extras.getString("KEYBOARD_TYPE");
            phraseCountVariable = extras.getLong("PHRASE_COUNT");
            interactionStyleVariable = extras.getString("INTER_STYLE");
            orientationVariable = extras.getString("ORIENTATION");
            showTimeVariable = extras.getBoolean("TIME");
            showResultsVariable = extras.getBoolean("RESULTS");
            phraseRepeatVariable = extras.getBoolean("PHRASE_REPEAT");
            cursorMovementVariable = extras.getBoolean("CURSOR");
            testName.setText(testNameVariable);
            keyboardType.setText(keyboardTypeVariable);
            phraseCount.setText(Long.toString(phraseCountVariable));
            if (interactionStyleVariable != ""){
                if (interactionStyleVariable.equals("Two-thumbs")){
                    interactionStyle.check(buttonTT.getId());
                }
                else if (interactionStyleVariable.equals("One-handed")){
                    interactionStyle.check(buttonOH.getId());
                }
                else {
                    interactionStyle.check(buttonC.getId());
                }
            }
            if (orientationVariable != ""){
                if (orientationVariable.equals("Portrait")){
                    orientation.check(buttonP.getId());
                }
                else{
                    orientation.check(buttonL.getId());
                }
            }
            if (showTimeVariable){
                if (showTimeVariable == true){
                    showTime.setChecked(true);
                }
                else {
                    showTime.setChecked(false);
                }
            }
            if (showResultsVariable){
                if (showResultsVariable == true){
                    showResults.setChecked(true);
                }
                else {
                    showResults.setChecked(false);
                }
            }
            if (phraseRepeatVariable){
                if (phraseRepeatVariable == true){
                    phraseRepeat.setChecked(true);
                }
                else {
                    phraseRepeat.setChecked(false);
                }
            }
            if (cursorMovementVariable){
                if (cursorMovementVariable == true){
                    cursorMovement.setChecked(true);
                }
                else{
                    cursorMovement.setChecked(false);
                }
            }


        }

        testName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                testNameVariable = testName.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (testName.getText().toString().trim().length() == 0){
                    testNameVariable = "";
                }

            }
        });

        keyboardType.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                keyboardTypeVariable = keyboardType.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (keyboardType.getText().toString().trim().length() == 0){
                    keyboardTypeVariable = "";
                }
            }
        });

        phraseCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (phraseCount.getText().toString().trim().length() == 0){
                    phraseCountVariable = 0;
                }
                else{
                    phraseCountVariable = Long.parseLong(phraseCount.getText().toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (phraseCount.getText().toString().trim().length() == 0){
                    phraseCountVariable = 0;
                }

            }
        });

        interactionStyle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if (buttonOH.isChecked()){
                    interactionStyleVariable = "One-handed";
                }
                else if (buttonC.isChecked()){
                    interactionStyleVariable = "Cradling";
                }
                else{
                    interactionStyleVariable = "Two-thumbs";
                }
            }
        });

        orientation.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if (buttonL.isChecked()){
                    orientationVariable = "Landscape";
                }
                else {
                    orientationVariable = "Portrait";
                }
            }
        });

       showTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
               if(showTime.isChecked()){
                   showTimeVariable = true;
               }
               else {
                   showTimeVariable = false;
               }
           }
       });

        showResults.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (showResults.isChecked()){
                    showResultsVariable = true;
                }
                else {
                    showResultsVariable = false;
                }
            }
        });

        phraseRepeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (phraseRepeat.isChecked()){
                    phraseRepeatVariable = true;
                }
                else{
                    phraseRepeatVariable = false;
                }
            }
        });

        cursorMovement.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (cursorMovement.isChecked()){
                    cursorMovementVariable = true;
                }
                else {
                    cursorMovementVariable = false;
                }
            }
        });

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (testNameVariable == "" || keyboardTypeVariable == "" || phraseCountVariable == 0 || interactionStyleVariable == "" || orientationVariable == ""){
                    Toast toast = Toast.makeText(getApplicationContext(),"Cant submit without selecting everything",Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    Intent intent = new Intent();
                    intent.putExtra("TEST_NAME", testNameVariable);
                    intent.putExtra("KEYBOARD_TYPE", keyboardTypeVariable);
                    intent.putExtra("INTER_STYLE", interactionStyleVariable);
                    intent.putExtra("ORIENTATION", orientationVariable);
                    intent.putExtra("PHRASE_COUNT",phraseCountVariable);
                    intent.putExtra("TIME", showTimeVariable);
                    intent.putExtra("RESULTS", showResultsVariable);
                    intent.putExtra("PHRASE_REPEAT", phraseRepeatVariable);
                    intent.putExtra("CURSOR", cursorMovementVariable);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TestSettings.this);

                // set title
                alertDialogBuilder.setTitle("Exit test settings");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Click yes to exit!")
                        .setCancelable(false)
                        .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, close
                                // current activity
                                TestSettings.this.finish();
                            }
                        })
                        .setNegativeButton("No",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });
    }
}
